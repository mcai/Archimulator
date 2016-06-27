package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;
import javaslang.collection.List;

/**
 * Output port.
 *
 * @author Min Cai
 */
public class OutputPort {
    private Router router;

    private Direction direction;

    private List<OutputVirtualChannel> virtualChannels;

    public OutputPort(Router router, Direction direction) {
        this.router = router;

        this.direction = direction;

        this.virtualChannels = List.empty();

        for (int i = 0; i < this.router.getNode().getNetwork().getExperiment().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.append(new OutputVirtualChannel(this, i));
        }
    }

    public Router getRouter() {
        return router;
    }

    public Direction getDirection() {
        return direction;
    }

    public List<OutputVirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }
}
