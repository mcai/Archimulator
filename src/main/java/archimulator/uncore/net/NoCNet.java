package archimulator.uncore.net;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationObject;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.coherence.msi.controller.L1IController;
import archimulator.uncore.net.noc.*;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.util.action.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NoCNet extends BasicSimulationObject implements Net, NoCSettings {
    private MemoryHierarchy memoryHierarchy;

    private Map<SimulationObject, Integer> devicesToNodeIds;

    private Network<? extends Node, ? extends RoutingAlgorithm> network;

    private Config config;

    private Random random;

    public NoCNet(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);

        this.memoryHierarchy = memoryHierarchy;

        this.devicesToNodeIds = new HashMap<>();

        int numNodes = 0;

        for (L1IController l1IController : memoryHierarchy.getL1IControllers()) {
            this.devicesToNodeIds.put(l1IController, numNodes);
            this.devicesToNodeIds.put(
                    memoryHierarchy.getL1DControllers().get(
                            memoryHierarchy.getL1IControllers().indexOf(l1IController)
                    ),
                    numNodes);

            numNodes++;
        }

        this.devicesToNodeIds.put(memoryHierarchy.getL2Controller(), numNodes);

        numNodes++;

        this.devicesToNodeIds.put(memoryHierarchy.getMemoryController(), numNodes);

        numNodes++;

        int width = (int) Math.sqrt(numNodes);
        if (width * width != numNodes) {
            numNodes = (width + 1) * (width + 1);
        }

        this.config = new Config();
        this.config.setNumNodes(numNodes);
        this.config.setMaxInputBufferSize(this.memoryHierarchy.getL2Controller().getCache().getLineSize() + 8);

        this.random = this.config.getRandSeed() != -1 ? new Random(this.config.getRandSeed()) : new Random();

        this.network = NetworkFactory.setupNetwork(this, this.memoryHierarchy.getCycleAccurateEventQueue());
    }

    @Override
    public void transfer(MemoryDevice deviceFrom, MemoryDevice deviceTo, int size, Action onCompletedCallback) {
        int src = this.devicesToNodeIds.get(deviceFrom);
        int dest = this.devicesToNodeIds.get(deviceTo);

        DataPacket packet = new DataPacket(this.network, src, dest, size, onCompletedCallback);

        this.network.getCycleAccurateEventQueue().schedule(this, () -> this.network.receive(packet), 1);
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
     * Get the network.
     *
     * @return the network
     */
    public Network<? extends Node, ? extends RoutingAlgorithm> getNetwork() {
        return network;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
