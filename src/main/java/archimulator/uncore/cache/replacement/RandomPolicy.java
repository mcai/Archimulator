/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.CacheAccess;
import archimulator.uncore.cache.EvictableCache;

import java.io.Serializable;
import java.util.Random;

/**
 * Random policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class RandomPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> {
    private Random random;

    /**
     * Create a random policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public RandomPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.random = new Random(13);
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        return new CacheAccess<>(this.getCache(), access, set, this.random.nextInt(this.getCache().getAssociativity()), tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }
}
