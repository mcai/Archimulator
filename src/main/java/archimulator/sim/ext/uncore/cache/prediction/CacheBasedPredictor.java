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
import net.pickapack.action.Function3;
import net.pickapack.math.SaturatingCounter;

public class CacheBasedPredictor<PredictableT extends Comparable<PredictableT>> implements Predictor<PredictableT> {
    private EvictableCache<Boolean, PredictorLine> evictableCache;

    public CacheBasedPredictor(Cache<?, ?> cache, String name, CacheGeometry geometry, final int counterThreshold, final int counterMaxValue) {
        this(cache, name, geometry, LRUPolicy.class, counterThreshold, counterMaxValue);
    }

    public CacheBasedPredictor(Cache<?, ?> cache, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, final int counterThreshold, final int counterMaxValue) {
        this.evictableCache = new EvictableCache<Boolean, PredictorLine>(cache, name, geometry, evictionPolicyClz,
                new Function3<Cache<?, ?>, Integer, Integer, PredictorLine>() {
                    public PredictorLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                        return new PredictorLine(cache, set, way, counterThreshold, counterMaxValue);
                    }
                });
    }

    public void update(int address, PredictableT observedValue) {
        CacheAccess<Boolean, PredictorLine> access = this.evictableCache.newAccess(null, null, address, CacheAccessType.UNKNOWN);
        if (!access.isHitInCache()) {
            access.getLine().setNonInitialState(true);
        }
        access.commit();

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
        PredictorLine lineFound = this.evictableCache.findLine(address).getLine();
        return lineFound != null && lineFound.confidence.isTaken() ? lineFound.predictedValue : defaultValue;
    }

    private class PredictorLine extends CacheLine<Boolean> {
        private PredictableT predictedValue;
        private SaturatingCounter confidence;

        private PredictorLine(Cache<?, ?> cache, int set, int way, int counterThreshold, int counterMaxValue) {
            super(cache, set, way, false);

            this.predictedValue = null;
            this.confidence = new SaturatingCounter(0, counterThreshold, counterMaxValue, 0);
        }
    }
}
