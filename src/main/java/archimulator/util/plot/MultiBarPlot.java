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
package archimulator.util.plot;

/**
 * Multi bar plot object.
 *
 * @author Min Cai
 */
public class MultiBarPlot {
    /**
     * Sub chart.
     */
    public static interface SubChart {
    }

    /**
     * XY part.
     */
    public static class XY {
        private String x;
        private double y;

        /**
         * Create an XY part.
         *
         * @param x the X value
         * @param y the Y value
         */
        public XY(String x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Get the X value.
         *
         * @return the X value
         */
        public String getX() {
            return x;
        }

        /**
         * Get the Y value.
         *
         * @return the Y value
         */
        public double getY() {
            return y;
        }
    }

    /**
     * Bar.
     */
    public static class Bar implements SubChart {
        private String key;
        private XY[] values;

        /**
         * Create a bar.
         *
         * @param key    the key
         * @param values the values
         */
        public Bar(String key, XY[] values) {
            this.key = key;
            this.values = values;
        }

        /**
         * Get the key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Get the values.
         *
         * @return the values
         */
        public XY[] getValues() {
            return values;
        }
    }

    private String title;

    private SubChart[] subCharts;

    /**
     * Create a multi bar plot object.
     *
     * @param title     the title
     * @param subCharts an array of constituent sub charts.
     */
    public MultiBarPlot(String title, SubChart[] subCharts) {
        this.title = title;
        this.subCharts = subCharts;
    }

    /**
     * Get the title of the multi bar plot object.
     *
     * @return the title of the multi bar plot object
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the array of constituent sub charts.
     *
     * @return the array of constituent sub charts
     */
    public SubChart[] getSubCharts() {
        return subCharts;
    }
}
