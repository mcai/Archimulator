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
package archimulator.uncore.net.simple.buffer;

import archimulator.uncore.net.simple.common.NetMessage;
import archimulator.uncore.net.simple.port.OutPort;

/**
 * Out buffer.
 *
 * @author Min Cai
 */
public class OutBuffer extends NetBuffer {
    private OutPort port;

    /**
     * Create an out buffer.
     *
     * @param port the out port
     * @param size the size of the buffer
     */
    public OutBuffer(OutPort port, int size) {
        super(size);
        this.port = port;
    }

    /**
     * End the writing of the specified message.
     *
     * @param message the message
     */
    public void endWrite(NetMessage message) {
        super.endWrite(message);
        this.getPort().toLink(message);
    }

    /**
     * Get the out port.
     *
     * @return the out port
     */
    @Override
    public OutPort getPort() {
        return port;
    }
}
