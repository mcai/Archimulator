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

import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.simulation.SimulationObject;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;

public class TranslationLookasideBuffer {
    private String name;
    private TranslationLookasideBufferConfig config;

    private EvictableCache<Boolean> cache;

    private long accesses;
    private long hits;
    private long evictions;

    public TranslationLookasideBuffer(SimulationObject parent, String name, TranslationLookasideBufferConfig config) {
        this.name = name;
        this.config = config;

        ValueProviderFactory<Boolean, ValueProvider<Boolean>> cacheLineStateProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        };

        this.cache = new EvictableCache<Boolean>(parent, name, config.getGeometry(), LRUPolicy.class, cacheLineStateProviderFactory);

        parent.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                TranslationLookasideBuffer.this.accesses = 0;
                TranslationLookasideBuffer.this.hits = 0;
                TranslationLookasideBuffer.this.evictions = 0;
            }
        });
    }

    public void access(MemoryHierarchyAccess access, Action onCompletedCallback) {
        int set = this.cache.getSet(access.getPhysicalAddress());
        CacheAccess<Boolean> cacheAccess = this.cache.newAccess(access, access.getPhysicalAddress());

        this.accesses++;

        if (cacheAccess.isHitInCache()) {
            getCache().getEvictionPolicy().handlePromotionOnHit(set, cacheAccess.getWay());
            this.hits++;
        } else {
            if (cacheAccess.isEviction()) {
                this.evictions++;
            }

            CacheLine<Boolean> line = this.cache.getLine(set, cacheAccess.getWay());
            BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
            stateProvider.state = true;
            line.setAccess(access);
            line.setTag(access.getPhysicalTag());
            getCache().getEvictionPolicy().handleInsertionOnMiss(set,  cacheAccess.getWay());
        }

        access.getThread().getCycleAccurateEventQueue().schedule(this, onCompletedCallback, cacheAccess.isHitInCache() ? this.config.getHitLatency() : this.config.getMissLatency());
    }

    public String getName() {
        return name;
    }

    public TranslationLookasideBufferConfig getConfig() {
        return config;
    }

    public long getMisses() {
        return this.accesses - this.hits;
    }

    public double getHitRatio() {
        return this.accesses > 0 ? (double) this.hits / this.accesses : 0.0;
    }

    public long getAccesses() {
        return accesses;
    }

    public long getHits() {
        return hits;
    }

    public long getEvictions() {
        return evictions;
    }

    public EvictableCache<Boolean> getCache() {
        return cache;
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        protected boolean state;

        public BooleanValueProvider() {
            this.state = false;
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
