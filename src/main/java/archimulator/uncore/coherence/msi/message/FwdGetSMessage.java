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
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * "Forwarded GetS" message.
 *
 * @author Min Cai
 */
public class FwdGetSMessage extends CoherenceMessage {
    private CacheController requester;

    /**
     * Create a "forwarded GetS" message.
     *
     * @param generator    the generator controller
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     * @param access       the memory hierarchy access
     */
    public FwdGetSMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController requester, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.FWD_GETS, access, tag);
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
        return String.format("[%d] %s: FwdGetSMessage{id=%d, requester=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), requester, getTag());
    }
}
