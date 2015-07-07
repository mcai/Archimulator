package archimulator.uncore.net.basic.routing.aco;

import archimulator.uncore.net.basic.BasicNet;
import archimulator.uncore.net.basic.Flit;
import archimulator.uncore.net.basic.Port;
import archimulator.uncore.net.basic.Router;
import archimulator.uncore.net.basic.routing.Routing;

/**
 * Ant colony optimization (ACO) based routing.
 *
 * @author Min Cai
 */
public class ACORouting implements Routing {
    public static final int WINDOW_LENGTH = 300;

    public static final double VAR_SIGMA = 0.005;

    //relative weight of heuristic info w.r.t. pheromone.
    public static final double ALPHA = 0.45;

    public static final double C1 = 0.7;
    public static final double C2 = 0.3;
    public static final double ZEE = 1.7;

    public static final double REINFORCEMENT_FACTOR = 0.05;

    private BasicNet net;

    /**
     * Create an ant colony optimization (ACO) based routing.
     *
     * @param net the parent net
     */
    public ACORouting(BasicNet net) {
        this.net = net;
    }

    @Override
    public Port getOutputPort(Router router, Flit flit) {
        //TODO
        return null;
    }

    /**
     * Get the number of neighbors of the specified router.
     *
      * @param router the router
     * @return the number of neighbors of the specified router
     */
    public int getNumNeighbors(Router router) {
        //TODO
        return -1;
    }

    /**
     * Get the queue length of the link between the two routers.
     *
     * @param router1 the router 1
     * @param router2 the router 2
     * @return the queue length of the link between the two routers
     */
    public int getQueueLength(Router router1, Router router2) {
        //TODO
        return -1;
    }

    /**
     * Get the parent net.
     *
     * @return the parent net
     */
    public BasicNet getNet() {
        return net;
    }
}
