/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache.replacement;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.*;
import archimulator.util.ValueProvider;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Least frequently used (LFU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class LFUPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    /**
     * Create a least frequently used (LFU) policy for the evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public LFUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new BasicCache<>(cache, cache.getName() + "/lfuPolicy/mirrorCache", cache.getGeometry(), args -> new BooleanValueProvider());
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int victimWay = this.mirrorCache.getLines(set).stream()
                .min(Comparator.comparing(mirrorCacheLine -> ((BooleanValueProvider) mirrorCacheLine.getStateProvider()).frequency))
                .get().getWay();

        return new CacheAccess<>(this.getCache(), access, set, victimWay, tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.frequency++;
    }

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
