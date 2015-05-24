/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util;

import java.io.Serializable;

/**
 * Triple.
 *
 * @author Min Cai
 * @param <K> the type of the first element
 * @param <T> the type of the second element
 * @param <P> the type of the third element
 */
public class Triple<K, T, P> implements Serializable {
    private K first;
    private T second;
    private P third;

    /**
     * Create a triple.
     *
     * @param first the first element
     * @param second the second element
     * @param third the third element
     */
    public Triple(K first, T second, P third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Get the first element.
     *
     * @return the first element
     */
    public K getFirst() {
        return first;
    }

    /**
     * Get the second element.
     *
     * @return the second element
     */
    public T getSecond() {
        return second;
    }

    /**
     * Get the third element.
     *
     * @return the third element
     */
    public P getThird() {
        return third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triple triple = (Triple) o;

        if (!first.equals(triple.first)) return false;
        if (!second.equals(triple.second)) return false;
        if (!third.equals(triple.third)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        result = 31 * result + third.hashCode();
        return result;
    }
}
