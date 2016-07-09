package archimulator.util.csv;

import java.util.function.Function;

/**
 * CSV field.
 *
 * @author Min Cai
 */
public class CSVField<ExperimentT> {
    private String name;

    private Function<ExperimentT, String> func;

    public CSVField(String name, Function<ExperimentT, String> func) {
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

    public Function<ExperimentT, String> getFunc() {
        return func;
    }
}
