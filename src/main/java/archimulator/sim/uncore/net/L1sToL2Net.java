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
package archimulator.sim.uncore.net;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.coherence.FirstLevelCache;

public class L1sToL2Net extends Net {
    public L1sToL2Net(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy);
    }

    @Override
    protected void setup(CacheHierarchy cacheHierarchy) {
        int l2CacheLineSize = 64;

        this.switchNode = new SwitchNode(this,
                "l1sToL2Switch",
                cacheHierarchy.getInstructionCaches().size() + cacheHierarchy.getDataCaches().size() + 1,
                (l2CacheLineSize + 8) * 2,
                cacheHierarchy.getInstructionCaches().size() + cacheHierarchy.getDataCaches().size() + 1,
                (l2CacheLineSize + 8) * 2, 8);

        for (FirstLevelCache intructionCache : cacheHierarchy.getInstructionCaches()) {
            EndPointNode instructionCacheNode = new EndPointNode(this, intructionCache.getName());
            this.endPointNodes.put(intructionCache, instructionCacheNode);
            this.createBidirectionalLink(instructionCacheNode, this.switchNode, 32);
        }

        for (FirstLevelCache dataCache : cacheHierarchy.getDataCaches()) {
            EndPointNode dataCacheNode = new EndPointNode(this, dataCache.getName());
            this.endPointNodes.put(dataCache, dataCacheNode);
            this.createBidirectionalLink(dataCacheNode, this.switchNode, 32);
        }

        EndPointNode l2CacheNode = new EndPointNode(this, cacheHierarchy.getL2Cache().getName());
        this.endPointNodes.put(cacheHierarchy.getL2Cache(), l2CacheNode);
        this.createBidirectionalLink(l2CacheNode, this.switchNode, 32);
    }
}
