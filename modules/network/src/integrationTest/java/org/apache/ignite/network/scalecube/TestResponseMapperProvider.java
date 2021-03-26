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

import java.io.IOException;
import org.apache.ignite.network.internal.MessageReader;
import org.apache.ignite.network.message.MessageDeserializer;
import org.apache.ignite.network.message.MessageMapperProvider;
import org.apache.ignite.network.message.MessageMappingException;
import org.apache.ignite.network.message.MessageSerializer;

/**
 * Mapper provider for {@link TestResponse}.
 */
public class TestResponseMapperProvider implements MessageMapperProvider<TestResponse> {
    /** {@inheritDoc} */
    @Override public MessageDeserializer<TestResponse> createDeserializer() {
        return new MessageDeserializer<TestResponse>() {
            @Override
            public boolean readMessage(MessageReader reader) throws MessageMappingException {
                return false;
            }

            @Override
            public Class<TestResponse> klass() {
                return null;
            }

            @Override
            public TestResponse getMessage() {
                return null;
            }
        };
    }

    /** {@inheritDoc} */
    @Override public MessageSerializer<TestResponse> createSerializer() {
        return (message, writer) -> {
            return false;
        };
    }

    @Override
    public byte fieldsCount() {
        return 0;
    }
}
