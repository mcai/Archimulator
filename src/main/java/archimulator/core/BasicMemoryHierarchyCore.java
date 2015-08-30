package archimulator.core;

import archimulator.common.report.ReportNode;
import archimulator.util.action.Action;

/**
 * Basic memory hierarchy core.
 *
 * @author Min Cai
 */
public class BasicMemoryHierarchyCore extends AbstractMemoryHierarchyCore {
    /**
     * Create a basic memory hierarchy core.
     *
     * @param processor the parent processor
     * @param num       the number of the core
     */
    public BasicMemoryHierarchyCore(Processor processor, int num) {
        super(processor, num);
    }

    @Override
    public boolean canIfetch(MemoryHierarchyThread thread, int virtualAddress) {
        //TODO
        return false;
    }

    @Override
    public boolean canLoad(MemoryHierarchyThread thread, int virtualAddress) {
        //TODO
        return false;
    }

    @Override
    public boolean canStore(MemoryHierarchyThread thread, int virtualAddress) {
        //TODO
        return false;
    }

    @Override
    public void ifetch(MemoryHierarchyThread thread, int virtualAddress, int virtualPc, Action onCompletedCallback) {
        //TODO

    }

    @Override
    public void load(MemoryHierarchyDynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, Action onCompletedCallback) {
        //TODO

    }

    @Override
    public void store(MemoryHierarchyDynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, Action onCompletedCallback) {
        //TODO

    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }
}
