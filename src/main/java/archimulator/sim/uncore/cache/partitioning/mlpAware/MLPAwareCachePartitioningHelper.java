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
package archimulator.sim.uncore.cache.partitioning.mlpAware;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.core.event.DynamicInstructionCommittedEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.LRUStack;
import archimulator.sim.uncore.cache.partitioning.MemoryLatencyMeter;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.mlp.PendingL2Hit;
import archimulator.sim.uncore.mlp.PendingL2Miss;
import net.pickapack.action.Function1;
import net.pickapack.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Memory level parallelism (MLP) aware cache partitioning helper.
 *
 * @author Min Cai
 */
public class MLPAwareCachePartitioningHelper extends CachePartitioningHelper {
    private Map<Integer, PendingL2Miss> pendingL2Misses;
    private Map<Integer, Map<Integer, PendingL2Hit>> pendingL2Hits;

    private L2AccessMLPCostProfile l2AccessMLPCostProfile;

    private MemoryLatencyMeter memoryLatencyMeter;

    private Map<Integer, MLPAwareStackDistanceProfile> mlpAwareStackDistanceProfiles;

    private Map<Integer, Map<Integer, LRUStack>> lruStacks;

    private Function1<Double, Integer> mlpCostQuantizer;
    private List<List<Integer>> partitions;

    /**
     * Create an MLP aware cache partitioning helper.
     *
     * @param cache the cache
     */
    public MLPAwareCachePartitioningHelper(EvictableCache<?> cache) {
        super(cache);

        this.pendingL2Misses = new LinkedHashMap<>();
        this.pendingL2Hits = new LinkedHashMap<>();

        this.l2AccessMLPCostProfile = new L2AccessMLPCostProfile(cache.getAssociativity());

        this.memoryLatencyMeter = new MemoryLatencyMeter();

        this.mlpAwareStackDistanceProfiles = new LinkedHashMap<>();

        this.lruStacks = new LinkedHashMap<>();

        this.mlpCostQuantizer = rawValue -> {
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
        };

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController().equals(getL2Controller()) && shouldInclude(event.getSet())) {
                if (!event.isHitInCache()) {
                    profileBeginServicingL2Miss(event.getAccess());
                } else {
                    profileBeginServicingL2Hit(event.getAccess());
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, event -> {
            if (event.getCacheController().equals(getL2Controller()) && pendingL2Misses.containsKey(event.getAccess().getPhysicalTag())) {
                profileEndServicingL2Miss(event.getAccess());
            }
        });

        cache.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            updateL2AccessMlpCostsPerCycle();
            updateL2HitElapsedCyclesPerCycle();
            freeInvalidL2HitsPerCycle();

        });

        cache.getBlockingEventDispatcher().addListener(DynamicInstructionCommittedEvent.class, event -> {
            if (pendingL2Hits.containsKey(getThreadIdentifier(event.getDynamicInstruction().getThread()))) {
                for (PendingL2Hit pendingL2Hit : pendingL2Hits.get(getThreadIdentifier(event.getDynamicInstruction().getThread())).values()) {
                    pendingL2Hit.incrementNumCommittedInstructionsSinceAccess();
                }
            }
        });
    }

    /**
     * New interval.
     */
    @Override
    protected void newInterval() {
        this.setPartition(this.getOptimalMlpCostSumAndPartition().getSecond());

        for(MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile : this.mlpAwareStackDistanceProfiles.values()) {
            mlpAwareStackDistanceProfile.newInterval();
        }
    }

    /**
     * To be invoked per cycle for updating MLP-costs for in-flight L2 cache accesses.
     */
    private void updateL2AccessMlpCostsPerCycle() {
        for (PendingL2Miss pendingL2Miss : this.pendingL2Misses.values()) {
            pendingL2Miss.setMlpCost(pendingL2Miss.getMlpCost() + (double) 1 / this.l2AccessMLPCostProfile.getN(pendingL2Miss.getStackDistance()));
        }

        for (Map<Integer, PendingL2Hit> pendingL2HitsPerThread : this.pendingL2Hits.values()) {
            for (PendingL2Hit pendingL2Hit : pendingL2HitsPerThread.values()) {
                pendingL2Hit.setMlpCost(pendingL2Hit.getMlpCost() + (double) 1 / this.l2AccessMLPCostProfile.getN(pendingL2Hit.getStackDistance()));
            }
        }
    }

    /**
     * To be invoked per cycle for updating elapsed cycles for in-flight L2 cache hits.
     */
    private void updateL2HitElapsedCyclesPerCycle() {
        for (Map<Integer, PendingL2Hit> pendingL2HitsPerThread : this.pendingL2Hits.values()) {
            for (PendingL2Hit pendingL2Hit : pendingL2HitsPerThread.values()) {
                pendingL2Hit.incrementNumCyclesElapsedSinceAccess();
            }
        }
    }

    /**
     * To be invoked per cycle for freeing invalid in-flight L2 cache hits.
     */
    private void freeInvalidL2HitsPerCycle() {
        for (Map<Integer, PendingL2Hit> pendingL2HitsPerThread : this.pendingL2Hits.values()) {
            List<Integer> tagsToFree = new ArrayList<>();

            for (PendingL2Hit pendingL2Hit : pendingL2HitsPerThread.values()) {
                if (pendingL2Hit.getNumCommittedInstructionsSinceAccess() >= this.getL2Controller().getExperiment().getArchitecture().getReorderBufferCapacity()
                        || pendingL2Hit.getNumCyclesElapsedSinceAccess() >= memoryLatencyMeter.getAverageLatency()) {
                    tagsToFree.add(pendingL2Hit.getAccess().getPhysicalTag());
                }
            }

            for (int tag : tagsToFree) {
                profileEndServicingL2Hit(pendingL2HitsPerThread.get(tag).getAccess());
            }
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache miss.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2Miss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2Controller().getCache().getSet(tag);

        LRUStack lruStack = getLruStack(set, getThreadIdentifier(access.getThread()));

        final int stackDistance = lruStack.access(tag);

        PendingL2Miss pendingL2Miss = new PendingL2Miss(access, getL2Controller().getCycleAccurateEventQueue().getCurrentCycle()) {
            {
                setStackDistance(stackDistance);
            }
        };
        this.pendingL2Misses.put(tag, pendingL2Miss);

        this.l2AccessMLPCostProfile.incrementCounter(stackDistance);
    }

    /**
     * Profile the end of servicing an L2 cache miss.
     *
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2Miss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2Controller().getCache().getSet(tag);

        PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
        pendingL2Miss.setEndCycle(this.getL2Controller().getCycleAccurateEventQueue().getCurrentCycle());

        this.l2AccessMLPCostProfile.decrementCounter(pendingL2Miss.getStackDistance());

        this.pendingL2Misses.remove(tag);

        this.memoryLatencyMeter.newSample(pendingL2Miss.getNumCycles());

        MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile = this.getMlpAwareStackDistanceProfile(getThreadIdentifier(access.getThread()));

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
    private void profileBeginServicingL2Hit(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2Controller().getCache().getSet(tag);

        LRUStack lruStack = getLruStack(set, getThreadIdentifier(access.getThread()));

        final int stackDistance = lruStack.access(tag);

        PendingL2Hit pendingL2Hit = new PendingL2Hit(access, getL2Controller().getCycleAccurateEventQueue().getCurrentCycle()) {
            {
                setStackDistance(stackDistance);
            }
        };

        if (!this.pendingL2Hits.containsKey(getThreadIdentifier(access.getThread()))) {
            this.pendingL2Hits.put(getThreadIdentifier(access.getThread()), new LinkedHashMap<>());
        }

        this.pendingL2Hits.get(getThreadIdentifier(access.getThread())).put(tag, pendingL2Hit);

        this.l2AccessMLPCostProfile.incrementCounter(stackDistance);
    }

    /**
     * Profile the end of servicing an L2 cache hit.
     *
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2Hit(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Hit pendingL2Hit = this.pendingL2Hits.get(getThreadIdentifier(access.getThread())).get(tag);
        pendingL2Hit.setEndCycle(this.getL2Controller().getCycleAccurateEventQueue().getCurrentCycle());

        this.l2AccessMLPCostProfile.decrementCounter(pendingL2Hit.getStackDistance());

        this.pendingL2Hits.get(getThreadIdentifier(access.getThread())).remove(tag);

        MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile = this.getMlpAwareStackDistanceProfile(getThreadIdentifier(access.getThread()));

        if (pendingL2Hit.getStackDistance() == -1) {
            mlpAwareStackDistanceProfile.incrementMissCounter(this.mlpCostQuantizer.apply(pendingL2Hit.getMlpCost()));
        } else {
            mlpAwareStackDistanceProfile.incrementHitCounter(pendingL2Hit.getStackDistance(), this.mlpCostQuantizer.apply(pendingL2Hit.getMlpCost()));
        }
    }

    /**
     * Get the LRU stack for the specified thread ID and set index in the L2 cache.
     *
     * @param threadId the thread ID
     * @param set      the set index
     * @return the LRU stack for the specified thread ID and set index in the L2 cache
     */
    private LRUStack getLruStack(int threadId, int set) {
        if (!this.lruStacks.containsKey(threadId)) {
            this.lruStacks.put(threadId, new LinkedHashMap<>());
        }

        if (!this.lruStacks.get(threadId).containsKey(set)) {
            this.lruStacks.get(threadId).put(set, new LRUStack(threadId, set, this.getL2Controller().getCache().getAssociativity()));
        }

        return this.lruStacks.get(threadId).get(set);
    }

    /**
     * Get the total MLP-cost for the specified thread ID and associativity in the specified set.
     *
     * @param threadId      the thread ID
     * @param associativity the associativity
     * @return the total MLP-cost for the specified thread ID and associativity in the specified set
     */
    public int getTotalMlpCost(int threadId, int associativity) {
        if (associativity > this.getL2Controller().getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }

        MLPAwareStackDistanceProfile mlpAwareStackDistanceProfile = this.getMlpAwareStackDistanceProfile(threadId);

        int totalMlpCost = 0;

        for (int i = associativity - 1; i < this.getL2Controller().getCache().getAssociativity(); i++) {
            totalMlpCost += mlpAwareStackDistanceProfile.getHitCounters().get(i);
        }

        totalMlpCost += mlpAwareStackDistanceProfile.getMissCounter();

        return totalMlpCost;
    }

    /**
     * Get the MLP-aware stack distance profile for the specified thread ID.
     *
     * @param threadId the thread ID
     * @return the MLP-aware stack distance profile for the specified thread ID
     */
    private MLPAwareStackDistanceProfile getMlpAwareStackDistanceProfile(int threadId) {
        if (!this.mlpAwareStackDistanceProfiles.containsKey(threadId)) {
            this.mlpAwareStackDistanceProfiles.put(threadId, new MLPAwareStackDistanceProfile(this.getL2Controller().getCache().getAssociativity()));
        }

        return this.mlpAwareStackDistanceProfiles.get(threadId);
    }

    /**
     * Get the minimal sum of MLP-cost and its associated optimal partition.
     *
     * @return the minimal sum of MLP-cost and its associated optimal partition
     */
    private Pair<Integer, List<Integer>> getOptimalMlpCostSumAndPartition() {
        if (this.partitions == null) {
            this.partitions = partition(this.getL2Controller().getCache().getAssociativity(), this.getNumThreads());
        }

        int minMlpCostSum = Integer.MAX_VALUE;
        List<Integer> minPartition = null;

        for (List<Integer> partition : this.partitions) {
            int sum = 0;

            for (int i = 0; i < partition.size(); i++) {
                sum += this.getTotalMlpCost(i, partition.get(i));
            }

            if (sum < minMlpCostSum) {
                minMlpCostSum = sum;
                minPartition = partition;
            }
        }

        return new Pair<>(minMlpCostSum, minPartition);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "mlpAwareCachePartitioningHelper") {{
            getChildren().add(new ReportNode(this, "partition", getPartition() + ""));
            getChildren().add(new ReportNode(this, "numIntervals", getNumIntervals() + ""));
            getChildren().add(new ReportNode(this, "l2AccessMLPCostProfile/hitCounters", getL2AccessMLPCostProfile().getHitCounters() + ""));
            getChildren().add(new ReportNode(this, "l2AccessMLPCostProfile/missCounter", getL2AccessMLPCostProfile().getMissCounter() + ""));
            getChildren().add(new ReportNode(this, "memoryLatencyMeter/averageLatency", getMemoryLatencyMeter().getAverageLatency() + ""));
        }});
    }

    /**
     * Get the L2 cache access MLP-cost profile.
     *
     * @return the L2 cache access MLP-cost profile
     */
    public L2AccessMLPCostProfile getL2AccessMLPCostProfile() {
        return l2AccessMLPCostProfile;
    }

    /**
     * Get the memory latency meter.
     *
     * @return the memory latency meter
     */
    public MemoryLatencyMeter getMemoryLatencyMeter() {
        return memoryLatencyMeter;
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
