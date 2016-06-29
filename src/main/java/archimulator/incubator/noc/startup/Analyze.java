package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;
import archimulator.incubator.noc.routers.FlitState;
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
    public static void main(String[] args) {
        List<CSVField> fields = new ArrayList<>();

        fields.add(new CSVField("Traffic", CSVFields::getTraffic));
        fields.add(new CSVField("Data Packet Injection Rate (packets/cycle/node)", CSVFields::getDataPacketInjectionRate));
        fields.add(new CSVField("Routing Algorithm", CSVFields::getRouting));
        fields.add(new CSVField("Selection Policy", CSVFields::getSelection));
        fields.add(new CSVField("Routing+Selection", CSVFields::getRoutingAndSelection));
        fields.add(new CSVField("Ant Packet Injection Rate (packets/cycle/node)", CSVFields::getAntPacketInjectionRate));
        fields.add(new CSVField("Alpha", CSVFields::getAcoSelectionAlpha));
        fields.add(new CSVField("Reinforcement Factor", CSVFields::getReinforcementFactor));
        fields.add(new CSVField("Routing+Selection/Alpha/Reinforcement Factor",
                CSVFields::getRoutingAndSelectionAndAcoSelectionAlphaAndReinforcementFactor));
        fields.add(new CSVField("Routing+Selection/Ant Packet Injection Rate/Alpha/Reinforcement Factor",
                CSVFields::getRoutingAndSelectionAndAntPacketInjectionRateAndAcoSelectionAlphaAndReinforcementFactor));
        fields.add(new CSVField("Simulation Time", CSVFields::getSimulationTime));
        fields.add(new CSVField("Total Cycles", CSVFields::getTotalCycles));
        fields.add(new CSVField("Packets Transmitted", CSVFields::getNumPacketsTransmitted));
        fields.add(new CSVField("Throughput (packets/cycle/node)", CSVFields::getThroughput));
        fields.add(new CSVField("Average Packet Delay (cycles)", CSVFields::getAveragePacketDelay));
        fields.add(new CSVField("Average Packet Hops", CSVFields::getAveragePacketHops));
        fields.add(new CSVField("Payload Packets Transmitted", CSVFields::getNumPayloadPacketsTransmitted));
        fields.add(new CSVField("Payload Throughput (packets/cycle/node)", CSVFields::getPayloadThroughput));
        fields.add(new CSVField("Average Payload Packet Delay (cycles)", CSVFields::getAveragePayloadPacketDelay));
        fields.add(new CSVField("Average Payload Packet Hops", CSVFields::getAveragePayloadPacketHops));

        for(FlitState state : FlitState.values()) {
            fields.add(new CSVField(String.format("Average Flit per State Delay::%s", state),
                    e -> CSVFields.getAverageFlitPerStateDelay(e, state)));
            fields.add(new CSVField(String.format("Max Flit per State Delay::%s", state),
                    e -> CSVFields.getMaxFlitPerStateDelay(e, state)));
        }

        for(String traffic : Common.trafficsAndDataPacketInjectionRates.keySet()) {
            Common.trafficsAndDataPacketInjectionRates.get(traffic).forEach(Experiment::loadStats);
            toCsv(String.format("results/trafficsAndDataPacketInjectionRates/t_%s.csv", traffic),
                    Common.trafficsAndDataPacketInjectionRates.get(traffic), fields);
        }

        Common.antPacketInjectionRates.forEach(Experiment::loadStats);
        toCsv("results/antPacketInjectionRates/t_transpose.csv",
                Common.antPacketInjectionRates, fields);

        Common.acoSelectionAlphasAndReinforcementFactors.forEach(Experiment::loadStats);
        toCsv("results/acoSelectionAlphasAndReinforcementFactors/t_transpose.csv",
                Common.acoSelectionAlphasAndReinforcementFactors, fields);
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

            for(Experiment experiment : results) {
                List<String> experimentData = new ArrayList<>();

                for(CSVField field : fields) {
                    experimentData.add(field.getFunc().apply(experiment));
                }

                printer.printRecord(experimentData);
            }

            printer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
