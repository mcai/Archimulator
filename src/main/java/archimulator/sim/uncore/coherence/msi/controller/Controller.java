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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;

/**
 * Controller.
 *
 * @author Min Cai
 */
public abstract class Controller extends MemoryDevice {
    private MemoryDevice next;

    /**
     * Create a controller.
     *
     * @param memoryHierarchy the parent memory hierarchy
     * @param name            the name
     */
    public Controller(MemoryHierarchy memoryHierarchy, String name) {
        super(memoryHierarchy, name);
    }

    /**
     * Receive a coherence message.
     *
     * @param message the coherence message
     */
    public abstract void receive(CoherenceMessage message);

    /**
     * Transfer the specified message to the destination controller.
     *
     * @param to      the destination controller
     * @param size    the size of the message
     * @param message the message
     */
    public void transfer(Controller to, int size, CoherenceMessage message) {
        this.getMemoryHierarchy().transfer(this, to, size, message);
    }

    /**
     * Get the next level memory device.
     *
     * @return the next level memory device
     */
    public MemoryDevice getNext() {
        return next;
    }

    /**
     * Set the next level memory device.
     *
     * @param next the next level memory device
     */
    public void setNext(MemoryDevice next) {
        this.next = next;
    }

    /**
     * Get the hit latency in cycles.
     *
     * @return the hit latency in cycles
     */
    public abstract int getHitLatency();

    /**
     * Get the cache replacement policy type.
     *
     * @return the  cache replacement policy type
     */
    public abstract CacheReplacementPolicyType getReplacementPolicyType();
}
