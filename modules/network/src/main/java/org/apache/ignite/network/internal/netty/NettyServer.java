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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.NetworkMessage;

public class NettyServer {

    private final ServerBootstrap bootstrap = new ServerBootstrap();

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private final int port;

    private final SerializerProvider serializerProvider;

    private final BiConsumer<InetSocketAddress, NetworkMessage> messageListener;

    private ServerSocketChannel channel;

    private final Consumer<SocketChannel> newConnectionListener;

    public NettyServer(
        int port,
        Consumer<SocketChannel> newConnectionListener,
        BiConsumer<InetSocketAddress, NetworkMessage> messageListener,
        SerializerProvider provider
    ) {
        this.port = port;
        this.newConnectionListener = newConnectionListener;
        this.messageListener = messageListener;
        this.serializerProvider = provider;
    }

    public CompletableFuture<Void> start() {
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                /** {@inheritDoc} */
                @Override public void initChannel(SocketChannel ch)
                    throws Exception {
                    ch.pipeline().addLast(new InboundDecoder(serializerProvider),
                        new RequestHandler(messageListener),
                        new ChunkedWriteHandler());
                }
            })
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        CompletableFuture<Void> serverStartFuture = new CompletableFuture<>();

        ChannelFuture bindFuture = bootstrap.bind(port);

        bindFuture.addListener(bind -> {
            this.channel = (ServerSocketChannel) bindFuture.channel();

            if (bind.isSuccess()) {
                serverStartFuture.complete(null);
            }
            else {
                Throwable cause = bind.cause();
                serverStartFuture.completeExceptionally(cause);
            }

            channel.closeFuture().addListener(close -> {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            });
        });

        return serverStartFuture;
    }

    public InetSocketAddress address() {
        return channel.localAddress();
    }

}
