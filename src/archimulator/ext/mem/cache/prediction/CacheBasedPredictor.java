/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.ext.mem.cache.prediction;

import archimulator.mem.CacheAccessType;
import archimulator.mem.cache.*;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.util.action.Function2;
import archimulator.util.math.SaturatingCounter;

public class CacheBasedPredictor<PredictableT extends Comparable<PredictableT>> implements Predictor<PredictableT> {
    private EvictableCache<Boolean, PredictorLine> evictableCache;

    public CacheBasedPredictor(Cache<?, ?> cache, String name, CacheGeometry geometry, final int counterThreshold, final int counterMaxValue) {
        this(cache, name, geometry, LeastRecentlyUsedEvictionPolicy.FACTORY, counterThreshold, counterMaxValue);
    }

    public CacheBasedPredictor(Cache<?, ?> cache, String name, CacheGeometry geometry, EvictionPolicyFactory evictionPolicyFactory, final int counterThreshold, final int counterMaxValue) {
        this.evictableCache = new EvictableCache<Boolean, PredictorLine>(cache, name, geometry, evictionPolicyFactory,
                new Function2<Integer, Integer, PredictorLine>() {
                    public PredictorLine apply(Integer set, Integer way) {
                        return new PredictorLine(set, way, counterThreshold, counterMaxValue);
                    }
                });
    }

    public void update(int address, PredictableT observedValue) {
        CacheAccess<Boolean, PredictorLine> access = this.evictableCache.newAccess(null, null, address, CacheAccessType.UNKNOWN);
        if (!access.isHitInCache()) {
            access.commit().getLine().setNonInitialState(true);
        } else {
            access.commit();
        }

        PredictorLine line = this.evictableCache.getLine(access.getReference().getSet(), access.getWay());

        if (!access.isHitInCache()) {
            line.predictedValue = observedValue;
            line.confidence.reset();
        } else {
            if (line.predictedValue == observedValue) {
                line.confidence.update(true);

            } else {
                if (line.confidence.getValue() == 0) {
                    line.predictedValue = observedValue;
                } else {
                    line.confidence.update(false);
                }
            }
        }
    }

    public PredictableT predict(int address, PredictableT defaultValue) {
        PredictorLine lineFound = this.evictableCache.findLine(address);
        return lineFound != null && lineFound.confidence.isTaken() ? lineFound.predictedValue : defaultValue;
    }

    private class PredictorLine extends CacheLine<Boolean> {
        private PredictableT predictedValue;
        private SaturatingCounter confidence;

        private PredictorLine(int set, int way, int counterThreshold, int counterMaxValue) {
            super(set, way, false);

            this.predictedValue = null;
            this.confidence = new SaturatingCounter(0, counterThreshold, counterMaxValue, 0);
        }
    }
}
