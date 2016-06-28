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

import com.sun.jna.*;
import org.jruby.ext.posix.*;
import org.jruby.ext.posix.util.Platform;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Posix utility class.
 *
 * @author Min Cai
 */
public class PosixUtil {
    private static final EnhancedLinuxPOSIX POSIX = loadLinuxPOSIX(new POSIXHandlerImpl());
    private static final String LIBC = Platform.IS_LINUX ? "libc.so.6" : "c";

    /**
     * Load the C library.
     *
     * @param libraryName the library name
     * @param libCClass   the libc class
     * @return the loaded libc instance
     */
    public static LibC loadLibC(String libraryName, Class<?> libCClass) {
        Map<Object, Object> defaultOptions = new HashMap<Object, Object>() {{
            put(Library.OPTION_TYPE_MAPPER, POSIXTypeMapper.INSTANCE);
        }};

        return (LibC) Native.loadLibrary(libraryName, libCClass, defaultOptions);
    }

    /**
     * load the enhanced linux POSIX implementation.
     *
     * @param handler the POSIX handler
     * @return the newly loaded enhanced linux POSIX implementation
     */
    public static EnhancedLinuxPOSIX loadLinuxPOSIX(POSIXHandler handler) {
        return new EnhancedLinuxPOSIX(LIBC, loadLibC(LIBC, LinuxLibC.class), handler);
    }

    /**
     * Get the current enhanced linux POSIX implementation.
     *
     * @return the current enhanced linux POSIX implementation
     */
    public static EnhancedLinuxPOSIX current() {
        return POSIX;
    }

    /**
     * POSIX handler implementation.
     */
    private static class POSIXHandlerImpl implements POSIXHandler {
        public void error(POSIX.ERRORS errors, String message) {
            throw new UnsupportedOperationException();
        }

        public void unimplementedError(String message) {
            throw new UnsupportedOperationException();
        }

        public void warn(WARNING_ID warningId, String message, Object... objects) {
        }

        public boolean isVerbose() {
            return false;
        }

        public File getCurrentWorkingDirectory() {
            throw new UnsupportedOperationException();
        }

        public String[] getEnv() {
            throw new UnsupportedOperationException();
        }

        public InputStream getInputStream() {
            return System.in;
        }

        public PrintStream getOutputStream() {
            return System.out;
        }

        public int getPID() {
            throw new UnsupportedOperationException();
        }

        public PrintStream getErrorStream() {
            return System.err;
        }
    }

    /**
     * POSIX type mapper.
     */
    private static class POSIXTypeMapper implements TypeMapper {
        /**
         * The POSIX type mapper singleton.
         */
        public static final TypeMapper INSTANCE = new POSIXTypeMapper();

        /**
         * Create a POSIX type mapper.
         */
        private POSIXTypeMapper() {
        }

        public FromNativeConverter getFromNativeConverter(Class klazz) {
            if (Passwd.class.isAssignableFrom(klazz)) {
                if (Platform.IS_MAC) {
                    return MacOSPOSIX.PASSWD;
                } else if (Platform.IS_LINUX) {
                    return LinuxPOSIX.PASSWD;
                } else if (Platform.IS_SOLARIS) {
                    return SolarisPOSIX.PASSWD;
                } else if (Platform.IS_FREEBSD) {
                    return FreeBSDPOSIX.PASSWD;
                } else if (Platform.IS_OPENBSD) {
                    return OpenBSDPOSIX.PASSWD;
                }
                return null;
            } else if (Group.class.isAssignableFrom(klazz)) {
                return BaseNativePOSIX.GROUP;
            }

            return null;
        }

        public ToNativeConverter getToNativeConverter(Class klazz) {
            return null;
        }
    }
}