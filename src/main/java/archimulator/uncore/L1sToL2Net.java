/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore;

import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.net.common.Net;
import archimulator.uncore.net.node.EndPointNode;
import archimulator.uncore.net.node.SwitchNode;

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
        super(memoryHierarchy, "l1sToL2Net");
    }

    /**
     * Setup the net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    @Override
    protected void setup(MemoryHierarchy memoryHierarchy) {
        int l2LineSize = 64;

        this.setSwitchNode(new SwitchNode(this,
                "l1sToL2Switch",
                memoryHierarchy.getL1IControllers().size() + memoryHierarchy.getL1DControllers().size() + 1,
                (l2LineSize + 8) * 2,
                memoryHierarchy.getL1IControllers().size() + memoryHierarchy.getL1DControllers().size() + 1,
                (l2LineSize + 8) * 2, 8));

        for (CacheController l1IController : memoryHierarchy.getL1IControllers()) {
            EndPointNode l1IControllerNode = new EndPointNode(this, l1IController.getName());
            this.getEndPointNodes().put(l1IController, l1IControllerNode);
            this.createBidirectionalLink(l1IControllerNode, this.getSwitchNode(), 32);
        }

        for (CacheController l1DController : memoryHierarchy.getL1DControllers()) {
            EndPointNode l1DControllerNode = new EndPointNode(this, l1DController.getName());
            this.getEndPointNodes().put(l1DController, l1DControllerNode);
            this.createBidirectionalLink(l1DControllerNode, this.getSwitchNode(), 32);
        }

        EndPointNode l2ControllerNode = new EndPointNode(this, memoryHierarchy.getL2Controller().getName());
        this.getEndPointNodes().put(memoryHierarchy.getL2Controller(), l2ControllerNode);
        this.createBidirectionalLink(l2ControllerNode, this.getSwitchNode(), 32);
    }
}
