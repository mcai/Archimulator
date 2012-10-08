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
import archimulator.sim.uncore.coherence.msi.controller.CacheController;

/**
 *
 * @author Min Cai
 */
public class L1sToL2Net extends Net {
    /**
     *
     * @param cacheHierarchy
     */
    public L1sToL2Net(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy);
    }

    /**
     *
     * @param cacheHierarchy
     */
    @Override
    protected void setup(CacheHierarchy cacheHierarchy) {
        int l2CacheLineSize = 64;

        this.switchNode = new SwitchNode(this,
                "l1sToL2Switch",
                cacheHierarchy.getL1ICacheControllers().size() + cacheHierarchy.getL1DCacheControllers().size() + 1,
                (l2CacheLineSize + 8) * 2,
                cacheHierarchy.getL1ICacheControllers().size() + cacheHierarchy.getL1DCacheControllers().size() + 1,
                (l2CacheLineSize + 8) * 2, 8);

        for (CacheController l1ICacheController : cacheHierarchy.getL1ICacheControllers()) {
            EndPointNode l1ICacheControllerNode = new EndPointNode(this, l1ICacheController.getName());
            this.endPointNodes.put(l1ICacheController, l1ICacheControllerNode);
            this.createBidirectionalLink(l1ICacheControllerNode, this.switchNode, 32);
        }

        for (CacheController l1DCacheController : cacheHierarchy.getL1DCacheControllers()) {
            EndPointNode l1DCacheControllerNode = new EndPointNode(this, l1DCacheController.getName());
            this.endPointNodes.put(l1DCacheController, l1DCacheControllerNode);
            this.createBidirectionalLink(l1DCacheControllerNode, this.switchNode, 32);
        }

        EndPointNode l2CacheControllerNode = new EndPointNode(this, cacheHierarchy.getL2CacheController().getName());
        this.endPointNodes.put(cacheHierarchy.getL2CacheController(), l2CacheControllerNode);
        this.createBidirectionalLink(l2CacheControllerNode, this.switchNode, 32);
    }
}
