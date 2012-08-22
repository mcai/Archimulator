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

public class Crossbar {
    private NetNode node;
    private int bandwidth;
    private boolean busy;
    private List<Action> pendingActions;

    public Crossbar(NetNode node, int bandwidth) {
        this.node = node;
        this.bandwidth = bandwidth;
        this.pendingActions = new ArrayList<Action>();
    }

    public void toOutBuffer(final NetMessage message) {
        final OutPort destinationPort = this.node.getPort(message.getDestinationNode());

        if (destinationPort.getBuffer() != null) {
            if (destinationPort.getBuffer().isWriteBusy()) {
                destinationPort.getBuffer().addPendingWriteAction(new Action() {
                    public void apply() {
                        toOutBuffer(message);
                    }
                });
            } else if (destinationPort.getBuffer().getCount() + message.getSize() > destinationPort.getBuffer().getSize()) {
                destinationPort.getBuffer().addPendingFullAction(new Action() {
                    public void apply() {
                        toOutBuffer(message);
                    }
                });
            } else {
                if (destinationPort.getBuffer() != null) {
                    if (destinationPort.getBuffer().getCount() + message.getSize() > destinationPort.getBuffer().getSize()) {
                        throw new IllegalArgumentException();
                    }

                    destinationPort.getBuffer().beginWrite();
                    this.node.getNet().getCycleAccurateEventQueue().schedule(this, new Action() {
                        public void apply() {
                            destinationPort.getBuffer().endWrite(message);
                        }
                    }, 1); //TODO: latency
                }
            }
        }
    }

    public void beginTransfer() {
        this.busy = true;
    }

    public void endTransfer(NetMessage message) {
        this.busy = false;
        this.doPendingActions();
        getNode().getCrossbar().toOutBuffer(message);
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

    public NetNode getNode() {
        return node;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public boolean isBusy() {
        return busy;
    }
}
