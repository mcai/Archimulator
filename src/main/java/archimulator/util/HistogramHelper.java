package archimulator.util;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class HistogramHelper {
    public static void main(String[] args) {
        double[] data = {1, 2, 2, 3, 3.5, 3, 4, 5, 6, 7, 8, 9};

        EmpiricalDistribution distribution = new EmpiricalDistribution(5);
        distribution.load(data);
        for (SummaryStatistics stats : distribution.getBinStats()) {
            if (stats.getN() > 0) {
                System.out.println("[" + stats.getMin() + "-" + stats.getMax() + "] = " + stats.getN());
            }
        }
    }
}
