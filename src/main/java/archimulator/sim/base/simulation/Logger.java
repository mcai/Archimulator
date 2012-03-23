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
package archimulator.sim.base.simulation;

public class Logger {
    public static void infof(String category, String format, long currentCycle, Object... args) {
        info(category, String.format(format, args), currentCycle);
    }

    public static void info(String category, String text, long currentCycle) {
        info(getMessage(category + "|" + "info", text), currentCycle);
    }

    public static void warnf(String category, String format, long currentCycle, Object... args) {
        warn(category, String.format(format, args), currentCycle);
    }

    public static void warn(String category, String text, long currentCycle) {
        warn(getMessage(category + "|" + "warn", text), currentCycle);
    }

    public static void fatalf(String category, String format, long currentCycle, Object... args) {
        fatal(category, String.format(format, args), currentCycle);
    }

    public static void fatal(String category, String text, long currentCycle) {
        throw new RuntimeException("[" + currentCycle + "] " + getMessage(category + "|" + "fatal", text));
    }

    public static void panicf(String category, String format, long currentCycle, Object... args) {
        panic(category, String.format(format, args), currentCycle);
    }

    public static void panic(String category, String text, long currentCycle) {
        throw new RuntimeException("[" + currentCycle + "] " + getMessage(category + "|" + "panic", text));
    }

    private static void info(String text, long currentCycle) {
        System.out.println("[" + currentCycle + "] " + text);
    }

    private static void warn(String text, long currentCycle) {
        System.err.println("[" + currentCycle + "] " + text);
    }

    private static String getMessage(String caption, String text) {
        return String.format("%s %s", caption.endsWith("info") ? "" : "[" + caption + "]", text);
    }

    public static final String EVENT_QUEUE = "EVENT_QUEUE";
    public static final String SIMULATOR = "SIMULATOR";
    public static final String SIMULATION = "SIMULATION";
    public static final String CORE = "CORE";
    public static final String THREAD = "THREAD";
    public static final String PROCESS = "PROCESS";
    public static final String REGISTER = "REGISTER";
    public static final String REQUEST = "REQUEST";
    public static final String CACHE = "CACHE";
    public static final String COHRENCE = "COHRENCE";
    public static final String MEMORY = "MEMORY";
    public static final String NET = "NET";
    public static final String INSTRUCTION = "INSTRUCTION";
    public static final String SYSCALL = "SYSCALL";
    public static final String PSEUDOCALL = "PSEUDOCALL";
    public static final String ROI = "ROI";
    public static final String ELF = "ELF";
    public static final String CONFIG = "CONFIG";
    public static final String STAT = "STAT";
    public static final String MISC = "MISC";
    public static final String OOO = "OOO";
    public static final String TEST = "TEST";
    public static final String DEBUG = "DEBUG";
    public static final String XML = "XML";
}
