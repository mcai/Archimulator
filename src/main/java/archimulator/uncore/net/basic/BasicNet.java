package archimulator.uncore.net.basic;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationObject;
import archimulator.common.SimulationType;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.coherence.msi.controller.L1IController;
import archimulator.uncore.net.Net;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic net.
 *
 * @author Min Cai
 */
public class BasicNet extends BasicSimulationObject implements Net {
    private MemoryHierarchy memoryHierarchy;

    private List<Router> routers;

    private Map<SimulationObject, Router> devicesToRouters;

    private int numVirtualChannels = 4;

    private int linkLatency = 1;
    private int linkWidth = 16;

    public long currentRequestId = 0;

    /**
     * Create a basic net.
     *
     * @param memoryHierarchy the memory hierarchy
     */
    public BasicNet(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);

        this.memoryHierarchy = memoryHierarchy;

        this.routers = new ArrayList<>();

        this.devicesToRouters = new HashMap<>();

        int numRouters = 0;

        for(L1IController l1IController : memoryHierarchy.getL1IControllers()) {
            Router router = new Router(this, RouterType.CORE, numRouters++);
            this.routers.add(router);
            this.devicesToRouters.put(l1IController, router);
            this.devicesToRouters.put(memoryHierarchy.getL1DControllers().get(memoryHierarchy.getL1IControllers().indexOf(l1IController)), router);
        }

        Router routerL2Controller = new Router(this, RouterType.L2_CONTROLLER, numRouters++);
        this.routers.add(routerL2Controller);
        this.devicesToRouters.put(memoryHierarchy.getL2Controller(), routerL2Controller);

        Router routerMemoryController = new Router(this, RouterType.MEMORY_CONTROLLER, numRouters++);
        this.routers.add(routerMemoryController);
        this.devicesToRouters.put(memoryHierarchy.getMemoryController(), routerMemoryController);

        int width = (int) Math.sqrt(numRouters);
        if (width * width != numRouters) {
            for (; numRouters < (width + 1) * (width + 1); ++numRouters) {
                this.routers.add(new Router(this, RouterType.DUMMY, numRouters));
            }
            ++width;
        }

        for (Router router : this.routers) {
            int id = router.getId();
            if (id / width > 0) {
                router.getLinks().put(Direction.UP, this.routers.get(id - width));
            }

            if (id / width < (width - 1)) {
                router.getLinks().put(Direction.DOWN, this.routers.get(id + width));
            }

            if (id % width != 0) {
                router.getLinks().put(Direction.LEFT, this.routers.get(id - 1));
            }

            if (id % width != (width - 1)) {
                router.getLinks().put(Direction.RIGHT, this.routers.get(id + 1));
            }
        }

        memoryHierarchy.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            if (memoryHierarchy.getSimulation().getType() != SimulationType.FAST_FORWARD) {
                advanceOneCycle();
            }
        });
    }

    /**
     * Advance one cycle.
     */
    public void advanceOneCycle() {
        this.routers.forEach(Router::advanceOneCycle);
    }

    /**
     * Transfer a message of the specified size from the source device to the destination device.
     *
     * @param from                the source device
     * @param to                  the destination device
     * @param size                the size
     * @param onCompletedCallback the callback action performed when the transfer is completed
     */
    @Override
    public void transfer(MemoryDevice from, MemoryDevice to, int size, Action onCompletedCallback) {
        boolean successful = this.getRouter(from).injectRequest(new Packet(this, from, to, size, onCompletedCallback));
        if(!successful) {
            //TODO: retry
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String getName() {
        return "net";
    }

    /**
     * Get the memory hierarchy.
     *
     * @return the memory hierarchy
     */
    public MemoryHierarchy getMemoryHierarchy() {
        return memoryHierarchy;
    }

    /**
     * Get the list of routers.
     *
     * @return the list of routers
     */
    public List<Router> getRouters() {
        return routers;
    }

    /**
     * Get the corresponding router from the specified memory device.
     *
     * @param device the memory device
     * @return the corresponding router from the specified memory device
     */
    public Router getRouter(MemoryDevice device) {
        return this.devicesToRouters.get(device);
    }

    /**
     * Get the number of virtual channels.
     *
     * @return the number of virtual channels
     */
    public int getNumVirtualChannels() {
        return numVirtualChannels;
    }

    /**
     * Get the link latency.
     *
     * @return the link latency
     */
    public int getLinkLatency() {
        return linkLatency;
    }

    /**
     * Get the link width.
     *
     * @return the link width
     */
    public int getLinkWidth() {
        return linkWidth;
    }
}
