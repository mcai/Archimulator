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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.EvictableCache;
import net.pickapack.fsm.FiniteStateMachineFactory;

import java.io.Serializable;

/**
 *
 * @author Min Cai
 * @param <StateT>
 */
public abstract class GeneralCacheController<StateT extends Serializable, ConditionT> extends Controller {
    private long numDownwardReadHits;
    private long numDownwardReadMisses;
    private long numDownwardWriteHits;
    private long numDownwardWriteMisses;

    private long numEvictions;

    /**
     *
     * @param cacheHierarchy
     * @param name
     */
    public GeneralCacheController(CacheHierarchy cacheHierarchy, String name) {
        super(cacheHierarchy, name);
    }

    /**
     *
     * @return
     */
    public abstract EvictableCache<StateT> getCache();

    /**
     *
     * @param cache
     * @param read
     * @param hitInCache
     */
    public void updateStats(EvictableCache<?> cache, boolean read, boolean hitInCache) {
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

    /**
     *
     */
    public void incrementNumEvictions() {
        this.numEvictions++;
    }

    /**
     *
     * @return
     */
    public double getHitRatio() {
        return getNumDownwardAccesses() > 0 ? (double) getNumDownwardHits() / (getNumDownwardAccesses()) : 0.0;
    }

    /**
     *
     * @return
     */
    public long getNumDownwardHits() {
        return numDownwardReadHits + numDownwardWriteHits;
    }

    /**
     *
     * @return
     */
    public long getNumDownwardMisses() {
        return numDownwardReadMisses + numDownwardWriteMisses;
    }

    /**
     *
     * @return
     */
    public long getNumDownwardAccesses() {
        return getNumDownwardHits() + getNumDownwardMisses();
    }

    /**
     *
     * @return
     */
    public long getNumDownwardReadHits() {
        return numDownwardReadHits;
    }

    /**
     *
     * @return
     */
    public long getNumDownwardReadMisses() {
        return numDownwardReadMisses;
    }

    /**
     *
     * @return
     */
    public long getNumDownwardWriteHits() {
        return numDownwardWriteHits;
    }

    /**
     *
     * @return
     */
    public long getNumDownwardWriteMisses() {
        return numDownwardWriteMisses;
    }

    /**
     *
     * @return
     */
    public long getNumEvictions() {
        return numEvictions;
    }

    /**
     *
     * @return
     */
    public double getOccupancyRatio() {
        return getCache().getOccupancyRatio();
    }

    /**
     *
     * @return
     */
    public abstract CacheGeometry getGeometry();

    /**
     *
     * @return
     */
    public abstract FiniteStateMachineFactory<StateT, ConditionT, ?> getFsmFactory();
}
