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

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.util.HighLowCounter;
import net.pickapack.action.Action1;
import net.pickapack.math.Quantizer;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;

/**
 * Reuse distance prediction policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class ReuseDistancePredictionPolicy<StateT extends Serializable> extends CacheReplacementPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    private Quantizer reuseDistanceQuantizer;

    private Predictor<Integer> reuseDistancePredictor;

    private ReuseDistanceSampler reuseDistanceSampler;

    private HighLowCounter highLowCounter;

    /**
     * Create a reuse distance prediction policy.
     *
     * @param cache the parent cache
     */
    public ReuseDistancePredictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".reuseDistancePredictionPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });

        this.reuseDistanceQuantizer = new Quantizer(15, 8192);

        this.reuseDistancePredictor = new CacheBasedPredictor<Integer>(cache, cache.getName() + ".reuseDistancePredictor", new CacheGeometry(16 * 16 * cache.getGeometry().getLineSize(), 16, cache.getGeometry().getLineSize()), 0, 3, 0);
        this.reuseDistanceSampler = new ReuseDistanceSampler(cache, cache.getName() + ".reuseDistanceSampler", 4096, (reuseDistanceQuantizer.getMaxValue() + 1) * reuseDistanceQuantizer.getQuantum(), reuseDistanceQuantizer);

        this.highLowCounter = new HighLowCounter(7, 16384);

        cache.getBlockingEventDispatcher().addListener(ReuseDistanceSampler.ReuseDistanceSampledEvent.class, new Action1<ReuseDistanceSampler.ReuseDistanceSampledEvent>() {
            @Override
            public void apply(ReuseDistanceSampler.ReuseDistanceSampledEvent event) {
                if (event.getSender() == reuseDistanceSampler) {
                    reuseDistancePredictor.update(event.getPc(), event.getReuseDistance());
                }
            }
        });
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int victimTime = 0;
        int victimWay = 0;

        for (CacheLine<Boolean> mirrorLine : this.mirrorCache.getLines(set)) {
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            int way = mirrorLine.getWay();

            int now = this.highLowCounter.getTimestampQuantizer().unQuantize(this.highLowCounter.getHighCounter() + (stateProvider.timeStamp > this.highLowCounter.getHighCounter() ? this.highLowCounter.getTimestampQuantizer().getMaxValue() + 1 : 0));

            int rawTimestamp = this.highLowCounter.getTimestampQuantizer().unQuantize(stateProvider.timeStamp);
            int rawPredictedReuseDistance = this.reuseDistanceQuantizer.unQuantize(stateProvider.predictedReuseDistance);

            int timeLeft = Math.max(rawTimestamp + rawPredictedReuseDistance - now, 0);
            if (timeLeft > victimTime) {
                victimTime = timeLeft;
                victimWay = way;
            }

            int timeIdle = now - rawTimestamp;
            if (timeIdle > victimTime) {
                victimTime = timeIdle;
                victimWay = way;
            }
        }

        return new CacheAccess<StateT>(this.getCache(), access, set, victimWay, tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.handleLineReference(set, way, access.getThread().getId(), access.getVirtualPc());
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.handleLineReference(set, way, access.getThread().getId(), access.getVirtualPc());
    }

    /**
     * Handle a line reference.
     *
     * @param set      the set index
     * @param way      the way
     * @param threadId the thread ID
     * @param pc       the value of the program counter (PC)
     */
    private void handleLineReference(int set, int way, int threadId, int pc) {
        this.highLowCounter.increment();
        this.reuseDistanceSampler.update(threadId, pc, this.getCache().getLine(set, way).getTag());

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.timeStamp = this.highLowCounter.getHighCounter();
        stateProvider.predictedReuseDistance = this.reuseDistancePredictor.predict(pc);
    }

    /**
     * Get the reuse distance predictor.
     *
     * @return the reuse distance predictor
     */
    public Predictor<Integer> getReuseDistancePredictor() {
        return reuseDistancePredictor;
    }

    /**
     * Get the reuse distance sampler.
     *
     * @return the reuse distance sampler
     */
    public ReuseDistanceSampler getReuseDistanceSampler() {
        return reuseDistanceSampler;
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;

        /**
         * The timestamp.
         */
        protected int timeStamp;

        /**
         * The predicted value of the reuse distance.
         */
        protected int predictedReuseDistance;

        /**
         * Create a boolean value provider.
         */
        private BooleanValueProvider() {
            this.state = true;
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
