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

package org.apache.ignite.raft.client.message;

import java.io.Serializable;
import java.util.List;
import org.apache.ignite.network.message.NetworkMessage;
import org.apache.ignite.raft.client.Peer;

/**
 * Add peers.
 */
public interface AddPeersRequest extends NetworkMessage, Serializable {
    /**
     * @return Group id.
     */
    String groupId();

    /**
     * @return Peers.
     */
    List<Peer> peers();

    /** */
    interface Builder {
        /**
         * @param groupId Group id.
         * @return The builder.
         */
        Builder groupId(String groupId);

        /**
         * @param peers Peers.
         * @return The builder.
         */
        Builder peers(List<Peer> peers);

        /**
         * @return The complete message.
         * @throws IllegalStateException If the message is not in valid state.
         */
        AddPeersRequest build();
    }
}
