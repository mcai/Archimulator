package archimulator.uncore.net.basic.routing.aco;

import archimulator.uncore.net.basic.BasicNet;
import archimulator.uncore.net.basic.Flit;
import archimulator.uncore.net.basic.Port;
import archimulator.uncore.net.basic.Router;
import archimulator.uncore.net.basic.routing.Routing;

import java.util.List;

/**
 * Ant colony optimization (ACO) based routing.
 *
 * @author Min Cai
 */
public class ACORouting implements Routing {
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
        throw new UnsupportedOperationException();
    }

    /**
     * Get the list of neighbors of the specified router.
     *
     * @param router the router
     * @return the list of neighbors of the specified router
     */
    public List<Router> getNeighbors(Router router) {
        //TODO
        throw new UnsupportedOperationException();
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
