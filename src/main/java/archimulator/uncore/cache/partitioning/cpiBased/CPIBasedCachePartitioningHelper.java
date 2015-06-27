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
package archimulator.uncore.cache.partitioning.cpiBased;

import archimulator.common.report.ReportNode;
import archimulator.core.Thread;
import archimulator.core.event.DynamicInstructionCommittedEvent;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.partitioning.CachePartitioningHelper;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CPI based cache partitioning helper.
 *
 * @author Min Cai
 */
public class CPIBasedCachePartitioningHelper extends CachePartitioningHelper {
    private Map<Integer, Long> committedInstructions;

    /**
     * Create a CPI based cache partitioning helper.
     *
     * @param cache the cache
     */
    public CPIBasedCachePartitioningHelper(EvictableCache<?> cache) {
        super(cache);

        this.committedInstructions = new TreeMap<>();

        cache.getBlockingEventDispatcher().addListener(DynamicInstructionCommittedEvent.class, event -> {
            Thread thread = event.getDynamicInstruction().getThread();

            if (!committedInstructions.containsKey(getThreadIdentifier(thread))) {
                committedInstructions.put(getThreadIdentifier(thread), 0L);
            }

            committedInstructions.put(getThreadIdentifier(thread), committedInstructions.get(getThreadIdentifier(thread)) + 1);
        });
    }

    @Override
    protected void newInterval() {
        List<Integer> partition = new ArrayList<>();

        List<Double> cyclePerInstructions = new ArrayList<>();

        for(int threadId = 0; threadId < this.getNumThreads(); threadId++) {
            if (!committedInstructions.containsKey(threadId)) {
                committedInstructions.put(threadId, 0L);
            }

            long numCommittedInstructions = this.committedInstructions.get(threadId);
            cyclePerInstructions.add((double) this.getNumCyclesElapsedPerInterval() / numCommittedInstructions);
        }

        double cyclePerInstructionSum = 0;
        for(double cyclePerInstruction : cyclePerInstructions) {
            cyclePerInstructionSum += cyclePerInstruction;
        }

        for(int threadId = 0; threadId < this.getNumThreads(); threadId++) {
            partition.add((int) Precision.round(cyclePerInstructions.get(threadId) * (this.getL2Controller().getCache().getAssociativity() - this.getNumThreads()) / cyclePerInstructionSum, 0) + 1);
        }

        this.setPartition(partition);

        this.committedInstructions.clear();
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "cpiBasedCachePartitioningHelper") {{
            getChildren().add(new ReportNode(this, "partition", getPartition() + ""));
            getChildren().add(new ReportNode(this, "numIntervals", getNumIntervals() + ""));
        }});
    }
}
