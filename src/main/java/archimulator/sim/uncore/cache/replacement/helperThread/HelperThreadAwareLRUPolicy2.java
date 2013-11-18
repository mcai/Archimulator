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

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;

/**
 * Helper thread aware least recently used (LRU) policy 2.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class HelperThreadAwareLRUPolicy2<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Create a helper thread aware least recently used (LRU) policy 2 for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public HelperThreadAwareLRUPolicy2(EvictableCache<StateT> cache) {
        super(cache);
    }

    /**
     * Handle promotion on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsMainThread(access) && lineFoundIsHelperThread(set, way)) {
            this.setLRU(set, way);
            return;
        }

        super.handlePromotionOnHit(access, set, way);
    }

    /**
     * Handle insertion on a cache miss.
     *
     * @param access the memory hierarchy access
     * @param set    the set
     * @param way    the way
     */
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsHelperThread(access) && !isUseful(access.getVirtualPc())) {
            this.setLRU(set, way);
            return;
        }

        super.handleInsertionOnMiss(access, set, way);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Get a value indicating whether the requester of the specified memory hierarchy access is the main thread or not.
     *
     * @param access the memory hierarchy access
     * @return a value indicating whether the requester of the specified memory hierarchy access is the main thread or not
     */
    private boolean requesterIsMainThread(MemoryHierarchyAccess access) {
        return HelperThreadingHelper.isMainThread(access.getThread());
    }

    /**
     * Get a value indicating whether the requester of the specified memory hierarchy access is the helper thread or not.
     *
     * @param access the memory hierarchy access
     * @return a value indicating whether the requester of the specified memory hierarchy access is the helper thread or not
     */
    private boolean requesterIsHelperThread(MemoryHierarchyAccess access) {
        return HelperThreadingHelper.isHelperThread(access.getThread());
    }

    /**
     * Get a value indicating whether the line found in the specified set index and way is brought by the helper thread or not.
     *
     * @param set the set index
     * @param way the way
     * @return a value indicating whether the line found in the specified set index and way is brought by the helper thread or not
     */
    private boolean lineFoundIsHelperThread(int set, int way) {
        return HelperThreadingHelper.isHelperThread(this.getCache().getLine(set, way).getAccess().getThread());
    }

    /**
     * Get a value indicating whether prefetches coming from the specified PC address are predicted as useful or not.
     *
     * @param pc the PC address
     * @return a value indicating whether prefetches coming from the specified PC address are predicted as useful or not
     */
    private boolean isUseful(int pc) {
        return getCache().getSimulation().getHelperThreadL2CacheRequestProfilingHelper()
                .getHelperThreadL2CacheRequestUsefulnessPredictor().predict(pc);
    }
}
