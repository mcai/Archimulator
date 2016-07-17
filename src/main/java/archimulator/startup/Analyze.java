package archimulator.startup;

import archimulator.common.Experiment;
import archimulator.util.csv.CSVHelper;
import archimulator.util.plots.PlotHelper;

/**
 * Analyze.
 *
 * @author Min Cai
 */
public class Analyze {
    public static void main(String[] args) {
        analyzeTrafficsAndDataPacketInjectionRates();

        analyzeAntPacketInjectionRates();

        analyzeAcoSelectionAlphasAndReinforcementFactors();
    }

    public static void analyzeTrafficsAndDataPacketInjectionRates() {
        for (String traffic : Experiments.trafficsAndDataPacketInjectionRates.keySet()) {
            Experiments.trafficsAndDataPacketInjectionRates.get(traffic).forEach(Experiment::loadStats);
            CSVHelper.toCsv(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    Experiments.trafficsAndDataPacketInjectionRates.get(traffic),
                    CSVFields.csvFields
            );

            PlotHelper.generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_throughput.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "NoC_Routing_Solution",
                    "Throughput_(packets/cycle/node)"
            );

            PlotHelper.generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_packet_delay.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "NoC_Routing_Solution",
                    "Avg._Packet_Delay_(cycles)"
            );

            PlotHelper.generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_packet_hops.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "NoC_Routing_Solution",
                    "Avg._Packet_Hops"
            );

            PlotHelper.generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_payload_throughput.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "NoC_Routing_Solution",
                    "Payload_Throughput_(packets/cycle/node)"
            );

            PlotHelper.generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_payload_packet_delay.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "NoC_Routing_Solution",
                    "Avg._Payload_Packet_Delay_(cycles)"
            );

            PlotHelper.generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_payload_packet_hops.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "NoC_Routing_Solution",
                    "Avg._Payload_Packet_Hops"
            );
        }
    }

    public static void analyzeAntPacketInjectionRates() {
        Experiments.antPacketInjectionRates.forEach(Experiment::loadStats);
        CSVHelper.toCsv(
                "results/antPacketInjectionRates/result.csv",
                Experiments.antPacketInjectionRates,
                CSVFields.csvFields
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Packet_Hops"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/antPacketInjectionRates/result.csv",
                "results/antPacketInjectionRates/average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Payload_Packet_Hops"
        );
    }

    public static void analyzeAcoSelectionAlphasAndReinforcementFactors() {
        Experiments.acoSelectionAlphasAndReinforcementFactors.forEach(Experiment::loadStats);
        CSVHelper.toCsv(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                Experiments.acoSelectionAlphasAndReinforcementFactors,
                CSVFields.csvFields
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Packet_Hops"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Payload_Throughput_(packets/cycle/node)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Payload_Packet_Delay_(cycles)"
        );

        PlotHelper.generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/result.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "NoC_Routing_Solution",
                "Avg._Payload_Packet_Hops"
        );
    }
}
