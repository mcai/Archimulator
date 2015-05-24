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
package archimulator.uncore.cache.replacement.costAware.helperThread;

import archimulator.uncore.MemoryHierarchyAccessType;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.replacement.costAware.CostSensitiveLRUPolicy;
import archimulator.uncore.coherence.event.GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.uncore.helperThread.HelperThreadL2RequestQuality;
import archimulator.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;

/**
 * Helper thread sensitive least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class HelperThreadSensitiveLRUPolicy<StateT extends Serializable> extends CostSensitiveLRUPolicy<StateT> {
    /**
     * Create a helper thread sensitive least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public HelperThreadSensitiveLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        cache.getBlockingEventDispatcher().addListener(
                LastLevelCacheControllerLineInsertEvent.class,
                event -> {
                    if (event.getAccess().getType() == MemoryHierarchyAccessType.LOAD && HelperThreadingHelper.isHelperThread(event.getAccess().getThread())) {
                        int pc = getCache().getLine(event.getSet(), event.getWay()).getAccess().getVirtualPc();
                        HelperThreadL2RequestQuality quality = getCache().getSimulation().getHelperThreadL2RequestProfilingHelper().getHelperThreadL2RequestQualityPredictor().predict(pc);
                        setCost(event.getSet(), event.getWay(), getCost(quality));
                    }
                }
        );

        cache.getBlockingEventDispatcher().addListener(
                GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent.class,
                event -> {
                    if (event.getCacheController().getCache().equals(getCache())) {
                        setCost(event.getSet(), event.getWay(), 0);
                    }
                }
        );
    }

    @Override
    protected int getQuantizedCost(double cost) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the cost for the specified helper thread L2 cache request quality.
     *
     * @param quality the quality
     * @return the cost for the specified helper thread L2 cache request quality
     */
    public int getCost(HelperThreadL2RequestQuality quality) {
        switch (quality) {
            case TIMELY:
            case LATE:
            case EARLY:
                return 2;
            case UGLY:
                return 1;
            case REDUNDANT_HIT_TO_TRANSIENT_TAG:
            case REDUNDANT_HIT_TO_CACHE:
                return -2;
            case BAD:
                return -4;
            default:
                throw new IllegalArgumentException(quality + "");
        }
    }
}
