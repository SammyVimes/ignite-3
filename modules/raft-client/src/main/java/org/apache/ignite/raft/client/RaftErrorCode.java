/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.raft.client;

/**
 * Error codes for raft protocol.
 */
public enum RaftErrorCode {
    /** */
    SUCCESS(1000, "Successful"),

    /** */
    NO_LEADER(1001, "No leader is found within a timeout"),

    /** */
    LEADER_CHANGED(1002, "A peer is no longer a leader"),

    /** */
    ILLEGAL_STATE(1003, "A peer is in illegal state"),

    /** */
    BUSY(1004, "A peer is busy, retry later");

    /** */
    private final int code;

    /** */
    private final String desc;

    /**
     * @param code The code.
     * @param desc The desctiption.
     */
    RaftErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @return The code.
     */
    public int code() {
        return code;
    }

    /**
     * @return The description.
     */
    public String description() {
        return desc;
    }
}
