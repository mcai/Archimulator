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
package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * "GetS" message.
 *
 * @author Min Cai
 */
public class GetSMessage extends CoherenceMessage {
    private CacheController requester;

    /**
     * Create a "GetS" message.
     *
     * @param generator    the generator controller
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     * @param access       the memory hierarchy access
     */
    public GetSMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController requester, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.GETS, access, tag);
        this.requester = requester;
    }

    /**
     * Get the requester L1 cache controller.
     *
     * @return the requester L1 cache controller
     */
    public CacheController getRequester() {
        return requester;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: GetSMessage{id=%d, requester=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), requester, getTag());
    }
}
