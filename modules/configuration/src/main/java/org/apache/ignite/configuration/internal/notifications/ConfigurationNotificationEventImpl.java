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

package org.apache.ignite.configuration.internal.notifications;

import org.apache.ignite.configuration.notifications.ConfigurationNotificationEvent;
import org.jetbrains.annotations.Nullable;

public class ConfigurationNotificationEventImpl<VIEW> implements ConfigurationNotificationEvent<VIEW> {
    private final VIEW oldValue;

    private final VIEW newValue;

    private final long storageRevision;

    public ConfigurationNotificationEventImpl(VIEW oldValue, VIEW newValue, long storageRevision) {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.storageRevision = storageRevision;
    }

    /** {@inheritDoc} */
    @Override public @Nullable VIEW oldValue() {
        return oldValue;
    }

    /** {@inheritDoc} */
    @Override public @Nullable VIEW newValue() {
        return newValue;
    }

    /** {@inheritDoc} */
    @Override public long storageRevision() {
        return storageRevision;
    }
}
