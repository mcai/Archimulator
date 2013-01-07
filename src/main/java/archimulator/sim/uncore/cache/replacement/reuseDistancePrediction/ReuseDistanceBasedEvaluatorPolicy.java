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

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;

import java.io.Serializable;

/**
 * Reuse distance based evaluator policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class ReuseDistanceBasedEvaluatorPolicy<StateT extends Serializable> extends AbstractReuseDistancePredictionPolicy<StateT> {
    private CacheReplacementPolicy<StateT> replacementPolicy;

    private long totalReplacements;
    private long pollutingReplacements;
    private long nonOptimalReplacements;

    /**
     * Create a reuse distance based evaluator policy.
     *
     * @param cache the parent cache
     * @param replacementPolicy the replacement policy to be evaluated
     */
    public ReuseDistanceBasedEvaluatorPolicy(EvictableCache<StateT> cache, CacheReplacementPolicy<StateT> replacementPolicy) {
        super(cache);
        this.replacementPolicy = replacementPolicy;
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        CacheAccess<StateT> miss = this.replacementPolicy.handleReplacement(access, set, tag);

        CacheAccess<StateT> reuseDistancePredictionBasedMiss = this.handleReplacementBasedOnReuseDistancePrediction(access, set, tag);

        this.totalReplacements++;

        if (this.isPolluting(miss)) {
            this.pollutingReplacements++;
        }

        if (miss.getWay() != reuseDistancePredictionBasedMiss.getWay()) {
            this.nonOptimalReplacements++;
        }

        return miss;
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.replacementPolicy.handlePromotionOnHit(access, set, way);

        super.handlePromotionOnHit(access, set, way);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.replacementPolicy.handleInsertionOnMiss(access, set, way);

        super.handleInsertionOnMiss(access, set, way);
    }

    /**
     * Get the total number of replacements.
     *
     * @return the total number of replacements
     */
    public long getTotalReplacements() {
        return totalReplacements;
    }

    /**
     * Get the number of polluting replacements.
     *
     * @return the number of polluting replacements
     */
    public long getPollutingReplacements() {
        return pollutingReplacements;
    }

    /**
     * Get the number of non-optimal replacements.
     *
     * @return the number of non-optimal replacements
     */
    public long getNonOptimalReplacements() {
        return nonOptimalReplacements;
    }

    /**
     * Get the degree of observed pollution.
     *
     * @return the degree of observed pollution
     */
    public double getPollution() {
        return (double) this.pollutingReplacements / this.totalReplacements;
    }

    /**
     * Get the degree of observed non-optimality.
     *
     * @return the degree of observed non-optimality
     */
    public double getNonOptimality() {
        return (double) this.nonOptimalReplacements / this.totalReplacements;
    }
}
