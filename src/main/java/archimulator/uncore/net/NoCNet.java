package archimulator.uncore.net;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationObject;
import archimulator.common.SimulationType;
import archimulator.uncore.net.noc.Config;
import archimulator.uncore.net.noc.DataPacket;
import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.NoCSettings;
import archimulator.uncore.net.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.uncore.net.noc.selection.aco.ACONode;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.coherence.msi.controller.L1IController;
import archimulator.uncore.net.noc.selection.aco.ForwardAntPacket;
import archimulator.uncore.net.noc.traffics.TransposeTrafficGenerator;
import archimulator.util.action.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NoCNet extends BasicSimulationObject implements Net, NoCSettings {
    private MemoryHierarchy memoryHierarchy;

    private Map<SimulationObject, Integer> devicesToNodeIds;

    private Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> network;

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

        this.network = new Network<ACONode, OddEvenTurnBasedRoutingAlgorithm>(
                this,
                this.memoryHierarchy.getCycleAccurateEventQueue(),
                this.config.getNumNodes(),
                ACONode::new,
                OddEvenTurnBasedRoutingAlgorithm::new) {
            @Override
            public boolean simulateAtCurrentCycle() {
                return memoryHierarchy.getSimulation().getType() != SimulationType.FAST_FORWARD;
            }
        };

        new TransposeTrafficGenerator<>(
                this.network,
                this.config.getAntPacketInjectionRate(),
                (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                this.config.getAntPacketSize(),
                -1
        );
    }

    @Override
    public void transfer(MemoryDevice deviceFrom, MemoryDevice deviceTo, int size, Action onCompletedCallback) {
        int src = this.devicesToNodeIds.get(deviceFrom);
        int dest = this.devicesToNodeIds.get(deviceTo);

        DataPacket packet = new DataPacket(this.network, src, dest, size, onCompletedCallback);

        this.network.getCycleAccurateEventQueue().schedule(this, () -> {
            this.network.receive(packet);
        }, 1);
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
    public Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> getNetwork() {
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
