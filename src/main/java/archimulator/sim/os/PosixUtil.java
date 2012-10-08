package archimulator.sim.os;

import com.sun.jna.*;
import org.jruby.ext.posix.*;
import org.jruby.ext.posix.util.Platform;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Min Cai
 */
public class PosixUtil {
    private static final EnhancedLinuxPOSIX POSIX = loadLinuxPOSIX(new POSIXHandlerImpl());
    private static final String LIBC = Platform.IS_LINUX ? "libc.so.6" : "c";

    /**
     *
     * @param libraryName
     * @param libCClass
     * @return
     */
    public static LibC loadLibC(String libraryName, Class<?> libCClass) {
        Map<Object, Object> defaultOptions = new HashMap<Object, Object>() {{
            put(Library.OPTION_TYPE_MAPPER, POSIXTypeMapper.INSTANCE);
        }};

        return (LibC) Native.loadLibrary(libraryName, libCClass, defaultOptions);
    }

    /**
     *
     * @param handler
     * @return
     */
    public static EnhancedLinuxPOSIX loadLinuxPOSIX(POSIXHandler handler) {
        return new EnhancedLinuxPOSIX(LIBC, loadLibC(LIBC, LinuxLibC.class), handler);
    }

    /**
     *
     * @return
     */
    public static EnhancedLinuxPOSIX current() {
        return POSIX;
    }

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

    private static class POSIXTypeMapper implements TypeMapper {
        public static final TypeMapper INSTANCE = new POSIXTypeMapper();

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