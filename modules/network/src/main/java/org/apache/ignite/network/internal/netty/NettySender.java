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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import java.nio.ByteBuffer;
import org.apache.ignite.network.internal.DirectMessageWriter;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.MessageSerializer;
import org.apache.ignite.network.message.NetworkMessage;

public class NettySender {

    private final Channel channel;

    private final SerializerProvider serializerProvider;

    public NettySender(Channel channel, SerializerProvider provider) {
        this.channel = channel;
        serializerProvider = provider;
    }

    public void send(NetworkMessage msg) {
        final DirectMessageWriter writer = new DirectMessageWriter((byte) 1);
        final MessageSerializer<NetworkMessage> serializer = serializerProvider.createSerializer(msg.type());
        final ChunkedInput<ByteBuf> input = new ChunkedInput<>() {

            boolean finished = false;

            @Override
            public boolean isEndOfInput() throws Exception {
                return finished;
            }

            @Override
            public void close() throws Exception {

            }

            @Override
            @Deprecated
            public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
                return readChunk(ctx.alloc());
            }

            @Override
            public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
                ByteBuf buffer = allocator.buffer(4096);
                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
                writer.setBuffer(byteBuffer);
                finished = serializer.writeMessage(msg, writer);
                byteBuffer.limit(byteBuffer.position());
                byteBuffer.rewind();
                buffer.writeBytes(byteBuffer);
                return buffer;
            }

            @Override
            public long length() {
                return -1;
            }

            @Override
            public long progress() {
                return 0;
            }
        };
        channel.writeAndFlush(input);
    }
}
