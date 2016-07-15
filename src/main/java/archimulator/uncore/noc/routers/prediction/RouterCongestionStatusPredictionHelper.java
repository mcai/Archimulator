package archimulator.uncore.noc.routers.prediction;

import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.uncore.cache.prediction.Predictor;
import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.routers.InputPort;
import archimulator.uncore.noc.routers.InputVirtualChannel;
import archimulator.uncore.noc.routing.RoutingAlgorithm;

/**
 * Router congestion status prediction helper.
 *
 * @author Min Cai
 */
public class RouterCongestionStatusPredictionHelper implements Reportable {
    private Predictor<RouterCongestionStatus> routerCongestionStatusPredictor;

    /**
     * Create a router congestion status prediction helper.
     *
     * @param network the parent network
     */
    public RouterCongestionStatusPredictionHelper(Network<? extends Node, ? extends RoutingAlgorithm> network) {
        this.routerCongestionStatusPredictor = new CacheBasedPredictor<>(
                network.getMemoryHierarchy(),
                "routerCongestionStatusPredictor",
                network.getNumNodes(),
                4,
                16,
                RouterCongestionStatus.NOT_CONGESTED
        );

        network.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            for(Node node : network.getNodes()) {
                int freeSlots = 0;

                for(InputPort inputPort : node.getRouter().getInputPorts().values()) {
                    for(InputVirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                        freeSlots += node.getRouter().freeSlots(inputPort.getDirection(), inputVirtualChannel.getId());
                    }
                }

                getRouterCongestionStatusPredictor().update(
                        node.getId(),
                        freeSlots > 720 ? RouterCongestionStatus.NOT_CONGESTED : RouterCongestionStatus.CONGESTED
                );
            }
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "routerCongestionStatusPredictionHelper") {{
            routerCongestionStatusPredictor.dumpStats(this);
        }});
    }

    /**
     * Get the router congestion status predictor.
     *
     * @return the router congestion status predictor
     */
    public Predictor<RouterCongestionStatus> getRouterCongestionStatusPredictor() {
        return routerCongestionStatusPredictor;
    }
}
