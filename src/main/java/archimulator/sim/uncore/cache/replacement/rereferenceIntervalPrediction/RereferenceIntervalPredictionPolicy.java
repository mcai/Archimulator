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
package archimulator.sim.uncore.cache.replacement.rereferenceIntervalPrediction;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import net.pickapack.util.ValueProvider;

import java.io.Serializable;

/**
 * Rereference interval prediction policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class RereferenceIntervalPredictionPolicy<StateT extends Serializable> extends CacheReplacementPolicy<StateT> {
    private int predictedRereferenceIntervalMaxValue;
    private DynamicInsertionPolicy insertionPolicy;
    private Cache<Boolean> mirrorCache;

    /**
     * Create a rereference interval prediction policy.
     *
     * @param cache the parent cache
     */
    public RereferenceIntervalPredictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.predictedRereferenceIntervalMaxValue = 3;

        this.mirrorCache = new Cache<>(
                cache,
                getCache().getName() + ".rereferenceIntervalPredictionEvictionPolicy.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.insertionPolicy = new DynamicInsertionPolicy(cache, cache.getExperiment().getArchitecture().getNumCores(), 2);
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        do {
            /* Search for victim whose predicted rereference interval value is furthest in future */
            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
                BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
                if (stateProvider.predictedRereferenceInterval == this.predictedRereferenceIntervalMaxValue) {
                    return new CacheAccess<>(this.getCache(), access, set, way, tag);
                }
            }

            /* If victim is not found, then move all rereference prediction values into future and then repeat the search again */
            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
                BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
                stateProvider.predictedRereferenceInterval++;
            }
        } while (true);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        // Set the line's predicted rereference interval value to near-immediate (0)
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.predictedRereferenceInterval = 0;
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (this.insertionPolicy.shouldDoNormalFill(set, access.getThread().getCore().getNum())) {
            CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
            stateProvider.predictedRereferenceInterval = this.predictedRereferenceIntervalMaxValue - 1;
        }
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private int predictedRereferenceInterval;

        /**
         * Create a boolean value provider.
         */
        public BooleanValueProvider() {
            this.state = true;
            this.predictedRereferenceInterval = predictedRereferenceIntervalMaxValue;
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
}
