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
package archimulator.sim.uncore.cache.setDueling;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.util.IntervalCounter;

import java.util.Comparator;

/**
 * Helper thread aware set dueling unit.
 *
 * @author Min Cai
 */
public class HelperThreadAwareSetDuelingUnit extends AbstractSetDuelingUnit<HelperThreadAwareSetDuelingUnit.SetDuelingMonitor> {
    /**
     * Set dueling monitor.
     */
    public class SetDuelingMonitor extends AbstractSetDuelingUnit.SetDuelingMonitor {
        private IntervalCounter numGoodHelperThreadL2CacheRequests;
        private IntervalCounter numTotalHelperThreadL2CacheRequests;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor(int threadId, int policyId) {
            super(threadId, policyId);
            this.numGoodHelperThreadL2CacheRequests = new IntervalCounter();
            this.numTotalHelperThreadL2CacheRequests = new IntervalCounter();
        }
    }

    private int numEvictedL2CacheLinesPerInterval;

    private long numIntervals;
    private int numEvictedL2CacheLines;

    /**
     * Create a helper thread aware set dueling unit.
     *
     * @param cache                          the parent cache
     * @param numThreads                     the number of threads
     * @param numSetDuelingMonitorsPerThread the number of set dueling monitors per thread
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public HelperThreadAwareSetDuelingUnit(final Cache<?> cache, int numThreads, final int numSetDuelingMonitorsPerThread, int numSetsPerSetDuelingMonitor) {
        super(cache, numThreads, numSetDuelingMonitorsPerThread, numSetsPerSetDuelingMonitor);

        this.numEvictedL2CacheLinesPerInterval = cache.getNumSets() * cache.getAssociativity() / 2;

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheRequestEvent.class, event -> {
            int policyId = this.getBinding(event.getSet()).getPolicyId();
            if (policyId != FOLLOWERS) {
                SetDuelingMonitor setDuelingMonitor = this.getSetDuelingMonitor(event.getCoreId(), policyId);
                if (event.getQuality().isUseful()) {
                    setDuelingMonitor.numGoodHelperThreadL2CacheRequests.increment();
                }
                setDuelingMonitor.numTotalHelperThreadL2CacheRequests.increment();
            }
        });

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, event -> {
            if (event.getCacheController().getCache() == cache) {
                numEvictedL2CacheLines++;

                if (numEvictedL2CacheLines == numEvictedL2CacheLinesPerInterval) {
                    newInterval();

                    numEvictedL2CacheLines = 0;
                    numIntervals++;
                }
            }
        });
    }

    /**
     * New interval.
     */
    private void newInterval() {
        for(int i = 0; i < this.getNumThreads(); i++) {
            for(int j = 0; j < this.getNumSetDuelingMonitorsPerThread(); j++) {
                SetDuelingMonitor setDuelingMonitor = this.getSetDuelingMonitor(i, j);
                setDuelingMonitor.numGoodHelperThreadL2CacheRequests.newInterval();
                setDuelingMonitor.numTotalHelperThreadL2CacheRequests.newInterval();
            }
        }
    }

    @Override
    protected SetDuelingMonitor createSetDuelingMonitor(int threadId, int policyId) {
        return new SetDuelingMonitor(threadId, policyId);
    }

    @Override
    public int getBestPolicyId(int threadId) {
        return this.getSetDuelingMonitors(threadId).stream().max(
                Comparator.comparing(setDuelingMonitor -> setDuelingMonitor.numTotalHelperThreadL2CacheRequests.getValue() == 0 ? 0 : (double) setDuelingMonitor.numGoodHelperThreadL2CacheRequests.getValue() / setDuelingMonitor.numTotalHelperThreadL2CacheRequests.getValue())
        ).get().getPolicyId();
    }

    /**
     * Get the number of evicted L2 cache lines per interval.
     *
     * @return the number of evicted L2 cache lines per interval
     */
    public int getNumEvictedL2CacheLinesPerInterval() {
        return numEvictedL2CacheLinesPerInterval;
    }

    /**
     * Get the number of intervals.
     *
     * @return the number of intervals
     */
    public long getNumIntervals() {
        return numIntervals;
    }
}
