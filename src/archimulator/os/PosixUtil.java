package archimulator.os;

import org.jruby.ext.posix.POSIX;
import org.jruby.ext.posix.POSIXFactory;
import org.jruby.ext.posix.POSIXHandler;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

public class PosixUtil {
    private static final POSIX POSIX = POSIXFactory.getPOSIX(new POSIXHandlerImpl(), true);

    public static POSIX current() {
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