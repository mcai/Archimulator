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

public class FileStat {
    public long st_dev;                // device ID of file system
    public long st_ino;                // i-number
    public int st_mode;                // mode (see below)
    public int st_nlink;            // number of hard links
    public int st_uid;                // user ID
    public int st_gid;                // group ID
    public long st_rdev;                // device ID (if special file)
    public long st_size;            // size in bytes
    public long st_atime;            // last access
    public long st_mtime;            // last data modification
    public long st_ctime;            // last i-node modification
    public int st_blksize;            // optimal I/O size
    public long st_blocks;            // allocated 512-byte blocks
}
