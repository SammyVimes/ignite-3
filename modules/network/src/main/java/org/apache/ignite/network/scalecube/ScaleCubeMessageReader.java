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

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.ignite.network.internal.MessageCollectionItemType;
import org.apache.ignite.network.internal.MessageReader;
import org.apache.ignite.network.message.NetworkMessage;

/** */
@Deprecated
public class ScaleCubeMessageReader implements MessageReader {
    /** */
    private final ObjectInputStream stream;

    /** */
    public ScaleCubeMessageReader(ObjectInputStream stream) {
        this.stream = stream;
    }

    @Override
    public void setBuffer(ByteBuffer buf) {

    }

    @Override
    public void setCurrentReadClass(Class<? extends NetworkMessage> msgCls) {

    }

    @Override
    public boolean beforeMessageRead() {
        return false;
    }

    @Override
    public boolean afterMessageRead(Class<? extends NetworkMessage> msgCls) {
        return false;
    }

    @Override
    public byte readByte(String name) {
        return 0;
    }

    @Override
    public short readShort(String name) {
        return 0;
    }

    @Override
    public int readInt(String name) {
        return 0;
    }

    @Override
    public int readInt(String name, int dflt) {
        return 0;
    }

    @Override
    public long readLong(String name) {
        return 0;
    }

    @Override
    public float readFloat(String name) {
        return 0;
    }

    @Override
    public double readDouble(String name) {
        return 0;
    }

    @Override
    public char readChar(String name) {
        return 0;
    }

    @Override
    public boolean readBoolean(String name) {
        return false;
    }

    @Override
    public byte[] readByteArray(String name) {
        return new byte[0];
    }

    @Override
    public short[] readShortArray(String name) {
        return new short[0];
    }

    @Override
    public int[] readIntArray(String name) {
        return new int[0];
    }

    @Override
    public long[] readLongArray(String name) {
        return new long[0];
    }

    @Override
    public float[] readFloatArray(String name) {
        return new float[0];
    }

    @Override
    public double[] readDoubleArray(String name) {
        return new double[0];
    }

    @Override
    public char[] readCharArray(String name) {
        return new char[0];
    }

    @Override
    public boolean[] readBooleanArray(String name) {
        return new boolean[0];
    }

    @Override
    public String readString(String name) {
        return null;
    }

    @Override
    public BitSet readBitSet(String name) {
        return null;
    }

    @Override
    public UUID readUuid(String name) {
        return null;
    }

    @Override
    public <T extends NetworkMessage> T readMessage(String name) {
        return null;
    }

    @Override
    public <T> T[] readObjectArray(String name, MessageCollectionItemType itemType, Class<T> itemCls) {
        return null;
    }

    @Override
    public <C extends Collection<?>> C readCollection(String name, MessageCollectionItemType itemType) {
        return null;
    }

    @Override
    public <M extends Map<?, ?>> M readMap(String name, MessageCollectionItemType keyType, MessageCollectionItemType valType, boolean linked) {
        return null;
    }

    @Override
    public boolean isLastRead() {
        return false;
    }

    @Override
    public int state() {
        return 0;
    }

    @Override
    public void incrementState() {

    }

    @Override
    public void beforeInnerMessageRead() {

    }

    @Override
    public void afterInnerMessageRead(boolean finished) {

    }

    @Override
    public void reset() {

    }
}
