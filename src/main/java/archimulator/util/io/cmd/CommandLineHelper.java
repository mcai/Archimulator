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
package archimulator.util.io.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandLineHelper {
    public static int invokeNativeCommand(String[] cmd) {
        try {
            Runtime r = Runtime.getRuntime();
            Process ps = r.exec(cmd);
//            ProcessBuilder pb = new ProcessBuilder(cmd);
//            Process ps = pb.start();

            int exitValue = ps.waitFor();
            if (exitValue != 0) {
                System.out.println("WARN: Process exits with non-zero code: " + exitValue);
            }

            ps.destroy();

            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int invokeNativeCommand(String args) {
        return invokeNativeCommand(args.split(" "));
    }

    public static int invokeShellCommand(String args) {
        return invokeNativeCommand(new String[]{"sh", "-c", args});
    }

    public static List<String> invokeNativeCommandAndGetResult(String[] cmd) {
        List<String> outputList = new ArrayList<String>();

        try {
            Runtime r = Runtime.getRuntime();
            Process ps = r.exec(cmd);
//            ProcessBuilder pb = new ProcessBuilder(cmd);
//            pb.redirectErrorStream(true);
//            Process ps = pb.start();

            BufferedReader rdr = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String in = rdr.readLine();
            while (in != null) {
                outputList.add(in);
                in = rdr.readLine();
            }

            int exitValue = ps.waitFor();
            if (exitValue != 0) {
                System.out.println("WARN: Process exits with non-zero code: " + exitValue);
            }

            ps.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputList;
    }

    public static List<String> invokeNativeCommandAndGetResult(String args) {
        return invokeNativeCommandAndGetResult(args.split(" "));
    }

    public static List<String> invokeShellCommandAndGetResult(String args) {
        return invokeNativeCommandAndGetResult(new String[]{"sh", "-c", args});
    }
}
