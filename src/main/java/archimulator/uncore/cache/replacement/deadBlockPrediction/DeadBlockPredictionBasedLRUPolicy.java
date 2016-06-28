/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement.deadBlockPrediction;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.*;
import archimulator.uncore.cache.replacement.LRUPolicy;
import archimulator.util.ValueProvider;

import java.io.Serializable;

/**
 * Dead block prediction based least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class DeadBlockPredictionBasedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        boolean dead;

        /**
         * Create a boolean value provider.
         */
        public BooleanValueProvider() {
            this.state = true;
            this.dead = false;
        }

        /**
         * Get the state.
         *
         * @return the state
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial state.
         *
         * @return the initial state
         */
        @Override
        public Boolean getInitialValue() {
            return true;
        }
    }

    private static final int numTraceBitsPerEntry = 16;

    private Cache<Boolean> mirrorCache;

    private DeadBlockPredictionSampler deadBlockPredictionSampler;

    /**
     * Create a dead block prediction based least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public DeadBlockPredictionBasedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new BasicCache<>(
                cache,
                getCache().getName() + ".deadBlockPredictionBasedLRUPolicy.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.deadBlockPredictionSampler = new DeadBlockPredictionSampler(cache.getNumSets());
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        for (int i = 0; i < this.getCache().getAssociativity(); i++) {
            CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, i);
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            if (stateProvider.dead) {
                return new CacheAccess<>(this.getCache(), access, set, i, tag);
            }
        }

        return super.handleReplacement(access, set, tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.updateSampler(set, way, access.getThread().getId(), access.getVirtualPc(), access.getPhysicalTag());
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.updateSampler(set, way, access.getThread().getId(), access.getVirtualPc(), access.getPhysicalTag());
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Update the sampler when there is an access to an LLC cache block.
     *
     * @param set the set index
     * @param way the way
     * @param threadId the thread ID
     * @param pc the virtual PC address
     * @param tag the tag
     */
    @SuppressWarnings("unchecked")
    private void updateSampler(int set, int way, int threadId, int pc, int tag) {
        if (set % DeadBlockPredictionSampler.getModulus() == 0) {
            int samplerSet = set / DeadBlockPredictionSampler.getModulus();
            this.deadBlockPredictionSampler.access(samplerSet, threadId, pc, tag);
        }

        int trace = makeTrace(pc);

        boolean dead = this.deadBlockPredictionSampler.getDeadBlockPredictor().predict(threadId, trace);

        if (dead) {
            this.setLRU(set, way);
        } else {
            this.setMRU(set, way);
        }

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.dead = dead;
    }

    /***
     * make a trace from the specified PC address (just extract some bits).
     *
     * @param pc the virtual PC address
     * @return a trace made from the specified PC address
     */
    public static int makeTrace(int pc) {
        return pc & ((1 << numTraceBitsPerEntry) - 1);
    }
}
