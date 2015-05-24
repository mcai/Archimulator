/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.coherence.event;

import archimulator.common.SimulationEvent;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.GeneralCacheController;

/**
 * The event fired when a general cache controller receives a "last PutS" or "PutM and data from the owner" message.
 *
 * @author Min Cai
 */
public class GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent extends SimulationEvent {
    private GeneralCacheController cacheController;
    private MemoryHierarchyAccess access;
    private int tag;
    private int set;
    private int way;

    /**
     * Create an event when a general cache controller receives a "last PutS" or "PutM and data from the owner" message.
     *
     * @param cacheController the cache controller
     * @param access          the memory hierarchy access
     * @param tag             the tag
     * @param set             the set index
     * @param way             the way
     */
    public GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, int tag, int set, int way) {
        super(cacheController);

        this.cacheController = cacheController;
        this.access = access;
        this.tag = tag;
        this.set = set;
        this.way = way;
    }

    /**
     * Get the cache controller.
     *
     * @return the cache controller
     */
    public GeneralCacheController getCacheController() {
        return cacheController;
    }

    /**
     * Get the memory hierarchy access.
     *
     * @return the memory hierarchy access
     */
    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    /**
     * Get the tag.
     *
     * @return the tag
     */
    public int getTag() {
        return tag;
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

    @Override
    public String toString() {
        return String.format("GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent{access=%s, tag=0x%08x, set=%d, way=%d}", access, tag, set, way);
    }
}
