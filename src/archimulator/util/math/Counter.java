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
package archimulator.util.math;

public class Counter {
    private long initialValue;
    private long value;

    public Counter(long initialValue) {
        this.initialValue = initialValue;
        this.value = initialValue;
    }

    public Counter inc() {
        this.value++;
        return this;
    }

    public Counter dec() {
        this.value--;
        return this;
    }

    public Counter reset() {
        this.value = this.initialValue;
        return this;
    }

    public long getValue() {
        return value;
    }
}
