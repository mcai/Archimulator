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
package archimulator.util.simpleCache;

import archimulator.util.Pair;
import archimulator.util.action.Action1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SimpleCacheTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        final SimpleCache<Integer, Integer, DefaultSimpleCacheAccessType> cache = new SimpleCache<Integer, Integer, DefaultSimpleCacheAccessType>(1, 3) {
            private Map<Integer, Integer> nextLevel = new HashMap<Integer, Integer>();

            @Override
            protected void doWriteToNextLevel(Integer key, Integer value, boolean writeback) {
                if (writeback) {
                    nextLevel.put(key, value);
                }

//                System.out.printf("doWriteToNextLevel(%d => %d)\n\n", key, value);
            }

            @Override
            protected Pair<Integer, DefaultSimpleCacheAccessType> doReadFromNextLevel(Integer key, Integer oldValue) {
//                System.out.printf("doReadFromNextLevel(%d)\n\n", key);

                Integer value = nextLevel.get(key);
                return new Pair<Integer, DefaultSimpleCacheAccessType>(value, DefaultSimpleCacheAccessType.READ);
            }

        };

        cache.getCacheEventDispatcher().addListener(SetValueEvent.class, new Action1<SetValueEvent>() {
            public void apply(SetValueEvent event) {
                System.out.printf("setValue(%s)\n", event);
                System.out.printf("keys: %s\n\n", Arrays.toString(cache.getKeys(0)));
            }
        });

        cache.getCacheEventDispatcher().addListener(GetValueEvent.class, new Action1<GetValueEvent>() {
            public void apply(GetValueEvent event) {
                System.out.printf("getValue(%s)\n", event);
                System.out.printf("keys: %s\n\n", Arrays.toString(cache.getKeys(0)));
            }
        });

        cache.put(0, 0, 11, DefaultSimpleCacheAccessType.WRITE);
        cache.put(0, 1, 22, DefaultSimpleCacheAccessType.WRITE);
        cache.put(0, 2, 33, DefaultSimpleCacheAccessType.WRITE);
        cache.put(0, 3, 44, DefaultSimpleCacheAccessType.WRITE);

        Pair<Integer, Integer> lru = cache.getLRU(0);
        System.out.println("LRU before removeLRU(..): " + lru);
        System.out.printf("keys: %s\n\n", Arrays.toString(cache.getKeys(0)));

        cache.removeLRU(0);

        Pair<Integer, Integer> lru1 = cache.getLRU(0);
        System.out.println("LRU after removeLRU(..): " + lru1);
        System.out.printf("keys: %s\n\n", Arrays.toString(cache.getKeys(0)));

        System.out.println(cache.get(0, 0, DefaultSimpleCacheAccessType.READ));
        System.out.println(cache.get(0, 1, DefaultSimpleCacheAccessType.READ));
        System.out.println(cache.get(0, 2, DefaultSimpleCacheAccessType.READ));
        System.out.println(cache.get(0, 3, DefaultSimpleCacheAccessType.READ));

        Pair<Integer, Integer> lru2 = cache.getLRU(0);
        System.out.println("LRU after get(..): " + lru2);
    }
}
