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
package archimulator.util;

import archimulator.service.ServiceManager;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.io.cmd.CommandLineHelper;
import net.pickapack.io.cmd.SedHelper;

import java.util.Date;
import java.util.List;

public class SimulatedProgramBuildHelper {
    public static void build(String cwd, boolean ht, int htLookahead, int htStride) {
        if (ht) {
            pushMacroDefineArg(cwd, "push_params.h", "LOOKAHEAD", htLookahead + "");
            pushMacroDefineArg(cwd, "push_params.h", "STRIDE", htStride + "");
        }
        buildWithMakefile(cwd);
    }

    public static void pushMacroDefineArg(String cwd, String fileName, String key, String value) {
        fileName = cwd.replaceAll(ServiceManager.USER_HOME_TEMPLATE_ARG, System.getProperty("user.home")) + "/" + fileName;
        System.out.printf("[%s] Pushing Macro Define Arg in %s: %s, %s\n", DateHelper.toString(new Date()), fileName, key, value);
        List<String> result = SedHelper.sedInPlace(fileName, "#define " + key, "#define " + key + " " + value);
        for(String line : result) {
            System.out.println(line);
        }
    }

    public static void buildWithMakefile(String cwd) {
        System.out.printf("[%s] Building with Makefile\n", DateHelper.toString(new Date()));
        List<String> result = CommandLineHelper.invokeShellCommandAndGetResult("sh -c 'cd " + cwd.replaceAll(ServiceManager.USER_HOME_TEMPLATE_ARG, System.getProperty("user.home")) + ";make -f Makefile.mips -B'");
        for(String line : result) {
            System.out.println(line);
        }
    }
}
