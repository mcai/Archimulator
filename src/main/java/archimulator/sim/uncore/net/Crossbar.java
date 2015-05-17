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
package archimulator.sim.uncore.net;

import net.pickapack.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Cross bar.
 *
 * @author Min Cai
 */
public class Crossbar {
    private NetNode node;
    private int bandwidth;
    private boolean busy;
    private List<Action> pendingActions;

    /**
     * Create a cross bar.
     *
     * @param node      the node
     * @param bandwidth the bandwidth
     */
    public Crossbar(NetNode node, int bandwidth) {
        this.node = node;
        this.bandwidth = bandwidth;
        this.pendingActions = new ArrayList<>();
    }

    /**
     * Transfer a message to the out buffer.
     *
     * @param message the message to be transferred to the out buffer
     */
    public void toOutBuffer(final NetMessage message) {
        final OutPort destinationPort = this.node.getPort(message.getDestinationNode());

        if (destinationPort.getBuffer() != null) {
            if (destinationPort.getBuffer().isWriteBusy()) {
                destinationPort.getBuffer().addPendingWriteAction(() -> toOutBuffer(message));
            } else if (destinationPort.getBuffer().getCount() + message.getSize() > destinationPort.getBuffer().getSize()) {
                destinationPort.getBuffer().addPendingFullAction(() -> toOutBuffer(message));
            } else {
                if (destinationPort.getBuffer() != null) {
                    if (destinationPort.getBuffer().getCount() + message.getSize() > destinationPort.getBuffer().getSize()) {
                        throw new IllegalArgumentException();
                    }

                    destinationPort.getBuffer().beginWrite();
                    this.node.getNet().getCycleAccurateEventQueue().schedule(this, () -> destinationPort.getBuffer().endWrite(message), 1); //TODO: latency
                }
            }
        }
    }

    /**
     * Begin the transfer.
     */
    public void beginTransfer() {
        this.busy = true;
    }

    /**
     * End the transfer.
     *
     * @param message the message to end transfer
     */
    public void endTransfer(NetMessage message) {
        this.busy = false;
        this.doPendingActions();
        getNode().getCrossbar().toOutBuffer(message);
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
     * Get the node.
     *
     * @return the node
     */
    public NetNode getNode() {
        return node;
    }

    /**
     * Get the bandwidth.
     *
     * @return the bandwidth
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     * Get a value indicating whether the cross bar is busy or not.
     *
     * @return a value indicating whether the cross bar is busy or not
     */
    public boolean isBusy() {
        return busy;
    }
}
