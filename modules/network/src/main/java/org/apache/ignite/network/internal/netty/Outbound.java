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
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.nio.ByteBuffer;
import org.apache.ignite.network.internal.DirectMessageWriter;
import org.apache.ignite.network.internal.MessageWriter;
import org.apache.ignite.network.internal.SerializerProvider;
import org.apache.ignite.network.message.NetworkMessage;

public class Outbound extends MessageToByteEncoder<NetworkMessage> {

    private final SerializerProvider msgFactory;

    private final AttributeKey<MessageWriter> WRITER_KEY = AttributeKey.valueOf("WRITER");

    public Outbound(SerializerProvider factory) {
        msgFactory = factory;
    }

    /** {@inheritDoc} */
    @Override protected void encode(ChannelHandlerContext ctx, NetworkMessage msg, ByteBuf out) throws Exception {
        ByteBuffer buffer = out.nioBuffer();

        final Attribute<MessageWriter> writerAttribute = ctx.attr(WRITER_KEY);

        MessageWriter writer = writerAttribute.get();

        if (writer == null)
            writerAttribute.set(writer = new DirectMessageWriter((byte) 1));


    }
}
