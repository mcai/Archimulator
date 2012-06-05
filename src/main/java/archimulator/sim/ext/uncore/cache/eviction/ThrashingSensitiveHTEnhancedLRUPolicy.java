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
package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.sim.base.event.MyBlockingEventDispatcher;
import archimulator.sim.base.event.ProcessorInitializedEvent;
import archimulator.sim.core.BasicThread;
import archimulator.sim.core.Processor;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.IntegerIntegerPair;
import net.pickapack.action.Action1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ThrashingSensitiveHTEnhancedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private Cache<Boolean> mirrorCache;
    private Processor processor;

    private List<IntegerIntegerPair> predefinedDelinquentPcs;

    public ThrashingSensitiveHTEnhancedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".ThrashingSensitiveHTEnhancedLRUPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });

        cache.getBlockingEventDispatcher().addListener2(ProcessorInitializedEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<ProcessorInitializedEvent>() {
            public void apply(ProcessorInitializedEvent event) {
                ThrashingSensitiveHTEnhancedLRUPolicy.this.processor = event.getProcessor();
            }
        });

        this.predefinedDelinquentPcs = new ArrayList<IntegerIntegerPair>();
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(2, 0x004014d8));
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(0, 0x00400a34));
    }

    @Override
    public CacheMiss<StateT> handleReplacement(CacheReference reference) {
        if (reference.getAccessType().isDownwardRead() && this.isDelinquentPc(reference.getAccess().getThread().getId(), reference.getAccess().getVirtualPc()) && BasicThread.isMainThread(reference.getAccess().getThread())) {
            return new CacheMiss<StateT>(this.getCache(), reference, -1); //bypass
        }

        return new CacheMiss<StateT>(this.getCache(), reference, this.getLRU(reference.getSet()));  //LRU victim selection
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT> hit) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(hit.getReference().getSet(), hit.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

        if (hit.getReference().getAccessType().isDownwardRead() && stateProvider.ht && BasicThread.isMainThread(hit.getReference().getAccess().getThread())) {
            this.setLRU(hit.getReference().getSet(), hit.getWay());  //HT-MT inter-thread hit: Demote to LRU position; turn off HT bit
            stateProvider.ht = false;
        } else {
            super.handlePromotionOnHit(hit);  //Promote to MRU position
        }
    }

    //TODO: add HT quality sensitivity (inter-thread reuse distance) into policy selection, e.g., if the HT miss will not be reused by MT, insert it in LRU
    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT> miss) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.ht = false;

        if (miss.getReference().getAccessType().isDownwardRead() && this.isDelinquentPc(miss.getReference().getAccess().getThread().getId(), miss.getReference().getAccess().getVirtualPc())) {
            if (BasicThread.isMainThread(miss.getReference().getAccess().getThread())) {
                this.setLRU(miss.getReference().getSet(), miss.getWay()); // MT miss: insert in LRU position
            } else if (BasicThread.isHelperThread(miss.getReference().getAccess().getThread())) {
                this.setMRU(miss.getReference().getSet(), miss.getWay());  //HT miss: insert in MRU position; turn on HT bit
                stateProvider.ht = true;
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

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private boolean ht;

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
