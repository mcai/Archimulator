package archimulator.util.csv;

import archimulator.uncore.net.noc.util.Function;

/**
 * CSV field.
 *
 * @author Min Cai
 */
public class CSVField<ExperimentT> {
    private String name;

    private Function<String, ExperimentT> func;

    public CSVField(String name, Function<String, ExperimentT> func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Function<String, ExperimentT> getFunc() {
        return func;
    }
}
