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

public class NoCNet extends BasicSimulationObject implements Net {
    private MemoryHierarchy memoryHierarchy;

    private Map<SimulationObject, Integer> devicesToNodeIds;

    private final Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> network;

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

        Config config = new Config();
        config.setNumNodes(numNodes);
        config.setMaxInputBufferSize(memoryHierarchy.getL2Controller().getCache().getLineSize() + 8);

        Random random = config.getRandSeed() != -1 ? new Random(config.getRandSeed()) : new Random();

        NoCSettings noCSettings = new NoCSettings() {
            @Override
            public Config getConfig() {
                return config;
            }

            @Override
            public Random getRandom() {
                return random;
            }
        };

        network = new Network<ACONode, OddEvenTurnBasedRoutingAlgorithm>(
                noCSettings,
                memoryHierarchy.getCycleAccurateEventQueue(),
                config.getNumNodes(),
                ACONode::new,
                OddEvenTurnBasedRoutingAlgorithm::new) {
            @Override
            public boolean simulateAtCurrentCycle() {
                return memoryHierarchy.getSimulation().getType() != SimulationType.FAST_FORWARD;
            }
        };

        new TransposeTrafficGenerator<>(
                network,
                config.getAntPacketInjectionRate(),
                (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                config.getAntPacketSize(),
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

    public Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> getNetwork() {
        return network;
    }
}
