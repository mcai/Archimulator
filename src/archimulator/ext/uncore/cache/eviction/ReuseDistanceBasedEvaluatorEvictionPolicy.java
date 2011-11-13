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
package archimulator.ext.uncore.cache.eviction;

import archimulator.uncore.cache.*;
import archimulator.uncore.cache.eviction.EvictionPolicy;
import archimulator.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action1;

import java.io.Serializable;
import java.util.Map;

public class ReuseDistanceBasedEvaluatorEvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends AbstractReuseDistancePredictionEvictionPolicy<StateT, LineT> {
    private EvictionPolicy<StateT, LineT> evictionPolicy;

    private long totalReplacements;
    private long pollutingReplacements;
    private long nonOptimalReplacements;

    public ReuseDistanceBasedEvaluatorEvictionPolicy(EvictableCache<StateT, LineT> cache, EvictionPolicy<StateT, LineT> evictionPolicy) {
        super(cache);

        this.evictionPolicy = evictionPolicy;

        cache.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                totalReplacements = 0;
                pollutingReplacements = 0;
                nonOptimalReplacements = 0;
            }
        });

        cache.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        String desc = this.evictionPolicy.getClass().getName() + ".reuseDistanceBasedEvaluatorEvictionPolicy";

        stats.put(getCache().getName() + "." + desc + ".totalReplacements", String.valueOf(totalReplacements));
        stats.put(getCache().getName() + "." + desc + ".pollutingReplacements", String.valueOf(pollutingReplacements));
        stats.put(getCache().getName() + "." + desc + ".nonOptimalReplacements", String.valueOf(nonOptimalReplacements));

        stats.put(getCache().getName() + "." + desc + ".pollution", String.valueOf(getPollution()));
        stats.put(getCache().getName() + "." + desc + ".nonOptimality", String.valueOf(getNonOptimality()));
    }

    private double getPollution() {
        return (double) this.pollutingReplacements / this.totalReplacements;
    }

    private double getNonOptimality() {
        return (double) this.nonOptimalReplacements / this.totalReplacements;
    }

    @Override
    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
        CacheMiss<StateT, LineT> miss = this.evictionPolicy.handleReplacement(reference);

        CacheMiss<StateT, LineT> reuseDistancePredictionBasedMiss = this.handleReplacementBasedOnReuseDistancePrediction(reference, true);

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
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        this.evictionPolicy.handleInsertionOnMiss(miss);

        super.handleInsertionOnMiss(miss);
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        this.evictionPolicy.handlePromotionOnHit(hit);

        super.handlePromotionOnHit(hit);
    }

    public static EvictionPolicyFactory factory(final EvictionPolicyFactory evictionPolicyFactory) {
        return new EvictionPolicyFactory() {
            public String getName() {
                return evictionPolicyFactory.getName() + "_WITH_POLLUTION_EVALUATION";
            }

            public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
                return new ReuseDistanceBasedEvaluatorEvictionPolicy<StateT, LineT>(cache, evictionPolicyFactory.create(cache));
            }
        };
    }
}
