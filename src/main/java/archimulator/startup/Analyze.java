package archimulator.startup;

import archimulator.common.CPUExperiment;
import archimulator.uncore.noc.routers.FlitState;
import archimulator.util.csv.CSVField;
import archimulator.util.csv.CSVHelper;
import archimulator.util.plots.PlotHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyze.
 *
 * @author Min Cai
 */
public class Analyze {
    private static List<CSVField<CPUExperiment>> csvFields = new ArrayList<>();

    static {
        csvFields.add(new CSVField<>("Routing_Algorithm", CSVFields::getRouting));
        csvFields.add(new CSVField<>("Selection_Policy", CSVFields::getSelection));
        csvFields.add(new CSVField<>("Routing+Selection", CSVFields::getRoutingAndSelection));
        csvFields.add(new CSVField<>("Ant_Packet_Injection_Rate_(packets/cycle/node)", CSVFields::getAntPacketInjectionRate));
        csvFields.add(new CSVField<>("Alpha", CSVFields::getAcoSelectionAlpha));
        csvFields.add(new CSVField<>("Reinforcement_Factor", CSVFields::getReinforcementFactor));
        csvFields.add(new CSVField<>("NoC_Routing_Solution", CSVFields::getNoCRoutingSolution));
        csvFields.add(new CSVField<>("Simulation_Time", CSVFields::getSimulationTime));
        csvFields.add(new CSVField<>("Total_Cycles", CSVFields::getTotalCycles));
        csvFields.add(new CSVField<>("Packets_Transmitted", CSVFields::getNumPacketsTransmitted));
        csvFields.add(new CSVField<>("Throughput_(packets/cycle/node)", CSVFields::getThroughput));
        csvFields.add(new CSVField<>("Avg._Packet_Delay_(cycles)", CSVFields::getAveragePacketDelay));
        csvFields.add(new CSVField<>("Avg._Packet_Hops", CSVFields::getAveragePacketHops));
        csvFields.add(new CSVField<>("Payload_Packets_Transmitted", CSVFields::getNumPayloadPacketsTransmitted));
        csvFields.add(new CSVField<>("Payload_Throughput_(packets/cycle/node)", CSVFields::getPayloadThroughput));
        csvFields.add(new CSVField<>("Avg._Payload_Packet_Delay_(cycles)", CSVFields::getAveragePayloadPacketDelay));
        csvFields.add(new CSVField<>("Avg._Payload_Packet_Hops", CSVFields::getAveragePayloadPacketHops));

        for (FlitState state : FlitState.values()) {
            csvFields.add(new CSVField<>(String.format("Average_Flit_per_State_Delay::%s", state),
                    e -> CSVFields.getAverageFlitPerStateDelay(e, state)));
            csvFields.add(new CSVField<>(String.format("Max_Flit_per_State_Delay::%s", state),
                    e -> CSVFields.getMaxFlitPerStateDelay(e, state)));
        }
    }

    public static void main(String[] args) {
        analyzeAntPacketInjectionRates();

        analyzeAcoSelectionAlphasAndReinforcementFactors();
    }

    public static void analyzeAntPacketInjectionRates() {
        Experiments.antPacketInjectionRates.forEach(CPUExperiment::loadStats);
        CSVHelper.toCsv(
                "results/antPacketInjectionRates/result.csv",
                Experiments.antPacketInjectionRates,
                csvFields
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/throughput.pdf",
                "NoC_Routing_Solution",
                null,
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_packet_delay.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_packet_hops.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Packet_Hops"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/payload_throughput.pdf",
                "NoC_Routing_Solution",
                null,
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_payload_packet_delay.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_payload_packet_hops.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Payload_Packet_Hops"
        );
    }

    public static void analyzeAcoSelectionAlphasAndReinforcementFactors() {
        Experiments.acoSelectionAlphasAndReinforcementFactors.forEach(CPUExperiment::loadStats);
        CSVHelper.toCsv(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                Experiments.acoSelectionAlphasAndReinforcementFactors,
                csvFields
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/throughput.pdf",
                "NoC_Routing_Solution",
                null,
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_packet_delay.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_packet_hops.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Packet_Hops"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/payload_throughput.pdf",
                "NoC_Routing_Solution",
                null,
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_payload_packet_delay.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_payload_packet_hops.pdf",
                "NoC_Routing_Solution",
                null,
                "Avg._Payload_Packet_Hops"
        );
    }
}
