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

package org.apache.ignite.network.scalecube;

import org.apache.ignite.network.message.NetworkMessage;

/**
 * Test response.
 */
public class TestResponse implements NetworkMessage {
    /** Public type for tests. */
    public static final short TYPE = 2;

    /**
     * Some response test value.
     */
    private final int responseNumber;

    /** Constructor. */
    public TestResponse(int responseNumber) {
        this.responseNumber = responseNumber;
    }

    /**
     * @return Response test value.
     */
    public int responseNumber() {
        return responseNumber;
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return TYPE;
    }
}
