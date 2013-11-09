/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Sort helper.
 *
 * @author Min Cai
 */
public class SortHelper {
    /**
     * Sort the elements using the specified list of key extractors.
     *
     * @param elements the elements to be sorted
     * @param keyExtractors the list of key extractors
     * @param <T> the type of elements
     */
    @SuppressWarnings("unchecked")
    public static <T> void sort(List<T> elements, List<Function<T, Comparable>> keyExtractors) {
        if(!keyExtractors.isEmpty()) {
            Comparator<? super T> comparator = Comparator.comparing(element -> keyExtractors.get(0).apply(element));

            if(keyExtractors.size() > 1) {
                for(Function<T, Comparable> function : keyExtractors.subList(1, keyExtractors.size())) {
                    comparator = comparator.thenComparing(element -> function.apply((T)element));
                }
            }

            elements.sort(comparator);
        }
    }
}
