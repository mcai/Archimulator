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
package archimulator.sim.uncore.coherence.msi.event.directory;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class LastRecallAcknowledgementEvent extends DirectoryControllerEvent {
    public LastRecallAcknowledgementEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, DirectoryControllerEventType.LAST_RECALL_ACKNOWLEDGEMENT, access, tag);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: LastRecallAcknowledgementEvent{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), getTag());
    }
}
