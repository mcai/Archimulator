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
import java.util.HashMap;
import java.util.Map;

/**
 * Parameters map.
 *
 * @author Min Cai
 */
public class Params implements Serializable {
    private Map<Object, Object> properties;

    /**
     * Create a parameters map.
     */
    public Params() {
        this.properties = new HashMap<>();
    }

    /**
     * Set the value for the specified key.
     *
     * @param key the key
     * @param value the value
     */
    public void put(Object key, Object value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }

        this.properties.put(key, value);
    }

    /**
     * Get the value from the specified key and the default value.
     *
     * @param <T> the type of the value
     * @param clz the class of the value
     * @param key the key
     * @param defaultValue the default value
     * @return the value corresponding to the specified key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clz, Object key, T defaultValue) {
        if (clz == null || key == null) {
            throw new IllegalArgumentException();
        }

        return this.properties.containsKey(key) ? (T) this.properties.get(key) : defaultValue;
    }

    /**
     * Get the value from the specified key.
     *
     * @param <T> the type of the value
     * @param clz the class of the value
     * @param key the key
     * @return the value corresponding to the specified key
     */
    public <T> T get(Class<T> clz, Object key) {
        return this.get(clz, key, null);
    }

    /**
     * Get the size of the parameters map.
     *
     * @return the size of the parameters map
     */
    public int size() {
        return this.properties.size();
    }

    /**
     * Get a value indicating whether the parameters map is empty or not.
     *
     * @return a value indicating whether the parameters map is empty or not
     */
    public boolean isEmpty() {
        return this.properties.isEmpty();
    }
}
