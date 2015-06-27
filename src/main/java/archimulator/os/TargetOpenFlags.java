/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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

/**
 * Target open flags.
 *
 * @author Min Cai
 */
public class TargetOpenFlags {
    /**
     * Open for reading only.
     */
    public static final int O_RDONLY = 0;

    /**
     * Open for writing only.
     */
    public static final int O_WRONLY = 1;

    /**
     * Open for reading and writing.
     */
    public static final int O_RDWR = 2;

    /**
     * If the file exists, this flag has no effect except as noted under O_EXCL below. Otherwise, the file is created.
     */
    public static final int O_CREAT = 0x100;

    /**
     * If O_CREAT and O_EXCL are set, open() will fail if the file exists.
     */
    public static final int O_EXCL = 0x400;

    /**
     * If set and path identifies a terminal device, open() will not cause the terminal device to become the controlling terminal for the process.
     */
    public static final int O_NOCTTY = 0x800;

    /**
     * If the file exists and is a regular file, and the file is successfully opened O_RDWR or O_WRONLY, its length is truncated to 0 and the mode and owner are unchanged.
     */
    public static final int O_TRUNC = 0x200;

    /**
     * If set, the file offset will be set to the end of the file prior to each write.
     */
    public static final int O_APPEND = 8;

    /**
     * Non-blocking.
     */
    public static final int O_NONBLOCK = 0x80;

    /**
     * Write I/O operations on the file descriptor complete as defined by synchronised I/O file integrity completion.
     */
    public static final int O_SYNC = 0x10;
}
