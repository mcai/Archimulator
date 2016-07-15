package archimulator.uncore.noc.routers;

import archimulator.uncore.noc.Direction;

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

    private SwitchArbiter arbiter;

    /**
     * Create an output port.
     *
     * @param router the parent router
     * @param direction the direction
     */
    public OutputPort(Router router, Direction direction) {
        this.router = router;

        this.direction = direction;

        this.virtualChannels = new ArrayList<>();

        for (int i = 0; i < this.router.getNode().getNetwork().getEnvironment().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new OutputVirtualChannel(this, i));
        }

        this.arbiter = new SwitchArbiter(this);
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
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the list of output virtual channels.
     *
     * @return the list of output virtual channels
     */
    public List<OutputVirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }

    /**
     * Get the switch arbiter.
     *
     * @return the switch arbiter
     */
    public SwitchArbiter getArbiter() {
        return arbiter;
    }
}
