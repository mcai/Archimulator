package archimulator.uncore.net.basic;

import java.util.*;

/**
 * Route computation component.
 *
 * @author Min Cai
 */
public class RouteComputation {
    private Router router;
    private EnumMap<Port, List<Map<Integer, Set<Port>>>> routes;

    /**
     * Create a route computation component.
     *
     * @param router the parent router
     */
    public RouteComputation(Router router) {
        this.router = router;

        this.routes = new EnumMap<>(Port.class);
        this.routes.put(Port.LOCAL, new ArrayList<>());
        this.routes.put(Port.LEFT, new ArrayList<>());
        this.routes.put(Port.RIGHT, new ArrayList<>());
        this.routes.put(Port.UP, new ArrayList<>());
        this.routes.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int ivc = 0; ivc < this.router.getNet().getNumVirtualChannels(); ivc++) {
                Map<Integer, Set<Port>> routes = new HashMap<>();
                routes.put(0, new HashSet<>());
                routes.put(1, new HashSet<>());
                this.routes.get(inputPort).add(routes);
            }
        }
    }

    /**
     * The route calculation (RC) stage. Calculate the permissible routes for every first flit in each ivc.
     */
    public void stageRouteCalculation() {
        for(Port inputPort : Port.values()) {
            for(int ivc = 0; ivc < this.router.getNet().getNumVirtualChannels(); ivc++) {
                if(this.router.getInputBuffers().get(inputPort).get(ivc).isEmpty()) {
                    continue;
                }

                Flit flit = this.router.getInputBuffers().get(inputPort).get(ivc).get(0);
                if(flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    this.routes.get(inputPort).get(ivc).get(0).clear();
                    this.routes.get(inputPort).get(ivc).get(1).clear();
                    if(flit.getDestination() == this.router) {
                        this.routes.get(inputPort).get(ivc).get(0).add(Port.LOCAL);
                        this.routes.get(inputPort).get(ivc).get(1).add(Port.LOCAL);
                    }
                    else {
                        //adaptive routing
                        if(this.router.getX() > flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.LEFT);
                        }
                        else if (this.router.getX() < flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.RIGHT);
                        }

                        if(this.router.getY() > flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.UP);
                        }
                        else if(this.router.getY() < flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(0).add(Port.DOWN);
                        }

                        //escape routing
                        if(this.router.getX() > flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.LEFT);
                        }
                        else if(this.router.getX() < flit.getDestination().getX()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.RIGHT);
                        }
                        else if(this.router.getY() > flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.UP);
                        }
                        else if(this.router.getY() < flit.getDestination().getY()) {
                            this.routes.get(inputPort).get(ivc).get(1).add(Port.DOWN);
                        }
                    }
                    flit.setState(FlitState.ROUTE_CALCULATION);
                }
            }
        }
    }

    /**
     * Get a boolean value indicating whether the specified path is routed or not.
     *
     * @param inputPort the input port
     * @param ivc the input virtual channel
     * @param outputPort the output port
     * @param ovc the output virtual channel
     *
     * @return a boolean value indicating whether the specified path is routed or not
     */
    public boolean isRouted(Port inputPort, int ivc, Port outputPort, int ovc) {
        int routeCalculationIndex = ovc == this.getRouter().getNet().getNumVirtualChannels() - 1 ? 0 : 1;
        return this.routes.get(inputPort).get(ivc).get(routeCalculationIndex).contains(outputPort);
    }

    /**
     * Get the parent router.
     *
     * @return the parent router
     */
    public Router getRouter() {
        return router;
    }
}
