/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement.rereferenceIntervalPrediction;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.*;
import archimulator.uncore.cache.replacement.AbstractCacheReplacementPolicy;
import archimulator.util.math.NoThresholdSaturatingCounter;
import archimulator.util.ValueProvider;

import java.io.Serializable;
import java.util.Optional;

/**
 * Rereference interval prediction policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class RereferenceIntervalPredictionPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> {
    private int predictedRereferenceIntervalMax;
    private DynamicInsertionPolicy insertionPolicy;
    private Cache<Boolean> mirrorCache;

    /**
     * Create a rereference interval prediction policy.
     *
     * @param cache the parent cache
     */
    @SuppressWarnings("unchecked")
    public RereferenceIntervalPredictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.predictedRereferenceIntervalMax = (1 << 3) - 1;

        this.mirrorCache = new BasicCache<>(
                cache,
                getCache().getName() + ".rereferenceIntervalPredictionPolicy.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.insertionPolicy = new DynamicInsertionPolicy(cache, 16);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        do {
            Optional<CacheLine<Boolean>> result = this.mirrorCache.getLines(set).stream().filter(
                    mirrorLine -> {
                        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
                        return stateProvider.predictedRereferenceInterval.getValue() == this.predictedRereferenceIntervalMax;
                    }
            ).findFirst();

            if (result.isPresent()) {
                return new CacheAccess<>(this.getCache(), access, set, result.get().getWay(), tag);
            }

            this.mirrorCache.getLines(set).forEach(mirrorLine -> {
                BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
                stateProvider.predictedRereferenceInterval.increment();
            });
        } while (true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
//        stateProvider.predictedRereferenceInterval.decrement();
        stateProvider.predictedRereferenceInterval.reset();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

        stateProvider.predictedRereferenceInterval.setValue(
                this.insertionPolicy.shouldDoNormalFill(set, access.getThread().getCore().getNum())
                        ? this.predictedRereferenceIntervalMax
                        : this.predictedRereferenceIntervalMax - 1
        );
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private NoThresholdSaturatingCounter predictedRereferenceInterval;

        /**
         * Create a boolean value provider.
         */
        public BooleanValueProvider() {
            this.state = true;
            this.predictedRereferenceInterval = new NoThresholdSaturatingCounter(0, predictedRereferenceIntervalMax, predictedRereferenceIntervalMax);
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
