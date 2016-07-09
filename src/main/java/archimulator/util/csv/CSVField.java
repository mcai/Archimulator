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

    /**
     * Create a CSV field.
     *
     * @param name the name
     * @param func the function
     */
    public CSVField(String name, Function<ExperimentT, String> func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the function.
     *
     * @return the function
     */
    public Function<ExperimentT, String> getFunc() {
        return func;
    }
}
