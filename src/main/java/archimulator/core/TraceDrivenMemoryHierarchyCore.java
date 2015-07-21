package archimulator.core;

import archimulator.common.report.ReportNode;

public class TraceDrivenMemoryHierarchyCore extends AbstractMemoryHierarchyCore {
    /**
     * Create an abstract memory hierarchy core.
     *
     * @param processor the parent processor
     * @param num       the number of the core
     */
    public TraceDrivenMemoryHierarchyCore(Processor processor, int num) {
        super(processor, num);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }
}
