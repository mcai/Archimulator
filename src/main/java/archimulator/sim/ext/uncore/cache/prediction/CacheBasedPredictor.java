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
package archimulator.sim.ext.uncore.cache.prediction;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.math.SaturatingCounter;

public class CacheBasedPredictor<PredictableT extends Comparable<PredictableT>> implements Predictor<PredictableT> {
    private EvictableCache<Boolean> evictableCache;

    public CacheBasedPredictor(Cache<?> cache, String name, CacheGeometry geometry, final int counterThreshold, final int counterMaxValue) {
        this(cache, name, geometry, LRUPolicy.class, counterThreshold, counterMaxValue);
    }

    public CacheBasedPredictor(Cache<?> cache, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, final int counterThreshold, final int counterMaxValue) {
        ValueProviderFactory<Boolean, ValueProvider<Boolean>> valueProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider(counterThreshold, counterMaxValue);
            }
        };
        this.evictableCache = new EvictableCache<Boolean>(cache, name, geometry, evictionPolicyClz, valueProviderFactory);
    }

    public void update(int address, PredictableT observedValue) {
        CacheAccess<Boolean> access = this.evictableCache.newAccess(null, null, address, CacheAccessType.UNKNOWN);
        if (!access.isHitInCache()) {
            CacheLine<Boolean> line = access.getLine();
            BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
            line.setTag(this.evictableCache.getTag(address));
            stateProvider.state = true;
        }
        access.commit();

        CacheLine<Boolean> line = this.evictableCache.getLine(access.getReference().getSet(), access.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();

        if (!access.isHitInCache()) {
            stateProvider.predictedValue = observedValue;
            stateProvider.confidence.reset();
        } else {
            if (stateProvider.predictedValue == observedValue) {
                stateProvider.confidence.update(true);

            } else {
                if (stateProvider.confidence.getValue() == 0) {
                    stateProvider.predictedValue = observedValue;
                } else {
                    stateProvider.confidence.update(false);
                }
            }
        }
    }

    public PredictableT predict(int address, PredictableT defaultValue) {
        CacheLine<Boolean> lineFound = this.evictableCache.findLine(address);
        BooleanValueProvider stateProvider = lineFound != null ? (BooleanValueProvider) lineFound.getStateProvider() : null;
        return lineFound != null && stateProvider.confidence.isTaken() ? stateProvider.predictedValue : defaultValue;
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private PredictableT predictedValue;
        private SaturatingCounter confidence;

        public BooleanValueProvider(int counterThreshold, int counterMaxValue) {
            this.state = false;

            this.predictedValue = null;
            this.confidence = new SaturatingCounter(0, counterThreshold, counterMaxValue, 0);
        }

        @Override
        public Boolean get() {
            return state;
        }

        @Override
        public Boolean getInitialValue() {
            return false;
        }
    }
}
