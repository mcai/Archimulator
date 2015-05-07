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
package archimulator.sim.uncore.cache.setDueling;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.helperThread.HelperThreadL2RequestProfilingHelper;
import archimulator.util.IntervalCounter;

import java.util.Comparator;

/**
 * Helper thread prefetch accuracy based set dueling unit.
 *
 * @author Min Cai
 */
public class HelperThreadPrefetchAccuracyBasedSetDuelingUnit extends AbstractSetDuelingUnit<HelperThreadPrefetchAccuracyBasedSetDuelingUnit.SetDuelingMonitor> {
    /**
     * Set dueling monitor.
     */
    public class SetDuelingMonitor extends AbstractSetDuelingUnit.SetDuelingMonitor {
        private IntervalCounter numUsefulHelperThreadL2Requests;
        private IntervalCounter numTotalHelperThreadL2Requests;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor(int threadId, int policyId) {
            super(threadId, policyId);
            this.numUsefulHelperThreadL2Requests = new IntervalCounter();
            this.numTotalHelperThreadL2Requests = new IntervalCounter();
        }
    }

    private int numEvictedL2LinesPerInterval;

    private long numIntervals;
    private int numEvictedL2Lines;

    /**
     * Create a helper thread prefetch accuracy based set dueling unit.
     *
     * @param cache                          the parent cache
     * @param numSetDuelingMonitorsPerThread the number of set dueling monitors per thread
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public HelperThreadPrefetchAccuracyBasedSetDuelingUnit(final Cache<?> cache, final int numSetDuelingMonitorsPerThread, int numSetsPerSetDuelingMonitor) {
        super(cache, 1, numSetDuelingMonitorsPerThread, numSetsPerSetDuelingMonitor);

        this.numEvictedL2LinesPerInterval = cache.getNumSets() * cache.getAssociativity() / 2;

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.HelperThreadL2RequestEvent.class, event -> {
            int policyId = this.getBinding(event.getSet()).getPolicyId();
            if (policyId != FOLLOWERS) {
                SetDuelingMonitor setDuelingMonitor = this.getSetDuelingMonitor(0, policyId);
                if (event.getQuality().isUseful()) {
                    setDuelingMonitor.numUsefulHelperThreadL2Requests.increment();
                }
                setDuelingMonitor.numTotalHelperThreadL2Requests.increment();
            }
        });

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, event -> {
            if (event.getCacheController().getCache() == cache) {
                numEvictedL2Lines++;

                if (numEvictedL2Lines == numEvictedL2LinesPerInterval) {
                    newInterval();

                    numEvictedL2Lines = 0;
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
                setDuelingMonitor.numUsefulHelperThreadL2Requests.newInterval();
                setDuelingMonitor.numTotalHelperThreadL2Requests.newInterval();
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
                Comparator.comparing(setDuelingMonitor -> setDuelingMonitor.numTotalHelperThreadL2Requests.getValue() == 0 ? 0 : (double) setDuelingMonitor.numUsefulHelperThreadL2Requests.getValue() / setDuelingMonitor.numTotalHelperThreadL2Requests.getValue())
        ).get().getPolicyId();
    }

    /**
     * Get the number of evicted L2 cache lines per interval.
     *
     * @return the number of evicted L2 cache lines per interval
     */
    public int getNumEvictedL2LinesPerInterval() {
        return numEvictedL2LinesPerInterval;
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
