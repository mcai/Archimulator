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
package archimulator.sim.uncore.cache.replacement.reuseDistance;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import net.pickapack.math.Quantizer;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;

/**
 * Abstract reuse distance prediction policy.
 *
 * @author Min Cai
 * @param <StateT> the state type of the parent evictable cache
 */
public abstract class AbstractReuseDistancePredictionPolicy<StateT extends Serializable> extends CacheReplacementPolicy<StateT> {
    protected Cache<Boolean> mirrorCache;

    protected Quantizer quantizerReuseDistance;
    protected Quantizer quantizerTimestamp;

    protected ReuseDistanceMonitor reuseDistanceMonitor;

    protected int accessesCounterLow;
    protected int accessesCounterHigh;

    public AbstractReuseDistancePredictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".rdPredictionEvictionPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });

        this.quantizerTimestamp = new Quantizer(7, 16384);
        this.quantizerReuseDistance = new Quantizer(15, 8192);

        this.reuseDistanceMonitor = new ReuseDistanceMonitor(this.getCache(), this.quantizerReuseDistance);

        this.accessesCounterLow = 0;
        this.accessesCounterHigh = 1;
    }

    protected CacheAccess<StateT> handleReplacementBasedOnReuseDistancePrediction(MemoryHierarchyAccess access, int set, int tag) {
        int victimTime = 0;
        int victimWay = 0;

        for (CacheLine<Boolean> mirrorLine : this.mirrorCache.getLines(set)) {
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            int way = mirrorLine.getWay();

            int now = this.quantizerTimestamp.unQuantize(this.accessesCounterHigh + (stateProvider.timeStamp > this.accessesCounterHigh ? this.quantizerTimestamp.getMaxValue() + 1 : 0));

            int rawTimestamp = this.quantizerTimestamp.unQuantize(stateProvider.timeStamp);
            int rawPredictedReuseDistance = this.quantizerReuseDistance.unQuantize(stateProvider.predictedReuseDistance);

            int timeLeft = rawTimestamp + rawPredictedReuseDistance > now ? rawTimestamp + rawPredictedReuseDistance - now : 0;
            if (timeLeft > victimTime) {
                victimTime = timeLeft;
            } else {
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

    protected boolean isPolluting(CacheAccess<StateT> cacheAccess) {
        int predictedRequesterReuseDistance = this.reuseDistanceMonitor.lookup(cacheAccess.getAccess().getVirtualPc()); //TODO: thread awareness: threadId-pc pair??!!

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(cacheAccess.getSet(), cacheAccess.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

        int predictedVictimReuseDistance = stateProvider.predictedReuseDistance;

        return predictedRequesterReuseDistance > predictedVictimReuseDistance;
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.handleLineReference(set, way, access.getVirtualPc());
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.handleLineReference(set, way, access.getVirtualPc());
    }

    private void handleLineReference(int set, int way, int pc) {
        this.updateOnEveryAccess(pc, this.getCache().getLine(set, way).getTag());

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.timeStamp = this.accessesCounterHigh;
        stateProvider.predictedReuseDistance = this.reuseDistanceMonitor.lookup(pc);
    }

    protected void updateOnEveryAccess(int pc, int address) {
        this.accessesCounterLow++;
        if (this.accessesCounterLow == this.quantizerTimestamp.getQuantum()) {
            this.accessesCounterLow = 0;
            this.accessesCounterHigh++;
            if (this.accessesCounterHigh > this.quantizerTimestamp.getMaxValue()) {
                this.accessesCounterHigh = 0;
            }
        }
        this.reuseDistanceMonitor.update(pc, address);
    }

    private static class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        protected int timeStamp;
        protected int predictedReuseDistance;

        @Override
        public Boolean get() {
            return state;
        }

        @Override
        public Boolean getInitialValue() {
            return true;
        }
    }
}
