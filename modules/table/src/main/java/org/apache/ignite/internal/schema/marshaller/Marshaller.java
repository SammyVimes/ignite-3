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

package org.apache.ignite.internal.schema.marshaller;

import org.apache.ignite.internal.table.TableRow;
import org.apache.ignite.table.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Marshaller interface.
 */
public interface Marshaller {
    /**
     * @param obj Object to serialize.
     * @return Table row with columns set from given object.
     */
    <T> TableRow serialize(@NotNull T obj);

    /**
     * @param tuple Record tuple.
     * @return Table row with columns set from given tuples.
     */
    TableRow marshalRecord(@NotNull Tuple tuple);

    /**
     * @param keyTuple Key tuple.
     * @param valTuple Value tuple.
     * @return Table row with columns set from given tuples.
     */
    TableRow marshalKVPair(@NotNull Tuple keyTuple, @Nullable Tuple valTuple);

    /**
     * @param row Table row.
     * @return Deserialized key object.
     */
    <K> @NotNull K deserializeKey(@NotNull TableRow row);

    /**
     * @param row Table row.
     * @return Deserialized value object.
     */
    <V> @Nullable V deserializeValue(@NotNull TableRow row);

    /**
     * @param row Table row.
     * @return Deserialized record object.
     */
    <R> R deserializeToRecord(@NotNull TableRow row);

    /**
     * Deserializes row and fills given record object fields.
     *
     * @param row Table row.
     * @param rec Record object to fill.
     * @return Given record with filled fields from the given row.
     */
    <R> R deserializeToRecord(@NotNull TableRow row, @NotNull R rec);
}