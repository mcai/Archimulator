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
package archimulator.os;

import archimulator.util.buffer.CircularByteBuffer;

/**
 * Pipe.
 *
 * @author Min Cai
 */
public class Pipe {
    private int[] fileDescriptors;
    private CircularByteBuffer buffer;

    /**
     * Create a pipe for the array of two file descriptors.
     *
     * @param fileDescriptors the array of two file descriptors
     */
    public Pipe(int[] fileDescriptors) {
        this.fileDescriptors = fileDescriptors;
        this.buffer = new CircularByteBuffer(1024);
    }

    /**
     * Get the array of two file descriptors.
     *
     * @return the array of two file descriptors
     */
    public int[] getFileDescriptors() {
        return fileDescriptors;
    }

    /**
     * Get the buffer.
     *
     * @return the buffer
     */
    public CircularByteBuffer getBuffer() {
        return buffer;
    }
}
