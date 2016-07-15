package archimulator.uncore.noc.routers;

import archimulator.uncore.noc.Direction;

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

    /**
     * Create an input port.
     *
     * @param router the parent router
     * @param direction the direction
     */
    public InputPort(Router router, Direction direction) {
        this.router = router;

        this.direction = direction;

        this.virtualChannels = new ArrayList<>();

        for (int i = 0; i < this.router.getNode().getNetwork().getEnvironment().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new InputVirtualChannel(this, i));
        }
    }

    /**
     * Get the parent router.
     *
     * @return the parent router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Get the direction.
     *
     * @return the parent router
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the list of input virtual channels.
     *
     * @return the list of input virtual channels
     */
    public List<InputVirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }
}
