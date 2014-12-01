/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.plot;

import net.pickapack.action.Function1;
import net.pickapack.util.Pair;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static archimulator.util.plot.BarGraphHelper.*;

/**
 * Clustered bar graph.
 *
 * @author Min Cai
 */
public class ClusteredBarGraph extends BarGraph {
    /**
     * Cluster.
     */
    public class Cluster {
        private String label;
        private List<Pair<String, Number>> bars;

        /**
         * Create a cluster.
         *
         * @param label the label
         */
        public Cluster(String label) {
            this.label = label;
            this.bars = new ArrayList<>();
        }

        /**
         * Get the label.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Get the list of bars in the cluster.
         *
         * @return the list of bars in the cluster
         */
        public List<Pair<String, Number>> getBars() {
            return bars;
        }
    }

    private List<Cluster> clusters;

    /**
     * Create a clustered bar graph.
     *
     * @param xLabel the x label
     * @param yLabel the y label
     */
    public ClusteredBarGraph(String xLabel, String yLabel) {
        super(xLabel, yLabel);
        this.clusters = new ArrayList<>();
    }

    @Override
    protected void save(PrintWriter pw) {
        StringBuilder sb = new StringBuilder();
        sb.append("=cluster");

        for(Pair<String, Number> bar : clusters.get(0).getBars()) {
            sb.append(';').append(bar.getFirst());
        }
        pw.println(sb);

        pw.println("=table");

        pw.println();

        for(Cluster cluster : clusters) {
            StringBuilder sbCluster = new StringBuilder();
            sbCluster.append(cluster.getLabel());

            for(Pair<String, Number> bar : cluster.getBars()) {
                if(bar.getSecond().doubleValue() == 0) {
                    bar.setSecond(bar.getSecond().doubleValue() + 0.001);
                }

                sbCluster.append("\t").append(bar.getSecond());
            }

            pw.println(sbCluster.toString());
        }
    }

    @Override
    protected Pair<Double, Double> calcYMinMax() {
        Function1<Pair<String, Number>, Number> func = Pair<String, Number>::getSecond;

        List<Pair<String, Number>> bars = new ArrayList<>();
        for(Cluster cluster : clusters) {
            for(Pair<String, Number> bar : cluster.getBars()) {
                bars.add(bar);
            }
        }

        double lowest = getLowest(bars, func).doubleValue();
        double highest = getHighest(bars, func).doubleValue();

        if(getHorizontalLineYValue() != Double.NEGATIVE_INFINITY) {
            lowest = min(lowest, getHorizontalLineYValue());
            highest = max(highest, getHorizontalLineYValue());
        }

        double automaticYMin = getAutomaticYMin(lowest, highest);
        double automaticYMax = getAutomaticYMax(lowest, highest);

        return new Pair<>(automaticYMin, automaticYMax);
    }

    /**
     * Get the list of clusters.
     *
     * @return the list of clusters
     */
    public List<Cluster> getClusters() {
        return clusters;
    }
}
