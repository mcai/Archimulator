/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache.replacement.reuseDistancePrediction;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.*;
import archimulator.uncore.cache.replacement.AbstractCacheReplacementPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.math.HighLowCounter;

import java.io.Serializable;

/**
 * Reuse distance prediction policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class ReuseDistancePredictionPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    private HighLowCounter highLowCounter;

    /**
     * Create a reuse distance prediction policy.
     *
     * @param cache the parent cache
     */
    @SuppressWarnings("unchecked")
    public ReuseDistancePredictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new BasicCache<>(
                cache,
                cache.getName() + ".reuseDistancePredictionPolicy.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.highLowCounter = new HighLowCounter(7, 16384);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int victimTime = 0;
        int victimWay = 0;

        for (CacheLine<Boolean> mirrorLine : this.mirrorCache.getLines(set)) {
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            int way = mirrorLine.getWay();

            int now = this.highLowCounter.getTimestampQuantizer().unQuantize(this.highLowCounter.getHighCounter() + (stateProvider.timeStamp > this.highLowCounter.getHighCounter() ? this.highLowCounter.getTimestampQuantizer().getMaxValue() + 1 : 0));

            int rawTimestamp = this.highLowCounter.getTimestampQuantizer().unQuantize(stateProvider.timeStamp);
            int rawPredictedReuseDistance = getCache().getSimulation().getReuseDistancePredictionHelper().getReuseDistanceQuantizer().unQuantize(stateProvider.predictedReuseDistance);

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

        return new CacheAccess<>(this.getCache(), access, set, victimWay, tag);
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
    @SuppressWarnings("unchecked")
    private void handleLineReference(int set, int way, int threadId, int pc) {
        this.highLowCounter.increment();

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.timeStamp = this.highLowCounter.getHighCounter();
        stateProvider.predictedReuseDistance = getCache().getSimulation().getReuseDistancePredictionHelper().getReuseDistancePredictor().predict(pc);
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
