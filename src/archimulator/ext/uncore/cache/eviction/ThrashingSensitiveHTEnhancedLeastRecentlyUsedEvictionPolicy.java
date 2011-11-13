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

import archimulator.core.BasicThread;
import archimulator.core.Processor;
import archimulator.uncore.cache.*;
import archimulator.uncore.cache.eviction.EvictionPolicy;
import archimulator.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.event.ProcessorInitializedEvent;
import archimulator.util.IntegerIntegerPair;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends LeastRecentlyUsedEvictionPolicy<StateT, LineT> {
    private MirrorCache mirrorCache;
    private Processor processor;

    private List<IntegerIntegerPair> predefinedDelinquentPcs;

    public ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy(EvictableCache<StateT, LineT> cache) {
        super(cache);

        this.mirrorCache = new MirrorCache();

        cache.getBlockingEventDispatcher().addListener(ProcessorInitializedEvent.class, new Action1<ProcessorInitializedEvent>() {
            public void apply(ProcessorInitializedEvent event) {
                ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy.this.processor = event.getProcessor();
            }
        });

        this.predefinedDelinquentPcs = new ArrayList<IntegerIntegerPair>();
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(2, 0x004014d8));
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(0, 0x00400a34));
    }

    @Override
    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
        if (reference.getAccessType().isDownwardRead() && this.isDelinquentPc(reference.getAccess().getThread().getId(), reference.getAccess().getVirtualPc()) && BasicThread.isMainThread(reference.getAccess().getThread())) {
            return new CacheMiss<StateT, LineT>(this.getCache(), reference, -1); //bypass
        }

        return new CacheMiss<StateT, LineT>(this.getCache(), reference, this.getLRU(reference.getSet()));  //LRU victim selection
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        MirrorCacheLine mirrorLine = this.mirrorCache.getLine(hit.getReference().getSet(), hit.getWay());

        if (hit.getReference().getAccessType().isDownwardRead() && mirrorLine.ht && BasicThread.isMainThread(hit.getReference().getAccess().getThread())) {
            this.setLRU(hit.getReference().getSet(), hit.getWay());  //HT-MT inter-thread hit: Demote to LRU position; turn off HT bit
            mirrorLine.ht = false;
        } else {
            super.handlePromotionOnHit(hit);  //Promote to MRU position
        }
    }

    //TODO: add HT quality sensitivity (inter-thread reuse distance) into policy selection, e.g., if the HT miss will not be reused by MT, insert it in LRU
    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        MirrorCacheLine mirrorLine = this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay());
        mirrorLine.ht = false;

        if (miss.getReference().getAccessType().isDownwardRead() && this.isDelinquentPc(miss.getReference().getAccess().getThread().getId(), miss.getReference().getAccess().getVirtualPc())) {
            if (BasicThread.isMainThread(miss.getReference().getAccess().getThread())) {
                this.setLRU(miss.getReference().getSet(), miss.getWay()); // MT miss: insert in LRU position
            } else if (BasicThread.isHelperThread(miss.getReference().getAccess().getThread())) {
                this.setMRU(miss.getReference().getSet(), miss.getWay());  //HT miss: insert in MRU position; turn on HT bit
                mirrorLine.ht = true;
            } else {
                super.handleInsertionOnMiss(miss); //insert in MRU position
            }
        } else {
            super.handleInsertionOnMiss(miss); //insert in MRU position
        }
    }

    private boolean isDelinquentPc(int threadId, int pc) {
        return this.predefinedDelinquentPcs.contains(new IntegerIntegerPair(threadId, pc));
//        return this.processor.getCapability(DelinquentLoadIdentificationCapability.class ).isDelinquentPc(threadId, pc);
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private boolean ht;

        private MirrorCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, true);
        }
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache() {
            super(getCache(), getCache().getName() + ".streamingHTEnhancedLeastRecentlyUsedEvictionPolicy.mirrorCache", getCache().getGeometry(), new Function3<Cache<?, ?>, Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                    return new MirrorCacheLine(cache, set, way);
                }
            });
        }
    }

    public static final EvictionPolicyFactory FACTORY = new EvictionPolicyFactory() {
        public String getName() {
            return "STREAMING_HT_ENHANCED_LEAST_RECENTLY_USED";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy<StateT, LineT>(cache);
        }
    };
}
