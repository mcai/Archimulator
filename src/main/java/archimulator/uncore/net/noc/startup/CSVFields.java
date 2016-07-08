package archimulator.uncore.net.noc.startup;

import archimulator.uncore.net.noc.NoCExperiment;
import archimulator.uncore.net.noc.routers.FlitState;

/**
 * CSV fields.
 *
 * @author Min Cai
 */
public class CSVFields {
    public static String getTraffic(NoCExperiment experiment) {
        return experiment.getConfig().getTraffic();
    }

    public static String getDataPacketInjectionRate(NoCExperiment experiment) {
        return String.format("%s", experiment.getConfig().getDataPacketInjectionRate());
    }

    public static String getRouting(NoCExperiment experiment) {
        return experiment.getConfig().getRouting();
    }

    public static String getSelection(NoCExperiment experiment) {
        return experiment.getConfig().getSelection();
    }

    public static String getRoutingAndSelection(NoCExperiment experiment) {
        return String.format(
                "%s+%s",
                getRouting(experiment),
                getSelection(experiment)
        );
    }

    public static String getAntPacketInjectionRate(NoCExperiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format("%s", experiment.getConfig().getAntPacketInjectionRate());
        }

        return "";
    }

    public static String getAcoSelectionAlpha(NoCExperiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s",
                    experiment.getConfig().getAcoSelectionAlpha()
            );
        }

        return "";
    }

    public static String getReinforcementFactor(NoCExperiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s",
                    experiment.getConfig().getReinforcementFactor()
            );
        }

        return "";
    }

    public static String getRoutingAndSelectionAndAcoSelectionAlphaAndReinforcementFactor(NoCExperiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s+%s/a=%s/rf=%s",
                    getRouting(experiment),
                    getSelection(experiment),
                    getAcoSelectionAlpha(experiment),
                    getReinforcementFactor(experiment)
            );
        }

        return String.format(
                "%s+%s",
                getRouting(experiment),
                getSelection(experiment)
        );
    }

    public static String getRoutingAndSelectionAndAntPacketInjectionRateAndAcoSelectionAlphaAndReinforcementFactor(NoCExperiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s+%s/aj=%s/a=%s/rf=%s",
                    getRouting(experiment),
                    getSelection(experiment),
                    getAntPacketInjectionRate(experiment),
                    getAcoSelectionAlpha(experiment),
                    getReinforcementFactor(experiment)
            );
        }

        return String.format(
                "%s+%s",
                getRouting(experiment),
                getSelection(experiment)
        );
    }

    public static String getSimulationTime(NoCExperiment experiment) {
        return (String) experiment.getStats().get("simulationTime");
    }

    public static String getTotalCycles(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("totalCycles")
        );
    }

    public static String getNumPacketsTransmitted(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("numPacketsTransmitted")
        );
    }

    public static String getThroughput(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("throughput")
        );
    }

    public static String getAveragePacketDelay(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePacketDelay")
        );
    }

    public static String getAveragePacketHops(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePacketHops")
        );
    }

    public static String getNumPayloadPacketsTransmitted(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("numPayloadPacketsTransmitted")
        );
    }

    public static String getPayloadThroughput(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("payloadThroughput")
        );
    }

    public static String getAveragePayloadPacketDelay(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePayloadPacketDelay")
        );
    }

    public static String getAveragePayloadPacketHops(NoCExperiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePayloadPacketHops")
        );
    }

    public static String getAverageFlitPerStateDelay(NoCExperiment experiment, FlitState state) {
        return String.format(
                "%s",
                experiment.getStats().get(String.format("averageFlitPerStateDelay::%s", state))
        );
    }

    public static String getMaxFlitPerStateDelay(NoCExperiment experiment, FlitState state) {
        return String.format(
                "%s",
                experiment.getStats().get(String.format("maxFlitPerStateDelay::%s", state))
        );
    }
}
