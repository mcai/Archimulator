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
package archimulator.os;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utsname {
    private static final int _SYSNAME_SIZE = 64 + 1;
    private static final int sizeof = _SYSNAME_SIZE * 6;

    public String sysname;
    public String nodename;
    public String release;
    public String version;
    public String machine;
    public String domainname;

    public byte[] getBytes(boolean littleEndian) {
        try {
            String charSet = "US-ASCII";

            byte[] sysname_buf = (this.sysname + "\0").getBytes(charSet);
            byte[] nodename_buf = (this.nodename + "\0").getBytes(charSet);
            byte[] release_buf = (this.release + "\0").getBytes(charSet);
            byte[] version_buf = (this.version + "\0").getBytes(charSet);
            byte[] machine_buf = (this.machine + "\0").getBytes(charSet);
            byte[] domainname_buf = (this.domainname + "\0").getBytes(charSet);

            byte[] buf = new byte[sizeof];

            ByteBuffer bb = ByteBuffer.wrap(buf).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

            bb.position(0);
            bb.put(sysname_buf);

            bb.position(_SYSNAME_SIZE);
            bb.put(nodename_buf);

            bb.position(_SYSNAME_SIZE * 2);
            bb.put(release_buf);

            bb.position(_SYSNAME_SIZE * 3);
            bb.put(version_buf);

            bb.position(_SYSNAME_SIZE * 4);
            bb.put(machine_buf);

            bb.position(_SYSNAME_SIZE * 5);
            bb.put(domainname_buf);

            return buf;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
