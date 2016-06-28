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
package archimulator.uncore.cache.replacement.helperThread;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.CacheAccess;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.replacement.LRUPolicy;
import archimulator.uncore.helperThread.HelperThreadingHelper;
import archimulator.util.IntegerIntegerPair;

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
    private List<IntegerIntegerPair> predefinedDelinquentPcs;

    public ThrashingSensitiveHelperThreadAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.predefinedDelinquentPcs = new ArrayList<>();
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(2, 0x004014d8));
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(0, 0x00400a34));
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        if (access.getType().isRead() && this.isDelinquentPc(access.getThread().getId(), access.getVirtualPc()) && HelperThreadingHelper.isMainThread(access.getThread())) {
            return new CacheAccess<>(this.getCache(), access, set, -1, tag); //bypass
        }

        return new CacheAccess<>(this.getCache(), access, set, this.getLRU(set), tag);  //LRU victim selection
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType().isRead() && HelperThreadingHelper.isHelperThread(this.getCache().getLine(set, way).getAccess().getThread()) && HelperThreadingHelper.isMainThread(access.getThread())) {
            this.setLRU(set, way);  //HT-MT inter-thread hit: Demote to LRU position; turn off HT bit
        } else {
            super.handlePromotionOnHit(access, set, way);  //Promote to MRU position
        }
    }

    //TODO: add HT quality sensitivity (inter-thread reuse distance) into policy selection, e.g., if the HT miss will not be reused by MT, insert it in LRU
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType().isRead() && this.isDelinquentPc(access.getThread().getId(), access.getVirtualPc())) {
            if (HelperThreadingHelper.isMainThread(access.getThread())) {
                this.setLRU(set, way); // MT miss: insert in LRU position
            } else if (HelperThreadingHelper.isHelperThread(access.getThread())) {
                this.setMRU(set, way);  //HT miss: insert in MRU position; turn on HT bit
            } else {
                super.handleInsertionOnMiss(access, set, way); //insert in MRU position
            }
        } else {
            super.handleInsertionOnMiss(access, set, way); //insert in MRU position
        }
    }

    /**
     * Get a value indicating whether the specified PC address in the specified thread is delinquent or not.
     *
     * @param threadId the ID of the thread
     * @param pc the PC address
     * @return a value indicating whether the specified PC address in the specified thread is delinquent or not
     */
    private boolean isDelinquentPc(int threadId, int pc) {
        return this.predefinedDelinquentPcs.contains(new IntegerIntegerPair(threadId, pc));
//        return this.processor.getCapability(DelinquentLoadIdentificationHelper.class).isDelinquentPc(threadId, pc);
    }
}
