/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.net.simple;

import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.net.simple.node.EndPointNode;
import archimulator.uncore.net.simple.node.SwitchNode;

/**
 * The net for connecting the L2 cache controller to the memory controller.
 *
 * @author Min Cai
 */
public class L2ToMemNet extends SimpleNet {
    /**
     * Create a net for connecting the L2 cache controller to the memory controller.
     *
     * @param memoryHierarchy the memory hierarchy
     */
    public L2ToMemNet(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy, "l2ToMemNet");
    }

    /**
     * Setup the net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    @Override
    protected void setup(MemoryHierarchy memoryHierarchy) {
        int memBlockSize = 64;

        this.setSwitchNode(new SwitchNode(this,
                "l2ToMemSwitch",
                2,
                (memBlockSize + 8) * 2,
                2,
                (memBlockSize + 8) * 2, 8));

        EndPointNode l2ControllerNode = new EndPointNode(this, memoryHierarchy.getL2Controller().getName());
        this.getEndPointNodes().put(memoryHierarchy.getL2Controller(), l2ControllerNode);
        this.createBidirectionalLink(l2ControllerNode, this.getSwitchNode(), 32);

        EndPointNode memoryControllerNode = new EndPointNode(this, memoryHierarchy.getMemoryController().getName());
        this.getEndPointNodes().put(memoryHierarchy.getMemoryController(), memoryControllerNode);
        this.createBidirectionalLink(memoryControllerNode, this.getSwitchNode(), 32);
    }
}
