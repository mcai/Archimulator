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
import archimulator.uncore.cache.CacheAccess;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.uncore.coherence.msi.state.CacheControllerState;

/**
 * The event fired when an L1 cache controller performs a line replacement.
 *
 * @author Min Cai
 */
public class ReplacementEvent extends CacheControllerEvent {
    private CacheAccess<CacheControllerState> cacheAccess;
    private int set;
    private int way;
    private Runnable onCompletedCallback;
    private Runnable onStalledCallback;

    /**
     * Create an event when an L1 cache controller performs a line replacement.
     *
     * @param generator           the generator L1 cache controller
     * @param producerFlow        the producer cache coherence flow
     * @param tag                 the tag
     * @param cacheAccess         the cache access
     * @param set                 the set index
     * @param way                 the way
     * @param onCompletedCallback the callback action performed when the line replacement is completed
     * @param onStalledCallback   the callback action performed when the line replacement is stalled
     * @param access              the memory hierarchy access
     */
    public ReplacementEvent(CacheController generator, CacheCoherenceFlow producerFlow, int tag, CacheAccess<CacheControllerState> cacheAccess, int set, int way, Runnable onCompletedCallback, Runnable onStalledCallback, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.REPLACEMENT, access, tag);
        this.cacheAccess = cacheAccess;
        this.set = set;
        this.way = way;
        this.onCompletedCallback = onCompletedCallback;
        this.onStalledCallback = onStalledCallback;
    }

    /**
     * Get the cache access.
     *
     * @return the cache access
     */
    public CacheAccess<CacheControllerState> getCacheAccess() {
        return cacheAccess;
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
     * Get the callback action performed when the line replacement is completed.
     *
     * @return the callback action performed when the line replacement is completed
     */
    public Runnable getOnCompletedCallback() {
        return onCompletedCallback;
    }

    /**
     * Get the callback action performed when the line replacement is stalled.
     *
     * @return the callback action performed when the line replacement is stalled
     */
    public Runnable getOnStalledCallback() {
        return onStalledCallback;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: ReplacementEvent{id=%d, tag=0x%08x, set=%d, way=%d}", getBeginCycle(), getGenerator(), getId(), getTag(), set, way);
    }
}
