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

package org.apache.ignite.network.internal.netty;

import io.netty.channel.socket.SocketChannel;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.NetworkMessage;

public class ConnectionManager {

    private NettyServer server;

    private Map<InetSocketAddress, NettySender> channels = new ConcurrentHashMap<>();

    private Map<InetSocketAddress, NettyClient> clients = new ConcurrentHashMap<>();

    private final int port;

    private final SerializerProvider serializerProvider;

    private List<BiConsumer<InetSocketAddress, NetworkMessage>> listeners = Collections.synchronizedList(new ArrayList<>());

    public ConnectionManager(int port, SerializerProvider provider) {
        this.port = port;
        this.serializerProvider = provider;
        this.server = new NettyServer(port, this::onNewClient, this::onMessage, provider);
    }

    public void start() {
        try {
            server.start().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress getLocalAddress() {
        return server.address();
    }

    public CompletableFuture<NettySender> channel(InetSocketAddress address) {
        NettySender channel = channels.get(address);

        if (channel == null) {
            NettyClient client = clients.computeIfAbsent(address, this::connect);
            return client.sender();
        }

        return CompletableFuture.completedFuture(channel);
    }

    private void onMessage(InetSocketAddress from, NetworkMessage message) {
        listeners.forEach(consumer -> {
            consumer.accept(from, message);
        });
    }

    private void onNewClient(SocketChannel channel) {
        InetSocketAddress remoteAddress = channel.remoteAddress();
        // TODO: where might be outgoing connection already
        channels.put(remoteAddress, new NettySender(channel, serializerProvider));
    }

    private NettyClient connect(InetSocketAddress address) {
        NettyClient client = new NettyClient(address.getHostName(), address.getPort(), serializerProvider, (src, message) -> {
            this.onMessage(src, message);
        });

        client.start().whenComplete((sender, throwable) -> {
            if (throwable != null) {
                clients.remove(address);
            }
            else {
                channels.put(address, sender);
            }
        });

        return client;
    }

    public void addListener(BiConsumer<InetSocketAddress, NetworkMessage> listener) {
        listeners.add(listener);
    }
}
