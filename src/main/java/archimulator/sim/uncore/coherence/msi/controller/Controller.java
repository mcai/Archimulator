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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;

/**
 *
 * @author Min Cai
 */
public abstract class Controller extends MemoryDevice {
    private MemoryDevice next;

    /**
     *
     * @param cacheHierarchy
     * @param name
     */
    public Controller(CacheHierarchy cacheHierarchy, String name) {
        super(cacheHierarchy, name);
    }

    /**
     *
     * @param message
     */
    public abstract void receive(CoherenceMessage message);

    /**
     *
     * @param to
     * @param size
     * @param message
     */
    public void transfer(Controller to, int size, CoherenceMessage message) {
        this.getCacheHierarchy().transfer(this, to, size, message);
    }

    /**
     *
     * @return
     */
    public MemoryDevice getNext() {
        return next;
    }

    /**
     *
     * @param next
     */
    public void setNext(MemoryDevice next) {
        this.next = next;
    }

    /**
     *
     * @return
     */
    public abstract int getHitLatency();

    /**
     *
     * @return
     */
    public abstract CacheReplacementPolicyType getReplacementPolicyType();
}
