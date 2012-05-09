package archimulator.sim.uncore.coherence.flow;

import archimulator.util.tree.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Flow {
    private long id;
    private Flow producerFlow;
    private long beginCycle;

    public Flow(Flow producerFlow) {
        this.producerFlow = producerFlow;
        this.id = currentId++;
    }

    public static void dumpTree() {
        Map<Flow, Node<Flow>> nodes = new LinkedHashMap<Flow, Node<Flow>>();

        for(Flow flow : getPendingFlows()) {
            if(flow.getProducerFlow() == null) {
                Node<Flow> node = new Node<Flow>(flow);
                nodes.put(flow, node);
            }
            else {
                if(!nodes.containsKey(flow.getProducerFlow())) {
                    System.out.printf("WARN: %s is pending but its producer flow %s is finished!!!%n", flow, flow.getProducerFlow());
                }
                else {
                    Node<Flow> node = new Node<Flow>(flow);
                    nodes.put(flow, node);
                    nodes.get(flow.getProducerFlow()).getChildren().add(node);
                }
            }
        }

        for(Node<Flow> node : nodes.values()) {
            if(node.getValue().getProducerFlow() == null) {
                node.print();
            }
        }

        System.out.flush();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCreate(long beginCycle) {
        this.beginCycle = beginCycle;
        pendingFlows.add(this);
    }

    public void onDestroy() {
        pendingFlows.remove(this);
    }

    public long getId() {
        return id;
    }

    public Flow getProducerFlow() {
        return producerFlow;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    @Override
    public String toString() {
        return String.format("[%d]", id);
    }

    private static long currentId = 0;
    private static List<Flow> pendingFlows = new ArrayList<Flow>();

    public static List<Flow> getPendingFlows() {
        return pendingFlows;
    }
}
