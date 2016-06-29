package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;

/**
 * Route computation.
 *
 * @author Min Cai
 */
public class RouteComputation {
    private Router router;

    public RouteComputation(Router router) {
        this.router = router;
    }

    public void stageRouteComputation() {
        for(InputPort inputPort : this.router.getInputPorts().values()) {
            for(InputVirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                Flit flit = inputVirtualChannel.getInputBuffer().peek();

                if(flit != null && flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    if(flit.getPacket().getDest() == this.router.getNode().getId()) {
                        inputVirtualChannel.setRoute(Direction.LOCAL);
                    } else {
                        inputVirtualChannel.setRoute(
                                this.router.getNode().doRouteCalculation(
                                        flit.getPacket(), inputVirtualChannel
                                )
                        );
                    }

                    flit.setState(FlitState.ROUTE_CALCULATION);
                }
            }
        }
    }

    public Router getRouter() {
        return router;
    }
}
