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
package archimulator.sim.uncore.cache.replacement.costAware;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;

import java.io.Serializable;

/**
 * Cost sensitive least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class CostSensitiveLRUPolicy<StateT extends Serializable> extends AbstractCostAwareLRUPolicy<StateT> {
    private double aCost;

    /**
     * Create a cost sensitive least recently used (LRU) policy.
     *
     * @param cache the parent evictable cache
     */
    public CostSensitiveLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param tag    the tag
     * @return the newly created cache access object
     */
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int lruWay = this.getLRU(set);

        for (int i = this.getCache().getAssociativity() - 2; i >= 0; i--) {
            int way = this.getWayInStackPosition(set, i);

            StateT state = this.getCache().getLine(set, way).getState();

            boolean stable = !(state instanceof DirectoryControllerState) || ((DirectoryControllerState) state).isStable();

            if(stable) {
                double cost = this.getCost(set, way);

                if(cost <= 0) {
                    throw new IllegalArgumentException(cost + "");
                }

                if (cost < aCost) {
                    aCost -= cost * 2;
                    new CacheAccess<>(this.getCache(), access, set, way, tag);
                }
            }
        }

        return new CacheAccess<>(this.getCache(), access, set, lruWay, tag);
    }

    @Override
    public void setStackPosition(int set, int way, int newStackPosition) {
        int oldLruWay = this.getLRU(set);

        super.setStackPosition(set, way, newStackPosition);

        int newLruWay = this.getLRU(set);

        if (this.getCache().getLine(set, newLruWay).isValid() && oldLruWay != newLruWay) {
            StateT state = this.getCache().getLine(set, newLruWay).getState();

            boolean stable = !(state instanceof DirectoryControllerState) || ((DirectoryControllerState) state).isStable();

            if(stable) {
                aCost = this.getCost(set, newLruWay);

                if (aCost <= 0) {
                    throw new IllegalArgumentException(aCost + "");
                }
            }
        }
    }
}
