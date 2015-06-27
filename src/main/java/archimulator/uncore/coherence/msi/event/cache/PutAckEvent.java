/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.coherence.msi.event.cache;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * The event fired when an L1 cache controller receives a "put acknowledgement" message.
 *
 * @author Min Cai
 */
public class PutAckEvent extends CacheControllerEvent {
    /**
     * Crate an even when an L1 cache controller receives a "put acknowledgement" message.
     *
     * @param generator    the generator L1 cache controller
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     * @param access       the memory hierarchy access
     */
    public PutAckEvent(CacheController generator, CacheCoherenceFlow producerFlow, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.PUT_ACK, access, tag);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: PutAckEvent{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), getTag());
    }
}
