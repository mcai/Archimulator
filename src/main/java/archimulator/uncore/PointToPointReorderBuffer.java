/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore;

import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.uncore.coherence.msi.message.CoherenceMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Point to point reorder buffer.
 *
 * @author Min Cai
 */
public class PointToPointReorderBuffer {
    private List<CoherenceMessage> messages;
    private Controller from;
    private Controller to;
    private long lastCompletedMessageId = -1;

    /**
     * Create a point to point reorder buffer from the source controller to the destination controller.
     *
     * @param from the source controller
     * @param to   the destination controller
     */
    public PointToPointReorderBuffer(Controller from, Controller to) {
        this.from = from;
        this.to = to;
        this.messages = new ArrayList<>();
    }

    /**
     * Transfer the specified message.
     *
     * @param message the message
     */
    public void transfer(CoherenceMessage message) {
        this.messages.add(message);
    }

    /**
     * Act on when a message arrives at the destination.
     *
     * @param message the message
     */
    public void onDestinationArrived(CoherenceMessage message) {
        message.onDestinationArrived();
        this.commit();
    }

    /**
     * Commit. Remove and notify the completion of the completed messages from the list of messages.
     */
    public void commit() {
        while (!this.messages.isEmpty()) {
            final CoherenceMessage message = this.messages.get(0);
            if (!message.isDestinationArrived()) {
                break;
            }

            this.messages.remove(message);

            to.getCycleAccurateEventQueue().schedule(this, () -> {
                message.onCompleted();
                if (message.getId() < lastCompletedMessageId) {
                    throw new IllegalArgumentException(String.format("p2pReorderBuffer[%s->%s] messageId: %d, lastCompletedMessageId: %d", from.getName(), to.getName(), message.getId(), lastCompletedMessageId));
                }
                lastCompletedMessageId = message.getId();
                to.receive(message);
            }, to.getHitLatency());
        }
    }

    /**
     * Get the list of messages.
     *
     * @return the list of messages
     */
    public List<CoherenceMessage> getMessages() {
        return messages;
    }

    /**
     * Get the source controller.
     *
     * @return the source controller
     */
    public Controller getFrom() {
        return from;
    }

    /**
     * Get the destination controller.
     *
     * @return the destination controller
     */
    public Controller getTo() {
        return to;
    }
}
