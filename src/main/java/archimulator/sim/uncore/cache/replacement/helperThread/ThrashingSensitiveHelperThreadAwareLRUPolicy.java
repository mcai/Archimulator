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
package archimulator.sim.uncore.cache.replacement.helperThread;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;
import net.pickapack.IntegerIntegerPair;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//TODO: to be refactored out

/**
 * Thrashing sensitive helper thread aware least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class ThrashingSensitiveHelperThreadAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    private List<IntegerIntegerPair> predefinedDelinquentPcs;

    public ThrashingSensitiveHelperThreadAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, getCache().getName() + ".thrashingSensitiveHelperThreadAwareLRUPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });

        this.predefinedDelinquentPcs = new ArrayList<IntegerIntegerPair>();
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(2, 0x004014d8));
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(0, 0x00400a34));
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        if (access.getType().isRead() && this.isDelinquentPc(access.getThread().getId(), access.getVirtualPc()) && HelperThreadingHelper.isMainThread(access.getThread())) {
            return new CacheAccess<StateT>(this.getCache(), access, set, -1, tag); //bypass
        }

        return new CacheAccess<StateT>(this.getCache(), access, set, this.getLRU(set), tag);  //LRU victim selection
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

        if (access.getType().isRead() && stateProvider.helperThread && HelperThreadingHelper.isMainThread(access.getThread())) {
            this.setLRU(set, way);  //HT-MT inter-thread hit: Demote to LRU position; turn off HT bit
            stateProvider.helperThread = false;
        } else {
            super.handlePromotionOnHit(access, set, way);  //Promote to MRU position
        }
    }

    //TODO: add HT quality sensitivity (inter-thread reuse distance) into policy selection, e.g., if the HT miss will not be reused by MT, insert it in LRU
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

        stateProvider.helperThread = false;

        if (access.getType().isRead() && this.isDelinquentPc(access.getThread().getId(), access.getVirtualPc())) {
            if (HelperThreadingHelper.isMainThread(access.getThread())) {
                this.setLRU(set, way); // MT miss: insert in LRU position
            } else if (HelperThreadingHelper.isHelperThread(access.getThread())) {
                this.setMRU(set, way);  //HT miss: insert in MRU position; turn on HT bit
                stateProvider.helperThread = true;
            } else {
                super.handleInsertionOnMiss(access, set, way); //insert in MRU position
            }
        } else {
            super.handleInsertionOnMiss(access, set, way); //insert in MRU position
        }
    }

    private boolean isDelinquentPc(int threadId, int pc) {
        return this.predefinedDelinquentPcs.contains(new IntegerIntegerPair(threadId, pc));
//        return this.processor.getCapability(DelinquentLoadIdentificationHelper.class).isDelinquentPc(threadId, pc);
    }

    private static class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private boolean helperThread;

        private BooleanValueProvider() {
            state = true;
        }

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
