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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.internal.netty.NettyClient;
import org.apache.ignite.network.internal.netty.NettySender;
import org.apache.ignite.network.internal.netty.NettyServer;
import org.apache.ignite.network.message.MessageMapperProvider;
import org.apache.ignite.network.message.NetworkMessage;

@Deprecated
// Only for WIP purposes
public class NettyTestRunner {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int port = 1234;

        BiConsumer<InetSocketAddress, NetworkMessage> listener = (addr, msg) -> {
            System.out.println(msg);
        };

        final MessageMapperProvider[] messageMapperProviders = new MessageMapperProvider[Short.MAX_VALUE << 1];

        TestMessageMapperProvider tProv = new TestMessageMapperProvider();

        messageMapperProviders[TestMessage.TYPE] = tProv;

        final SerializerProvider provider = new SerializerProvider(Arrays.asList(messageMapperProviders));

        final NettyServer server = new NettyServer(port, channel -> {}, listener, provider);
        server.start().get();

        final NettyClient client = new NettyClient("localhost", port, provider, listener);

        StringBuilder message = new StringBuilder("");

        for (int i = 0; i < 950; i++) {
            message.append("f");
        }

        Map<Integer, String> someMap = new HashMap<>();

        for (int i = 0; i < 26; i++) {
            someMap.put(i, "" + (char) ('a' + i));
        }

        NettySender sender = client.start().get();

        sender.send(new TestMessage(message.toString(), someMap));
    }
}
