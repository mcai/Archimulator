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
package archimulator.sim.uncore.cache.replacement.reuseDistance;

import archimulator.sim.uncore.cache.Cache;
import net.pickapack.math.MathHelper;
import net.pickapack.math.SaturatingCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dynamic insertion policy.
 *
 * @author Min Cai
 */
public class DynamicInsertionPolicy {
    private Cache<?> cache;
    private List<SaturatingCounter> policySelectionCounter;

    private boolean adaptive;

    private List<SetDuelingMonitor> setDuelingMonitors;

    private int numThreads;

    private int setDuelingMonitorSize;

    private int bimodalSuggestionThrottle;

    private Random random;

    public DynamicInsertionPolicy(Cache<?> cache, int numThreads, int policySelectionCounterMaxValue, int setDuelingMonitorSize) {
        this.cache = cache;

        this.numThreads = numThreads;
        this.setDuelingMonitorSize = setDuelingMonitorSize;

        this.bimodalSuggestionThrottle = 5;
        this.adaptive = true;

        this.random = new Random(13);

        int policySelectionCounterThreshold = (int) ((float) policySelectionCounterMaxValue * 0.5);

        this.policySelectionCounter = new ArrayList<SaturatingCounter>();

        for (int i1 = 0; i1 < this.numThreads; i1++) {
            this.policySelectionCounter.add(new SaturatingCounter(0, policySelectionCounterThreshold, policySelectionCounterThreshold, 0));
        }

        this.setDuelingMonitors = new ArrayList<SetDuelingMonitor>();

        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.setDuelingMonitors.add(new SetDuelingMonitor());
        }

        this.initSetDuelingMonitorsRandomly();
//        this.initSetDuelingMonitorsBasedOnSetIndexBits();
    }

    /* Randomly assign sets to SDMs */
    private void initSetDuelingMonitorsRandomly() {
        /* total SDM size of cache */
        int totalSdmSize = this.setDuelingMonitorSize * this.numThreads;
        /* Number of SDMs per thread */
        int totalSdmCount = 2;

        assert (this.cache.getNumSets() >= totalSdmSize * totalSdmCount);

        /* When using multiple cache banks, seeding is to ensure that all banks use the same sampled sets */
        for (int p = 0; p < totalSdmCount; p++) {
            /* Assign per-thread SDMs */
            int threadId = 0;
            int ownerSets = this.setDuelingMonitorSize;

            for (int ldr = 0; ldr < totalSdmSize; ldr++) {
                int set;
                do {
                    set = this.random.nextInt(this.cache.getNumSets());
                }
                while (this.setDuelingMonitors.get(set).type != SetDuelingMonitorType.FOLLOWERS);

                /* Set the Leader Set Type (NF or BF) */
                this.setDuelingMonitors.get(set).type = p == 0 ? SetDuelingMonitorType.LRU : SetDuelingMonitorType.BIP;
                /* Set the leader set owner thread id */
                this.setDuelingMonitors.get(set).ownerThreadId = threadId;

                ownerSets--;

                /* If owner sets has reached zero, move to next threadId */
                if (ownerSets == 0) {
                    threadId++;
                    ownerSets = this.setDuelingMonitorSize;
                }
            }
        }
    }

    /* Choose Leader Sets Based on bits 0-5 and 6-10 of the set index */
    private void initSetDuelingMonitorsBasedOnSetIndexBits() {
        for (int set = 0; set < this.cache.getNumSets(); set++) {
            /* Dedicate Per Thread SDMs. Can determine if it is my dedicated set or not */
            for (int threadId = 0; threadId < this.numThreads; threadId++) {
                SetDuelingMonitor setDuelingMonitor = this.setDuelingMonitors.get(set);
                int index = set - threadId - 1;

                if (((index >> 5) & 1) != 0 && index >= 0 && (MathHelper.bits(index, 11, 6) == MathHelper.bits(index, 5, 0))) {
                    /* Check to make sure this set isn't already assigned */
                    assert (setDuelingMonitor.type == SetDuelingMonitorType.FOLLOWERS);

                    setDuelingMonitor.type = SetDuelingMonitorType.LRU;
                    setDuelingMonitor.ownerThreadId = threadId;
                }

                index = set + threadId + 1;

                if (((index >> 5) & 1) == 0 && index <= 2047 && (MathHelper.bits(index, 11, 6) == MathHelper.bits(index, 5, 0))) {
                    // Check to make sure this set isn't already assigned
                    assert (setDuelingMonitor.type == SetDuelingMonitorType.FOLLOWERS);

                    setDuelingMonitor.type = SetDuelingMonitorType.BIP;
                    setDuelingMonitor.ownerThreadId = threadId;
                }
            }
        }
    }

    public boolean shouldDoNormalFill(int threadId, int set) {
        if (this.setDuelingMonitors.get(set).type == SetDuelingMonitorType.LRU && this.setDuelingMonitors.get(set).ownerThreadId == threadId) {
            /* Is it an SDM that does normal fill (NF) policy and is dedicated to this thread? */
            return true;
        } else if (this.setDuelingMonitors.get(set).type == SetDuelingMonitorType.BIP && this.setDuelingMonitors.get(set).ownerThreadId == threadId) {
            /* Is it an SDM that does bimodal fill (BF) policy and is dedicated to this thread? */
            return this.bimodalSuggestion(this.bimodalSuggestionThrottle);
        } else {
            /* it is a follower set */
            return !this.adaptive || !this.policySelectionCounter.get(threadId).isTaken() || this.bimodalSuggestion(this.bimodalSuggestionThrottle);
        }
    }

    public void recordMiss(int set) {
        SetDuelingMonitorType sdmType = this.setDuelingMonitors.get(set).type;

        if (sdmType != SetDuelingMonitorType.FOLLOWERS) {
            int owner = this.setDuelingMonitors.get(set).ownerThreadId;

            /* if it is an SDM that does NF policy increment PSEL */
            /* if it is an SDM that does BF policy decrement PSEL */
            this.policySelectionCounter.get(owner).update(sdmType == SetDuelingMonitorType.LRU);
        }
    }

    private boolean bimodalSuggestion(int throttle) {
        return this.random.nextInt(100) <= throttle;
    }

    private enum SetDuelingMonitorType {
        FOLLOWERS,
        LRU,
        BIP
    }

    private class SetDuelingMonitor {
        private SetDuelingMonitorType type;
        private int ownerThreadId;

        private SetDuelingMonitor() {
            this.type = SetDuelingMonitorType.FOLLOWERS;
        }
    }
}
