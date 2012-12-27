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
package archimulator.sim.uncore.cache.prediction;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.math.SaturatingCounter;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

/**
 * Cache based predictor.
 *
 * @author Min Cai
 * @param <PredictableT> the predictable type
 */
public class CacheBasedPredictor<PredictableT extends Comparable<PredictableT>> implements Predictor<PredictableT> {
    private EvictableCache<Boolean> cache;
    private PredictableT defaultValue;
    private long numHits;
    private long numMisses;

    /**
     * Create a cache based predictor.
     *
     * @param parent the parent simulation object
     * @param name the name of the cache based predictor
     * @param geometry the geometry of the cache based predictor
     * @param counterThreshold the threshold value of the counter
     * @param counterMaxValue the maximum value of the counter
     * @param defaultValue the default value
     */
    public CacheBasedPredictor(SimulationObject parent, String name, CacheGeometry geometry, final int counterThreshold, final int counterMaxValue, PredictableT defaultValue) {
        this(parent, name, geometry, CacheReplacementPolicyType.LRU, counterThreshold, counterMaxValue, defaultValue);
    }

    /**
     * Create a cache based predictor.
     *
     * @param parent the parent simulation object
     * @param name the name of the cache based predictor
     * @param geometry the geometry of the cache based predictor
     * @param cacheReplacementPolicyType the type of the cache replacement policy
     * @param counterThreshold the threshold value of the predictor
     * @param counterMaxValue the maximum value of the predictor
     * @param defaultValue the default value
     */
    public CacheBasedPredictor(SimulationObject parent, String name, CacheGeometry geometry, CacheReplacementPolicyType cacheReplacementPolicyType, final int counterThreshold, final int counterMaxValue, PredictableT defaultValue) {
        ValueProviderFactory<Boolean, ValueProvider<Boolean>> cacheLineStateProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider(counterThreshold, counterMaxValue);
            }
        };

        this.cache = new EvictableCache<Boolean>(parent, name, geometry, cacheReplacementPolicyType, cacheLineStateProviderFactory);
        this.defaultValue = defaultValue;
    }

    public PredictableT predict(int address) {
        CacheLine<Boolean> lineFound = this.cache.findLine(address);
        BooleanValueProvider stateProvider = lineFound != null ? (BooleanValueProvider) lineFound.getStateProvider() : null;
        return lineFound != null && stateProvider.confidence.isTaken() ? stateProvider.predictedValue : getDefaultValue();
    }

    public void update(int address, PredictableT observedValue) {
        if(this.predict(address).equals(observedValue)) {
            this.numHits++;
        }
        else {
            this.numMisses++;
        }

        int set = this.cache.getSet(address);
        int tag = this.cache.getTag(address);

        CacheAccess<Boolean> cacheAccess = this.cache.newAccess(null, address);

        CacheLine<Boolean> line = this.cache.getLine(set, cacheAccess.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();

        if (cacheAccess.isHitInCache()) {
            if (stateProvider.predictedValue == observedValue) {
                stateProvider.confidence.update(true);
            } else {
                if (stateProvider.confidence.getValue() == 0) {
                    stateProvider.predictedValue = observedValue;
                } else {
                    stateProvider.confidence.update(false);
                }
            }

            this.cache.getReplacementPolicy().handlePromotionOnHit(null, set, cacheAccess.getWay());
        } else {
            stateProvider.state = true;
            line.setTag(tag);

            stateProvider.predictedValue = observedValue;
            stateProvider.confidence.reset();

            this.cache.getReplacementPolicy().handleInsertionOnMiss(null, set, cacheAccess.getWay());
        }
    }

    public void dumpState() {
        for(int i = 0; i < this.cache.getNumSets(); i++) {
            for (CacheLine<Boolean> line : this.cache.getLines(i)) {
                BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
                System.out.printf("%s: %s\n", line, stateProvider);
            }
        }
    }

    /**
     * Get the default value.
     *
     * @return the default value
     */
    public PredictableT getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the number of hits.
     *
     * @return the number of hits
     */
    @Override
    public long getNumHits() {
        return numHits;
    }

    /**
     * Get the number of misses.
     *
     * @return the number of misses
     */
    @Override
    public long getNumMisses() {
        return numMisses;
    }

    /**
     * Get the number of accesses.
     *
     * @return the number of accesses
     */
    public long getNumAccesses() {
        return numHits + numMisses;
    }

    /**
     * Get the hit ratio.
     *
     * @return the hit ratio
     */
    public double getHitRatio() {
        return this.getNumAccesses() > 0 ? (double) this.numHits / this.getNumAccesses() : 0.0;
    }

    /**
     * Boolean value predictor.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        protected boolean state;
        private PredictableT predictedValue;
        private SaturatingCounter confidence;

        /**
         * Create a boolean value predictor.
         *
         * @param counterThreshold the threshold value of the counter
         * @param counterMaxValue the maximum value of the counter
         */
        public BooleanValueProvider(int counterThreshold, int counterMaxValue) {
            this.state = false;

            this.predictedValue = null;
            this.confidence = new SaturatingCounter(0, counterThreshold, counterMaxValue, 0);
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
            return false;
        }

        @Override
        public String toString() {
            return String.format("{predictedValue=%s, confidence=%s}", predictedValue, confidence);
        }
    }
}
