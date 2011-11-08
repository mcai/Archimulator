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
package archimulator.mem.net;

import archimulator.util.action.Action;
import archimulator.util.action.NamedAction;

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
        final OutPort destPort = this.node.getPort(message.getDestNode());

        if (destPort.getBuffer() != null) {
            if (destPort.getBuffer().isWriteBusy()) {
                destPort.getBuffer().addPendingWriteAction(new Action() {
                    public void apply() {
                        toOutBuffer(message);
                    }
                });
            } else if (destPort.getBuffer().getCount() + message.getSize() > destPort.getBuffer().getSize()) {
                destPort.getBuffer().addPendingFullAction(new Action() {
                    public void apply() {
                        toOutBuffer(message);
                    }
                });
            } else {
                if (destPort.getBuffer() != null) {
                    if (destPort.getBuffer().getCount() + message.getSize() > destPort.getBuffer().getSize()) {
                        throw new IllegalArgumentException();
                    }

                    destPort.getBuffer().beginWrite();
                    this.node.getNet().getCycleAccurateEventQueue().schedule(new NamedAction("Crossbar.toOutBuffer") {
                        public void apply() {
                            destPort.getBuffer().endWrite(message);
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
