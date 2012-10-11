package archimulator.util;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;

import java.io.File;
import java.util.Arrays;

public class MetaModelHelper {
    public static void main(String[] args) {
        DataContext dataContext = DataContextFactory.createCsvDataContext(new File("/home/itecgo/Desktop/mst_ht_2048_summary.csv"));

        DataSet dataSet = dataContext.query()
                .from("mst_ht_2048_summary")
                .select("L2 Repl")
                .where("L2 Size").eq("96 KB")
                .execute();

        for(Row row : dataSet) {
            System.out.println(Arrays.toString(row.getValues()));
        }
    }
}
