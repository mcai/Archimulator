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
package archimulator.sim.uncore.cache.partitioning.mlpAware;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.core.event.InstructionCommittedEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.LRUStack;
import archimulator.sim.uncore.cache.partitioning.MemoryLatencyMeter;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.mlp.PendingL2Hit;
import archimulator.sim.uncore.mlp.PendingL2Miss;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Function1;
import net.pickapack.util.Pair;

import java.util.*;

/**
 * Memory level parallelism (MLP) aware cache partitioning helper.
 *
 * @author Min Cai
 */
public class MLPAwareCachePartitioningHelper extends CachePartitioningHelper {
    private class PerSetDataEntry {
        private int set;
        private Map<Integer, MLPAwareStackDistanceProfile> mlpAwareStackDistanceProfiles;
        private Map<Integer, Map<Integer, LRUStack>> lruStacks;

        public PerSetDataEntry(int set) {
            this.set = set;
            this.mlpAwareStackDistanceProfiles = new LinkedHashMap<Integer, MLPAwareStackDistanceProfile>();
            this.lruStacks = new LinkedHashMap<Integer, Map<Integer, LRUStack>>();
        }

        /**
         * Get the map of MLP aware stack distance profiles.
         *
         * @return the map of MLP aware stack distance profiles
         */
        public Map<Integer, MLPAwareStackDistanceProfile> getMlpAwareStackDistanceProfiles() {
            return mlpAwareStackDistanceProfiles;
        }

        /**
         * Get the map of LRU stacks.
         *
         * @return the map of LRU stacks
         */
        public Map<Integer, Map<Integer, LRUStack>> getLruStacks() {
            return lruStacks;
        }
    }

    private Map<Integer, PendingL2Miss> pendingL2Misses;
    private Map<Integer, Map<Integer, PendingL2Hit>> pendingL2Hits;

    private L2CacheAccessMLPCostProfile l2CacheAccessMLPCostProfile;

    private MemoryLatencyMeter memoryLatencyMeter;

    private Function1<Double, Integer> mlpCostQuantizer;
    private List<List<Integer>> partitions;
    private Map<Integer, PerSetDataEntry> entries;

    /**
     * Create an MLP aware cache partitioning helper.
     *
     * @param cache the cache
     */
    public MLPAwareCachePartitioningHelper(EvictableCache<?> cache) {
        super(cache);

        this.pendingL2Misses = new LinkedHashMap<Integer, PendingL2Miss>();
        this.pendingL2Hits = new LinkedHashMap<Integer, Map<Integer, PendingL2Hit>>();

        this.l2CacheAccessMLPCostProfile = new L2CacheAccessMLPCostProfile(cache.getAssociativity());

        this.memoryLatencyMeter = new MemoryLatencyMeter();

        this.mlpCostQuantizer = new Function1<Double, Integer>() {
            @Override
            public Integer apply(Double rawValue) {
                if (rawValue < 0) {
                    throw new IllegalArgumentException();
                }

                if (rawValue <= 42) {
                    return 0;
                } else if (rawValue <= 85) {
                    return 1;
                } else if (rawValue <= 128) {
                    return 2;
                } else if (rawValue <= 170) {
                    return 3;
                } else if (rawValue <= 213) {
                    return 4;
                } else if (rawValue <= 246) {
                    return 5;
                } else if (rawValue <= 300) {
                    return 6;
                } else {
                    return 7;
                }
            }
        };

        this.entries = new TreeMap<Integer, PerSetDataEntry>();
        for(int i = 0; i < cache.getNumSets(); i++) {
            this.entries.put(i, new PerSetDataEntry(i));
        }

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(getL2CacheController())) {
                    if (!event.isHitInCache()) {
                        profileBeginServicingL2CacheMiss(event.getAccess());
                    } else {
                        profileBeginServicingL2CacheHit(event.getAccess());
                    }
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, new Action1<LastLevelCacheControllerLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheControllerLineInsertEvent event) {
                if (event.getCacheController().equals(getL2CacheController())) {
                    profileEndServicingL2CacheMiss(event.getAccess());
                }
            }
        });

        cache.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                updateL2CacheAccessMlpCostsPerCycle();
                updateL2CacheHitElapsedCyclesPerCycle();
                freeInvalidL2CacheHitsPerCycle();

            }
        });

        cache.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
            @Override
            public void apply(InstructionCommittedEvent event) {
                if (pendingL2Hits.containsKey(getThreadIdentifier(event.getDynamicInstruction().getThread()))) {
                    for (PendingL2Hit pendingL2Hit : pendingL2Hits.get(getThreadIdentifier(event.getDynamicInstruction().getThread())).values()) {
                        pendingL2Hit.incrementNumCommittedInstructionsSinceAccess();
                    }
                }
            }
        });
    }

    /**
     * New interval.
     */
    @Override
    protected void newInterval() {
        for(int i = 0; i < this.getL2CacheController().getCache().getNumSets(); i++) {
            this.setPartition(i, this.getOptimalMlpCostSumAndPartition(i).getSecond());

            for(MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile : this.entries.get(i).mlpAwareStackDistanceProfiles.values()) {
                mlpAwareStackDistanceProfile.newInterval();
            }
        }
    }

    /**
     * To be invoked per cycle for updating MLP-costs for in-flight L2 cache accesses.
     */
    private void updateL2CacheAccessMlpCostsPerCycle() {
        for (PendingL2Miss pendingL2Miss : this.pendingL2Misses.values()) {
            pendingL2Miss.setMlpCost(pendingL2Miss.getMlpCost() + (double) 1 / this.l2CacheAccessMLPCostProfile.getN(pendingL2Miss.getStackDistance()));
        }

        for (Map<Integer, PendingL2Hit> pendingL2HitsPerThread : this.pendingL2Hits.values()) {
            for (PendingL2Hit pendingL2Hit : pendingL2HitsPerThread.values()) {
                pendingL2Hit.setMlpCost(pendingL2Hit.getMlpCost() + (double) 1 / this.l2CacheAccessMLPCostProfile.getN(pendingL2Hit.getStackDistance()));
            }
        }
    }

    /**
     * To be invoked per cycle for updating elapsed cycles for in-flight L2 cache hits.
     */
    private void updateL2CacheHitElapsedCyclesPerCycle() {
        for (Map<Integer, PendingL2Hit> pendingL2HitsPerThread : this.pendingL2Hits.values()) {
            for (PendingL2Hit pendingL2Hit : pendingL2HitsPerThread.values()) {
                pendingL2Hit.incrementNumCyclesElapsedSinceAccess();
            }
        }
    }

    /**
     * To be invoked per cycle for freeing invalid in-flight L2 cache hits.
     */
    private void freeInvalidL2CacheHitsPerCycle() {
        for (Map<Integer, PendingL2Hit> pendingL2HitsPerThread : this.pendingL2Hits.values()) {
            List<Integer> tagsToFree = new ArrayList<Integer>();

            for (PendingL2Hit pendingL2Hit : pendingL2HitsPerThread.values()) {
                if (pendingL2Hit.getNumCommittedInstructionsSinceAccess() >= this.getL2CacheController().getExperiment().getArchitecture().getReorderBufferCapacity()
                        || pendingL2Hit.getNumCyclesElapsedSinceAccess() >= memoryLatencyMeter.getAverageLatency()) {
                    tagsToFree.add(pendingL2Hit.getAccess().getPhysicalTag());
                }
            }

            for (int tag : tagsToFree) {
                profileEndServicingL2CacheHit(pendingL2HitsPerThread.get(tag).getAccess());
            }
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache miss.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2CacheMiss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2CacheController().getCache().getSet(tag);

        LRUStack lruStack = getLruStack(set, getThreadIdentifier(access.getThread()));

        final int stackDistance = lruStack.access(tag);

        PendingL2Miss pendingL2Miss = new PendingL2Miss(access, getL2CacheController().getCycleAccurateEventQueue().getCurrentCycle()) {
            {
                setStackDistance(stackDistance);
            }
        };
        this.pendingL2Misses.put(tag, pendingL2Miss);

        this.l2CacheAccessMLPCostProfile.incrementCounter(stackDistance);
    }

    /**
     * Profile the end of servicing an L2 cache miss.
     *
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2CacheMiss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2CacheController().getCache().getSet(tag);

        PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
        pendingL2Miss.setEndCycle(this.getL2CacheController().getCycleAccurateEventQueue().getCurrentCycle());

        this.l2CacheAccessMLPCostProfile.decrementCounter(pendingL2Miss.getStackDistance());

        this.pendingL2Misses.remove(tag);

        this.memoryLatencyMeter.newSample(pendingL2Miss.getNumCycles());

        MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile = this.getMlpAwareStackDistanceProfile(set, getThreadIdentifier(access.getThread()));

        if (pendingL2Miss.getStackDistance() == -1) {
            mlpAwareStackDistanceProfile.incrementMissCounter(this.mlpCostQuantizer.apply(pendingL2Miss.getMlpCost()));
        } else {
            mlpAwareStackDistanceProfile.incrementHitCounter(pendingL2Miss.getStackDistance(), this.mlpCostQuantizer.apply(pendingL2Miss.getMlpCost()));
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache hit.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2CacheHit(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2CacheController().getCache().getSet(tag);

        LRUStack lruStack = getLruStack(set, getThreadIdentifier(access.getThread()));

        final int stackDistance = lruStack.access(tag);

        PendingL2Hit pendingL2Hit = new PendingL2Hit(access, getL2CacheController().getCycleAccurateEventQueue().getCurrentCycle()) {
            {
                setStackDistance(stackDistance);
            }
        };

        if (!this.pendingL2Hits.containsKey(getThreadIdentifier(access.getThread()))) {
            this.pendingL2Hits.put(getThreadIdentifier(access.getThread()), new LinkedHashMap<Integer, PendingL2Hit>());
        }

        this.pendingL2Hits.get(getThreadIdentifier(access.getThread())).put(tag, pendingL2Hit);

        this.l2CacheAccessMLPCostProfile.incrementCounter(stackDistance);
    }

    /**
     * Profile the end of servicing an L2 cache hit.
     *
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2CacheHit(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2CacheController().getCache().getSet(tag);

        PendingL2Hit pendingL2Hit = this.pendingL2Hits.get(getThreadIdentifier(access.getThread())).get(tag);
        pendingL2Hit.setEndCycle(this.getL2CacheController().getCycleAccurateEventQueue().getCurrentCycle());

        this.l2CacheAccessMLPCostProfile.decrementCounter(pendingL2Hit.getStackDistance());

        this.pendingL2Hits.get(getThreadIdentifier(access.getThread())).remove(tag);

        MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile = this.getMlpAwareStackDistanceProfile(set, getThreadIdentifier(access.getThread()));

        if (pendingL2Hit.getStackDistance() == -1) {
            mlpAwareStackDistanceProfile.incrementMissCounter(this.mlpCostQuantizer.apply(pendingL2Hit.getMlpCost()));
        } else {
            mlpAwareStackDistanceProfile.incrementHitCounter(pendingL2Hit.getStackDistance(), this.mlpCostQuantizer.apply(pendingL2Hit.getMlpCost()));
        }
    }

    /**
     * Get the LRU stack for the specified thread ID and set index in the L2 cache.
     *
     *
     * @param set      the set index
     * @param threadId the thread ID
     * @return the LRU stack for the specified thread ID and set index in the L2 cache
     */
    private LRUStack getLruStack(int set, int threadId) {
        if (!this.entries.get(set).lruStacks.containsKey(threadId)) {
            this.entries.get(set).lruStacks.put(threadId, new LinkedHashMap<Integer, LRUStack>());
        }

        if (!this.entries.get(set).lruStacks.get(threadId).containsKey(set)) {
            this.entries.get(set).lruStacks.get(threadId).put(set, new LRUStack(threadId, set, this.getL2CacheController().getCache().getAssociativity()));
        }

        return this.entries.get(set).lruStacks.get(threadId).get(set);
    }

    /**
     * Get the total MLP-cost for the specified thread ID and associativity in the specified set.
     *
     *
     * @param set           the set
     * @param threadId      the thread ID
     * @param associativity the associativity
     * @return the total MLP-cost for the specified thread ID and associativity in the specified set
     */
    public int getTotalMlpCost(int set, int threadId, int associativity) {
        if (associativity > this.getL2CacheController().getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }

        MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile = this.getMlpAwareStackDistanceProfile(set, threadId);

        int totalMlpCost = 0;

        for (int i = associativity - 1; i < this.getL2CacheController().getCache().getAssociativity(); i++) {
            totalMlpCost += mlpAwareStackDistanceProfile.getHitCounters().get(i);
        }

        totalMlpCost += mlpAwareStackDistanceProfile.getMissCounter();

        return totalMlpCost;
    }

    /**
     * Get the MLP-aware stack distance profile for the specified set and thread ID.
     *
     * @param set the set
     * @param threadId the thread ID
     * @return the MLP-aware stack distance profile for the specified set and thread ID
     */
    private MLPAwareStackDistanceProfile getMlpAwareStackDistanceProfile(int set, int threadId) {
        if (!this.entries.get(set).mlpAwareStackDistanceProfiles.containsKey(threadId)) {
            this.entries.get(set).mlpAwareStackDistanceProfiles.put(threadId, new MLPAwareStackDistanceProfile(this.getL2CacheController().getCache().getAssociativity()));
        }

        return this.entries.get(set).mlpAwareStackDistanceProfiles.get(threadId);
    }

    /**
     * Get the minimal sum of MLP-cost and its associated optimal partition for the specified set.
     *
     * @param set the set
     * @return the minimal sum of MLP-cost and its associated optimal partition for the specified set
     */
    private Pair<Integer, List<Integer>> getOptimalMlpCostSumAndPartition(int set) {
        if (this.partitions == null) {
            this.partitions = partition(this.getL2CacheController().getCache().getAssociativity(), this.getNumThreads());
        }

        int minMlpCostSum = Integer.MAX_VALUE;
        List<Integer> minPartition = null;

        for (List<Integer> partition : this.partitions) {
            int sum = 0;

            for (int i = 0; i < partition.size(); i++) {
                sum += this.getTotalMlpCost(set, i, partition.get(i));
            }

            if (sum < minMlpCostSum) {
                minMlpCostSum = sum;
                minPartition = partition;
            }
        }

        return new Pair<Integer, List<Integer>>(minMlpCostSum, minPartition);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "mlpAwareCachePartitioningHelper") {{
            getChildren().add(new ReportNode(this, "partition", getPartition(0) + ""));
            getChildren().add(new ReportNode(this, "numIntervals", getNumIntervals() + ""));
            getChildren().add(new ReportNode(this, "l2CacheAccessMLPCostProfile/hitCounters", getL2CacheAccessMLPCostProfile().getHitCounters() + ""));
            getChildren().add(new ReportNode(this, "l2CacheAccessMLPCostProfile/missCounter", getL2CacheAccessMLPCostProfile().getMissCounter() + ""));
            getChildren().add(new ReportNode(this, "memoryLatencyMeter/averageLatency", getMemoryLatencyMeter().getAverageLatency() + ""));
        }});
    }

    /**
     * Get the L2 cache access MLP-cost profile.
     *
     * @return the L2 cache access MLP-cost profile
     */
    public L2CacheAccessMLPCostProfile getL2CacheAccessMLPCostProfile() {
        return l2CacheAccessMLPCostProfile;
    }

    /**
     * Get the memory latency meter.
     *
     * @return the memory latency meter
     */
    public MemoryLatencyMeter getMemoryLatencyMeter() {
        return memoryLatencyMeter;
    }
}
