package archimulator.startup;

import archimulator.common.Experiment;
import archimulator.uncore.net.noc.routers.FlitState;
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
    private static List<CSVField<Experiment>> csvFields = new ArrayList<>();

    static {
        csvFields.add(new CSVField<>("Data_Packet_Injection_Rate_(packets/cycle/node)", CSVFields::getDataPacketInjectionRate));
        csvFields.add(new CSVField<>("Routing_Algorithm", CSVFields::getRouting));
        csvFields.add(new CSVField<>("Selection_Policy", CSVFields::getSelection));
        csvFields.add(new CSVField<>("Routing+Selection", CSVFields::getRoutingAndSelection));
        csvFields.add(new CSVField<>("Ant_Packet_Injection_Rate_(packets/cycle/node)", CSVFields::getAntPacketInjectionRate));
        csvFields.add(new CSVField<>("Alpha", CSVFields::getAcoSelectionAlpha));
        csvFields.add(new CSVField<>("Reinforcement_Factor", CSVFields::getReinforcementFactor));
        csvFields.add(new CSVField<>("Routing+Selection/Alpha/Reinforcement_Factor",
                CSVFields::getRoutingAndSelectionAndAcoSelectionAlphaAndReinforcementFactor));
        csvFields.add(new CSVField<>("Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                CSVFields::getRoutingAndSelectionAndAntPacketInjectionRateAndAcoSelectionAlphaAndReinforcementFactor));
        csvFields.add(new CSVField<>("Simulation_Time", CSVFields::getSimulationTime));
        csvFields.add(new CSVField<>("Total_Cycles", CSVFields::getTotalCycles));
        csvFields.add(new CSVField<>("Packets_Transmitted", CSVFields::getNumPacketsTransmitted));
        csvFields.add(new CSVField<>("Throughput_(packets/cycle/node)", CSVFields::getThroughput));
        csvFields.add(new CSVField<>("Average_Packet_Delay_(cycles)", CSVFields::getAveragePacketDelay));
        csvFields.add(new CSVField<>("Average_Packet_Hops", CSVFields::getAveragePacketHops));
        csvFields.add(new CSVField<>("Payload_Packets_Transmitted", CSVFields::getNumPayloadPacketsTransmitted));
        csvFields.add(new CSVField<>("Payload_Throughput_(packets/cycle/node)", CSVFields::getPayloadThroughput));
        csvFields.add(new CSVField<>("Average_Payload_Packet_Delay_(cycles)", CSVFields::getAveragePayloadPacketDelay));
        csvFields.add(new CSVField<>("Average_Payload_Packet_Hops", CSVFields::getAveragePayloadPacketHops));

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
        Experiments.antPacketInjectionRates.forEach(Experiment::loadStats);
        CSVHelper.toCsv(
                "results/antPacketInjectionRates/t_transpose.csv",
                Experiments.antPacketInjectionRates,
                csvFields
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Hops"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Hops"
        );
    }

    public static void analyzeAcoSelectionAlphasAndReinforcementFactors() {
        Experiments.acoSelectionAlphasAndReinforcementFactors.forEach(Experiment::loadStats);
        CSVHelper.toCsv(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                Experiments.acoSelectionAlphasAndReinforcementFactors,
                csvFields
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Hops"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Hops"
        );
    }
}
