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

import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.ignite.network.internal.MessageCollectionItemType;
import org.apache.ignite.network.internal.MessageWriter;
import org.apache.ignite.network.message.NetworkMessage;

/** */
@Deprecated
public class ScaleCubeMessageWriter implements MessageWriter {
    /** */
    private final ObjectOutputStream stream;

    /** */
    public ScaleCubeMessageWriter(ObjectOutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void setBuffer(ByteBuffer buf) {

    }

    @Override
    public void setCurrentWriteClass(Class<? extends NetworkMessage> msgCls) {

    }

    @Override
    public boolean writeHeader(short type, byte fieldCnt) {
        return false;
    }

    @Override
    public boolean writeByte(String name, byte val) {
        return false;
    }

    @Override
    public boolean writeShort(String name, short val) {
        return false;
    }

    @Override
    public boolean writeInt(String name, int val) {
        return false;
    }

    @Override
    public boolean writeLong(String name, long val) {
        return false;
    }

    @Override
    public boolean writeFloat(String name, float val) {
        return false;
    }

    @Override
    public boolean writeDouble(String name, double val) {
        return false;
    }

    @Override
    public boolean writeChar(String name, char val) {
        return false;
    }

    @Override
    public boolean writeBoolean(String name, boolean val) {
        return false;
    }

    @Override
    public boolean writeByteArray(String name, byte[] val) {
        return false;
    }

    @Override
    public boolean writeByteArray(String name, byte[] val, long off, int len) {
        return false;
    }

    @Override
    public boolean writeShortArray(String name, short[] val) {
        return false;
    }

    @Override
    public boolean writeIntArray(String name, int[] val) {
        return false;
    }

    @Override
    public boolean writeLongArray(String name, long[] val) {
        return false;
    }

    @Override
    public boolean writeLongArray(String name, long[] val, int len) {
        return false;
    }

    @Override
    public boolean writeFloatArray(String name, float[] val) {
        return false;
    }

    @Override
    public boolean writeDoubleArray(String name, double[] val) {
        return false;
    }

    @Override
    public boolean writeCharArray(String name, char[] val) {
        return false;
    }

    @Override
    public boolean writeBooleanArray(String name, boolean[] val) {
        return false;
    }

    @Override
    public boolean writeString(String name, String val) {
        return false;
    }

    @Override
    public boolean writeBitSet(String name, BitSet val) {
        return false;
    }

    @Override
    public boolean writeUuid(String name, UUID val) {
        return false;
    }

    @Override
    public boolean writeMessage(String name, NetworkMessage val) {
        return false;
    }

    @Override
    public <T> boolean writeObjectArray(String name, T[] arr, MessageCollectionItemType itemType) {
        return false;
    }

    @Override
    public <T> boolean writeCollection(String name, Collection<T> col, MessageCollectionItemType itemType) {
        return false;
    }

    @Override
    public <K, V> boolean writeMap(String name, Map<K, V> map, MessageCollectionItemType keyType, MessageCollectionItemType valType) {
        return false;
    }

    @Override
    public boolean isHeaderWritten() {
        return false;
    }

    @Override
    public void onHeaderWritten() {

    }

    @Override
    public int state() {
        return 0;
    }

    @Override
    public void incrementState() {

    }

    @Override
    public void beforeInnerMessageWrite() {

    }

    @Override
    public void afterInnerMessageWrite(boolean finished) {

    }

    @Override
    public void reset() {

    }
}
