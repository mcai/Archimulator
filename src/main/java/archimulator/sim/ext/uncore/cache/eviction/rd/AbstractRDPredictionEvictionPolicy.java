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
package archimulator.sim.ext.uncore.cache.eviction.rd;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.math.Quantizer;

import java.io.Serializable;

public abstract class AbstractRDPredictionEvictionPolicy<StateT extends Serializable> extends EvictionPolicy<StateT> {
    protected Cache<Boolean> mirrorCache;

    protected Quantizer quantizerReuseDistance;
    protected Quantizer quantizerTimestamp;

    protected RDMonitor rdMonitor;

    protected int accessesCounterLow;
    protected int accessesCounterHigh;

    public AbstractRDPredictionEvictionPolicy(EvictableCache<StateT> cache) {
        super(cache);

        ValueProviderFactory<Boolean, ValueProvider<Boolean>> valueProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        };

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".rdPredictionEvictionPolicy..mirrorCache", cache.getGeometry(), valueProviderFactory);

        this.quantizerTimestamp = new Quantizer(7, 16384);
        this.quantizerReuseDistance = new Quantizer(15, 8192);

        this.rdMonitor = new RDMonitor(this.getCache(), this.quantizerReuseDistance);

        this.accessesCounterLow = 0;
        this.accessesCounterHigh = 1;
    }

    protected CacheMiss<StateT> handleReplacementBasedOnRDPrediction(CacheReference reference, boolean selectiveCaching) {
        int victimTime = 0;
        int victimWay = 0;

        int newPredictedReuseDistance = 0;

        if (selectiveCaching) {
            newPredictedReuseDistance = this.rdMonitor.lookup(reference.getAccess().getVirtualPc());
        }

        if (newPredictedReuseDistance == this.quantizerReuseDistance.getMaxValue()) {
            return new CacheMiss<StateT>(this.getCache(), reference, -1);
        } else {
            for (CacheLine<Boolean> line : this.mirrorCache.getLines(reference.getSet())) {
                int way = line.getWay();

                BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();

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

            if (this.quantizerReuseDistance.unQuantize(newPredictedReuseDistance) >= victimTime) {
                return new CacheMiss<StateT>(this.getCache(), reference, -1);
            }
        }

        return new CacheMiss<StateT>(this.getCache(), reference, victimWay);
    }

    protected boolean isPolluting(CacheMiss<StateT> miss) {
        int predictedRequesterReuseDistance = this.rdMonitor.lookup(miss.getReference().getAccess().getVirtualPc()); //TODO: thread awareness: threadId-pc pair??!!
        CacheLine<Boolean> line = this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        int predictedVictimReuseDistance = stateProvider.predictedReuseDistance;

        return predictedRequesterReuseDistance > predictedVictimReuseDistance;
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT> hit) {
        this.handleLineReference(hit.getReference().getSet(), hit.getWay(), hit.getReference().getAccess().getVirtualPc(), hit.getReference().getAccessType());
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT> miss) {
        this.handleLineReference(miss.getReference().getSet(), miss.getWay(), miss.getReference().getAccess().getVirtualPc(), miss.getReference().getAccessType());
    }

    private void handleLineReference(int set, int way, int pc, CacheAccessType accessType) {
        this.updateOnEveryAccess(pc, this.getCache().getLine(set, way).getTag(), accessType);

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        if (!accessType.isWriteback()) {
            stateProvider.timeStamp = this.accessesCounterHigh;
            stateProvider.predictedReuseDistance = accessType.isDownwardReadOrWrite() ? this.rdMonitor.lookup(pc) : 0;
        } else {
            stateProvider.timeStamp = 0;
            stateProvider.predictedReuseDistance = this.quantizerReuseDistance.getMaxValue();
        }
    }

    protected void updateOnEveryAccess(int pc, int address, CacheAccessType accessType) {
        if (accessType.isDownwardReadOrWrite()) {
            this.accessesCounterLow++;
            if (this.accessesCounterLow == this.quantizerTimestamp.getQuantum()) {
                this.accessesCounterLow = 0;
                this.accessesCounterHigh++;
                if (this.accessesCounterHigh > this.quantizerTimestamp.getMaxValue()) {
                    this.accessesCounterHigh = 0;
                }
            }
            this.rdMonitor.update(pc, address, accessType);
        }
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        protected int timeStamp;
        protected int predictedReuseDistance;

        public BooleanValueProvider() {
            this.state = true;
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
