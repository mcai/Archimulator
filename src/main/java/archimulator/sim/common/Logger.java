/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.common;

/**
 * Logger.
 *
 * @author Min Cai
 */
public class Logger {
    /**
     * Print formatted information text.
     *
     * @param category     the category
     * @param format       the format
     * @param currentCycle the current cycle
     * @param args         the arguments
     */
    public static void infof(String category, String format, long currentCycle, Object... args) {
        info(category, String.format(format, args), currentCycle);
    }

    /**
     * Print information text.
     *
     * @param category     the category
     * @param text         the text
     * @param currentCycle the current cycle
     */
    public static void info(String category, String text, long currentCycle) {
        info(getMessage(category + "|" + "info", text), currentCycle);
    }

    /**
     * Print formatted warning text.
     *
     * @param category     the category
     * @param format       the format
     * @param currentCycle the current cycle
     * @param args         the arguments
     */
    public static void warnf(String category, String format, long currentCycle, Object... args) {
        warn(category, String.format(format, args), currentCycle);
    }

    /**
     * Print warning text.
     *
     * @param category     the category
     * @param text         the text
     * @param currentCycle the current cycle
     */
    public static void warn(String category, String text, long currentCycle) {
        warn(getMessage(category + "|" + "warn", text), currentCycle);
    }

    /**
     * Print formatted fatal text.
     *
     * @param category     the category
     * @param format       the format
     * @param currentCycle the current cycle
     * @param args         the arguments
     */
    public static void fatalf(String category, String format, long currentCycle, Object... args) {
        fatal(category, String.format(format, args), currentCycle);
    }

    /**
     * Print fatal text.
     *
     * @param category     the category
     * @param text         the text
     * @param currentCycle the current cycle
     */
    public static void fatal(String category, String text, long currentCycle) {
        throw new RuntimeException("[" + currentCycle + "] " + getMessage(category + "|" + "fatal", text));
    }

    /**
     * Print formatted panic text.
     *
     * @param category     the category
     * @param format       the format
     * @param currentCycle the current cycle
     * @param args         the arguments
     */
    public static void panicf(String category, String format, long currentCycle, Object... args) {
        panic(category, String.format(format, args), currentCycle);
    }

    /**
     * Print panic text.
     *
     * @param category     the category
     * @param text         the text
     * @param currentCycle the current cycle
     */
    public static void panic(String category, String text, long currentCycle) {
        throw new RuntimeException("[" + currentCycle + "] " + getMessage(category + "|" + "panic", text));
    }

    /**
     * Print information text.
     *
     * @param text         the text
     * @param currentCycle the current cycle
     */
    private static void info(String text, long currentCycle) {
        System.out.println("[" + currentCycle + "] " + text);
    }

    /**
     * Print warning text.
     *
     * @param text         the text
     * @param currentCycle the current cycle
     */
    private static void warn(String text, long currentCycle) {
        System.err.println("[" + currentCycle + "] " + text);
    }

    /**
     * Create the message string from the specified caption and text.
     *
     * @param caption the caption
     * @param text    the text
     * @return the newly created message string from the specified caption and text
     */
    private static String getMessage(String caption, String text) {
        return String.format("%s %s", caption.endsWith("info") ? "" : "[" + caption + "]", text);
    }

    /**
     * Event queue.
     */
    public static final String EVENT_QUEUE = "EVENT_QUEUE";

    /**
     * Simulator.
     */
    public static final String SIMULATOR = "SIMULATOR";

    /**
     * Simulation.
     */
    public static final String SIMULATION = "SIMULATION";

    /**
     * Core.
     */
    public static final String CORE = "CORE";

    /**
     * Thread.
     */
    public static final String THREAD = "THREAD";

    /**
     * Process.
     */
    public static final String PROCESS = "PROCESS";

    /**
     * Register.
     */
    public static final String REGISTER = "REGISTER";

    /**
     * Request.
     */
    public static final String REQUEST = "REQUEST";

    /**
     * Cache.
     */
    public static final String CACHE = "CACHE";

    /**
     * Coherence.
     */
    public static final String COHERENCE = "COHERENCE";

    /**
     * Memory.
     */
    public static final String MEMORY = "MEMORY";

    /**
     * Net.
     */
    public static final String NET = "NET";

    /**
     * Instruction.
     */
    public static final String INSTRUCTION = "INSTRUCTION";

    /**
     * System call.
     */
    public static final String SYSTEM_CALL = "SYSTEM_CALL";

    /**
     * Pseudo call.
     */
    public static final String PSEUDO_CALL = "PSEUDO_CALL";

    /**
     * Region of interest (ROI).
     */
    public static final String ROI = "ROI";

    /**
     * ELF.
     */
    public static final String ELF = "ELF";

    /**
     * Config.
     */
    public static final String CONFIG = "CONFIG";

    /**
     * Statistics.
     */
    public static final String STAT = "STAT";

    /**
     * Miscellaneous.
     */
    public static final String MISC = "MISC";

    /**
     * Out of order.
     */
    public static final String OOO = "OOO";

    /**
     * Testing.
     */
    public static final String TEST = "TEST";

    /**
     * Debugging.
     */
    public static final String DEBUG = "DEBUG";
}
