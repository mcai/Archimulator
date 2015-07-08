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
package archimulator.uncore;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationObject;
import archimulator.uncore.cache.MemoryDeviceType;
import archimulator.util.action.Action;

import java.io.Serializable;

/**
 * Memory device.
 *
 * @author Min Cai
 */
public abstract class MemoryDevice extends BasicSimulationObject implements SimulationObject, Serializable {
    private MemoryHierarchy memoryHierarchy;
    private String name;
    private MemoryDeviceType type;

    /**
     * Create a memory device.
     *
     * @param memoryHierarchy the parent memory hierarchy
     * @param name            the name of the memory device
     * @param type            the type of the memory device
     */
    public MemoryDevice(MemoryHierarchy memoryHierarchy, String name, MemoryDeviceType type) {
        super(memoryHierarchy);

        this.memoryHierarchy = memoryHierarchy;
        this.name = name;
        this.type = type;
    }

    /**
     * Transfer a message of the specified size from the device itself to the destination.
     *
     * @param to     the destination device
     * @param size   the size of the message to be transferred
     * @param action the callback action performed when the message arrives at the destination
     */
    public void transfer(MemoryDevice to, int size, Action action) {
        this.getMemoryHierarchy().getNet(this, to).transfer(this, to, size, action);
    }

    /**
     * Get the parent memory hierarchy.
     *
     * @return the parent memory hierarchy
     */
    public MemoryHierarchy getMemoryHierarchy() {
        return memoryHierarchy;
    }

    /**
     * Get the name of the memory device.
     *
     * @return the name of the memory device
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of the memory device.
     *
     * @return the type of the memory device
     */
    public MemoryDeviceType getType() {
        return type;
    }
}
