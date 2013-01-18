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
package archimulator.sim.uncore.cache.replacement.helperThread;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;

/**
 * Helper thread interval aware least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class HelperThreadIntervalAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Create a helper thread interval aware LRU policy.
     *
     * @param cache the parent evictable cache
     */
    public HelperThreadIntervalAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (HelperThreadingHelper.isHelperThread(access.getThread().getId())) {
            int newStackPosition;

            switch (this.getCache().getSimulation().getFeedbackDirectedHelperThreadingHelper().getPollutionForInsertionPolicy()) {
                case HIGH:
                    newStackPosition = this.getCache().getAssociativity() - 1;
                    break;
                case MEDIUM:
                    newStackPosition = (int) ((double) (this.getCache().getAssociativity() - 1) * 3 / 4);
                    break;
                case LOW:
                    newStackPosition = (int) ((double) (this.getCache().getAssociativity() - 1) / 2) - 1;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            this.setStackPosition(set, way, newStackPosition);
        } else {
            super.handleInsertionOnMiss(access, set, way);
        }
    }
}
