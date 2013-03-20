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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import net.pickapack.util.IntegerIntegerPair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//TODO: to be refactored out

/**
 * Test policy #1.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class TestPolicy1<StateT extends Serializable> extends LRUPolicy<StateT> {
    private List<IntegerIntegerPair> predefinedDelinquentPcs;

    /**
     * Create a test policy #1 for the specified cache.
     *
     * @param cache the parent cache
     */
    public TestPolicy1(EvictableCache<StateT> cache) {
        super(cache);

        this.predefinedDelinquentPcs = new ArrayList<IntegerIntegerPair>();
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(2, 0x004014d8));
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(0, 0x00400a34));
    }

//    @Override
//    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
//        if (BasicThread.isHelperThread(access.getThreadId()) && !isDelinquentLoad(access.getPc())) {
//            return new CacheMiss<>(getCache(), access, set, -1, tag);
//        } else {
//            return super.handleReplacement(reference);
//        }
//    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (this.isDelinquentPc(access.getThread().getId(), access.getVirtualPc())) {
            this.setMRU(set, way);
        } else {
            this.setStackPosition(set, way, Math.max(this.getStackPosition(set, way) - 1, 0));
        }
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (this.isDelinquentPc(access.getThread().getId(), access.getVirtualPc())) {
            this.setMRU(set, way);
        } else {
//            this.setLRU(set, way);
            this.setStackPosition(set, way, 4);
        }
    }

    /**
     * Get a value indicating whether the specified program counter (PC) is delinquent or not for the specified thread.
     *
     * @param threadId the ID of the thread
     * @param pc       the value of the program counter (PC)
     * @return a value indicating whether the specified program counter (PC) is delinquent or not for the specified thread
     */
    private boolean isDelinquentPc(int threadId, int pc) {
        return this.predefinedDelinquentPcs.contains(new IntegerIntegerPair(threadId, pc));
//        return this.processor.getCapability(DelinquentLoadIdentificationHelper.class ).isDelinquentPc(threadId, pc);
    }
}
