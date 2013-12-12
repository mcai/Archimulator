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
package archimulator.sim.uncore.cache.partitioning.minMiss;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.LRUStack;
import archimulator.sim.uncore.cache.stackDistanceProfile.StackDistanceProfile;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import net.pickapack.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Min-miss cache partitioning helper.
 *
 * @author Min Cai
 */
public class MinMissCachePartitioningHelper extends CachePartitioningHelper {
    private Map<Integer, StackDistanceProfile> stackDistanceProfiles;

    private Map<Integer, Map<Integer, LRUStack>> lruStacks;

    private List<List<Integer>> partitions;

    /**
     * Create a min-miss cache partitioning helper.
     *
     * @param cache the cache
     */
    public MinMissCachePartitioningHelper(EvictableCache<?> cache) {
        super(cache);

        this.stackDistanceProfiles = new LinkedHashMap<>();

        this.lruStacks = new LinkedHashMap<>();

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController() == getL2Controller() && shouldInclude(event.getSet())) {
                profileStackDistance(event.getAccess());
            }
        });
    }

    /**
     * New interval.
     */
    @Override
    protected void newInterval() {
        this.setPartition(this.getMinMissSumAndPartition().getSecond());

        for (StackDistanceProfile stackDistanceProfile : this.stackDistanceProfiles.values()) {
            stackDistanceProfile.newInterval();
        }
    }

    /**
     * Profile the stack distance for an access.
     *
     * @param access the memory hierarchy access
     */
    private void profileStackDistance(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.getL2Controller().getCache().getSet(tag);
        int threadId = getThreadIdentifier(access.getThread());

        LRUStack lruStack = getLruStack(threadId, set);

        int stackDistance = lruStack.access(tag);

        if (stackDistance == -1) {
            this.getStackDistanceProfile(threadId).incrementMissCounter();
        } else {
            this.getStackDistanceProfile(threadId).incrementHitCounter(stackDistance);
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
     * Get the total number of misses for the specified thread ID and associativity.
     *
     * @param threadId      the thread ID
     * @param associativity the associativity
     * @return the total number of misses for the specified thread ID and associativity
     */
    public int getTotalMisses(int threadId, int associativity) {
        if (associativity > getL2Controller().getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }

        StackDistanceProfile stackDistanceProfile = this.getStackDistanceProfile(threadId);

        int totalMisses = 0;

        for (int i = associativity - 1; i < getL2Controller().getCache().getAssociativity(); i++) {
            totalMisses += stackDistanceProfile.getHitCounters().get(i);
        }

        totalMisses += stackDistanceProfile.getMissCounter();

        return totalMisses;
    }

    /**
     * Get the stack distance profile for the specified thread ID.
     *
     * @param threadId the thread ID
     * @return the stack distance profile for the specified thread ID
     */
    private StackDistanceProfile getStackDistanceProfile(int threadId) {
        if (!this.stackDistanceProfiles.containsKey(threadId)) {
            this.stackDistanceProfiles.put(threadId, new StackDistanceProfile(getL2Controller().getCache().getAssociativity()));
        }

        return this.stackDistanceProfiles.get(threadId);
    }

    /**
     * Get the minimal sum of misses and its associated optimal partition.
     *
     * @return the minimal sum of misses and its associated optimal partition
     */
    private Pair<Integer, List<Integer>> getMinMissSumAndPartition() {
        if (partitions == null) {
            partitions = partition(getL2Controller().getCache().getAssociativity(), getNumThreads());
        }

        int minMissSum = Integer.MAX_VALUE;
        List<Integer> minPartition = null;

        for (List<Integer> partition : partitions) {
            int sum = 0;

            for (int i = 0; i < partition.size(); i++) {
                sum += this.getTotalMisses(i, partition.get(i));
            }

            if (sum < minMissSum) {
                minMissSum = sum;
                minPartition = partition;
            }
        }

        return new Pair<>(minMissSum, minPartition);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "minMissCachePartitioningHelper") {{
            getChildren().add(new ReportNode(this, "partition", getPartition() + ""));
            getChildren().add(new ReportNode(this, "numIntervals", getNumIntervals() + ""));
        }});
    }
}
