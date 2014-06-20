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
package archimulator.sim.uncore.cache.replacement.costAware;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

/**
 * Cost based least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class CostBasedLRUPolicy<StateT extends Serializable> extends AbstractCostAwareLRUPolicy<StateT> {
    protected int lambda;

    /**
     * Create a cost based least recently used (LRU) policy.
     *
     * @param cache the parent evictable cache
     * @param lambda the lambda value
     */
    public CostBasedLRUPolicy(EvictableCache<StateT> cache, int lambda) {
        super(cache);
        this.lambda = lambda;
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int victimLinearSum = Integer.MAX_VALUE;
        int victimWay = 0;

        for(int way = 0; way < this.getCache().getAssociativity(); way++) {
            int recency = this.getCache().getAssociativity() - getStackPosition(set, way);
            int quantizedCost = this.getQuantizedCost(this.getCost(set, way));

            int linearSum = recency + this.lambda * quantizedCost;

            if (linearSum < victimLinearSum) {
                victimLinearSum = linearSum;
                victimWay = way;
            }
        }

        return new CacheAccess<>(this.getCache(), access, set, victimWay, tag);
    }
}
