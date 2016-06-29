package archimulator.incubator.noc.routers;

import archimulator.incubator.noc.Direction;

import java.util.ArrayList;
import java.util.List;

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

        this.virtualChannels = new ArrayList<>();

        for (int i = 0; i < this.router.getNode().getNetwork().getSettings().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new OutputVirtualChannel(this, i));
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
