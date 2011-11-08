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

public class StopWatch {
    private final long nsPerTick = 100;
    private final long nsPerMs = 1000000;
    private final long nsPerSs = 1000000000;
    private final long nsPerMm = 60000000000L;
    private final long nsPerHh = 3600000000000L;

    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;

    public void start() {
        this.startTime = System.nanoTime();
        this.running = true;
    }

    public void stop() {
        this.stopTime = System.nanoTime();
        this.running = false;
    }

    public void reset() {
        this.startTime = 0;
        this.stopTime = 0;
        this.running = false;
    }

    public long getElapsedTicks() {
        long elapsed;
        if (this.running) {
            elapsed = (System.nanoTime() - this.startTime);
        } else {
            elapsed = (this.stopTime - this.startTime);
        }
        return elapsed / this.nsPerTick;
    }

    public long getElapsedMilliseconds() {
        long elapsed;
        if (this.running) {
            elapsed = (System.nanoTime() - this.startTime);
        } else {
            elapsed = (this.stopTime - this.startTime);
        }
        return elapsed / this.nsPerMs;
    }

    public long getElapsedSeconds() {
        long elapsed;
        if (this.running) {
            elapsed = (System.nanoTime() - this.startTime);
        } else {
            elapsed = (this.stopTime - this.startTime);
        }
        return elapsed / this.nsPerSs;
    }

    public long getElapsedMinutes() {
        long elapsed;
        if (this.running) {
            elapsed = (System.nanoTime() - this.startTime);
        } else {
            elapsed = (this.stopTime - this.startTime);
        }
        return elapsed / this.nsPerMm;
    }

    public long getElapsedHours() {
        long elapsed;
        if (this.running) {
            elapsed = (System.nanoTime() - this.startTime);
        } else {
            elapsed = (this.stopTime - this.startTime);
        }
        return elapsed / this.nsPerHh;
    }

    public String getElapsed() {
        return this.formatTime();
    }

    private String formatTime() {
        return TimeSpanHelper.toString(this.getElapsedMilliseconds());
    }
}
