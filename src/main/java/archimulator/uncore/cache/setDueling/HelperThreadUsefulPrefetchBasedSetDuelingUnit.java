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
package archimulator.uncore.cache.setDueling;

import archimulator.uncore.cache.Cache;
import archimulator.uncore.helperThread.HelperThreadL2RequestProfilingHelper;

/**
 * Helper thread useful L2 prefetch request based Set dueling unit.
 *
 * @author Min Cai
 */
public class HelperThreadUsefulPrefetchBasedSetDuelingUnit extends SaturatingCounterBasedSetDuelingUnit {
    /**
     * Create a helper thread useful L2 prefetch request based set dueling unit.
     *
     * @param cache                          the parent cache
     * @param numSetDuelingMonitorsPerThread the number of set dueling monitors per thread
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public HelperThreadUsefulPrefetchBasedSetDuelingUnit(
            final Cache<?> cache,
            final int numSetDuelingMonitorsPerThread,
            int numSetsPerSetDuelingMonitor
    ) {
        super(cache, 1, numSetDuelingMonitorsPerThread, numSetsPerSetDuelingMonitor);

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.HelperThreadL2RequestEvent.class, event -> {
            if (event.getQuality().isUseful()) {
                inc(event.getSet(), 0);
            }
        });
    }
}
