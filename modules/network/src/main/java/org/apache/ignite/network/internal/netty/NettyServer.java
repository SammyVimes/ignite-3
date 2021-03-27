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
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.NetworkMessage;

public class NettyServer {

    private final SerializerProvider serializerProvider;

    private final List<Consumer<NetworkMessage>> messageListener = Collections.synchronizedList(new ArrayList<>());

    private ChannelFuture f;

    public NettyServer(SerializerProvider provider) {
        serializerProvider = provider;
    }

    private void setF(ChannelFuture f) {
        this.f = f;
    }

    private void onMesage(NetworkMessage message) {
        messageListener.forEach(consumer -> consumer.accept(message));
    }

    public void addListener(Consumer<NetworkMessage> listener) {
        messageListener.add(listener);
    }

    public InetSocketAddress address() {
        return ((ServerSocketChannel) f.channel()).localAddress();
    }


    public static class NettyServerBuilder {

        private final ServerBootstrap b = new ServerBootstrap();
        private final int port;
        private final NettyServer server;
        private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        public NettyServerBuilder(int port, SerializerProvider provider) {
            this.port = port;
            this.server = new NettyServer(provider);
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    /** {@inheritDoc} */
                    @Override public void initChannel(SocketChannel ch)
                        throws Exception {
                        ch.pipeline().addLast(new InboundDecoder(provider),
                            new RequestHandler(server::onMesage),
                            new ChunkedWriteHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        }

        public void addListener(Consumer<NetworkMessage> listener) {
            server.messageListener.add(listener);
        }

        public CompletableFuture<NettyServer> start() {
            CompletableFuture<NettyServer> serverStartFuture = new CompletableFuture<>();

            new Thread(() -> {
            try {
                ChannelFuture f = b.bind(port).sync();

                server.setF(f);

                serverStartFuture.complete(server);

                f.channel().closeFuture().sync();
            }
            catch (InterruptedException ignored) {
            }
            finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }).start();
            return serverStartFuture;
        }
    }
}
