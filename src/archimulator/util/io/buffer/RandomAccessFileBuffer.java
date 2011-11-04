/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.io.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileBuffer implements Buffer {
    private RandomAccessFile file;

    public RandomAccessFileBuffer(RandomAccessFile file) {
        this.file = file;
    }

    public void read(long position, byte[] buf, int len)
            throws IOException {
        file.seek(position);
        file.read(buf, 0, len);
    }

    public void write(long position, byte[] buf, int len)
            throws IOException {
        file.seek(position);
        file.read(buf, 0, len);
    }

    public RandomAccessFile getFile() {
        return file;
    }
}
