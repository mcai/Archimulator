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
 * Pair.
 *
 * @author Min Cai
 * @param <K> the type of the key
 * @param <T> the type of the value
 */
public class Pair<K, T> implements Serializable {
    private K first;
    private T second;

    /**
     * Create a pair.
     *
     * @param first the key
     * @param second the value
     */
    public Pair(K first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public K getFirst() {
        return first;
    }

    /**
     * Set the key.
     *
     * @param first the key
     */
    public void setFirst(K first) {
        this.first = first;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public T getSecond() {
        return second;
    }

    /**
     * Set the value.
     *
     * @param second the value
     */
    public void setSecond(T second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (!first.equals(pair.first)) return false;
        if (!second.equals(pair.second)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Pair{first=%s, second=%s}", first, second);
    }
}
