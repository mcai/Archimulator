package archimulator.uncore.net.noc.startup;

import archimulator.uncore.net.noc.Experiment;
import archimulator.uncore.net.noc.routers.FlitState;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Analyze.
 *
 * @author Min Cai
 */
public class Analyze {
    private static List<CSVField> fields = new ArrayList<>();

    static {
        fields.add(new CSVField("Traffic", CSVFields::getTraffic));
        fields.add(new CSVField("Data_Packet_Injection_Rate_(packets/cycle/node)", CSVFields::getDataPacketInjectionRate));
        fields.add(new CSVField("Routing_Algorithm", CSVFields::getRouting));
        fields.add(new CSVField("Selection_Policy", CSVFields::getSelection));
        fields.add(new CSVField("Routing+Selection", CSVFields::getRoutingAndSelection));
        fields.add(new CSVField("Ant_Packet_Injection_Rate_(packets/cycle/node)", CSVFields::getAntPacketInjectionRate));
        fields.add(new CSVField("Alpha", CSVFields::getAcoSelectionAlpha));
        fields.add(new CSVField("Reinforcement_Factor", CSVFields::getReinforcementFactor));
        fields.add(new CSVField("Routing+Selection/Alpha/Reinforcement_Factor",
                CSVFields::getRoutingAndSelectionAndAcoSelectionAlphaAndReinforcementFactor));
        fields.add(new CSVField("Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                CSVFields::getRoutingAndSelectionAndAntPacketInjectionRateAndAcoSelectionAlphaAndReinforcementFactor));
        fields.add(new CSVField("Simulation_Time", CSVFields::getSimulationTime));
        fields.add(new CSVField("Total_Cycles", CSVFields::getTotalCycles));
        fields.add(new CSVField("Packets_Transmitted", CSVFields::getNumPacketsTransmitted));
        fields.add(new CSVField("Throughput_(packets/cycle/node)", CSVFields::getThroughput));
        fields.add(new CSVField("Average_Packet_Delay_(cycles)", CSVFields::getAveragePacketDelay));
        fields.add(new CSVField("Average_Packet_Hops", CSVFields::getAveragePacketHops));
        fields.add(new CSVField("Payload_Packets_Transmitted", CSVFields::getNumPayloadPacketsTransmitted));
        fields.add(new CSVField("Payload_Throughput_(packets/cycle/node)", CSVFields::getPayloadThroughput));
        fields.add(new CSVField("Average_Payload_Packet_Delay_(cycles)", CSVFields::getAveragePayloadPacketDelay));
        fields.add(new CSVField("Average_Payload_Packet_Hops", CSVFields::getAveragePayloadPacketHops));

        for (FlitState state : FlitState.values()) {
            fields.add(new CSVField(String.format("Average_Flit_per_State_Delay::%s", state),
                    e -> CSVFields.getAverageFlitPerStateDelay(e, state)));
            fields.add(new CSVField(String.format("Max_Flit_per_State_Delay::%s", state),
                    e -> CSVFields.getMaxFlitPerStateDelay(e, state)));
        }
    }

    public static void main(String[] args) {
        analyzeTrafficsAndDataPacketInjectionRates();

        analyzeAntPacketInjectionRates();

        analyzeAcoSelectionAlphasAndReinforcementFactors();
    }

    public static void analyzeTrafficsAndDataPacketInjectionRates() {
        for (String traffic : Common.trafficsAndDataPacketInjectionRates.keySet()) {
            Common.trafficsAndDataPacketInjectionRates.get(traffic).forEach(Experiment::loadStats);
            toCsv(String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    Common.trafficsAndDataPacketInjectionRates.get(traffic), fields);

            generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_throughput.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                    "Throughput_(packets/cycle/node)"
            );

            generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_packet_delay.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                    "Average_Packet_Delay_(cycles)"
            );

            generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_packet_hops.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                    "Average_Packet_Hops"
            );

            generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_payload_throughput.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                    "Payload_Throughput_(packets/cycle/node)"
            );

            generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_payload_packet_delay.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                    "Average_Payload_Packet_Delay_(cycles)"
            );

            generatePlot(
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    String.format("results/trafficsAndDataPacketInjectionRates/t_%s_average_payload_packet_hops.pdf", traffic),
                    "Data_Packet_Injection_Rate_(packets/cycle/node)",
                    "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                    "Average_Payload_Packet_Hops"
            );
        }
    }

    public static void analyzeAntPacketInjectionRates() {
        Common.antPacketInjectionRates.forEach(Experiment::loadStats);
        toCsv("results/antPacketInjectionRates/t_transpose.csv",
                Common.antPacketInjectionRates, fields);

        generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Throughput_(packets/cycle/node)"
        );

        generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Delay_(cycles)"
        );

        generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Hops"
        );

        generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Payload_Throughput_(packets/cycle/node)"
        );

        generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Delay_(cycles)"
        );

        generatePlot(
                "results/antPacketInjectionRates/t_transpose.csv",
                "results/antPacketInjectionRates/t_transpose_average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Hops"
        );
    }

    public static void analyzeAcoSelectionAlphasAndReinforcementFactors() {
        Common.acoSelectionAlphasAndReinforcementFactors.forEach(Experiment::loadStats);
        toCsv("results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                Common.acoSelectionAlphasAndReinforcementFactors, fields);

        generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Throughput_(packets/cycle/node)"
        );

        generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Delay_(cycles)"
        );

        generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Packet_Hops"
        );

        generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_payload_throughput.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Payload_Throughput_(packets/cycle/node)"
        );

        generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_payload_packet_delay.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Delay_(cycles)"
        );

        generatePlot(
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                "results/acoSelectionAlphasAndReinforcementFactors/t_transpose_average_payload_packet_hops.pdf",
                "Data_Packet_Injection_Rate_(packets/cycle/node)",
                "Routing+Selection/Ant_Packet_Injection_Rate/Alpha/Reinforcement_Factor",
                "Average_Payload_Packet_Hops"
        );
    }

    public static void toCsv(String outputCSVFileName, List<Experiment> results, List<CSVField> fields) {
        File resultDirFile = new File(outputCSVFileName).getParentFile();

        if (!resultDirFile.exists()) {
            if (!resultDirFile.mkdirs()) {
                throw new RuntimeException();
            }
        }

        CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',').withQuoteMode(QuoteMode.ALL).withQuote('"');

        try {
            FileWriter writer = new FileWriter(outputCSVFileName);
            CSVPrinter printer = new CSVPrinter(writer, format);
            printer.printRecord(fields);

            for (Experiment experiment : results) {
                List<String> experimentData = new ArrayList<>();

                for (CSVField field : fields) {
                    experimentData.add(field.getFunc().apply(experiment));
                }

                printer.printRecord(experimentData);
            }

            printer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generatePlot(String csvFileName, String plotFileName, String x, String hue, String y) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "tools/plots/plots.sh",
                    "--csv_file_name", csvFileName,
                    "--plot_file_name", plotFileName,
                    "--x", x,
                    "--hue", hue,
                    "--y", y
            ).inheritIO();

            pb.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
