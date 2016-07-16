package archimulator.startup;

import archimulator.common.Experiment;
import archimulator.common.NoCConfig;
import archimulator.uncore.noc.NoCExperiment;
import archimulator.uncore.noc.routers.FlitState;

/**
 * CSV fields.
 *
 * @author Min Cai
 */
public class CSVFields {
    public static final String NOC_PREFIX = "noc/";
    public static final String SIMULATION_PREFIX = "simulation/";

    public static String getDataPacketTraffic(NoCExperiment experiment) {
        return experiment.getConfig().getDataPacketTraffic();
    }

    public static String getDataPacketInjectionRate(NoCExperiment experiment) {
        return String.format("%s", experiment.getConfig().getDataPacketInjectionRate());
    }

    public static String getRouting(Experiment<? extends NoCConfig> experiment) {
        return experiment.getConfig().getRouting();
    }

    public static String getSelection(Experiment<? extends NoCConfig> experiment) {
        return experiment.getConfig().getSelection();
    }

    public static String getRoutingAndSelection(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s+%s",
                getRouting(experiment),
                getSelection(experiment)
        );
    }

    public static String getAntPacketTraffic(Experiment<? extends NoCConfig> experiment) {
        return String.format("%s", experiment.getConfig().getAntPacketTraffic());
    }

    public static String getAntPacketInjectionRate(Experiment<? extends NoCConfig> experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format("%s", experiment.getConfig().getAntPacketInjectionRate());
        }

        return "";
    }

    public static String getAcoSelectionAlpha(Experiment<? extends NoCConfig> experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s",
                    experiment.getConfig().getAcoSelectionAlpha()
            );
        }

        return "";
    }

    public static String getReinforcementFactor(Experiment<? extends NoCConfig> experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s",
                    experiment.getConfig().getReinforcementFactor()
            );
        }

        return "";
    }

    public static String getNoCRoutingSolution(Experiment<? extends NoCConfig> experiment) {
        switch (experiment.getConfig().getRouting()) {
            case "xy":
                return "XY";
            case "oddEven":
                switch (experiment.getConfig().getSelection()) {
                    case "random":
                        return "Rand";
                    case "bufferLevel":
                        return "BL";
                    case "neighborOnPath":
                        return "NoP";
                    case "aco":
                        return String.format(
                                "ACO/aj=%s/a=%s/rf=%s",
                                getAntPacketInjectionRate(experiment),
                                getAcoSelectionAlpha(experiment),
                                getReinforcementFactor(experiment)
                        );
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String getSimulationTime(Experiment<? extends NoCConfig> experiment) {
        return (String) experiment.getStatsMap().get(SIMULATION_PREFIX + "durationInSeconds");
    }

    public static String getTotalCycles(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(SIMULATION_PREFIX + "cycleAccurateEventQueue/currentCycle")
        );
    }

    public static String getNumPacketsTransmitted(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "numPacketsTransmitted")
        );
    }

    public static String getThroughput(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "throughput")
        );
    }

    public static String getAveragePacketDelay(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "averagePacketDelay")
        );
    }

    public static String getAveragePacketHops(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "averagePacketHops")
        );
    }

    public static String getNumPayloadPacketsTransmitted(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "numPayloadPacketsTransmitted")
        );
    }

    public static String getPayloadThroughput(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "payloadThroughput")
        );
    }

    public static String getAveragePayloadPacketDelay(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "averagePayloadPacketDelay")
        );
    }

    public static String getAveragePayloadPacketHops(Experiment<? extends NoCConfig> experiment) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + "averagePayloadPacketHops")
        );
    }

    public static String getAverageFlitPerStateDelay(Experiment<? extends NoCConfig> experiment, FlitState state) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + String.format("averageFlitPerStateDelay::%s", state))
        );
    }

    public static String getMaxFlitPerStateDelay(Experiment<? extends NoCConfig> experiment, FlitState state) {
        return String.format(
                "%s",
                experiment.getStatsMap().get(NOC_PREFIX + String.format("maxFlitPerStateDelay::%s", state))
        );
    }
}
