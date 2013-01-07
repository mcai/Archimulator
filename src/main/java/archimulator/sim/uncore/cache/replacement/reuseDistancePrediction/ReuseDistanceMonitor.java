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
package archimulator.sim.uncore.cache.replacement.reuseDistancePrediction;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import net.pickapack.math.Quantizer;

/**
 * Reuse distance monitor.
 *
 * @author Min Cai
 */
public class ReuseDistanceMonitor {
    private Predictor<Integer> predictor;
    private ReuseDistanceSampler sampler;

    /**
     * Create a reuse distance monitor.
     *
     * @param cache the parent cache
     * @param reuseDistanceQuantizer the reuse distance quantizer
     */
    public ReuseDistanceMonitor(Cache<?> cache, Quantizer reuseDistanceQuantizer) {
        this.predictor = new CacheBasedPredictor<Integer>(cache, cache.getName() + ".reuseDistancePredictor", new CacheGeometry(16 * 16 * cache.getGeometry().getLineSize(), 16, cache.getGeometry().getLineSize()), 0, 3, 0);
        this.sampler = new ReuseDistanceSampler(this, 4096, (reuseDistanceQuantizer.getMaxValue() + 1) * reuseDistanceQuantizer.getQuantum(), reuseDistanceQuantizer);
    }

    /**
     * Get the predictor.
     *
     * @return the predictor
     */
    public Predictor<Integer> getPredictor() {
        return predictor;
    }

    /**
     * Get the sampler.
     *
     * @return the sampler
     */
    public ReuseDistanceSampler getSampler() {
        return sampler;
    }
}
