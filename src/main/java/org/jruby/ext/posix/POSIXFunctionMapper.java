/*
 * POSIXFunctionMapper.java
 */

package org.jruby.ext.posix;

import com.sun.jna.FunctionMapper;
import com.sun.jna.NativeLibrary;

import java.lang.reflect.Method;

public class POSIXFunctionMapper implements FunctionMapper {

    public POSIXFunctionMapper() {
    }

    public String getFunctionName(NativeLibrary library, Method method) {
        String name = method.getName();
        if (library.getName().equals("msvcrt")) {
            // FIXME: We should either always _ name for msvcrt or get good list of _ methods
            if (name.equals("getpid") || name.equals("chmod")) {
                name = "_" + name;
            }
        }
        return name;
    }

}
