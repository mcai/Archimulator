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
package archimulator.uncore.net.simple;

import archimulator.uncore.net.simple.node.NetNode;
import archimulator.uncore.net.simple.port.OutPort;
import archimulator.util.action.Action;

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
        final OutPort portTo = this.node.getPort(message.getNodeTo());

        if (portTo.getBuffer() != null) {
            if (portTo.getBuffer().isWriteBusy()) {
                portTo.getBuffer().addPendingWriteAction(() -> toOutBuffer(message));
            } else if (portTo.getBuffer().becomesFull(message)) {
                portTo.getBuffer().addPendingFullAction(() -> toOutBuffer(message));
            } else {
                portTo.getBuffer().beginWrite();
                this.node.getNet().getCycleAccurateEventQueue().schedule(this, () -> portTo.getBuffer().endWrite(message), 1); //TODO: latency
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
