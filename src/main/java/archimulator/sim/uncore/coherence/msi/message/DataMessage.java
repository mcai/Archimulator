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
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class DataMessage extends CoherenceMessage {
    private Controller sender;
    private int numInvalidationAcknowledgements;

    public DataMessage(Controller generator, CacheCoherenceFlow producerFlow, Controller sender, int tag, int numInvalidationAcknowledgements, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.DATA, access, tag);
        this.sender = sender;
        this.numInvalidationAcknowledgements = numInvalidationAcknowledgements;
    }

    public Controller getSender() {
        return sender;
    }

    public int getNumInvalidationAcknowledgements() {
        return numInvalidationAcknowledgements;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: DataMessage{%d, sender=%s, tag=0x%08x, numInvalidationAcknowledgements=%d}", getBeginCycle(), getGenerator(), getId(), sender, getTag(), numInvalidationAcknowledgements);
    }
}
