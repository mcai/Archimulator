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

import net.pickapack.util.Pair;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Stacked bar graph.
 *
 * @author Min Cai
 */
public class StackedBarGraph extends BarGraph {
    /**
     * Stacked bar.
     */
    public class StackedBar {
        private String label;
        private List<Pair<String, Number>> stack;

        /**
         * Create a stacked bar.
         *
         * @param label the label
         */
        public StackedBar(String label) {
            this.label = label;
            this.stack = new ArrayList<>();
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
         * Get the stack.
         *
         * @return the stack
         */
        public List<Pair<String, Number>> getStack() {
            return stack;
        }
    }

    private List<StackedBar> stackedBars;

    /**
     * Create a stacked bar graph.
     *
     * @param xLabel the x label
     * @param yLabel the y label
     */
    public StackedBarGraph(String xLabel, String yLabel) {
        super(xLabel, yLabel);
        this.stackedBars = new ArrayList<>();
    }

    @Override
    protected void save(PrintWriter pw) {
        StringBuilder sb = new StringBuilder();
        sb.append("=stacked");

        for(Pair<String, Number> bar : stackedBars.get(0).getStack()) {
            sb.append(';').append(bar.getFirst());
        }
        pw.println(sb);

        pw.println("=table");

        pw.println();

        for(StackedBar bar : stackedBars) {
            StringBuilder sbBar = new StringBuilder();
            sbBar.append(bar.getLabel());
            for(Pair<String, Number> stackItem : bar.getStack()) {
                if(stackItem.getSecond().doubleValue() == 0) {
                    stackItem.setSecond(stackItem.getSecond().doubleValue() + 0.001);
                }

                sbBar.append("\t").append(stackItem.getSecond());
            }
            pw.println(sbBar.toString());
        }
    }

    @Override
    protected Pair<Double, Double> calcYMinMax() {
        return null;
    }

    /**
     * Get the list of stackedBars.
     *
     * @return the list of stackedBars
     */
    public List<StackedBar> getStackedBars() {
        return stackedBars;
    }
}