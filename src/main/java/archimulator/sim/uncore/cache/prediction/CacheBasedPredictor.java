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
 *
 * @author Min Cai
 * @param <PredictableT>
 */
public class CacheBasedPredictor<PredictableT extends Comparable<PredictableT>> implements Predictor<PredictableT> {
    private EvictableCache<Boolean> cache;

    /**
     *
     * @param parent
     * @param name
     * @param geometry
     * @param counterThreshold
     * @param counterMaxValue
     */
    public CacheBasedPredictor(SimulationObject parent, String name, CacheGeometry geometry, final int counterThreshold, final int counterMaxValue) {
        this(parent, name, geometry, CacheReplacementPolicyType.LRU, counterThreshold, counterMaxValue);
    }

    /**
     *
     * @param parent
     * @param name
     * @param geometry
     * @param cacheReplacementPolicyType
     * @param counterThreshold
     * @param counterMaxValue
     */
    public CacheBasedPredictor(SimulationObject parent, String name, CacheGeometry geometry, CacheReplacementPolicyType cacheReplacementPolicyType, final int counterThreshold, final int counterMaxValue) {
        ValueProviderFactory<Boolean, ValueProvider<Boolean>> cacheLineStateProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider(counterThreshold, counterMaxValue);
            }
        };

        this.cache = new EvictableCache<Boolean>(parent, name, geometry, cacheReplacementPolicyType, cacheLineStateProviderFactory);
    }

    /**
     *
     * @param address
     * @param observedValue
     */
    public void update(int address, PredictableT observedValue) {
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

    /**
     *
     * @param address
     * @param defaultValue
     * @return
     */
    public PredictableT predict(int address, PredictableT defaultValue) {
        CacheLine<Boolean> lineFound = this.cache.findLine(address);
        BooleanValueProvider stateProvider = lineFound != null ? (BooleanValueProvider) lineFound.getStateProvider() : null;
        return lineFound != null && stateProvider.confidence.isTaken() ? stateProvider.predictedValue : defaultValue;
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        protected boolean state;
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
