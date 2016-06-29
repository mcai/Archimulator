package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;
import archimulator.incubator.noc.util.Function;

/**
 * CSV field.
 *
 * @author Min Cai
 */
public class CSVField {
    private String name;

    private Function<String, Experiment> func;

    public CSVField(String name, Function<String, Experiment> func) {
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

    public Function<String, Experiment> getFunc() {
        return func;
    }
}
