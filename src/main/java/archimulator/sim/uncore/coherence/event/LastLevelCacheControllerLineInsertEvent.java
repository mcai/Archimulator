/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.coherence.event;

import archimulator.sim.common.SimulationEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;

/**
 * The event when the last level cache (LLC) controller has a line inserted.
 *
 * @author Min Cai
 */
public class LastLevelCacheControllerLineInsertEvent extends SimulationEvent {
    private GeneralCacheController cacheController;
    private MemoryHierarchyAccess access;
    private int tag;
    private int set;
    private int way;
    private int victimTag;

    /**
     * Create an event when the last level cache (LLC) controller has a line inserted.
     *
     * @param cacheController the cache controller
     * @param access          the access
     * @param tag             the tag
     * @param set             the set index
     * @param way             the way
     * @param victimTag       the victim tag
     */
    public LastLevelCacheControllerLineInsertEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, int tag, int set, int way, int victimTag) {
        super(cacheController);

        this.cacheController = cacheController;
        this.tag = tag;
        this.set = set;
        this.way = way;
        this.access = access;
        this.victimTag = victimTag;
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

    /**
     * Get the victim tag.
     *
     * @return the victim tag
     */
    public int getVictimTag() {
        return victimTag;
    }

    /**
     * Get a value indicating whether an eviction is needed or not.
     *
     * @return a value indicating whether an eviction is needed or not
     */
    public boolean isEviction() {
        return victimTag != CacheLine.INVALID_TAG;
    }

    @Override
    public String toString() {
        return String.format("LastLevelCacheControllerLineInsertEvent{access=%s, tag=0x%08x, set=%d, way=%d, accessType=%s}", access, tag, set, way, access.getType());
    }
}
