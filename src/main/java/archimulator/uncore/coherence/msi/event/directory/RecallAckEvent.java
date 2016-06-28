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
package archimulator.uncore.coherence.msi.event.directory;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.controller.DirectoryController;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * The event fired when the directory controller receives a "recall acknowledgement" message.
 *
 * @author Min Cai
 */
public class RecallAckEvent extends DirectoryControllerEvent {
    private CacheController sender;

    /**
     * Create an event when the directory controller receives a "recall acknowledgement" message.
     *
     * @param generator    the generator directory controller
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender L1 cache controller
     * @param tag          the tag
     * @param access       the memory hierarchy access
     */
    public RecallAckEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController sender, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, DirectoryControllerEventType.RECALL_ACK, access, tag);
        this.sender = sender;
    }

    /**
     * Get the sender L1 cache controller.
     *
     * @return the sender L1 cache controller
     */
    public CacheController getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: RecallAckEvent{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, getTag());
    }
}
