/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Histogram helper.
 *
 * @author Min Cai
 */
public class HistogramHelper {
    /**
     * Entry point.
     *
     * @param args the arguments
     */
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
