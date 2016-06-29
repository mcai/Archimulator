package archimulator.uncore.net.noc;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationObject;
import archimulator.common.SimulationType;
import archimulator.incubator.noc.Config;
import archimulator.incubator.noc.DataPacket;
import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.NoCSettings;
import archimulator.incubator.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.incubator.noc.selection.aco.ACONode;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.coherence.msi.controller.L1IController;
import archimulator.uncore.net.Net;
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
}
