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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.NetworkMessage;

public class NettyClient {

    private final Bootstrap bootstrap = new Bootstrap();

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();;

    private final SerializerProvider serializerProvider;

    private final String host;

    private final int port;

    private final BiConsumer<InetSocketAddress, NetworkMessage> messageListener;

    private final CompletableFuture<NettySender> clientFuture = new CompletableFuture<>();

    public NettyClient(String host, int port, SerializerProvider provider, BiConsumer<InetSocketAddress, NetworkMessage> listener) {
        this.host = host;
        this.port = port;
        this.serializerProvider = provider;
        this.messageListener = listener;
    }

    public CompletableFuture<NettySender> start() {
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            /** {@inheritDoc} */
            @Override public void initChannel(SocketChannel ch)
                throws Exception {
                ch.pipeline().addLast(new InboundDecoder(serializerProvider),
                    new RequestHandler(messageListener),
                    new ChunkedWriteHandler());
            }
        });

        ChannelFuture connectFuture = bootstrap.connect(host, port);

        connectFuture.addListener(connect -> {
            if (connect.isSuccess()) {
                clientFuture.complete(new NettySender(connectFuture.channel(), serializerProvider));
            }
            else {
                Throwable cause = connect.cause();
                clientFuture.completeExceptionally(cause);
            }
            connectFuture.channel().closeFuture().addListener(close -> {
               workerGroup.shutdownGracefully();
            });
        });

        return clientFuture;
    }

    public CompletableFuture<NettySender> sender() {
        return clientFuture;
    }
}
