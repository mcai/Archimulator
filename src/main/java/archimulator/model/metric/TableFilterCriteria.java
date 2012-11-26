package archimulator.model.metric;

import net.pickapack.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableFilterCriteria {
    private List<String> columns;
    private List<Pair<String, String>> conditions;

    public TableFilterCriteria(String... columns) {
        this(Arrays.asList(columns));
    }

    public TableFilterCriteria(List<String> columns) {
        this.columns = columns;
        this.conditions = new ArrayList<Pair<String, String>>();
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Pair<String, String>> getConditions() {
        return conditions;
    }
}
