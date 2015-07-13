/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.net.basic;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationObject;
import archimulator.common.SimulationType;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.coherence.msi.controller.L1IController;
import archimulator.uncore.net.Net;
import archimulator.uncore.net.basic.routing.Routing;
import archimulator.uncore.net.basic.routing.aco.ACORouting;
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

    private Routing routing;

    private int numVirtualChannels;

    private int linkLatency;
    private int linkWidth;

    private int injectionBufferMaxSize;

    private int inputBufferMaxSize;

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

        this.numVirtualChannels = 4;

        this.linkLatency = 1;
        this.linkWidth = 16;

        this.injectionBufferMaxSize = 32;

        this.inputBufferMaxSize = 10;

        int numRouters = 0;

        for (L1IController l1IController : memoryHierarchy.getL1IControllers()) {
            Router router = new Router(this, numRouters++);
            this.routers.add(router);
            this.devicesToRouters.put(l1IController, router);
            this.devicesToRouters.put(
                    memoryHierarchy.getL1DControllers().get(
                            memoryHierarchy.getL1IControllers().indexOf(l1IController)
                    ),
                    router);
        }

        Router routerL2Controller = new Router(this, numRouters++);
        this.routers.add(routerL2Controller);
        this.devicesToRouters.put(memoryHierarchy.getL2Controller(), routerL2Controller);

        Router routerMemoryController = new Router(this, numRouters++);
        this.routers.add(routerMemoryController);
        this.devicesToRouters.put(memoryHierarchy.getMemoryController(), routerMemoryController);

        int width = (int) Math.sqrt(numRouters);
        if (width * width != numRouters) {
            for (; numRouters < (width + 1) * (width + 1); ++numRouters) {
                this.routers.add(new Router(this, numRouters));
            }
            ++width;
        }

        for (Router router : this.routers) {
            int id = router.getId();
            if (id / width > 0) {
                router.getLinks().put(Port.UP, this.routers.get(id - width));
            }

            if (id / width < (width - 1)) {
                router.getLinks().put(Port.DOWN, this.routers.get(id + width));
            }

            if (id % width != 0) {
                router.getLinks().put(Port.LEFT, this.routers.get(id - 1));
            }

            if (id % width != (width - 1)) {
                router.getLinks().put(Port.RIGHT, this.routers.get(id + 1));
            }
        }

//        this.routing = new XYRouting();
//        this.routing = new ShortestPathFirstRouting(this);
        this.routing = new ACORouting(this);

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
        int index = (int) (this.getCycleAccurateEventQueue().getCurrentCycle() % this.routers.size());
        for (int i = index; i < index + this.routers.size(); i++) {
            this.routers.get(i % this.routers.size()).advanceOneCycle();
        }
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
        if (!this.getRouter(from).injectPacket(new Packet(this, from, to, size, onCompletedCallback))) {
            this.getCycleAccurateEventQueue().schedule(
                    this, () -> this.transfer(from, to, size, onCompletedCallback), 1
            );
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
     * Get the routing.
     *
     * @return the routing
     */
    public Routing getRouting() {
        return routing;
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

    /**
     * Get the injection buffer max size.
     *
     * @return the injection buffer max size
     */
    public int getInjectionBufferMaxSize() {
        return injectionBufferMaxSize;
    }

    /**
     * Get the input buffer max size.
     *
     * @return the input buffer max size
     */
    public int getInputBufferMaxSize() {
        return inputBufferMaxSize;
    }
}
