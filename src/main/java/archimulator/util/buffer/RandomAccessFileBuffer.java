/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Random access file buffer.
 *
 * @author Min Cai
 */
public class RandomAccessFileBuffer implements Buffer {
    private RandomAccessFile file;

    /**
     * Create a random access file buffer.
     *
     * @param file the random access file
     */
    public RandomAccessFileBuffer(RandomAccessFile file) {
        this.file = file;
    }

    /**
     * Read data from the random access file buffer into the specified byte array.
     *
     * @param position the position
     * @param buf the destination byte array
     * @param len the length
     * @throws IOException
     */
    public void read(long position, byte[] buf, int len)
            throws IOException {
        file.seek(position);
        file.read(buf, 0, len);
    }

    /**
     * Write the data from the specified source byte array into the random access file buffer.
     *
     * @param position the position
     * @param buf the source byte array
     * @param len the length
     * @throws IOException
     */
    public void write(long position, byte[] buf, int len)
            throws IOException {
        file.seek(position);
        file.read(buf, 0, len);
    }

    /**
     * Get the supporting random access file.
     *
     * @return the supporting random access file
     */
    public RandomAccessFile getFile() {
        return file;
    }
}
