package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;
import archimulator.incubator.noc.routers.FlitState;

/**
 * CSV fields.
 *
 * @author Min Cai
 */
public class CSVFields {
    public static String getTraffic(Experiment experiment) {
        return experiment.getConfig().getTraffic();
    }

    public static String getDataPacketInjectionRate(Experiment experiment) {
        return String.format("%s", experiment.getConfig().getDataPacketInjectionRate());
    }

    public static String getRouting(Experiment experiment) {
        return experiment.getConfig().getRouting();
    }

    public static String getSelection(Experiment experiment) {
        return experiment.getConfig().getSelection();
    }

    public static String getRoutingAndSelection(Experiment experiment) {
        return String.format(
                "%s+%s",
                getRouting(experiment),
                getSelection(experiment)
        );
    }

    public static String getAntPacketInjectionRate(Experiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format("%s", experiment.getConfig().getAntPacketInjectionRate());
        }

        return "";
    }

    public static String getAcoSelectionAlpha(Experiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s",
                    experiment.getConfig().getAcoSelectionAlpha()
            );
        }

        return "";
    }

    public static String getReinforcementFactor(Experiment experiment) {
        if(experiment.getConfig().getSelection().equals("aco")) {
            return String.format(
                    "%s",
                    experiment.getConfig().getReinforcementFactor()
            );
        }

        return "";
    }

    public static String getRoutingAndSelectionAndAcoSelectionAlphaAndReinforcementFactor(Experiment experiment) {
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

    public static String getRoutingAndSelectionAndAntPacketInjectionRateAndAcoSelectionAlphaAndReinforcementFactor(Experiment experiment) {
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

    public static String getSimulationTime(Experiment experiment) {
        return (String) experiment.getStats().get("simulationTime");
    }

    public static String getTotalCycles(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("totalCycles")
        );
    }

    public static String getNumPacketsTransmitted(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("numPacketsTransmitted")
        );
    }

    public static String getThroughput(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("throughput")
        );
    }

    public static String getAveragePacketDelay(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePacketDelay")
        );
    }

    public static String getAveragePacketHops(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePacketHops")
        );
    }

    public static String getNumPayloadPacketsTransmitted(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("numPayloadPacketsTransmitted")
        );
    }

    public static String getPayloadThroughput(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("payloadThroughput")
        );
    }

    public static String getAveragePayloadPacketDelay(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePayloadPacketDelay")
        );
    }

    public static String getAveragePayloadPacketHops(Experiment experiment) {
        return String.format(
                "%s",
                experiment.getStats().get("averagePayloadPacketHops")
        );
    }

    public static String getAverageFlitPerStateDelay(Experiment experiment, FlitState state) {
        return String.format(
                "%s",
                experiment.getStats().get(String.format("averageFlitPerStateDelay::%s", state))
        );
    }

    public static String getMaxFlitPerStateDelay(Experiment experiment, FlitState state) {
        return String.format(
                "%s",
                experiment.getStats().get(String.format("maxFlitPerStateDelay::%s", state))
        );
    }
}
