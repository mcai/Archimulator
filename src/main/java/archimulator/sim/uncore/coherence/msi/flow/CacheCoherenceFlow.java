package archimulator.sim.uncore.coherence.msi.flow;

import archimulator.sim.uncore.coherence.msi.controller.Controller;
import net.pickapack.Params;
import net.pickapack.tree.Node;
import net.pickapack.tree.NodeHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class CacheCoherenceFlow extends Params implements Node {
    private long id;
    private Controller generator;
    private CacheCoherenceFlow producerFlow;
    private CacheCoherenceFlow ancestorFlow;
    private List<CacheCoherenceFlow> childFlows;
    private int numPendingDescendantFlows;
    private long beginCycle;
    private boolean completed;

    public CacheCoherenceFlow(Controller generator, CacheCoherenceFlow producerFlow) {
        this.id = currentId++;
        this.generator = generator;
        this.producerFlow = producerFlow;
        this.ancestorFlow = producerFlow == null ? this : producerFlow.ancestorFlow;
        this.childFlows = new ArrayList<CacheCoherenceFlow>();
        this.onCreate(this.generator.getCycleAccurateEventQueue().getCurrentCycle());
    }

    public static void dumpTree() {
        for (CacheCoherenceFlow pendingFlow : pendingFlows) {
            NodeHelper.print(pendingFlow);
            System.out.println();
        }

        System.out.println();
    }

    public void onCreate(long beginCycle) {
        this.beginCycle = beginCycle;
        if (this.producerFlow == null) {
            pendingFlows.add(this);
        } else {
            this.producerFlow.childFlows.add(this);
        }
        this.ancestorFlow.numPendingDescendantFlows++;
    }

    public void onCompleted() {
        this.completed = true;
        this.ancestorFlow.numPendingDescendantFlows--;

        if (this.ancestorFlow.numPendingDescendantFlows == 0) {
            pendingFlows.remove(this.ancestorFlow);
        }
    }

    public long getId() {
        return id;
    }

    public CacheCoherenceFlow getAncestorFlow() {
        return ancestorFlow;
    }

    public CacheCoherenceFlow getProducerFlow() {
        return producerFlow;
    }

    public List<CacheCoherenceFlow> getChildFlows() {
        return childFlows;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    public Object getGenerator() {
        return generator;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public Object getValue() {
        return this + (this.completed ? " -> completed" : "");
    }

    @Override
    public List<CacheCoherenceFlow> getChildren() {
        return this.childFlows;
    }

    private static long currentId = 0;
    private static List<CacheCoherenceFlow> pendingFlows = new ArrayList<CacheCoherenceFlow>();

    public static List<CacheCoherenceFlow> getPendingFlows() {
        return pendingFlows;
    }
}
