/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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

import java.io.Serializable;

/**
 * No threshold saturating counter.
 *
 * @author Min Cai
 */
public class NoThresholdSaturatingCounter implements Serializable {
    private int minValue;
    private int maxValue;
    private int value;
    private int initialValue;

    /**
     * Create a no threshold saturating counter.
     *
     * @param minValue     the minimum value
     * @param maxValue     the max value
     * @param initialValue the initial value
     */
    public NoThresholdSaturatingCounter(int minValue, int maxValue, int initialValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = initialValue;
        this.initialValue = initialValue;
    }

    /**
     * Reset the value of the saturating counter to the initial value.
     */
    public void reset() {
        this.value = this.initialValue;
    }

    /**
     * Increment the value of the saturating counter.
     */
    public void increment() {
        if (this.value < this.maxValue) {
            this.value++;
        }
    }

    /**
     * Decrement the value of the saturating counter.
     */
    public void decrement() {
        if (this.value > this.minValue) {
            this.value--;
        }
    }

    /**
     * Increment exponentially the value of the saturating counter.
     */
    public void incrementExponentially() {
        if (this.value << 1 <= this.maxValue) {
            this.value = value << 1;
        }
    }

    /**
     * Decrement exponentially the value of the saturating counter.
     */
    public void decrementExponentially() {
        if (this.value >> 1 >= this.minValue) {
            this.value = value >> 1;
        }
    }

    /**
     * Set the value.
     *
     * @param value the value
     */
    public void setValue(int value) {
        this.value = value;

        if (this.value > this.maxValue) {
            this.value = this.maxValue;
        }

        if (this.value < this.minValue) {
            this.value = this.minValue;
        }
    }

    /**
     * Get the maximum value.
     *
     * @return the maximum value
     */
    public int getMinValue() {
        return minValue;
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
     * Get the value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Get the initial value.
     *
     * @return the initial value
     */
    public int getInitialValue() {
        return initialValue;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public int get() {
        return this.getValue();
    }

    /**
     * Set the value.
     *
     * @param value the value
     */
    public void set(int value) {
        this.setValue(value);
    }

    @Override
    public String toString() {
        return String.format("NoThresholdSaturatingCounter{minValue=%d, maxValue=%d, value=%d, initialValue=%d}", minValue, maxValue, value, initialValue);
    }
}
