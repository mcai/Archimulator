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
package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.FindCacheLineResult;
import archimulator.sim.uncore.cache.FindCacheLineResultType;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.dram.MainMemory;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Function3;

import java.util.ArrayList;
import java.util.List;

public class LastLevelCache extends CoherentCache<LastLevelCacheLineState, LastLevelCacheLine> {
    public LastLevelCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config, new LockableCache<LastLevelCacheLineState, LastLevelCacheLine>(cacheHierarchy, name, config.getGeometry(), config.getEvictionPolicyClz(), new Function3<Cache<?, ?>, Integer, Integer, LastLevelCacheLine>() {
            public LastLevelCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new LastLevelCacheLine(cache, set, way, LastLevelCacheLineState.INVALID);
            }
        }));
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return to instanceof MainMemory ? this.getCacheHierarchy().getL2ToMemNetwork() : this.getCacheHierarchy().getL1sToL2Network();
    }

    public void setNext(MainMemory next) {
        super.setNext(next);
    }

    public MainMemory getNext() {
        return (MainMemory) super.getNext();
    }

    public boolean isShared(int tag) {
        return this.getSharers(tag).size() > 1;
    }

    public boolean isOwned(int tag) {
        return this.getSharers(tag).size() == 1;
    }

    public boolean isOwnedOrShared(int tag) {
        return this.getSharers(tag).size() > 0;
    }

    public FirstLevelCache getOwnerOrFirstSharer(int tag) {
        return this.getSharers(tag).get(0);
    }

    public List<FirstLevelCache> getSharers(int tag) {
        List<FirstLevelCache> sharers = new ArrayList<FirstLevelCache>();

        for(FirstLevelCache firstLevelCache : this.getCacheHierarchy().getDataCaches()) {
            FindCacheLineResult<FirstLevelCacheLine> findCacheLineResult = firstLevelCache.getCache().findLine(tag);
            if(findCacheLineResult.getType() == FindCacheLineResultType.CACHE_HIT) {
                sharers.add(firstLevelCache);
            }
        }

        return sharers;
    }
}
