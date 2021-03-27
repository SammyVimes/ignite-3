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

package org.apache.ignite.network.scalecube.message;

import java.util.Map;
import org.apache.ignite.network.message.NetworkMessage;

public class ScaleCubeMessage extends NetworkMessage {
    public static final short TYPE = 100;

    private final byte[] array;

    private final String className;

    private final Map<String, String> headers;

    public ScaleCubeMessage(byte[] array, String className, Map<String, String> headers) {
        this.array = array;
        this.className = className;
        this.headers = headers;
    }

    public byte[] getArray() {
        return array;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /** {@inheritDoc} */
    @Override public short type() {
        return TYPE;
    }
}
