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
 * Counter.
 *
 * @author Min Cai
 */
public class Counter implements Serializable {
    private long initialValue;
    private long value;

    /**
     * Create a counter using the specified initial value.
     *
     * @param initialValue the initial value
     */
    public Counter(long initialValue) {
        this.initialValue = initialValue;
        this.value = initialValue;
    }

    /**
     * Create a counter using 0 as the initial value.
     */
    public Counter() {
        this(0);
    }

    /**
     * Increment the value of the counter.
     *
     * @return the counter itself
     */
    public Counter increment() {
        this.value++;
        return this;
    }

    /**
     * Decrement the value of the counter.
     *
     * @return the counter itself
     */
    public Counter decrement() {
        this.value--;
        return this;
    }

    /**
     * Reset the value of the counter.
     *
     * @return the counter itself
     */
    public Counter reset() {
        this.value = this.initialValue;
        return this;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Counter{initialValue=%d, value=%d}", initialValue, value);
    }
}
