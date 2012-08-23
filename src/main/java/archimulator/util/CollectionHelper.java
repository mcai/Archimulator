/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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

import net.pickapack.Pair;
import net.pickapack.action.Function1;
import net.pickapack.action.Function2;
import net.pickapack.action.Predicate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionHelper {
    public static  <E> boolean allMatch(List<E> elements, Predicate<? super E> predicate) {
        for(E element : elements) {
            if(!predicate.apply(element)) {
                return false;
            }
        }

        return true;
    }

    public static  <E> boolean anyMatch(List<E> elements, Predicate<? super E> predicate) {
        for(E element : elements) {
            if(predicate.apply(element)) {
                return true;
            }
        }

        return false;
    }

    public static <E> List<E> filter(List<E> elements, Predicate<? super E> predicate) {
        List<E> results = new ArrayList<E>();

        for(E element : elements) {
            if(predicate.apply(element)) {
                results.add(element);
            }
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    public static <E, T> List<T> transform(List<E> elements, final Class<T> clz) {
        return transform(elements, new Function1<E, T>() {
            @Override
            public T apply(E param) {
                return (T)param;
            }
        });
    }

    public static <E, T> List<T> transform(List<E> elements, final Function1<? super E, T> function) {
        return transform(elements, new Function2<Integer, E, T>() {
            @Override
            public T apply(Integer index, E element) {
                return function.apply(element);
            }
        });
    }

    public static <K, V, T> Map<K, T> transform(Map<K, V> elements, final Function2<? super K, V, T> function) {
        Map<K, T> results = new LinkedHashMap<K, T>();

        for(K key : elements.keySet()) {
            results.put(key, function.apply(key, elements.get(key)));
        }

        return results;
    }

    public static <K, V> Map<K, V> toMap(List<Pair<K, V>> elements) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        for(Pair<K, V> element : elements) {
            result.put(element.getFirst(), element.getSecond());
        }
        return result;
    }

    public static <E, T> List<T> transform(List<E> elements, Function2<Integer, ? super E, T> function) {
        List<T> results = new ArrayList<T>();

        int i = 0;
        for(E element : elements) {
            results.add(function.apply(i++, element));
        }

        return results;
    }
}
