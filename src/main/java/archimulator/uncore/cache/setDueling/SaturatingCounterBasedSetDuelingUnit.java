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
import archimulator.util.math.SaturatingCounter;

import java.util.Comparator;

/**
 * Saturating counter based set dueling unit.
 *
 * @author Min Cai
 */
public abstract class SaturatingCounterBasedSetDuelingUnit extends AbstractSetDuelingUnit<SaturatingCounterBasedSetDuelingUnit.SetDuelingMonitor> {
    /**
     * Set dueling monitor.
     */
    public class SetDuelingMonitor extends AbstractSetDuelingUnit.SetDuelingMonitor {
        private SaturatingCounter counter;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor(int threadId, int policyId) {
            super(threadId, policyId);
            this.counter = new SaturatingCounter(0, 1, 1 << 10, 0);
        }
    }

    /**
     * Create a saturating counter based set dueling unit.
     *
     * @param cache                          the parent cache
     * @param numThreads                     the number of threads
     * @param numSetDuelingMonitorsPerThread the number of set dueling monitors per thread
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public SaturatingCounterBasedSetDuelingUnit(final Cache<?> cache, int numThreads, final int numSetDuelingMonitorsPerThread, int numSetsPerSetDuelingMonitor) {
        super(cache, numThreads, numSetDuelingMonitorsPerThread, numSetsPerSetDuelingMonitor);
    }

    @Override
    protected SetDuelingMonitor createSetDuelingMonitor(int threadId, int policyId) {
        return new SetDuelingMonitor(threadId, policyId);
    }

    @Override
    public int getBestPolicyId(int threadId) {
        return this.getSetDuelingMonitors(threadId).stream().min(
                Comparator.comparing(setDuelingMonitor -> setDuelingMonitor.counter.getValue())
        ).get().getPolicyId();
    }

    /**
     * Increment the specified set's corresponding set dueling monitor's saturating counter value.
     *
     * @param set the set index
     * @param threadId the thread ID
     */
    protected void inc(int set, int threadId) {
        int policyId = this.getBinding(set).getPolicyId();

        if (policyId != FOLLOWERS) {
            this.getSetDuelingMonitor(threadId, policyId).counter.update(true);

            if (this.getSetDuelingMonitors(threadId).stream().anyMatch(
                    setDuelingMonitor -> setDuelingMonitor.counter.getValue() == setDuelingMonitor.counter.getMaxValue()
            )) {
                this.getSetDuelingMonitors(threadId).forEach(setDuelingMonitor -> setDuelingMonitor.counter.reset());
            }
        }
    }
}
