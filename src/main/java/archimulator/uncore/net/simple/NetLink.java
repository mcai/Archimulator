/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.uncore.net.simple;

import archimulator.uncore.net.simple.node.NetNode;
import archimulator.uncore.net.simple.port.InPort;
import archimulator.uncore.net.simple.port.NetPort;
import archimulator.uncore.net.simple.port.OutPort;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Net link.
 *
 * @author Min Cai
 */
public class NetLink {
    private OutPort portFrom;
    private InPort portTo;
    private int bandwidth;
    private boolean busy;
    private List<Action> pendingActions;

    /**
     * Create a net link from the source node to the destination node.
     *
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @param bandwidth       the bandwidth of the link
     */
    public NetLink(NetNode sourceNode, NetNode destinationNode, int bandwidth) {
        this.portFrom = sourceNode.findFreeOutPort();
        this.portTo = destinationNode.findFreeInPort();

        this.bandwidth = bandwidth;

        this.portFrom.setLink(this);
        this.portTo.setLink(this);

        this.pendingActions = new ArrayList<>();
    }

    /**
     * Transfer the specified message to the in buffer.
     *
     * @param message the message
     */
    public void toInBuffer(final NetMessage message) {
        if (this.portTo.getBuffer() != null) {
            if (this.portTo.getBuffer().isWriteBusy()) {
                this.portTo.getBuffer().addPendingWriteAction(() -> toInBuffer(message));
            } else if (this.portTo.getBuffer().becomesFull(message)) {
                this.portTo.getBuffer().addPendingFullAction(() -> toInBuffer(message));
            } else {
                this.portTo.getBuffer().beginWrite();
                this.portFrom.getNode().getNet().getCycleAccurateEventQueue().schedule(this, () -> portTo.getBuffer().endWrite(message), 1); //TODO: latency
            }
        } else {
            message.complete();
        }
    }

    /**
     * Begin the transfer.
     */
    public void beginTransfer() {
        this.busy = true;
    }

    /**
     * End the transfer of the specified message.
     *
     * @param message the message to end transferring
     */
    public void endTransfer(NetMessage message) {
        this.busy = false;
        this.doPendingActions();

        this.toInBuffer(message);
    }

    /**
     * Add the specified action to the pending action list.
     *
     * @param action the action
     */
    public void addPendingAction(Action action) {
        this.pendingActions.add(action);
    }

    /**
     * Do pending actions.
     */
    private void doPendingActions() {
        if (!this.pendingActions.isEmpty()) {
            Action action = this.pendingActions.get(0);
            action.apply();
            this.pendingActions.remove(action);
        }
    }

    /**
     * Get the source port.
     *
     * @return the source port
     */
    public NetPort getPortFrom() {
        return portFrom;
    }

    /**
     * Get the destination port.
     *
     * @return the destination port
     */
    public NetPort getPortTo() {
        return portTo;
    }

    /**
     * Get the bandwidth of the link.
     *
     * @return the bandwidth of the link
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     * Get a value indicating whether the link is busy or not.
     *
     * @return a value indicating whether the link is busy or not
     */
    public boolean isBusy() {
        return busy;
    }
}
