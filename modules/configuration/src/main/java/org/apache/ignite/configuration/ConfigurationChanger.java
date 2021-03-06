/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.configuration;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import org.apache.ignite.configuration.internal.SuperRoot;
import org.apache.ignite.configuration.internal.validation.MemberKey;
import org.apache.ignite.configuration.internal.validation.ValidationUtil;
import org.apache.ignite.configuration.storage.ConfigurationStorage;
import org.apache.ignite.configuration.storage.Data;
import org.apache.ignite.configuration.storage.StorageException;
import org.apache.ignite.configuration.tree.ConfigurationSource;
import org.apache.ignite.configuration.tree.InnerNode;
import org.apache.ignite.configuration.validation.ConfigurationValidationException;
import org.apache.ignite.configuration.validation.ValidationIssue;
import org.apache.ignite.configuration.validation.Validator;
import org.jetbrains.annotations.NotNull;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.ignite.configuration.internal.util.ConfigurationUtil.addDefaults;
import static org.apache.ignite.configuration.internal.util.ConfigurationUtil.cleanupMatchingValues;
import static org.apache.ignite.configuration.internal.util.ConfigurationUtil.fillFromPrefixMap;
import static org.apache.ignite.configuration.internal.util.ConfigurationUtil.nodeToFlatMap;
import static org.apache.ignite.configuration.internal.util.ConfigurationUtil.patch;
import static org.apache.ignite.configuration.internal.util.ConfigurationUtil.toPrefixMap;

/**
 * Class that handles configuration changes, by validating them, passing to storage and listening to storage updates.
 */
public final class ConfigurationChanger {
    /** */
    private final ForkJoinPool pool = new ForkJoinPool(2);

    /** */
    private final Map<String, RootKey<?, ?>> rootKeys = new TreeMap<>();

    /** Map that has all the trees in accordance to their storages. */
    private final Map<Class<? extends ConfigurationStorage>, StorageRoots> storagesRootsMap = new ConcurrentHashMap<>();

    /** Annotation classes mapped to validator objects. */
    private Map<Class<? extends Annotation>, Set<Validator<?, ?>>> validators = new HashMap<>();

    /**
     * Closure interface to be used by the configuration changer. An instance of this closure is passed into the constructor and
     * invoked every time when there's an update from any of the storages.
     */
    @FunctionalInterface
    public interface Notificator {
        /**
         * Invoked every time when the configuration is updated.
         * @param oldRoot Old roots values. All these roots always belong to a single storage.
         * @param newRoot New values for the same roots as in {@code oldRoot}.
         * @param storageRevision Revision of the storage.
         * @return Not-null future that must signify when processing is completed. Exceptional completion is not
         *      expected.
         */
        @NotNull CompletableFuture<Void> notify(SuperRoot oldRoot, SuperRoot newRoot, long storageRevision);
    }

    /** Closure to execute when an update from the storage is received. */
    private final Notificator notificator;

    /**
     * Immutable data container to store version and all roots associated with the specific storage.
     */
    private static class StorageRoots {
        /** Immutable forest, so to say. */
        private final SuperRoot roots;

        /** Version associated with the currently known storage state. */
        private final long version;

        /** */
        private StorageRoots(SuperRoot roots, long version) {
            this.roots = roots;
            this.version = version;
        }
    }

    /** Lazy annotations cache for configuration schema fields. */
    private final Map<MemberKey, Annotation[]> cachedAnnotations = new ConcurrentHashMap<>();

    /** Storage instances by their classes. Comes in handy when all you have is {@link RootKey}. */
    private final Map<Class<? extends ConfigurationStorage>, ConfigurationStorage> storageInstances = new HashMap<>();

    /**
     * @param notificator Closure to execute when update from the storage is received.
     */
    public ConfigurationChanger(Notificator notificator) {
        this.notificator = notificator;
    }

    /** */
    public <A extends Annotation> void addValidator(Class<A> annotationType, Validator<A, ?> validator) {
        validators
            .computeIfAbsent(annotationType, a -> new HashSet<>())
            .add(validator);
    }

    /** */
    public void addRootKey(RootKey<?, ?> rootKey) {
        assert !storageInstances.containsKey(rootKey.getStorageType());

        rootKeys.put(rootKey.key(), rootKey);
    }

    /**
     * Register changer.
     */
    // ConfigurationChangeException, really?
    public void register(ConfigurationStorage configurationStorage) throws ConfigurationChangeException {
        storageInstances.put(configurationStorage.getClass(), configurationStorage);

        Set<RootKey<?, ?>> storageRootKeys = rootKeys.values().stream().filter(
            rootKey -> configurationStorage.getClass() == rootKey.getStorageType()
        ).collect(Collectors.toSet());

        Data data;

        try {
            data = configurationStorage.readAll();
        }
        catch (StorageException e) {
            throw new ConfigurationChangeException("Failed to initialize configuration: " + e.getMessage(), e);
        }

        SuperRoot superRoot = new SuperRoot(rootKeys);

        Map<String, ?> dataValuesPrefixMap = toPrefixMap(data.values());

        for (RootKey<?, ?> rootKey : storageRootKeys) {
            Map<String, ?> rootPrefixMap = (Map<String, ?>)dataValuesPrefixMap.get(rootKey.key());

            InnerNode rootNode = rootKey.createRootNode();

            if (rootPrefixMap != null)
                fillFromPrefixMap(rootNode, rootPrefixMap);

            superRoot.addRoot(rootKey, rootNode);
        }

        StorageRoots storageRoots = new StorageRoots(superRoot, data.cfgVersion());

        storagesRootsMap.put(configurationStorage.getClass(), storageRoots);

        configurationStorage.addListener(changedEntries -> updateFromListener(
            configurationStorage.getClass(),
            changedEntries
        ));
    }

    /** */
    public void initialize(Class<? extends ConfigurationStorage> storageType) {
        ConfigurationStorage configurationStorage = storageInstances.get(storageType);

        assert configurationStorage != null : storageType;

        StorageRoots storageRoots = storagesRootsMap.get(storageType);

        SuperRoot superRoot = storageRoots.roots;
        SuperRoot defaultsNode = new SuperRoot(rootKeys);

        addDefaults(superRoot, defaultsNode);

        List<ValidationIssue> validationIssues = ValidationUtil.validate(
            superRoot,
            defaultsNode,
            this::getRootNode,
            cachedAnnotations,
            validators
        );

        if (!validationIssues.isEmpty())
            throw new ConfigurationValidationException(validationIssues);

        try {
            changeInternally(defaultsNode, storageInstances.get(storageType)).get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new ConfigurationChangeException(
                "Failed to write defalut configuration values into the storage " + configurationStorage.getClass(), e
            );
        }
    }

    /** Temporary until the IGNITE-14372 */
    public CompletableFuture<?> changeX(
        List<String> path,
        ConfigurationSource source,
        ConfigurationStorage storage
    ) {
        assert path.isEmpty() : "Path support is not yet implemented.";

        SuperRoot superRoot = new SuperRoot(rootKeys);

        source.descend(superRoot);

        return changeInternally(superRoot, storage);
    }

    /** Stop component. */
    public void stop() {
        pool.shutdownNow();
    }

    /**
     * Get root node by root key. Subject to revisiting.
     *
     * @param rootKey Root key.
     */
    public InnerNode getRootNode(RootKey<?, ?> rootKey) {
        return storagesRootsMap.get(rootKey.getStorageType()).roots.getRoot(rootKey);
    }

    /**
     * Change configuration.
     * @param changes Map of changes by root key.
     */
    public CompletableFuture<Void> change(Map<RootKey<?, ?>, InnerNode> changes) {
        if (changes.isEmpty())
            return completedFuture(null);

        Set<Class<? extends ConfigurationStorage>> storagesTypes = changes.keySet().stream()
            .map(RootKey::getStorageType)
            .collect(Collectors.toSet());

        assert !storagesTypes.isEmpty();

        if (storagesTypes.size() != 1) {
            return CompletableFuture.failedFuture(
                new ConfigurationChangeException("Cannot change configurations belonging to different storages.")
            );
        }

        return changeInternally(new SuperRoot(rootKeys, changes), storageInstances.get(storagesTypes.iterator().next()));
    }

    /** */
    public SuperRoot mergedSuperRoot() {
        SuperRoot mergedSuperRoot = new SuperRoot(rootKeys);

        for (StorageRoots storageRoots : storagesRootsMap.values())
            mergedSuperRoot.append(storageRoots.roots);

        return mergedSuperRoot;
    }

    /**
     * Internal configuration change method that completes provided future.
     * @param changes Map of changes by root key.
     * @param storage Storage instance.
     * @return fut Future that will be completed after changes are written to the storage.
     */
    private CompletableFuture<Void> changeInternally(
        SuperRoot changes,
        ConfigurationStorage storage
    ) {
        StorageRoots storageRoots = storagesRootsMap.get(storage.getClass());

        return CompletableFuture
            .supplyAsync(() -> {
                SuperRoot curRoots = storageRoots.roots;

                // It is necessary to reinitialize default values every time.
                // Possible use case that explicitly requires it: creation of the same named list entry with slightly
                // different set of values and different dynamic defaults at the same time.
                SuperRoot patchedSuperRoot = patch(curRoots, changes);

                SuperRoot defaultsNode = new SuperRoot(rootKeys);

                addDefaults(patchedSuperRoot, defaultsNode);

                SuperRoot patchedChanges = patch(changes, defaultsNode);

                cleanupMatchingValues(curRoots, changes);

                Map<String, Serializable> allChanges = nodeToFlatMap(curRoots, patchedChanges);

                // Unlikely but still possible.
                if (allChanges.isEmpty())
                    return null;

                List<ValidationIssue> validationIssues = ValidationUtil.validate(
                    storageRoots.roots,
                    patch(patchedSuperRoot, defaultsNode),
                    this::getRootNode,
                    cachedAnnotations,
                    validators
                );

                if (!validationIssues.isEmpty())
                    throw new ConfigurationValidationException(validationIssues);

                return allChanges;
            }, pool)
            .thenCompose(allChanges -> {
                if (allChanges == null)
                    return CompletableFuture.completedFuture(true);
                return storage.write(allChanges, storageRoots.version)
                    .exceptionally(throwable -> {
                        throw new ConfigurationChangeException("Failed to change configuration", throwable);
                    });
            })
            .thenCompose(casResult -> {
                if (casResult)
                    return CompletableFuture.completedFuture(null);
                else {
                    try {
                        // Is this ok to have a busy wait on concurrent configuration updates?
                        // Maybe we'll fix it while implementing metastorage storage implementation.
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e) {
                        return CompletableFuture.failedFuture(e);
                    }

                    return changeInternally(changes, storage);
                }
            });
    }

    /**
     * Update configuration from storage listener.
     * @param storageType Type of the storage that propagated these changes.
     * @param changedEntries Changed data.
     */
    private void updateFromListener(
        Class<? extends ConfigurationStorage> storageType,
        Data changedEntries
    ) {
        StorageRoots oldStorageRoots = this.storagesRootsMap.get(storageType);

        Map<String, ?> dataValuesPrefixMap = toPrefixMap(changedEntries.values());

        compressDeletedEntries(dataValuesPrefixMap);

        SuperRoot oldSuperRoot = oldStorageRoots.roots;
        SuperRoot newSuperRoot = oldSuperRoot.copy();

        fillFromPrefixMap(newSuperRoot, dataValuesPrefixMap);

        StorageRoots newStorageRoots = new StorageRoots(newSuperRoot, changedEntries.cfgVersion());

        storagesRootsMap.put(storageType, newStorageRoots);

        ConfigurationStorage storage = storageInstances.get(storageType);

        long storageRevision = changedEntries.storageRevision();

        // This will also be updated during the metastorage integration.
        notificator.notify(
            oldSuperRoot,
            newSuperRoot,
            storageRevision
        ).whenCompleteAsync((res, throwable) -> storage.notifyApplied(storageRevision), pool);
    }

    /**
     * "Compress" prefix map - this means that deleted named list elements will be represented as a single {@code null}
     * objects instead of a number of nullified configuration leaves.
     *
     * @param prefixMap Prefix map, constructed from the storage notification data or its subtree.
     */
    private void compressDeletedEntries(Map<String, ?> prefixMap) {
        // Here we basically assume that if prefix subtree contains single null child then all its childrens are nulls.
        Set<String> keysForRemoval = prefixMap.entrySet().stream()
            .filter(entry ->
                entry.getValue() instanceof Map && ((Map<?, ?>)entry.getValue()).containsValue(null)
            )
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        // Replace all such elements will nulls, signifying that these are deleted named list elements.
        for (String key : keysForRemoval)
            prefixMap.put(key, null);

        // Continue recursively.
        for (Object value : prefixMap.values()) {
            if (value instanceof Map)
                compressDeletedEntries((Map<String, ?>)value);
        }
    }
}
