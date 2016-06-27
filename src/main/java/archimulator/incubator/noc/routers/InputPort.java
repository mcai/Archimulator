package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;
import javaslang.collection.List;

/**
 * Input port.
 *
 * @author Min Cai
 */
public class InputPort {
    private Router router;

    private Direction direction;

    private List<InputVirtualChannel> virtualChannels;

    public InputPort(Router router, Direction direction) {
        this.router = router;

        this.direction = direction;

        this.virtualChannels = List.empty();

        for (int i = 0; i < this.router.getNode().getNetwork().getExperiment().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.append(new InputVirtualChannel(this, i));
        }
    }

    public Router getRouter() {
        return router;
    }

    public Direction getDirection() {
        return direction;
    }

    public List<InputVirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }
}
