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

/**
 * In buffer.
 *
 * @author Min Cai
 */
public class InBuffer extends NetBuffer {
    private InPort port;

    /**
     * Create an in buffer.
     *
     * @param port the in port
     * @param size the size of the buffer
     */
    public InBuffer(InPort port, int size) {
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
        this.getPort().toCrossbar(message);
    }

    /**
     * Get the in port.
     *
     * @return the in port
     */
    @Override
    public InPort getPort() {
        return port;
    }
}
