/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net;

import archimulator.uncore.net.node.NetNode;
import archimulator.uncore.net.port.InPort;
import archimulator.uncore.net.port.NetPort;
import archimulator.uncore.net.port.OutPort;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Net link.
 *
 * @author Min Cai
 */
public class NetLink {
    private OutPort sourcePort;
    private InPort destinationPort;
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
        this.sourcePort = sourceNode.findFreeOutPort();
        this.destinationPort = destinationNode.findFreeInPort();

        this.bandwidth = bandwidth;

        this.sourcePort.setLink(this);
        this.destinationPort.setLink(this);

        this.pendingActions = new ArrayList<>();
    }

    /**
     * Transfer the specified message to the in buffer.
     *
     * @param message the message
     */
    public void toInBuffer(final NetMessage message) {
        if (this.destinationPort.getBuffer() != null) {
            if (this.destinationPort.getBuffer().isWriteBusy()) {
                this.destinationPort.getBuffer().addPendingWriteAction(() -> toInBuffer(message));
            } else if (this.destinationPort.getBuffer().getCount() + message.getSize() > this.destinationPort.getBuffer().getSize()) {
                this.destinationPort.getBuffer().addPendingFullAction(() -> toInBuffer(message));
            } else {
                if (destinationPort.getBuffer().getCount() + message.getSize() > destinationPort.getBuffer().getSize()) {
                    throw new IllegalArgumentException();
                }

                this.destinationPort.getBuffer().beginWrite();
                this.sourcePort.getNode().getNet().getCycleAccurateEventQueue().schedule(this, () -> destinationPort.getBuffer().endWrite(message), 1); //TODO: latency
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
    public NetPort getSourcePort() {
        return sourcePort;
    }

    /**
     * Get the destination port.
     *
     * @return the destination port
     */
    public NetPort getDestinationPort() {
        return destinationPort;
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
