/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.coherence.msi.message;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * Cache coherence message.
 *
 * @author Min Cai
 */
public abstract class CoherenceMessage extends CacheCoherenceFlow {
    private CoherenceMessageType type;
    private boolean destinationArrived;

    /**
     * Create a cache coherence message.
     *
     * @param generator    the generator controller
     * @param producerFlow the producer cache coherence flow
     * @param type         the type of the cache coherence message
     * @param access       the memory hierarchy access
     * @param tag          the tag
     */
    public CoherenceMessage(Controller generator, CacheCoherenceFlow producerFlow, CoherenceMessageType type, MemoryHierarchyAccess access, int tag) {
        super(generator, producerFlow, access, tag);
        this.type = type;
    }

    /**
     * Act on when the cache coherence message has arrived at the destination.
     */
    public void onDestinationArrived() {
        this.destinationArrived = true;
    }

    /**
     * Get the type of the cache coherence message.
     *
     * @return the type of the cache coherence message
     */
    public CoherenceMessageType getType() {
        return type;
    }

    /**
     * Get a value indicating whether the message has arrived at the destination.
     *
     * @return a value indicating whether the message has arrived at the destination
     */
    public boolean isDestinationArrived() {
        return destinationArrived;
    }
}
