/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim;

import archimulator.util.io.TelnetServer;
import archimulator.util.io.appender.CompositeOutputAppender;
import archimulator.util.io.appender.ConsoleOutputAppender;
import archimulator.util.io.appender.OutputAppender;

public abstract class Logger {
    private OutputAppender outputAppender;

    public Logger() {
        this.outputAppender = new CompositeOutputAppender(new ConsoleOutputAppender(), new TelnetServer());
    }

    private String getMessage(String caption, String text) {
        return String.format("%s %s", caption.endsWith("info") ? "" : "[" + caption + "]", text);
    }

    public void infof(String category, String format, Object... args) {
        this.info(category, String.format(format, args));
    }

    public void info(String category, String text) {
        this.info(this.getMessage(category + "|" + "info", text));
    }

    public void warnf(String category, String format, Object... args) {
        this.warn(category, String.format(format, args));
    }

    public void warn(String category, String text) {
        this.warn(this.getMessage(category + "|" + "warn", text));
    }

    public void fatalf(String category, String format, Object... args) {
        this.fatal(category, String.format(format, args));
    }

    public void fatal(String category, String text) {
        throw new RuntimeException("[" + this.getCurrentCycle() + "] " + this.getMessage(category + "|" + "fatal", text));
    }

    public void panicf(String category, String format, Object... args) {
        this.panic(category, String.format(format, args));
    }

    public void panic(String category, String text) {
        throw new RuntimeException("[" + this.getCurrentCycle() + "] " + this.getMessage(category + "|" + "panic", text));
    }

    private void info(String text) {
        this.outputAppender.appendStdOutLine(this.getCurrentCycle(), text);
    }

    private void warn(String text) {
        this.outputAppender.appendStdErrLine(this.getCurrentCycle(), text);
    }

    protected abstract long getCurrentCycle();

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
