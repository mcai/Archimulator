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
package archimulator.sim.os;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Native system calls implementation.
 *
 * @author Min Cai
 */
public class NativeSystemCalls {
    /**
     * C library.
     */
    public interface LibC extends Library {
        /**
         * Get the user ID.
         *
         * @return the result
         */
        long getuid();

        /**
         * Get the effective user ID.
         *
         * @return the result
         */
        long geteuid();

        /**
         * Get the group ID.
         *
         * @return the result
         */
        long getgid();

        /**
         * Get the effective group ID.
         *
         * @return the result
         */
        long getegid();

        /**
         * read.
         *
         * @param fd    the file descriptor
         * @param buf   the buffer
         * @param count the count
         * @return the number of bytes read
         */
        int read(int fd, byte[] buf, int count);

        /**
         * write.
         *
         * @param fd    the file descriptor
         * @param buf   the buffer
         * @param count the count
         * @return the number of bytes written
         */
        int write(int fd, byte[] buf, int count);

        /**
         * Open a file.
         *
         * @param path  the file path
         * @param flags the flags
         * @param mode  the open mode
         * @return the file descriptor for the opened file
         */
        int open(String path, int flags, int mode);

        /**
         * Open a file.
         *
         * @param path  the file path
         * @param flags the flags
         * @return the file descriptor for the opened file
         */
        int open(String path, int flags);

        /**
         * Close a file.
         *
         * @param fd the file descriptor
         * @return the result
         */
        int close(int fd);

        /**
         * lseek.
         *
         * @param fd     the file descriptor
         * @param offset the offset
         * @param whence the whence
         * @return the result
         */
        int lseek(int fd, int offset, int whence);

        /**
         * ioctrl.
         *
         * @param fd      the file descriptor
         * @param request the request
         * @param buf     the buffer
         * @return the result
         */
        int ioctl(int fd, int request, byte[] buf);
    }

    /**
     * Clocks per second.
     */
    public static final int CLOCKS_PER_SEC = 1000000;

    /**
     * CPU frequency.
     */
    public static final int CPU_FREQUENCY = 300000;

    private static final String LINUX = "linux";

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_NAME_LC = OS_NAME.toLowerCase();
    private static final boolean IS_LINUX = OS_NAME_LC.startsWith(LINUX);

    private static final String LIBC_NAME = IS_LINUX ? "libc.so.6" : "c";

    /**
     * Libc singleton.
     */
    public static final LibC LIBC = (LibC) Native.loadLibrary(LIBC_NAME, LibC.class);

    /**
     * Get the clocks for the specified number of cycles.
     *
     * @param numCycles the number of cycles
     * @return the clocks for the specified number of cycles
     */
    public static long clock(long numCycles) {
        return CLOCKS_PER_SEC * numCycles / CPU_FREQUENCY;
    }
}
