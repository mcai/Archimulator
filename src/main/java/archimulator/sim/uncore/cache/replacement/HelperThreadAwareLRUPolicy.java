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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

public class HelperThreadAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    public HelperThreadAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType().isRead() && BasicThread.isMainThread(access.getThread())) {
            if(!access.getThread().getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled()) {
                super.handlePromotionOnHit(access, set, way);
                return;
            }

            if (BasicThread.isHelperThread(access.getThread().getSimulation().getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestStates().get(set).get(way).getThreadId())) {
                this.setLRU(set, way);  // HT-MT inter-thread hit, never used again: low locality => Demote to LRU position
            } else {
                super.handlePromotionOnHit(access, set, way);
            }
        } else {
            super.handlePromotionOnHit(access, set, way);
        }
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType().isRead() && BasicThread.isMainThread(access.getThread())) {
            this.setLRU(set, way); // MT miss, prevented from thrashing: low locality => insert in LRU position
        } else {
            super.handleInsertionOnMiss(access, set, way);
        }
    }
}
