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

import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

public class NetLink {
    private OutPort srcPort;
    private InPort destPort;
    private int bandwidth;
    private boolean busy;
    private List<Action> pendingActions;

    public NetLink(NetNode srcNode, NetNode destNode, int bandwidth) {
        this.srcPort = srcNode.findFreeOutPort();
        this.destPort = destNode.findFreeInPort();

        this.bandwidth = bandwidth;

        this.srcPort.setLink(this);
        this.destPort.setLink(this);

        this.pendingActions = new ArrayList<Action>();
    }

    public void toInBuffer(final NetMessage message) {
        if (this.destPort.getBuffer() != null) {
            if (this.destPort.getBuffer().isWriteBusy()) {
                this.destPort.getBuffer().addPendingWriteAction(new Action() {
                    public void apply() {
                        toInBuffer(message);
                    }
                });
            } else if (this.destPort.getBuffer().getCount() + message.getSize() > this.destPort.getBuffer().getSize()) {
                this.destPort.getBuffer().addPendingFullAction(new Action() {
                    public void apply() {
                        toInBuffer(message);
                    }
                });
            } else {
                if (this.destPort.getBuffer() != null) {
                    if (destPort.getBuffer().getCount() + message.getSize() > destPort.getBuffer().getSize()) {
                        throw new IllegalArgumentException();
                    }

                    this.destPort.getBuffer().beginWrite();
                    this.srcPort.getNode().getNet().getCycleAccurateEventQueue().schedule(this, new Action() {
                        public void apply() {
                            destPort.getBuffer().endWrite(message);
                        }
                    }, 1); //TODO: latency
                }
            }
        } else {
            message.complete(this.srcPort.getNode().getNet().getCycleAccurateEventQueue().getCurrentCycle());
        }
    }

    public void beginTransfer() {
        this.busy = true;
    }

    public void endTransfer(NetMessage message) {
        this.busy = false;
        this.doPendingActions();

        this.toInBuffer(message);
    }

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

    public NetPort getSrcPort() {
        return srcPort;
    }

    public NetPort getDestPort() {
        return destPort;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public boolean isBusy() {
        return busy;
    }
}
