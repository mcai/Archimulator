/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.os;

import org.jruby.ext.posix.*;
import org.jruby.ext.posix.util.Platform;

import java.io.FileDescriptor;

/**
 * Enhanced Linux POSIX implementation.
 *
 * @author Min Cai
 */
public final class EnhancedLinuxPOSIX extends BaseNativePOSIX {
    private final boolean hasFxstat;
    private final boolean hasLxstat;
    private final boolean hasXstat;
    private final boolean hasFstat;
    private final boolean hasLstat;
    private final boolean hasStat;
    private final int statVersion;

    /**
     * Create an enhanced Linux POSIX implementation.
     *
     * @param libraryName the library name
     * @param libc        the libc
     * @param handler     the POSIX handler
     */
    public EnhancedLinuxPOSIX(String libraryName, LibC libc, POSIXHandler handler) {
        super(libraryName, libc, handler);

        this.statVersion = Platform.IS_32_BIT ? 3 : 0;

        /*
         * Most linux systems define stat/lstat/fstat as macros which force
         * us to call these weird signature versions.
         */
        this.hasFxstat = this.hasMethod("__fxstat64");
        this.hasLxstat = this.hasMethod("__lxstat64");
        this.hasXstat = this.hasMethod("__xstat64");

        /*
        * At least one person is using uLibc on linux which has real
        * definitions for stat/lstat/fstat.
        */
        this.hasFstat = !this.hasFxstat && this.hasMethod("fstat64");
        this.hasLstat = !this.hasLxstat && this.hasMethod("lstat64");
        this.hasStat = !this.hasXstat && this.hasMethod("stat64");
    }

    /**
     * Allocate a file stat structure.
     *
     * @return the allocated file stat structure
     */
    @Override
    public FileStat allocateStat() {
        if (Platform.IS_32_BIT) {
            return new LinuxHeapFileStat(this);
        } else {
            return new Linux64HeapFileStat(this);
        }
    }

    /**
     * Create a file stat structure based on the specified file descriptor.
     *
     * @param fileDescriptor the file descriptor
     * @return the newly created file stat structure based on the specified file descriptor
     */
    @Override
    public FileStat fstat(FileDescriptor fileDescriptor) {
        if (!this.hasFxstat) {
            if (this.hasFstat) return super.fstat(fileDescriptor);

            this.handler.unimplementedError("fstat");
        }

        FileStat stat = allocateStat();
        int fd = this.helper.getfd(fileDescriptor);

        if (((LinuxLibC) this.libc).__fxstat64(this.statVersion, fd, stat) < 0)
            this.handler.error(ERRORS.ENOENT, "" + fd);

        return stat;
    }

    /**
     * Create a file stat structure based on the specified file descriptor number.
     *
     * @param fd the file descriptor number
     * @return the newly created file stat structure based on the specified file descriptor number
     */
    public FileStat fstat(int fd) {
        if (!this.hasFxstat) {
            throw new UnsupportedOperationException();
        }

        FileStat stat = allocateStat();

        if (((LinuxLibC) this.libc).__fxstat64(this.statVersion, fd, stat) < 0)
            this.handler.error(ERRORS.ENOENT, "" + fd);

        return stat;
    }
}
