package archimulator.sim.uncore.cache.partitioning.cpiBased;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.core.Thread;
import archimulator.sim.core.event.InstructionCommittedEvent;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper;
import net.pickapack.action.Action1;
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
public class CPIBasedCachePartitioningHelper extends CachePartitioningHelper implements Reportable {
    private Map<Integer, Long> committedInstructions;

    /**
     * Create a CPI based cache partitioning helper.
     *
     * @param cache the cache
     */
    public CPIBasedCachePartitioningHelper(EvictableCache<?> cache) {
        super(cache);

        this.committedInstructions = new TreeMap<Integer, Long>();

        cache.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
            public void apply(InstructionCommittedEvent event) {
                Thread thread = event.getDynamicInstruction().getThread();

                if (!committedInstructions.containsKey(getThreadIdentifier(thread))) {
                    committedInstructions.put(getThreadIdentifier(thread), 0L);
                }

                committedInstructions.put(getThreadIdentifier(thread), committedInstructions.get(getThreadIdentifier(thread)) + 1);
            }
        });
    }

    @Override
    protected void newInterval() {
        List<Integer> partition = new ArrayList<Integer>();

        List<Double> cyclePerInstructions = new ArrayList<Double>();

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
            partition.add((int) Precision.round(cyclePerInstructions.get(threadId) * (this.getL2CacheController().getCache().getAssociativity() - this.getNumThreads()) / cyclePerInstructionSum, 0) + 1);
        }

        for(int i = 0; i < this.getL2CacheController().getCache().getNumSets(); i++) {
            this.setPartition(i, partition);
        }

        this.committedInstructions.clear();
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "cpiBasedCachePartitioningHelper") {{
            getChildren().add(new ReportNode(this, "partition", getPartition(0) + ""));
            getChildren().add(new ReportNode(this, "numIntervals", getNumIntervals() + ""));
        }});
    }
}
