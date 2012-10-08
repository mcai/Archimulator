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

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 *
 * @author Min Cai
 */
public class NativeSystemCalls {
    /**
     *
     */
    public interface LibC extends Library {
        /**
         *
         * @return
         */
        long getuid();

        /**
         *
         * @return
         */
        long geteuid();

        /**
         *
         * @return
         */
        long getgid();

        /**
         *
         * @return
         */
        long getegid();

        /**
         *
         * @param fd
         * @param buf
         * @param count
         * @return
         */
        int read(int fd, byte[] buf, int count);

        /**
         *
         * @param fd
         * @param buf
         * @param count
         * @return
         */
        int write(int fd, byte[] buf, int count);

        /**
         *
         * @param path
         * @param flags
         * @param mode
         * @return
         */
        int open(String path, int flags, int mode);

        /**
         *
         * @param path
         * @param flags
         * @return
         */
        int open(String path, int flags);

        /**
         *
         * @param fd
         * @return
         */
        int close(int fd);

        /**
         *
         * @param fd
         * @param offset
         * @param whence
         * @return
         */
        int lseek(int fd, int offset, int whence);

        /**
         *
         * @param fd
         * @param request
         * @param buf
         * @return
         */
        int ioctl(int fd, int request, byte[] buf);
    }

    /**
     *
     */
    public static final int CLOCKS_PER_SEC = 1000000;
    /**
     *
     */
    public static final int CPU_FREQUENCY = 300000;

    private static final String LINUX = "linux";

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_NAME_LC = OS_NAME.toLowerCase();
    private static final boolean IS_LINUX = OS_NAME_LC.startsWith(LINUX);

    private static final String LIBC_NAME = IS_LINUX ? "libc.so.6" : "c";

    /**
     *
     */
    public static final LibC LIBC = (LibC) Native.loadLibrary(LIBC_NAME, LibC.class);

    /**
     *
     * @param totalCycles
     * @return
     */
    public static long clock(long totalCycles) {
        return CLOCKS_PER_SEC * totalCycles / CPU_FREQUENCY;
    }
}
