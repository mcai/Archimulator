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
package archimulator.uncore.coherence.msi.event.cache;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.util.action.Action;

/**
 * The event fired when an L1 cache controller receives a "load" memory hierarchy access.
 *
 * @author Min Cai
 */
public class LoadEvent extends CacheControllerEvent {
    private int set;
    private int way;
    private Action onCompletedCallback;
    private Action onStalledCallback;

    /**
     * Create an event when an L1 cache controller receives a "load" memory hierarchy access.
     *
     * @param generator           the generator L1 cache controller
     * @param producerFlow        the producer cache coherence flow
     * @param tag                 the tag
     * @param set                 the set index
     * @param way                 the way
     * @param onCompletedCallback the callback action performed when the access is completed
     * @param onStalledCallback   the callback action performed when the access is stalled
     * @param access              the memory hierarchy access
     */
    public LoadEvent(CacheController generator, CacheCoherenceFlow producerFlow, int tag, int set, int way, Action onCompletedCallback, Action onStalledCallback, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.LOAD, access, tag);

        this.set = set;
        this.way = way;
        this.onCompletedCallback = onCompletedCallback;
        this.onStalledCallback = onStalledCallback;
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
     * Get the callback action performed when the access is completed.
     *
     * @return the callback action performed when the access is completed
     */
    public Action getOnCompletedCallback() {
        return onCompletedCallback;
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
        return String.format("[%d] %s: LoadEvent{id=%d, tag=0x%08x, set=%d, way=%d}", getBeginCycle(), getGenerator(), getId(), getTag(), set, way);
    }
}
