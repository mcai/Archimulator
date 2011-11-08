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

public class OutPort extends NetPort {
    private OutBuffer buffer;
    private int num;

    public OutPort(NetNode node, int num, int bufferSize) {
        super(node);

        this.num = num;

        if (bufferSize > 0) {
            this.buffer = new OutBuffer(this, bufferSize);
        }
    }

    public void toLink(final NetMessage message) {
        final NetLink link = this.getLink();

        if (this.buffer != null && this.buffer.isReadBusy()) {
            this.buffer.addPendingReadAction(new Action() {
                public void apply() {
                    toLink(message);
                }
            });
        } else if (this.getLink().isBusy()) {
            this.getLink().addPendingAction(new Action() {
                public void apply() {
                    toLink(message);
                }
            });
        } else {
            int latency = (message.getSize() + this.getLink().getBandwidth()) / this.getLink().getBandwidth();

            link.beginTransfer();
            this.getNode().getNet().getCycleAccurateEventQueue().schedule(new NamedAction(String.format("OutPort{%s.%d}.toLink(%d)", this.getNode().getName(), this.num, message.getId())) {
                public void apply() {
                    link.endTransfer(message);
                }
            }, latency);

            if (this.buffer != null) {
                this.buffer.beginRead();
                this.getNode().getNet().getCycleAccurateEventQueue().schedule(new NamedAction("OutPort.toLink[buffer]") {
                    public void apply() {
                        buffer.endRead(message);
                    }
                }, latency);
            }
        }
    }

    public OutBuffer getBuffer() {
        return buffer;
    }

    public int getNum() {
        return num;
    }
}
