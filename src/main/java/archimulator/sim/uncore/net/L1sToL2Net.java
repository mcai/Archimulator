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
package archimulator.sim.uncore.net;

import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;

/**
 * The net for connecting the L1 cache controllers to the L2 cache controller
 *
 * @author Min Cai
 */
public class L1sToL2Net extends Net {
    /**
     * Create a net for connecting the L1 cache controllers to the L2 cache controller.
     *
     * @param memoryHierarchy the memory hierarchy
     */
    public L1sToL2Net(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);
    }

    /**
     * Setup the net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    @Override
    protected void setup(MemoryHierarchy memoryHierarchy) {
        int l2CacheLineSize = 64;

        this.switchNode = new SwitchNode(this,
                "l1sToL2Switch",
                memoryHierarchy.getL1ICacheControllers().size() + memoryHierarchy.getL1DCacheControllers().size() + 1,
                (l2CacheLineSize + 8) * 2,
                memoryHierarchy.getL1ICacheControllers().size() + memoryHierarchy.getL1DCacheControllers().size() + 1,
                (l2CacheLineSize + 8) * 2, 8);

        for (CacheController l1ICacheController : memoryHierarchy.getL1ICacheControllers()) {
            EndPointNode l1ICacheControllerNode = new EndPointNode(this, l1ICacheController.getName());
            this.endPointNodes.put(l1ICacheController, l1ICacheControllerNode);
            this.createBidirectionalLink(l1ICacheControllerNode, this.switchNode, 32);
        }

        for (CacheController l1DCacheController : memoryHierarchy.getL1DCacheControllers()) {
            EndPointNode l1DCacheControllerNode = new EndPointNode(this, l1DCacheController.getName());
            this.endPointNodes.put(l1DCacheController, l1DCacheControllerNode);
            this.createBidirectionalLink(l1DCacheControllerNode, this.switchNode, 32);
        }

        EndPointNode l2CacheControllerNode = new EndPointNode(this, memoryHierarchy.getL2CacheController().getName());
        this.endPointNodes.put(memoryHierarchy.getL2CacheController(), l2CacheControllerNode);
        this.createBidirectionalLink(l2CacheControllerNode, this.switchNode, 32);
    }

    /**
     * Get the name of the net.
     *
     * @return the name of the net
     */
    @Override
    public String getName() {
        return "l1sToL2Net";
    }
}
