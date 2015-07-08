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
 * Saturating counter.
 *
 * @author Min Cai
 */
public class SaturatingCounter implements Serializable {
    private int minValue;
    private int threshold;
    private int maxValue;
    private int value;
    private int initialValue;

    /**
     * Create a saturating counter.
     *
     * @param minValue the minimum value
     * @param threshold the threshold
     * @param maxValue the max value
     * @param initialValue the initial value
     */
    public SaturatingCounter(int minValue, int threshold, int maxValue, int initialValue) {
        this.minValue = minValue;
        this.threshold = threshold;
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
     * Update the value based on the new observed direction.
     *
     * @param direction the new observed direction
     */
    public void update(boolean direction) {
        if (direction) {
            this.increment();
        } else {
            this.decrement();
        }
    }

    /**
     * Increment the value of the saturating counter.
     */
    private void increment() {
        if (this.value < this.maxValue) {
            this.value++;
        }
    }

    /**
     * Decrement the value of the saturating counter.
     */
    private void decrement() {
        if (this.value > this.minValue) {
            this.value--;
        }
    }

    /**
     * Get a value indicating whether the direction is taken or not.
     *
     * @return a value indicating whether the direction is taken or not
     */
    public boolean isTaken() {
        return this.getValue() >= this.threshold;
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
     * Get the threshold value.
     *
     * @return the threshold value
     */
    public int getThreshold() {
        return threshold;
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

    @Override
    public String toString() {
        return String.format("SaturatingCounter{minValue=%d, threshold=%d, maxValue=%d, value=%d, initialValue=%d}", minValue, threshold, maxValue, value, initialValue);
    }
}
