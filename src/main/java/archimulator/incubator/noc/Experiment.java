package archimulator.incubator.noc;

import archimulator.incubator.noc.routing.RoutingAlgorithm;
import archimulator.incubator.noc.routing.XYRoutingAlgorithm;
import archimulator.incubator.noc.selection.RandomSelectionBasedNode;
import archimulator.incubator.noc.traffics.PacketFactory;
import archimulator.incubator.noc.traffics.TransposeTrafficGenerator;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public class Experiment {
    private Config config;
    private Map<String, Object> stats;
    private Random random;

    public Experiment() {
        this.config = new Config();
        this.stats = new TreeMap<>();
        this.random = this.config.getRandSeed() != -1 ? new Random(this.config.getRandSeed()) : new Random();
    }

    public void run() {
        CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

//        Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> network =
//                new Network<>(
//                        this,
//                        cycleAccurateEventQueue,
//                        this.config.getNumNodes(),
//                        new NodeFactory<ACONode>() {
//                            @Override
//                            public ACONode createNode(Network<ACONode, ?> network, int i) {
//                                return new ACONode(network, i);
//                            }
//                        },
//                        OddEvenTurnBasedRoutingAlgorithm::new);

        Network<RandomSelectionBasedNode, XYRoutingAlgorithm> network =
                new Network<>(
                        this,
                        cycleAccurateEventQueue,
                        this.config.getNumNodes(),
                        new NodeFactory<RandomSelectionBasedNode>() {
                            @Override
                            public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                                return new RandomSelectionBasedNode(network, i);
                            }
                        },
                        XYRoutingAlgorithm::new);

        new TransposeTrafficGenerator<>(
                network,
                this.config.getDataPacketInjectionRate(),
                 new PacketFactory<DataPacket>() {
                    @Override
                    public DataPacket create(Network<? extends Node, ? extends RoutingAlgorithm> network, int src, int dest, int size) {
                        return new DataPacket(network, src, dest, size, () -> {});
                    }
                },
                this.config.getDataPacketSize(),
                this.config.getMaxPackets()
        );

//        new TransposeTrafficGenerator<>(
//                network,
//                this.config.getAntPacketInjectionRate(),
//                 new PacketFactory<ForwardAntPacket>() {
//                    @Override
//                    public ForwardAntPacket create(Network<? extends Node, ? extends RoutingAlgorithm> network, int src, int dest, int size) {
//                        return new ForwardAntPacket(network, src, dest, size, () -> {});
//                    }
//                },
//                this.config.getAntPacketSize(),
//                -1
//        );

        while (this.config.getWarmupCycles() > 0 && cycleAccurateEventQueue.getCurrentCycle() < this.config.getWarmupCycles()) {
            cycleAccurateEventQueue.advanceOneCycle();
        }

        while ((this.config.getMaxCycles() == -1 || cycleAccurateEventQueue.getCurrentCycle() < this.config.getMaxCycles())
        && (this.config.getMaxPackets() == -1 || network.getNumPacketsReceived() < this.config.getMaxPackets())) {
            cycleAccurateEventQueue.advanceOneCycle();
        }

        if (!this.config.isNoDrain()) {
            network.setAcceptPacket(false);

            while(network.getNumPacketsReceived() != network.getNumPacketsTransmitted()) {
                cycleAccurateEventQueue.advanceOneCycle();
            }
        }

//        this.stats.put("simulation_time", time() - time_start);
        this.stats.put("total_cycles", cycleAccurateEventQueue.getCurrentCycle());

        this.stats.put("num_packets_received", network.getNumPacketsReceived());
        this.stats.put("num_packets_transmitted", network.getNumPacketsTransmitted());
        this.stats.put("throughput", network.throughput());
        this.stats.put("average_packet_delay", network.averagePacketDelay());
        this.stats.put("average_packet_hops", network.averagePacketHops());
        this.stats.put("max_packet_delay", network.getMaxPacketDelay());
        this.stats.put("max_packet_hops", network.getMaxPacketHops());

        this.stats.put("num_payload_packets_received", network.getNumPayloadPacketsReceived());
        this.stats.put("num_payload_packets_transmitted", network.getNumPayloadPacketsTransmitted());
        this.stats.put("payload_throughput", network.payloadThroughput());
        this.stats.put("average_payload_packet_delay", network.averagePayloadPacketDelay());
        this.stats.put("average_payload_packet_hops", network.averagePayloadPacketHops());
        this.stats.put("max_payload_packet_delay", network.getMaxPayloadPacketDelay());
        this.stats.put("max_payload_packet_hops", network.getMaxPayloadPacketHops());

        for(String key : this.stats.keySet()) {
            Object value = this.stats.get(key);
            System.out.println(String.format("%s: %s", key, value));
        }
    }

    public Config getConfig() {
        return config;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public Random getRandom() {
        return random;
    }
}
