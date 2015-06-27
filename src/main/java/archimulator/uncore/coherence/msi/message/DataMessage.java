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
package archimulator.uncore.coherence.msi.message;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * "Data" message.
 *
 * @author Min Cai
 */
public class DataMessage extends CoherenceMessage {
    private Controller sender;
    private int numInvAcks;

    /**
     * Create a "data" message.
     *
     * @param generator    the generator controller
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender controller
     * @param tag          the tag
     * @param numInvAcks   the number of pending invalidation acknowledgements expected
     * @param access       the memory hierarchy access
     */
    public DataMessage(Controller generator, CacheCoherenceFlow producerFlow, Controller sender, int tag, int numInvAcks, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.DATA, access, tag);
        this.sender = sender;
        this.numInvAcks = numInvAcks;
    }

    /**
     * Get the sender controller.
     *
     * @return the sender controller
     */
    public Controller getSender() {
        return sender;
    }

    /**
     * Get the number of pending invalidation acknowledgements expected.
     *
     * @return the number of pending invalidation acknowledgements expected
     */
    public int getNumInvAcks() {
        return numInvAcks;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: DataMessage{%d, sender=%s, tag=0x%08x, numInvAcks=%d}", getBeginCycle(), getGenerator(), getId(), sender, getTag(), numInvAcks);
    }
}
