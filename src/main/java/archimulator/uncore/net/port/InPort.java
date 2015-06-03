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
package archimulator.uncore.net.port;

import archimulator.uncore.net.common.NetMessage;
import archimulator.uncore.net.node.NetNode;
import archimulator.uncore.net.buffer.InBuffer;

/**
 * In port.
 *
 * @author Min Cai
 */
public class InPort extends NetPort {
    private InBuffer buffer;
    private int num;

    /**
     * Create an in port.
     *
     * @param node       the node
     * @param num        the number of the in port
     * @param bufferSize the size of the buffer
     */
    public InPort(NetNode node, int num, int bufferSize) {
        super(node);

        this.num = num;

        if (bufferSize > 0) {
            this.buffer = new InBuffer(this, bufferSize);
        }
    }

    /**
     * Transfer the message to the cross bar.
     *
     * @param message the message
     */
    public void toCrossbar(final NetMessage message) {
        if (this.buffer != null && this.buffer.isReadBusy()) {
            this.buffer.addPendingReadAction(() -> toCrossbar(message));
        } else if (this.getNode().getCrossbar() != null && this.getNode().getCrossbar().isBusy()) {
            this.getNode().getCrossbar().addPendingAction(() -> toCrossbar(message));
        } else {
            if (this.getNode().getCrossbar() != null) {
                int latency = (message.getSize() + this.getNode().getCrossbar().getBandwidth()) / this.getNode().getCrossbar().getBandwidth();

                this.getNode().getCrossbar().beginTransfer();
                this.getNode().getNet().getCycleAccurateEventQueue().schedule(this, () -> getNode().getCrossbar().endTransfer(message), latency);

                if (this.buffer != null) {
                    this.buffer.beginRead();
                    this.getNode().getNet().getCycleAccurateEventQueue().schedule(this, () -> buffer.endRead(message), latency);
                }
            }
        }
    }

    /**
     * Get the in buffer.
     *
     * @return the in buffer
     */
    public InBuffer getBuffer() {
        return buffer;
    }

    /**
     * Get the number of the in port.
     *
     * @return the number of the in port
     */
    public int getNum() {
        return num;
    }
}
