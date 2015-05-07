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
package archimulator.util;

/**
 * Interval counter.
 *
 * @author Min Cai
 */
public class IntervalCounter {
    private long value;
    private long valueInTheCurrentInterval;

    /**
     * Increment.
     */
    public void increment() {
        this.valueInTheCurrentInterval++;
    }

    /**
     * Reset.
     */
    public void reset() {
        this.valueInTheCurrentInterval = this.value = 0;
    }

    /**
     * New interval.
     *
     * @return the initial value for the next interval
     */
    public long newInterval() {
        this.value = (this.value + this.valueInTheCurrentInterval) / 2;
        this.valueInTheCurrentInterval = 0;
        return this.value;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * Get the value in the current interval.
     *
     * @return the value in the current interval
     */
    public long getValueInTheCurrentInterval() {
        return valueInTheCurrentInterval;
    }

    @Override
    public String toString() {
        return String.format("IntervalCounter{value=%d, valueInTheCurrentInterval=%d}", value, valueInTheCurrentInterval);
    }
}
