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
package archimulator.sim.uncore.cache.eviction;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class EvictionPolicyFactory {
    private static List<Class<? extends EvictionPolicy>> evictionPolicyClasses = new ArrayList<Class<? extends EvictionPolicy>>();

    static {
        evictionPolicyClasses.add(LRUPolicy.class);
    }

    @SuppressWarnings("unchecked")
    public static <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT> createEvictionPolicy(Class<? extends EvictionPolicy> evictionPolicyClz, EvictableCache<StateT> cache) {
        try {
            return evictionPolicyClz.getConstructor(new Class[]{EvictableCache.class}).newInstance(cache);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Class<? extends EvictionPolicy>> getEvictionPolicyClasses() {
        return evictionPolicyClasses;
    }
}
