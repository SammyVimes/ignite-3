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

package org.apache.ignite.configuration.notifications;

import org.apache.ignite.configuration.ConfigurationProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Event object propogated on configuration change. Passed to listeners after configuration changes are applied.
 *
 * @see ConfigurationProperty#listen(ConfigurationListener)
 * @see ConfigurationListener
 * @see ConfigurationNotificationEvent
 */
public interface ConfigurationNotificationEvent<VIEW> {
    /**
     * Previous value of the updated configuration.
     */
    @Nullable VIEW oldValue();

    /**
     * Updated value of the configuration.
     */
    @Nullable VIEW newValue();

    /**
     * Monotonously increasing counter, linked to the specific storage for current configuration values. Gives you a
     * unique change identifier inside a specific configuration storage.
     */
    long storageRevision();
}
