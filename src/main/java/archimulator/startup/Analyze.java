/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
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
