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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.ignite.network.internal.DirectMessageReader;
import org.apache.ignite.network.internal.MessageReader;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.MessageDeserializer;
import org.apache.ignite.network.message.NetworkMessage;

public class Inbound extends ByteToMessageDecoder {

    private static final AttributeKey<MessageReader> READER_KEY = AttributeKey.valueOf("READER");
    private static final AttributeKey<MessageDeserializer<NetworkMessage>> DESERIALIZER_KEY = AttributeKey.valueOf("DESERIALIZER");

    private final SerializerProvider serializerProvider;

    public Inbound(SerializerProvider provider) {
        serializerProvider = provider;
    }

    /** {@inheritDoc} */
    @Override public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuffer buffer = in.nioBuffer();

        Attribute<MessageReader> readerAttr = ctx.channel().attr(READER_KEY);
        MessageReader reader = readerAttr.get();

        if (reader == null)
            readerAttr.set(reader = new DirectMessageReader(serializerProvider, (byte) 1));

        Attribute<MessageDeserializer<NetworkMessage>> messageAttr = ctx.channel().attr(DESERIALIZER_KEY);

        while (buffer.hasRemaining()) {
            MessageDeserializer<NetworkMessage> msg = messageAttr.get();

            try {
                if (msg == null && buffer.remaining() >= NetworkMessage.DIRECT_TYPE_SIZE) {
                    byte b0 = buffer.get();
                    byte b1 = buffer.get();

                    msg = serializerProvider.createDeserializer(makeMessageType(b0, b1));
                }

                boolean finished = false;

                if (msg != null && buffer.hasRemaining()) {
                    reader.setCurrentReadClass(msg.klass());
                    reader.setBuffer(buffer);

                    finished = msg.readMessage(reader);
                }

                if (finished) {
                    reader.reset();
                    messageAttr.set(null);

                    out.add(msg.getMessage());
                }
                else {
                    messageAttr.set(msg);
                }
            }
            catch (Throwable e) {
//            U.error(log, "Failed to read message [msg=" + msg +
//                    ", buf=" + buf +
//                    ", reader=" + reader +
//                    ", ses=" + ses + "]",
//                e);

                throw e;
            }
        }
    }

    /**
     * Concatenates the two parameter bytes to form a message type value.
     *
     * @param b0 The first byte.
     * @param b1 The second byte.
     */
    public static short makeMessageType(byte b0, byte b1) {
        return (short)((b1 & 0xFF) << 8 | b0 & 0xFF);
    }
}
