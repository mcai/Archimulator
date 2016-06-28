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
package archimulator.uncore.coherence.msi.event.cache;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * The event fired when an L1 cache controller receives a "data from the directory controller where there is no pending acknowledgement expected" message.
 *
 * @author Min Cai
 */
public class DataFromDirAcksEq0Event extends CacheControllerEvent {
    private Controller sender;

    /**
     * Create an event when a L1 cache controller receives a "data from the directory controller where there is no pending acknowledgement expected" message.
     *
     * @param generator    the generator L1 cache controller
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender controller
     * @param tag          the tag
     * @param access       the memory hierarchy access
     */
    public DataFromDirAcksEq0Event(CacheController generator, CacheCoherenceFlow producerFlow, Controller sender, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.DATA_FROM_DIR_ACKS_EQ_0, access, tag);
        this.sender = sender;
    }

    /**
     * Get the sender controller.
     *
     * @return the sender controller
     */
    public Controller getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: DataFromDirAcksEq0Event{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, getTag());
    }
}
