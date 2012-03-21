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
import archimulator.util.action.Function3;
import archimulator.util.math.Quantizer;

import java.io.Serializable;

public abstract class AbstractRDPredictionEvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends EvictionPolicy<StateT, LineT> {
    protected MirrorCache mirrorCache;

    protected Quantizer quantizerReuseDistance;
    protected Quantizer quantizerTimestamp;

    protected RDMonitor rdMonitor;

    protected int accessesCounterLow;
    protected int accessesCounterHigh;

    public AbstractRDPredictionEvictionPolicy(EvictableCache<StateT, LineT> cache) {
        super(cache);

        this.mirrorCache = new MirrorCache();

        this.quantizerTimestamp = new Quantizer(7, 16384);
        this.quantizerReuseDistance = new Quantizer(15, 8192);

        this.rdMonitor = new RDMonitor(this.getCache(), this.quantizerReuseDistance);

        this.accessesCounterLow = 0;
        this.accessesCounterHigh = 1;
    }

    protected CacheMiss<StateT, LineT> handleReplacementBasedOnRDPrediction(CacheReference reference, boolean selectiveCaching) {
        int victimTime = 0;
        int victimWay = 0;

        int newPredictedReuseDistance = 0;

        if (selectiveCaching) {
            newPredictedReuseDistance = this.rdMonitor.lookup(reference.getAccess().getVirtualPc());
        }

        if (newPredictedReuseDistance == this.quantizerReuseDistance.getMaxValue()) {
            return new CacheMiss<StateT, LineT>(this.getCache(), reference, -1);
        } else {
            for (MirrorCacheLine line : this.mirrorCache.getLines(reference.getSet())) {
                int way = line.getWay();

                int now = this.quantizerTimestamp.unQuantize(this.accessesCounterHigh + (line.timeStamp > this.accessesCounterHigh ? this.quantizerTimestamp.getMaxValue() + 1 : 0));

                int rawTimestamp = this.quantizerTimestamp.unQuantize(line.timeStamp);
                int rawPredictedReuseDistance = this.quantizerReuseDistance.unQuantize(line.predictedReuseDistance);

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
                return new CacheMiss<StateT, LineT>(this.getCache(), reference, -1);
            }
        }

        return new CacheMiss<StateT, LineT>(this.getCache(), reference, victimWay);
    }

    protected boolean isPolluting(CacheMiss<StateT, LineT> miss) {
        if (miss.isBypass()) {
            return false;
        }

        int predictedRequesterReuseDistance = this.rdMonitor.lookup(miss.getReference().getAccess().getVirtualPc()); //TODO: thread awareness: threadId-pc pair??!!
        int predictedVictimReuseDistance = this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay()).predictedReuseDistance;

        return predictedRequesterReuseDistance > predictedVictimReuseDistance;
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        this.handleLineReference(hit.getReference().getSet(), hit.getWay(), hit.getReference().getAccess().getVirtualPc(), hit.getReference().getAccessType());
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        this.handleLineReference(miss.getReference().getSet(), miss.getWay(), miss.getReference().getAccess().getVirtualPc(), miss.getReference().getAccessType());
    }

    private void handleLineReference(int set, int way, int pc, CacheAccessType accessType) {
        this.updateOnEveryAccess(pc, this.getCache().getLine(set, way).getTag(), accessType);

        MirrorCacheLine mirrorLine = this.mirrorCache.getLine(set, way);
        if (!accessType.isWriteback()) {
            mirrorLine.timeStamp = this.accessesCounterHigh;
            mirrorLine.predictedReuseDistance = accessType.isDownwardReadOrWrite() ? this.rdMonitor.lookup(pc) : 0;
        } else {
            mirrorLine.timeStamp = 0;
            mirrorLine.predictedReuseDistance = this.quantizerReuseDistance.getMaxValue();
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

    protected class MirrorCacheLine extends CacheLine<Boolean> {
        protected int timeStamp;
        protected int predictedReuseDistance;

        private MirrorCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, true);
        }
    }

    protected class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache() {
            super(getCache(), getCache().getName() + ".rdPredictionEvictionPolicy.mirrorCache", getCache().getGeometry(), new Function3<Cache<?, ?>, Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                    return new MirrorCacheLine(cache, set, way);
                }
            });
        }
    }
}
