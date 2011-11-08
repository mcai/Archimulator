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
        final SimpleCache<Integer, Integer, DefaultSimpleCacheAccessType> cache = new SimpleCache<Integer, Integer, DefaultSimpleCacheAccessType>(3){
            private Map<Integer, Integer> nextLevel = new HashMap<Integer, Integer>();

            @Override
            protected void doWriteToNextLevel(Integer key, Integer value, DefaultSimpleCacheAccessType accessType) {
                if(accessType == DefaultSimpleCacheAccessType.WRITE) {
                    nextLevel.put(key, value);
                }

//                System.out.printf("doWriteToNextLevel(%d => %d)\n\n", key, value);
            }

            @Override
            protected Pair<Integer, DefaultSimpleCacheAccessType> doReadFromNextLevel(Integer key) {
//                System.out.printf("doReadFromNextLevel(%d)\n\n", key);

                Integer value = nextLevel.get(key);
                nextLevel.remove(key);
                return new Pair<Integer, DefaultSimpleCacheAccessType>(value, DefaultSimpleCacheAccessType.READ);
            }

            @Override
            protected boolean existsOnNextLevel(Integer key) {
                return nextLevel.containsKey(key);
            }
        };

        cache.getCacheEventDispatcher().addListener(SetValueEvent.class, new Action1<SetValueEvent>() {
            public void apply(SetValueEvent event) {
                System.out.printf("setValue(%s)\n", event);
                System.out.printf("keys: %s\n\n", Arrays.toString(cache.getKeys()));
            }
        });

        cache.getCacheEventDispatcher().addListener(GetValueEvent.class, new Action1<GetValueEvent>() {
            public void apply(GetValueEvent event) {
                System.out.printf("getValue(%s)\n", event);
                System.out.printf("keys: %s\n\n", Arrays.toString(cache.getKeys()));
            }
        });

        cache.put(0, 11, DefaultSimpleCacheAccessType.WRITE);
        cache.put(1, 22, DefaultSimpleCacheAccessType.WRITE);
        cache.put(2, 33, DefaultSimpleCacheAccessType.WRITE);
        cache.put(3, 44, DefaultSimpleCacheAccessType.WRITE);

        System.out.println(cache.get(0, DefaultSimpleCacheAccessType.READ));
        System.out.println(cache.get(1, DefaultSimpleCacheAccessType.READ));
        System.out.println(cache.get(2, DefaultSimpleCacheAccessType.READ));
        System.out.println(cache.get(3, DefaultSimpleCacheAccessType.READ));
    }
}
