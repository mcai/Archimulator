package archimulator.sim.os;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.jruby.ext.posix.LibC;
import org.jruby.ext.posix.LinuxLibC;
import org.jruby.ext.posix.POSIX;
import org.jruby.ext.posix.POSIXHandler;
import org.jruby.ext.posix.util.Platform;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class PosixUtil {
    private static final EnhancedLinuxPOSIX POSIX = loadLinuxPOSIX(new POSIXHandlerImpl());
    private static final String LIBC = Platform.IS_LINUX ? "libc.so.6" : "c";

    public static LibC loadLibC(String libraryName, Class<?> libCClass) {
        Map<Object, Object> defaultOptions = new HashMap<Object, Object>() {{
            put(Library.OPTION_TYPE_MAPPER, POSIXTypeMapper.INSTANCE);
        }};

        return (LibC) Native.loadLibrary(libraryName, libCClass, defaultOptions);
    }

    public static EnhancedLinuxPOSIX loadLinuxPOSIX(POSIXHandler handler) {
        return new EnhancedLinuxPOSIX(LIBC, loadLibC(LIBC, LinuxLibC.class), handler);
    }

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
}