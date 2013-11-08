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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import net.pickapack.util.ValueProvider;

import java.io.Serializable;

/**
 * Least frequently used (LFU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class LFUPolicy<StateT extends Serializable> extends CacheReplacementPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    /**
     * Create a least frequently used (LFU) policy for the evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public LFUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<>(cache, cache.getName() + "/lfuPolicy/mirrorCache", cache.getGeometry(), args -> new BooleanValueProvider());
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set
     * @param tag    the tag
     * @return the newly created cache access object
     */
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int minFrequency = Integer.MAX_VALUE;
        int victimWay = getCache().getAssociativity() - 1;

        for (CacheLine<Boolean> mirrorCacheLine : this.mirrorCache.getLines(set)) {
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorCacheLine.getStateProvider();
            int frequency = stateProvider.frequency;

            if (frequency < minFrequency) {
                minFrequency = frequency;
                victimWay = mirrorCacheLine.getWay();
            }
        }

        return new CacheAccess<>(this.getCache(), access, set, victimWay, tag);
    }

    /**
     * Handle promotion on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.frequency++;
    }

    /**
     * Handle insertion on a cache miss.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.frequency = 0;
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Boolean value provider.
     */
    private static class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private int frequency;

        /**
         * Create a boolean value provider.
         */
        private BooleanValueProvider() {
            this.state = true;
        }

        /**
         * Get the value.
         *
         * @return the value
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial value.
         *
         * @return the initial value
         */
        @Override
        public Boolean getInitialValue() {
            return true;
        }
    }
}
