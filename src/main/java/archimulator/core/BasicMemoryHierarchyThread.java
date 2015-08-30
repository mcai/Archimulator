package archimulator.core;

import archimulator.common.BasicSimulationObject;
import archimulator.common.report.ReportNode;

/**
 * Basic memory hierarchy thread.
 *
 * @author Min Cai
 */
public class BasicMemoryHierarchyThread extends BasicSimulationObject implements MemoryHierarchyThread {
    /**
     * The number of the thread.
     */
    private int num;

    /**
     * The ID of the thread.
     */
    private int id;

    /**
     * The name of the thread.
     */
    private String name;

    /**
     * The parent core.
     */
    private MemoryHierarchyCore core;

    /**
     * Createa basic memory hierarchy thread.
     *
     * @param core the parent core
     * @param num the number of the thread
     */
    public BasicMemoryHierarchyThread(MemoryHierarchyCore core, int num) {
        super(core);

        this.core = core;

        this.num = num;
        this.id = this.core.getNum() * getExperiment().getNumThreadsPerCore() + this.num;

        this.name = "c" + this.core.getNum() + "t" + this.num;

    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MemoryHierarchyCore getCore() {
        return core;
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }
}
