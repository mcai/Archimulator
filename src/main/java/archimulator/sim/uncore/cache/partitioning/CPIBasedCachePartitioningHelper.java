package archimulator.sim.uncore.cache.partitioning;

import archimulator.sim.common.Simulation;
import archimulator.sim.core.Thread;
import archimulator.sim.core.event.InstructionCommittedEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action1;

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

    public CPIBasedCachePartitioningHelper(Simulation simulation) {
        this(simulation.getProcessor().getMemoryHierarchy().getL2CacheController());
    }

    public CPIBasedCachePartitioningHelper(DirectoryController l2CacheController) {
        super(l2CacheController);

        this.committedInstructions = new TreeMap<Integer, Long>();

        l2CacheController.getBlockingEventDispatcher().addListener(InstructionCommittedEvent.class, new Action1<InstructionCommittedEvent>() {
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
            partition.add((int) (cyclePerInstructions.get(threadId) / cyclePerInstructionSum * (this.getL2CacheController().getCache().getAssociativity() - this.getNumThreads())) + 1);
        }

        this.setPartition(partition);
        this.committedInstructions.clear();
    }
}
