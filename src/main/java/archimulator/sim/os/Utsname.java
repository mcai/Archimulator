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
package archimulator.sim.os;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Min Cai
 */
public class Utsname {
    private static final int _SYSNAME_SIZE = 64 + 1;
    private static final int SIZE_OF = _SYSNAME_SIZE * 6;

    /**
     *
     */
    public String sysname;
    /**
     *
     */
    public String nodename;
    /**
     *
     */
    public String release;
    /**
     *
     */
    public String version;
    /**
     *
     */
    public String machine;
    /**
     *
     */
    public String domainname;

    /**
     *
     * @param littleEndian
     * @return
     */
    public byte[] getBytes(boolean littleEndian) {
        try {
            String charSet = "US-ASCII";

            byte[] sysname_buf = (this.sysname + "\0").getBytes(charSet);
            byte[] nodename_buf = (this.nodename + "\0").getBytes(charSet);
            byte[] release_buf = (this.release + "\0").getBytes(charSet);
            byte[] version_buf = (this.version + "\0").getBytes(charSet);
            byte[] machine_buf = (this.machine + "\0").getBytes(charSet);
            byte[] domainname_buf = (this.domainname + "\0").getBytes(charSet);

            byte[] buffer = new byte[SIZE_OF];

            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

            byteBuffer.position(0);
            byteBuffer.put(sysname_buf);

            byteBuffer.position(_SYSNAME_SIZE);
            byteBuffer.put(nodename_buf);

            byteBuffer.position(_SYSNAME_SIZE * 2);
            byteBuffer.put(release_buf);

            byteBuffer.position(_SYSNAME_SIZE * 3);
            byteBuffer.put(version_buf);

            byteBuffer.position(_SYSNAME_SIZE * 4);
            byteBuffer.put(machine_buf);

            byteBuffer.position(_SYSNAME_SIZE * 5);
            byteBuffer.put(domainname_buf);

            return buffer;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
