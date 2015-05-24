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

/**
 *
 * @author Min Cai
 */
public class LittleEndianBufferAccessor extends BufferAccessor {
    /**
     *
     * @param buffer
     * @param position
     * @return
     * @throws IOException
     */
    public long getU4(Buffer buffer, long position) throws IOException {
        byte[] buf = new byte[4];
        buffer.read(position, buf, 4);
        int b3 = (int) buf[0];
        int b2 = (int) buf[1];
        int b1 = (int) buf[2];
        int b0 = (int) buf[3];

        return (((b3 & 0xff) | ((b2 & 0xff) << 8) |
                ((b1 & 0xff) << 16) | ((b0 & 0xff) << 24)) &
                0x00000000ffffffffL);
    }

    /**
     *
     * @param buffer
     * @param position
     * @return
     * @throws IOException
     */
    public int getU2(Buffer buffer, long position) throws IOException {
        byte[] buf = new byte[2];
        buffer.read(position, buf, 2);

        int b1 = (int) buf[0];
        int b0 = (int) buf[1];

        return (b1 & 0xff) | ((b0 & 0xff) << 8);
    }
}
