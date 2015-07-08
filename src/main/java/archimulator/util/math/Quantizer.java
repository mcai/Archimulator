/**
 * ****************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.math;

import java.io.Serializable;

/**
 * Quantizer.
 *
 * @author Min Cai
 */
public class Quantizer implements Serializable {
    private int maxValue;
    private int quantum;

    /**
     * Create a quantizer.
     *
     * @param maxValue the max value
     * @param quantum the quantum
     */
    public Quantizer(int maxValue, int quantum) {
        this.maxValue = maxValue;
        this.quantum = quantum;
    }

    /**
     * Quantize the specified raw value.
     *
     * @param rawValue the raw value
     * @return the quantized value
     */
    public int quantize(int rawValue) {
        return Math.min(rawValue / this.quantum, this.maxValue);
    }

    /**
     * Un-quantize the specified value.
     *
     * @param value the value
     * @return the un-quantized raw value
     */
    public int unQuantize(int value) {
        return value * this.quantum;
    }

    /**
     * Get the maximum value.
     *
     * @return the maximum value
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Get the quantum.
     *
     * @return the quantum
     */
    public int getQuantum() {
        return quantum;
    }

    @Override
    public String toString() {
        return String.format("Quantizer{maxValue=%d, quantum=%d}", maxValue, quantum);
    }
}
