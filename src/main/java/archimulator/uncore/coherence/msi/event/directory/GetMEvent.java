/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.util.action.Action;

/**
 * The event fired when the directory controller receives a "GetM" message.
 *
 * @author Min Cai
 */
public class GetMEvent extends DirectoryControllerEvent {
    private CacheController requester;
    private int set;
    private int way;
    private Action onStalledCallback;

    /**
     * Create an event when the directory controller receives a "GetM" message.
     *
     * @param generator         the generator directory controller
     * @param producerFlow      the producer cache coherence flow
     * @param requester         the requester L1 cache controller
     * @param tag               the tag
     * @param set               the set index
     * @param way               the way
     * @param onStalledCallback the callback action performed when the access is stalled
     * @param access            the memory hierarchy access
     */
    public GetMEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController requester, int tag, int set, int way, Action onStalledCallback, MemoryHierarchyAccess access) {
        super(generator, producerFlow, DirectoryControllerEventType.GETM, access, tag);
        this.requester = requester;
        this.set = set;
        this.way = way;
        this.onStalledCallback = onStalledCallback;
    }

    /**
     * Get the requester L1 cache controller.
     *
     * @return the requester L1 cache controller
     */
    public CacheController getRequester() {
        return requester;
    }

    /**
     * Get the set index.
     *
     * @return the set index
     */
    public int getSet() {
        return set;
    }

    /**
     * Get the way.
     *
     * @return the way
     */
    public int getWay() {
        return way;
    }

    /**
     * Get the callback action performed when the access is stalled.
     *
     * @return the callback action performed when the access is stalled
     */
    public Action getOnStalledCallback() {
        return onStalledCallback;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: GetMEvent{id=%d, requester=%s, tag=0x%08x, set=%d, way=%d}", getBeginCycle(), getGenerator(), getId(), requester, getTag(), set, way);
    }
}
