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
package archimulator.uncore.net;

import archimulator.uncore.CacheHierarchy;

public class L2ToMemNet extends Net {
    public L2ToMemNet(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy);
    }

    @Override
    protected void setup(CacheHierarchy cacheHierarchy) {
        int memBlockSize = 64;

        this.switchNode = new SwitchNode(this,
                "l2ToMemSwitch",
                2,
                (memBlockSize + 8) * 2,
                2,
                (memBlockSize + 8) * 2, 8);

        EndPointNode l2CacheNode = new EndPointNode(this, cacheHierarchy.getL2Cache().getName());
        this.endPointNodes.put(cacheHierarchy.getL2Cache(), l2CacheNode);
        this.createBidirectionalLink(l2CacheNode, this.switchNode, 32);

        EndPointNode memNode = new EndPointNode(this, cacheHierarchy.getMainMemory().getName());
        this.endPointNodes.put(cacheHierarchy.getMainMemory(), memNode);
        this.createBidirectionalLink(memNode, this.switchNode, 32);
    }
}
