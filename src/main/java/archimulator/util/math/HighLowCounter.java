/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.math;

/**
 * High/low counter.
 *
 * @author Min Cai
 */
public class HighLowCounter {
    private Quantizer timestampQuantizer;

    private int lowCounter;
    private int highCounter;

    /**
     * Create a high/low counter.
     *
     * @param maxValue the maximum value
     * @param quantum the quantum
     */
    public HighLowCounter(int maxValue, int quantum) {
        this.timestampQuantizer = new Quantizer(maxValue, quantum);

        this.lowCounter = 0;
        this.highCounter = 1;
    }

    /**
     * Increment.
     */
    public void increment() {
        this.lowCounter++;
        if (this.lowCounter == this.timestampQuantizer.getQuantum()) {
            this.lowCounter = 0;
            this.highCounter++;
            if (this.highCounter > this.timestampQuantizer.getMaxValue()) {
                this.highCounter = 0;
            }
        }
    }

    /**
     * Get the timestamp quantizer.
     *
     * @return the timestamp quantizer
     */
    public Quantizer getTimestampQuantizer() {
        return timestampQuantizer;
    }

    /**
     * Get the low counter.
     *
     * @return the low counter
     */
    public int getLowCounter() {
        return lowCounter;
    }

    /**
     * Get the high counter.
     *
     * @return the high counter
     */
    public int getHighCounter() {
        return highCounter;
    }
}
