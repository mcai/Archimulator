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
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import net.pickapack.action.Action;

public class GetSEvent extends DirectoryControllerEvent {
    private CacheController requester;
    private int set;
    private int way;
    private Action onStalledCallback;

    public GetSEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController requester, int tag, int set, int way, Action onStalledCallback, MemoryHierarchyAccess access) {
        super(generator, producerFlow, DirectoryControllerEventType.GETS, access, tag);
        this.requester = requester;
        this.set = set;
        this.way = way;
        this.onStalledCallback = onStalledCallback;
    }

    public CacheController getRequester() {
        return requester;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public Action getOnStalledCallback() {
        return onStalledCallback;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: GetSEvent{id=%d, requester=%s, tag=0x%08x, set=%d, way=%d}", getBeginCycle(), getGenerator(), getId(), requester, getTag(), set, way);
    }
}