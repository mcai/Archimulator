package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.Direction;

import java.util.ArrayList;
import java.util.List;

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

        this.virtualChannels = new ArrayList<>();

        for (int i = 0; i < this.router.getNode().getNetwork().getSettings().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new InputVirtualChannel(this, i));
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
