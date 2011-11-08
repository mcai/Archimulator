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
package archimulator.os;

public class OpenFlags {
    public static final int O_RDONLY = 0x00000000;
    public static final int O_WRONLY = 0x00000001;
    public static final int O_RDWR = 0x00000002;
    public static final int O_CREAT = 0x00000040;
    public static final int O_EXCL = 0x00000080;
    public static final int O_NOCTTY = 0x00000100;
    public static final int O_TRUNC = 0x00000200;
    public static final int O_APPEND = 0x00000400;
    public static final int O_NONBLOCK = 0x00000800;
    public static final int O_SYNC = 0x00001000;
}
