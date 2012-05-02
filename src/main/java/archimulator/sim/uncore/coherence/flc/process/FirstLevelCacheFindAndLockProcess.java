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
package archimulator.sim.uncore.coherence.flc.process;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.process.FindAndLockProcess;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class FirstLevelCacheFindAndLockProcess extends FindAndLockProcess {
    public FirstLevelCacheFindAndLockProcess(FirstLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(MemoryHierarchyAccess access, final Action onCompletedCallback) {
        getPendingActions().push(new EvictProcess(getCache(), access, cacheAccess).addOnCompletedCallback(new Action1<EvictProcess>() {
            public void apply(EvictProcess evictProcess) {
                onCompletedCallback.apply();
            }
        }));
    }

    @Override
    public FirstLevelCache getCache() {
        return (FirstLevelCache) super.getCache();
    }
}
