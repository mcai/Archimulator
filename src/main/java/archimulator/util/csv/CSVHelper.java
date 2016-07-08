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
    public static <ResultT> void toCsv(String outputCSVFileName, List<ResultT> results, List<CSVField<ResultT>> fields) {
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

            for (ResultT result : results) {
                List<String> record = new ArrayList<>();

                for (CSVField<ResultT> field : fields) {
                    record.add(field.getFunc().apply(result));
                }

                printer.printRecord(record);
            }

            printer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
