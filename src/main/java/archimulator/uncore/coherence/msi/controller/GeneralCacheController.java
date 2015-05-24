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
package archimulator.uncore.coherence.msi.controller;

import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.cache.CacheGeometry;
import archimulator.uncore.cache.EvictableCache;
import archimulator.util.fsm.FiniteStateMachineFactory;

import java.io.Serializable;

/**
 * General cache controller.
 *
 * @param <StateT> state
 * @author Min Cai
 */
public abstract class GeneralCacheController<StateT extends Serializable, ConditionT> extends Controller implements Reportable {
    private long numDownwardReadHits;
    private long numDownwardReadMisses;
    private long numDownwardWriteHits;
    private long numDownwardWriteMisses;

    private long numEvictions;

    /**
     * Create a general cache controller.
     *
     * @param memoryHierarchy the memory hierarchy
     * @param name            the name
     */
    public GeneralCacheController(MemoryHierarchy memoryHierarchy, String name) {
        super(memoryHierarchy, name);
    }

    /**
     * Get the owned evictable cache.
     *
     * @return the owned evictable cache
     */
    public abstract EvictableCache<StateT> getCache();

    /**
     * Update statistics.
     *
     * @param read       a value indicating whether the access involved is a read or not
     * @param hitInCache a value indicating whether the access involved hits in the cache or not
     */
    public void updateStats(boolean read, boolean hitInCache) {
        if (read) {
            if (hitInCache) {
                numDownwardReadHits++;
            } else {
                numDownwardReadMisses++;
            }
        } else {
            if (hitInCache) {
                numDownwardWriteHits++;
            } else {
                numDownwardWriteMisses++;
            }
        }
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, getName()) {{
            getChildren().add(new ReportNode(this, "hitRatio", getHitRatio() + ""));
            getChildren().add(new ReportNode(this, "numDownwardAccesses", getNumDownwardAccesses() + ""));
            getChildren().add(new ReportNode(this, "numDownwardHits", getNumDownwardHits() + ""));
            getChildren().add(new ReportNode(this, "numDownwardMisses", getNumDownwardMisses() + ""));

            getChildren().add(new ReportNode(this, "numDownwardReadHits", getNumDownwardReadHits() + ""));
            getChildren().add(new ReportNode(this, "numDownwardReadMisses", getNumDownwardReadMisses() + ""));
            getChildren().add(new ReportNode(this, "numDownwardWriteHits", getNumDownwardWriteHits() + ""));
            getChildren().add(new ReportNode(this, "numDownwardWriteMisses", getNumDownwardWriteMisses() + ""));

            getChildren().add(new ReportNode(this, "numEvictions", getNumEvictions() + ""));

            getChildren().add(new ReportNode(this, "occupancyRatio", getOccupancyRatio() + ""));
        }});
    }

    /**
     * Increment the number of evictions.
     */
    public void incrementNumEvictions() {
        this.numEvictions++;
    }

    /**
     * Get the hit ratio.
     *
     * @return the hit ratio
     */
    public double getHitRatio() {
        return getNumDownwardAccesses() > 0 ? (double) getNumDownwardHits() / (getNumDownwardAccesses()) : 0.0;
    }

    /**
     * Get the number of downward hits.
     *
     * @return the number of downward hits
     */
    public long getNumDownwardHits() {
        return numDownwardReadHits + numDownwardWriteHits;
    }

    /**
     * Get the number of downward misses.
     *
     * @return the number of downward misses
     */
    public long getNumDownwardMisses() {
        return numDownwardReadMisses + numDownwardWriteMisses;
    }

    /**
     * Get the number of downward accesses.
     *
     * @return the number of downward accesses
     */
    public long getNumDownwardAccesses() {
        return getNumDownwardHits() + getNumDownwardMisses();
    }

    /**
     * Get the number of downward read hits.
     *
     * @return the number of downward read hits
     */
    public long getNumDownwardReadHits() {
        return numDownwardReadHits;
    }

    /**
     * Get the number of downward read misses.
     *
     * @return the number of downward read misses
     */
    public long getNumDownwardReadMisses() {
        return numDownwardReadMisses;
    }

    /**
     * Get the number of downward write hits.
     *
     * @return the number of downward write hits
     */
    public long getNumDownwardWriteHits() {
        return numDownwardWriteHits;
    }

    /**
     * Get the number of write misses.
     *
     * @return the number of write misses
     */
    public long getNumDownwardWriteMisses() {
        return numDownwardWriteMisses;
    }

    /**
     * Get the number of evictions.
     *
     * @return the number of evictions
     */
    public long getNumEvictions() {
        return numEvictions;
    }

    /**
     * Get the occupancy ratio.
     *
     * @return the occupancy ratio
     */
    public double getOccupancyRatio() {
        return getCache().getOccupancyRatio();
    }

    /**
     * Get the geometry of the owned evictable cache.
     *
     * @return the geometry of the owned evictable cache
     */
    public abstract CacheGeometry getGeometry();

    /**
     * Get the finite state machine factory.
     *
     * @return the finite state machine factory
     */
    public abstract FiniteStateMachineFactory<StateT, ConditionT, ?> getFsmFactory();
}
