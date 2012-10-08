/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
     *
     * @param sourceNode
     * @param destinationNode
     * @param bandwidth
     */
    public NetLink(NetNode sourceNode, NetNode destinationNode, int bandwidth) {
        this.sourcePort = sourceNode.findFreeOutPort();
        this.destinationPort = destinationNode.findFreeInPort();

        this.bandwidth = bandwidth;

        this.sourcePort.setLink(this);
        this.destinationPort.setLink(this);

        this.pendingActions = new ArrayList<Action>();
    }

    /**
     *
     * @param message
     */
    public void toInBuffer(final NetMessage message) {
        if (this.destinationPort.getBuffer() != null) {
            if (this.destinationPort.getBuffer().isWriteBusy()) {
                this.destinationPort.getBuffer().addPendingWriteAction(new Action() {
                    public void apply() {
                        toInBuffer(message);
                    }
                });
            } else if (this.destinationPort.getBuffer().getCount() + message.getSize() > this.destinationPort.getBuffer().getSize()) {
                this.destinationPort.getBuffer().addPendingFullAction(new Action() {
                    public void apply() {
                        toInBuffer(message);
                    }
                });
            } else {
                if (this.destinationPort.getBuffer() != null) {
                    if (destinationPort.getBuffer().getCount() + message.getSize() > destinationPort.getBuffer().getSize()) {
                        throw new IllegalArgumentException();
                    }

                    this.destinationPort.getBuffer().beginWrite();
                    this.sourcePort.getNode().getNet().getCycleAccurateEventQueue().schedule(this, new Action() {
                        @Override
                        public void apply() {
                            destinationPort.getBuffer().endWrite(message);
                        }
                    }, 1); //TODO: latency
                }
            }
        } else {
            message.complete(sourcePort.getNode().getNet().getCycleAccurateEventQueue().getCurrentCycle());
        }
    }

    /**
     *
     */
    public void beginTransfer() {
        this.busy = true;
    }

    /**
     *
     * @param message
     */
    public void endTransfer(NetMessage message) {
        this.busy = false;
        this.doPendingActions();

        this.toInBuffer(message);
    }

    /**
     *
     * @param action
     */
    public void addPendingAction(Action action) {
        this.pendingActions.add(action);
    }

    private void doPendingActions() {
        if (!this.pendingActions.isEmpty()) {
            Action action = this.pendingActions.get(0);
            action.apply();
            this.pendingActions.remove(action);
        }
    }

    /**
     *
     * @return
     */
    public NetPort getSourcePort() {
        return sourcePort;
    }

    /**
     *
     * @return
     */
    public NetPort getDestinationPort() {
        return destinationPort;
    }

    /**
     *
     * @return
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     *
     * @return
     */
    public boolean isBusy() {
        return busy;
    }
}
