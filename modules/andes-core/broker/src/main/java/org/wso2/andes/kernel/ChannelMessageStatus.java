/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.andes.kernel;

import java.util.EnumSet;

/**
 * This enum defines the states a message can have channel wise. A particular message can be
 * delivered to multiple channels (subscriptions). ChannelMessageStatus is used to define state
 * of a message related to a particular channel. This is merged with MessageStatus at some states.
 */
public enum  ChannelMessageStatus {


    /**
     * Message has been dispatched to subscriber
     */
    DISPATCHED(1),

    /**
     * Message has been sent to its routed consumer
     */
    SENT(2),

    /**
     * Message has been failed to send to its routed consumer
     */
    SEND_FAILED(3),

    /**
     * Message has been sent more than once.
     */
    RESENT(4),

    /**
     * The consumer has acknowledged receipt of the message
     */
    ACKED(5),

    /**
     * Consumer has rejected the message ad it has been buffered again for delivery (possibly to another waiting
     * consumer)
     */
    CLIENT_REJECTED(6);


    private int code;

    //keep next possible states
    private EnumSet<ChannelMessageStatus> next;

    //keep previous possible states
    private EnumSet<ChannelMessageStatus> previous;

    /**
     * Define a message state
     *
     * @param code integer representing state
     */
    ChannelMessageStatus(int code) {
        this.code = code;
    }

    /**
     * Get code of the state
     *
     * @return integer representing state
     */
    public int getCode() {
        return code;
    }

    /**
     * Check if submitted state is an allowed state as per state model
     *
     * @param nextState suggested next state to transit
     * @return if transition is valid
     */
    public boolean isValidNextTransition(ChannelMessageStatus nextState) {
        return next.contains(nextState);
    }

    /**
     * Check if submitted state is an allowed state as per state model
     *
     * @param previousState suggested next state to transit
     * @return if transition is valid
     */
    public boolean isValidPreviousState(ChannelMessageStatus previousState) {
        return previous.contains(previousState);
    }

    static ChannelMessageStatus parseSlotState(int state) {

        for (ChannelMessageStatus s : ChannelMessageStatus.values()) {
            if (s.code == state) {
                return s;
            }
        }

        throw new IllegalArgumentException("Invalid channel message state argument specified: " + state);
    }

    static {

        //Channel wise message status begins at DISPATCHED state.
        // If message SEND_FAILED or ACKED there is no next state for message.

        DISPATCHED.next = EnumSet.of(SENT, RESENT, SEND_FAILED);
        DISPATCHED.previous = EnumSet.complementOf(EnumSet.allOf(ChannelMessageStatus.class));

        SEND_FAILED.next = EnumSet.complementOf(EnumSet.allOf(ChannelMessageStatus.class));
        SEND_FAILED.previous = EnumSet.of(DISPATCHED);

        SENT.next = EnumSet.of(ACKED, CLIENT_REJECTED);
        SENT.previous = EnumSet.of(DISPATCHED);

        RESENT.next = EnumSet.of(ACKED, CLIENT_REJECTED);
        RESENT.previous = EnumSet.of(DISPATCHED);

        ACKED.next = EnumSet.complementOf(EnumSet.allOf(ChannelMessageStatus.class));
        ACKED.previous = EnumSet.of(SENT, RESENT);

        CLIENT_REJECTED.next = EnumSet.of(DISPATCHED);
        CLIENT_REJECTED.previous = EnumSet.of(SENT, RESENT);
    }

}