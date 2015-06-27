package archimulator.uncore.net.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * Port.
 *
 * @author Min Cai
 */
public class Port {
    private Router router;
    private Direction direction;
    private List<VirtualChannel> virtualChannels;

    /**
     * Create a port.
     *
     * @param router the router
     * @param direction the direction
     */
    public Port(Router router, Direction direction) {
        this.router = router;
        this.direction = direction;

        this.virtualChannels = new ArrayList<>();

        for(int i = 0; i < this.router.getNet().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new VirtualChannel(this, i));
        }
    }

    /**
     * Get the router.
     *
     * @return the router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Get the direction.
     *
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the list of virtual channels.
     *
     * @return the list of virtual channels
     */
    public List<VirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }
}
