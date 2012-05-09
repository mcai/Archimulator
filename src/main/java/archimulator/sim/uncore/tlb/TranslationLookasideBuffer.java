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
package archimulator.sim.uncore.tlb;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.simulation.SimulationObject;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;

public class TranslationLookasideBuffer {
    private String name;
    private TranslationLookasideBufferConfig config;

    private EvictableCache<Boolean, CacheLine<Boolean>> cache;

    private long accesses;
    private long hits;
    private long evictions;

    public TranslationLookasideBuffer(SimulationObject parent, String name, TranslationLookasideBufferConfig config) {
        this.name = name;
        this.config = config;

        this.cache = new EvictableCache<Boolean, CacheLine<Boolean>>(parent, name, config.getGeometry(), LRUPolicy.class, new Function3<Cache<?, ?>, Integer, Integer, CacheLine<Boolean>>() {
            public CacheLine<Boolean> apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new CacheLine<Boolean>(cache, set, way, false);
            }
        });

        parent.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                TranslationLookasideBuffer.this.accesses = 0;
                TranslationLookasideBuffer.this.hits = 0;
                TranslationLookasideBuffer.this.evictions = 0;
            }
        });

        parent.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(TranslationLookasideBuffer.this.name + ".hitRatio", String.valueOf(TranslationLookasideBuffer.this.getHitRatio()));
                    event.getStats().put(TranslationLookasideBuffer.this.name + ".accesses", String.valueOf(TranslationLookasideBuffer.this.accesses));
                    event.getStats().put(TranslationLookasideBuffer.this.name + ".hits", String.valueOf(TranslationLookasideBuffer.this.hits));
                    event.getStats().put(TranslationLookasideBuffer.this.name + ".misses", String.valueOf(TranslationLookasideBuffer.this.getMisses()));

                    event.getStats().put(TranslationLookasideBuffer.this.name + ".evictions", String.valueOf(TranslationLookasideBuffer.this.evictions));
                }
            }
        });
    }

    public void access(MemoryHierarchyAccess access, Action onCompletedCallback) {
        CacheAccess<Boolean, CacheLine<Boolean>> cacheAccess = this.cache.newAccess(null, access, access.getPhysicalAddress(), CacheAccessType.UNKNOWN);

        this.accesses++;

        if (cacheAccess.isHitInCache()) {
            cacheAccess.commit();
            this.hits++;
        } else {
            if (cacheAccess.isEviction()) {
                this.evictions++;
            }

            cacheAccess.getLine().setNonInitialState(true);
            cacheAccess.commit();
        }

        access.getThread().getCycleAccurateEventQueue().schedule(this, onCompletedCallback, cacheAccess.isHitInCache() ? this.config.getHitLatency() : this.config.getMissLatency());
    }

    public String getName() {
        return name;
    }

    public TranslationLookasideBufferConfig getConfig() {
        return config;
    }

    private long getMisses() {
        return this.accesses - this.hits;
    }

    private double getHitRatio() {
        return this.accesses > 0 ? (double) this.hits / this.accesses : 0.0;
    }

    public EvictableCache<Boolean, CacheLine<Boolean>> getCache() {
        return cache;
    }
}
