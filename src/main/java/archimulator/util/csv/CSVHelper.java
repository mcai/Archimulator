package archimulator.util.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV helper.
 *
 * @author Min Cai
 */
public class CSVHelper {
    public static <ExperimentT> void toCsv(String outputCSVFileName, List<ExperimentT> results, List<CSVField<ExperimentT>> fields) {
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

            for (ExperimentT experiment : results) {
                List<String> experimentData = new ArrayList<>();

                for (CSVField<ExperimentT> field : fields) {
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
