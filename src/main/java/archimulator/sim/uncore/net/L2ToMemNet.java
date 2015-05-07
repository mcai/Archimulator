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
package archimulator.sim.uncore.net;

import archimulator.sim.uncore.MemoryHierarchy;

/**
 * The net for connecting the L2 cache controller to the memory controller.
 *
 * @author Min Cai
 */
public class L2ToMemNet extends Net {
    /**
     * Create a net for connecting the L2 cache controller to the memory controller.
     *
     * @param memoryHierarchy the memory hierarchy
     */
    public L2ToMemNet(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);
    }

    /**
     * Setup the net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    @Override
    protected void setup(MemoryHierarchy memoryHierarchy) {
        int memBlockSize = 64;

        this.switchNode = new SwitchNode(this,
                "l2ToMemSwitch",
                2,
                (memBlockSize + 8) * 2,
                2,
                (memBlockSize + 8) * 2, 8);

        EndPointNode l2ControllerNode = new EndPointNode(this, memoryHierarchy.getL2Controller().getName());
        this.endPointNodes.put(memoryHierarchy.getL2Controller(), l2ControllerNode);
        this.createBidirectionalLink(l2ControllerNode, this.switchNode, 32);

        EndPointNode memoryControllerNode = new EndPointNode(this, memoryHierarchy.getMemoryController().getName());
        this.endPointNodes.put(memoryHierarchy.getMemoryController(), memoryControllerNode);
        this.createBidirectionalLink(memoryControllerNode, this.switchNode, 32);
    }

    /**
     * Get the name of the net.
     *
     * @return the name of the net
     */
    @Override
    public String getName() {
        return "l2ToMemNet";
    }
}
