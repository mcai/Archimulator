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
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.replacement.AbstractCacheReplacementPolicy;
import net.pickapack.util.ValueProvider;

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

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.predictedRereferenceInterval.decrement();
    }

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

    /**
     * No threshold saturating counter.
     */
    private class NoThresholdSaturatingCounter implements Serializable {
        private int minValue;
        private int maxValue;
        private int value;
        private int initialValue;

        /**
         * Create a no threshold saturating counter.
         *
         * @param minValue     the minimum value
         * @param maxValue     the max value
         * @param initialValue the initial value
         */
        public NoThresholdSaturatingCounter(int minValue, int maxValue, int initialValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.value = initialValue;
            this.initialValue = initialValue;
        }

        /**
         * Reset the value of the saturating counter to the initial value.
         */
        public void reset() {
            this.value = this.initialValue;
        }

        /**
         * Increment the value of the saturating counter.
         */
        public void increment() {
            if (this.value < this.maxValue) {
                this.value++;
            }
        }

        /**
         * Decrement the value of the saturating counter.
         */
        public void decrement() {
            if (this.value > this.minValue) {
                this.value--;
            }
        }

        /**
         * Set the value.
         *
         * @param value the value
         */
        public void setValue(int value) {
            this.value = value;

            if (this.value > this.maxValue) {
                this.value = this.maxValue;
            }

            if (this.value < this.minValue) {
                this.value = this.minValue;
            }
        }

        /**
         * Get the maximum value.
         *
         * @return the maximum value
         */
        public int getMinValue() {
            return minValue;
        }

        /**
         * Get the maximum value.
         *
         * @return the maximum value
         */
        public int getMaxValue() {
            return maxValue;
        }

        /**
         * Get the value.
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * Get the initial value.
         *
         * @return the initial value
         */
        public int getInitialValue() {
            return initialValue;
        }

        @Override
        public String toString() {
            return String.format("NoThresholdSaturatingCounter{minValue=%d, maxValue=%d, value=%d, initialValue=%d}", minValue, maxValue, value, initialValue);
        }
    }
}
