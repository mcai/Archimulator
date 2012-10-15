/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.model.metric;

/**
 * @author Min Cai
 */
public class MultiBarPlot {
    public static interface SubChart {
    }

    public static class XY {
        private String x;
        private double y;

        public XY(String x, double y) {
            this.x = x;
            this.y = y;
        }

        public String getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    public static class Bar implements SubChart {
        private String key;
        private XY[] values;

        public Bar(String key, XY[] values) {
            this.key = key;
            this.values = values;
        }

        public String getKey() {
            return key;
        }

        public XY[] getValues() {
            return values;
        }
    }

    private String title;

    private SubChart[] subCharts;

    public MultiBarPlot(String title, SubChart[] subCharts) {
        this.title = title;
        this.subCharts = subCharts;
    }

    public String getTitle() {
        return title;
    }

    public SubChart[] getSubCharts() {
        return subCharts;
    }
}
