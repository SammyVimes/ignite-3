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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import org.apache.ignite.network.message.NetworkMessage;

public class RequestHandler extends ChannelInboundHandlerAdapter {

    private final BiConsumer<InetSocketAddress, NetworkMessage> requestListener;

    public RequestHandler(BiConsumer<InetSocketAddress, NetworkMessage> listener) {
        requestListener = listener;
    }

    /** {@inheritDoc} */
    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        NetworkMessage message = (NetworkMessage) msg;
        requestListener.accept(address, message);
    }
}
