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
package archimulator.util;

import java.io.Serializable;

/**
 * Reference.
 *
 * @author Min Cai
 * @param <T> the type of the value contained in the reference
 */
public class Reference<T> implements Serializable {
    private T value;

    /**
     * Create a reference.
     */
    public Reference() {
    }

    /**
     * Create a reference from the specified value.
     *
     * @param value the value
     */
    public Reference(T value) {
        this.value = value;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public T get() {
        return value;
    }

    /**
     * Set the value.
     *
     * @param value the value
     */
    public void set(T value) {
        this.value = value;
    }
}
