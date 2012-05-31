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
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Function3;

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
                if (args.length != 2) {
                    throw new IllegalArgumentException();
                }

                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new BooleanValueProvider(set, way);
            }
        };

        this.cache = new EvictableCache<Boolean>(name, config.getGeometry(), LRUPolicy.class, cacheLineStateProviderFactory);

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
        this.accesses++;

        int address = access.getPhysicalAddress();
        int set = this.cache.getSet(address);

        int way = this.cache.findWay(address);

        if(way != -1) {
            this.hits++;
            this.cache.handlePromotionOnHit(set, way);
        }
        else {
            int victimWay = this.cache.findVictim(address);
            CacheLine<Boolean> victimLine = this.cache.getLine(set, victimWay);
            BooleanValueProvider victimLineStateProvider = (BooleanValueProvider) victimLine.getStateProvider();
            if(victimLine.getState()) {
                this.evictions++;
                victimLine.setTag(CacheLine.INVALID_TAG);
                victimLineStateProvider.setState(false);
            }
            this.cache.handleInsertionOnMiss(set, victimWay);
            victimLine.setTag(this.cache.getTag(address));
            victimLineStateProvider.setState(true);
        }

        access.getThread().getCycleAccurateEventQueue().schedule(this, onCompletedCallback, way != -1 ? this.config.getHitLatency() : this.config.getMissLatency());
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

    public EvictableCache<Boolean> getCache() {
        return cache;
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private final int set;
        private final int way;
        protected boolean state;

        public BooleanValueProvider(int set, int way) {
            this.set = set;
            this.way = way;
        }

        @Override
        public Boolean get() {
            return state;
        }

        public void setState(boolean state) {
            this.state = state;
        }

        @Override
        public Boolean getInitialValue() {
            return false;
        }
    }
}
