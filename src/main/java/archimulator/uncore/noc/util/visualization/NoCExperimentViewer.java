package archimulator.uncore.noc.util.visualization;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.NoCExperiment;
import archimulator.uncore.noc.Node;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class NoCExperimentViewer {
    private NoCExperiment experiment;

    private Graph graph;

//    static {
//        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
//    }

    public NoCExperimentViewer(NoCExperiment experiment) {
        this.experiment = experiment;

        this.graph = new SingleGraph("NoC Experiment");

        experiment.getBlockingEventDispatcher().addListener(
                NoCExperiment.NoCExperimentStartedEvent.class,
                e -> {
                    for (Node node : experiment.getNetwork().getNodes()) {
                        org.graphstream.graph.Node graphNode = graph.addNode(String.format("%d", node.getId()));
                        graphNode.addAttribute("xy", node.getX(), node.getY());
                        graphNode.addAttribute("label", String.format("%d", node.getId()));
                    }

                    for(Node node : experiment.getNetwork().getNodes()) {
                        for(Direction direction : node.getNeighbors().keySet()) {
                            if(direction == Direction.NORTH || direction == Direction.EAST) {
                                graph.addEdge(
                                        node.getId() + "-" + node.getNeighbors().get(direction),
                                        node.getId(),
                                        node.getNeighbors().get(direction)
                                );
                            }
                        }
                    }

                    this.graph.display(false);
                });

        experiment.getBlockingEventDispatcher().addListener(
                NoCExperiment.NoCExperimentAdvanceOneCycleEvent.class,
                e -> {

                });
    }

    public static void main(String[] args) {
        int numNodes = 64;
        int maxCycles = 20000;
        int maxPackets = -1;
        boolean noDrain = false;

        NoCExperiment experimentAco = new NoCExperiment(
                "test_results/synthetic/aco",
                numNodes,
                maxCycles,
                maxPackets,
                noDrain
        );

        experimentAco.getConfig().setRouting("oddEven");
        experimentAco.getConfig().setSelection("aco");

        experimentAco.getConfig().setDataPacketTraffic("transpose");
        experimentAco.getConfig().setDataPacketInjectionRate(0.06);

        experimentAco.getConfig().setAntPacketTraffic("uniform");
        experimentAco.getConfig().setAntPacketInjectionRate(0.0002);
        experimentAco.getConfig().setAcoSelectionAlpha(0.45);
        experimentAco.getConfig().setReinforcementFactor(0.001);

        NoCExperimentViewer viewer = new NoCExperimentViewer(experimentAco);

        experimentAco.run();
    }
}
