package archimulator.uncore.net.noc;

import archimulator.uncore.net.noc.routers.FlitState;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.uncore.net.noc.traffics.HotspotTrafficGenerator;
import archimulator.uncore.net.noc.traffics.TransposeTrafficGenerator;
import archimulator.uncore.net.noc.traffics.UniformTrafficGenerator;
import archimulator.util.dateTime.DateHelper;
import archimulator.util.event.CycleAccurateEventQueue;
import archimulator.util.serialization.JsonSerializationHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public class Experiment implements NoCSettings {
    private Config config;
    private Map<String, Object> stats;
    private Random random;

    public Experiment() {
        this.config = new Config();
        this.stats = new LinkedHashMap<>();
        this.random = this.config.getRandSeed() != -1 ? new Random(this.config.getRandSeed()) : new Random();
    }

    public static void runExperiments(List<Experiment> experiments, boolean parallel) {
        if(parallel) {
            experiments.parallelStream().forEach(Experiment::run);
        } else {
            experiments.forEach(Experiment::run);
        }
    }

    public void run() {
        CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

        Network<? extends Node, ? extends RoutingAlgorithm> network;

        switch (this.config.getRouting()) {
            case "xy":
                network = NetworkFactory.xy(this, cycleAccurateEventQueue);
                break;
            case "oddEven":
                switch (this.config.getSelection()) {
                    case "random":
                        network = NetworkFactory.random(this, cycleAccurateEventQueue);
                        break;
                    case "bufferLevel":
                        network = NetworkFactory.bufferLevel(this, cycleAccurateEventQueue);
                        break;
                    case "neighborOnPath":
                        network = NetworkFactory.neighborOnPath(this, cycleAccurateEventQueue);
                        break;
                    case "aco":
                        network = NetworkFactory.aco(this, cycleAccurateEventQueue);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        switch (this.config.getTraffic()) {
            case "uniform":
                new UniformTrafficGenerator<>(
                        network,
                        this.config.getDataPacketInjectionRate(),
                        (n, src, dest, size) -> new DataPacket(n, src, dest, size, () -> {}),
                        this.config.getDataPacketSize(),
                        this.config.getMaxPackets()
                );
                break;
            case "transpose":
                new TransposeTrafficGenerator<>(
                        network,
                        this.config.getDataPacketInjectionRate(),
                        (n, src, dest, size) -> new DataPacket(n, src, dest, size, () -> {}),
                        this.config.getDataPacketSize(),
                        this.config.getMaxPackets()
                );
                break;
            case "hotspot":
                new HotspotTrafficGenerator<>(
                        network,
                        this.config.getDataPacketInjectionRate(),
                        (n, src, dest, size) -> new DataPacket(n, src, dest, size, () -> {}),
                        this.config.getDataPacketSize(),
                        this.config.getMaxPackets()
                );
                break;
        }

        long beginTime = DateHelper.toTick(new Date());

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

        long endTime = DateHelper.toTick(new Date());

        this.stats.put("simulationTime", DurationFormatUtils.formatDurationHMS(endTime - beginTime));
        this.stats.put("totalCycles", cycleAccurateEventQueue.getCurrentCycle());

        this.stats.put("numPacketsReceived", network.getNumPacketsReceived());
        this.stats.put("numPacketsTransmitted", network.getNumPacketsTransmitted());
        this.stats.put("throughput", network.throughput());
        this.stats.put("averagePacketDelay", network.averagePacketDelay());
        this.stats.put("averagePacketHops", network.averagePacketHops());
        this.stats.put("maxPacketDelay", network.getMaxPacketDelay());
        this.stats.put("maxPacketHops", network.getMaxPacketHops());

        this.stats.put("numPayloadPacketsReceived", network.getNumPayloadPacketsReceived());
        this.stats.put("numPayloadPacketsTransmitted", network.getNumPayloadPacketsTransmitted());
        this.stats.put("payloadThroughput", network.payloadThroughput());
        this.stats.put("averagePayloadPacketDelay", network.averagePayloadPacketDelay());
        this.stats.put("averagePayloadPacketHops", network.averagePayloadPacketHops());
        this.stats.put("maxPayloadPacketDelay", network.getMaxPayloadPacketDelay());
        this.stats.put("maxPayloadPacketHops", network.getMaxPayloadPacketHops());

        for(FlitState state : FlitState.values()) {
            this.stats.put(String.format("averageFlitPerStateDelay::%s", state),
                    network.averageFlitPerStateDelay(state));
        }

        for(FlitState state : FlitState.values()) {
            this.stats.put(String.format("maxFlitPerStateDelay::%s", state),
                    network.getMaxFlitPerStateDelay().containsKey(state) ? network.getMaxFlitPerStateDelay().get(state) : 0.0);
        }

        this.dumpConfigAndStats();
    }

    private void dumpConfigAndStats() {
        System.out.println();

        System.out.println("Config: ");
        this.config.dump();

        System.out.println();

        System.out.println("Stats: ");
        for(String key : this.stats.keySet()) {
            Object value = this.stats.get(key);
            System.out.println(String.format("  %s: %s", key, value));
        }

        File resultDirFile = new File(config.getResultDir());

        if (!resultDirFile.exists()) {
            if (!resultDirFile.mkdirs()) {
                throw new RuntimeException();
            }
        }

        JsonSerializationHelper.writeJsonFile(config, this.config.getResultDir(), "config.json");
        JsonSerializationHelper.writeJsonFile(stats, this.config.getResultDir(), "stats.json");
    }

    @SuppressWarnings("unchecked")
    public void loadStats() {
        File file = new File(config.getResultDir(), "stats.json");

        try {
            String json = FileUtils.readFileToString(file);

            stats = JsonSerializationHelper.fromJson(Map.class, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
