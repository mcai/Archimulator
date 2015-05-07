package archimulator.util;

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentSummary;
import archimulator.service.ServiceManager;
import archimulator.util.plot.Table;
import archimulator.util.plot.TableFilterCriteria;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExperimentTableHelper {
    /**
     * Normalize the table.
     *
     * @param table the table
     */
    public static void normalize(Table table) {
        for (List<String> row : table.getRows()) {
            for (String cell : row) {
                if (cell.startsWith("S=")) {
                    row.set(row.indexOf(cell), cell.replaceFirst("S=", ""));
                } else if (cell.startsWith("L=")) {
                    row.set(row.indexOf(cell), cell.replaceFirst("L=", ""));
                } else if (cell.startsWith("P=")) {
                    row.set(row.indexOf(cell), cell.replaceFirst("P=", ""));
                }
            }
        }
    }

    /**
     * Generate the CSV file.
     *
     * @param criteria                   the criteria
     * @param currentExperimentPackTitle the current experiment pack title
     * @param experimentPackTitles       the array of experiment pack titles
     */
    public static Table table(
            TableFilterCriteria criteria,
            String currentExperimentPackTitle,
            String... experimentPackTitles
    ) {
        List<Experiment> experiments = new ArrayList<>();

        for (String experimentPackTitle : experimentPackTitles) {
            ExperimentPack experimentPackFound = ServiceManager.getExperimentService().getExperimentPackByTitle(experimentPackTitle);

            if (experimentPackFound == null) {
                List<ExperimentPack> allExperimentPacks = ServiceManager.getExperimentService().getAllExperimentPacks();
                allExperimentPacks.forEach(expPack -> System.out.println("Found: " + expPack.getTitle()));

                throw new IllegalArgumentException(experimentPackTitle);
            }

            experiments.addAll(ServiceManager.getExperimentService().getExperimentsByParent(experimentPackFound));
        }

        sort(experiments);

        ExperimentPack currentExperimentPack = ServiceManager.getExperimentService().getExperimentPackByTitle(currentExperimentPackTitle);

        return ServiceManager.getExperimentStatService().tableSummary(currentExperimentPack, experiments).filter(criteria);
    }

    /**
     * Sort the specified list of experiments.
     *
     * @param experiments   the list of experiments to be sorted
     * @param propertyNames the array of property names
     */
    @SuppressWarnings("unchecked")
    public static void sort(List<Experiment> experiments, final String... propertyNames) {
        SortHelper.sort(experiments, new ArrayList<Function<Experiment, Comparable>>() {{
            for (String propertyName : propertyNames) {
                add(experiment -> {
                    try {
                        ExperimentSummary summary = ServiceManager.getExperimentStatService().getSummaryByParent(experiment);
                        return (Comparable) PropertyUtils.getProperty(summary, propertyName);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                });
            }
        }});
    }

    /**
     * Sort the specified list of experiments.
     *
     * @param experiments the list of experiments to be sorted
     */
    @SuppressWarnings("unchecked")
    public static void sort(List<Experiment> experiments) {
        sort(experiments, "helperThreadStride", "helperThreadLookahead", "l2Size", "l2Associativity", "l2ReplacementPolicyType", "numMainThreadWaysInStaticPartitionedLRUPolicy");
    }
}
