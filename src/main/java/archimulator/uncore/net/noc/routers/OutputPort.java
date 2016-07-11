package archimulator.uncore.net.noc.routers;

import archimulator.uncore.net.noc.Direction;

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

    public OutputPort(Router router, Direction direction) {
        this.router = router;

        this.direction = direction;

        this.virtualChannels = new ArrayList<>();

        for (int i = 0; i < this.router.getNode().getNetwork().getMemoryHierarchy().getExperiment().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new OutputVirtualChannel(this, i));
        }

        this.arbiter = new SwitchArbiter(this);
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

    public SwitchArbiter getArbiter() {
        return arbiter;
    }
}
