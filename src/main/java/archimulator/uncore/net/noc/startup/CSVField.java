package archimulator.uncore.net.noc.startup;

import archimulator.uncore.net.noc.NoCExperiment;
import archimulator.uncore.net.noc.util.Function;

/**
 * CSV field.
 *
 * @author Min Cai
 */
public class CSVField {
    private String name;

    private Function<String, NoCExperiment> func;

    public CSVField(String name, Function<String, NoCExperiment> func) {
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

    public Function<String, NoCExperiment> getFunc() {
        return func;
    }
}
