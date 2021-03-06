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

package org.apache.ignite.internal.schema;

/**
 * Column description for a type schema. Column contains a column name, a column type and a nullability flag.
 * <p>
 * Column instances are comparable in lexicographic order, native type first and then column name. Nullability
 * flag is not taken into account when columns are compared.
 */
public class Column implements Comparable<Column> {
    /** Absolute index in schema descriptor. */
    private final int schemaIndex;

    /**
     * Column name.
     */
    private final String name;

    /**
     * An instance of column data type.
     */
    private final NativeType type;

    /**
     * If {@code false}, null values will not be allowed for this column.
     */
    private final boolean nullable;

    /**
     * @param name Column name.
     * @param type An instance of column data type.
     * @param nullable If {@code false}, null values will not be allowed for this column.
     */
    public Column(
        String name,
        NativeType type,
        boolean nullable
    ) {
        this(-1, name, type, nullable);
    }

    /**
     * @param schemaIndex Absolute index of this column in its schema descriptor.
     * @param name Column name.
     * @param type An instance of column data type.
     * @param nullable If {@code false}, null values will not be allowed for this column.
     */
    Column(
        int schemaIndex,
        String name,
        NativeType type,
        boolean nullable
    ) {
        this.schemaIndex = schemaIndex;
        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }

    /**
     * @return Absolute index of this column in its schema descriptor.
     */
    public int schemaIndex() {
        return schemaIndex;
    }

    /**
     * @return Column name.
     */
    public String name() {
        return name;
    }

    /**
     * @return An instance of column data type.
     */
    public NativeType type() {
        return type;
    }

    /**
     * @return {@code false} if null values will not be allowed for this column.
     */
    public boolean nullable() {
        return nullable;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Column col = (Column)o;

        return name.equals(col.name) &&
            type.equals(col.type);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return name.hashCode() + 31 * type.hashCode();
    }

    /** {@inheritDoc} */
    @Override public int compareTo(Column o) {
        int cmp = type.compareTo(o.type);

        if (cmp != 0)
            return cmp;

        return name.compareTo(o.name);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "Column [idx=" + schemaIndex + ", name=" + name + ", type=" + type + ", nullable=" + nullable + ']';
    }
}
