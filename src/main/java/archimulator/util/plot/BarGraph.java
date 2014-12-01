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

import net.pickapack.io.cmd.CommandLineHelper;
import net.pickapack.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Bar graph base class.
 *
 * @author Min cai
 */
public abstract class BarGraph implements Graph {
    /**
     * Y format.
     */
    public enum YFormat {
        /**
         * Long.
         */
        LONG,

        /**
         * Long scientific.
         */
        LONG_SCIENTIFIC,

        /**
         * Double.
         */
        DOUBLE,

        /**
         * Double scientific.
         */
        DOUBLE_SCIENTIFIC;

        /**
         * Get a value indicating whether it is double or not.
         *
         * @return a value indicating whether it is double or not
         */
        public boolean isDouble() {
            return this == DOUBLE || this == DOUBLE_SCIENTIFIC;
        }

        /**
         * Get a value indicating whether it is scientific or not.
         *
         * @return a value indicating whether it is scientific or not
         */
        public boolean isScientific() {
            return this == LONG_SCIENTIFIC || this == DOUBLE_SCIENTIFIC;
        }

        public String getFormatString() {
            return "%10.10g";
        }
    }

    private String xLabel;
    private String yLabel;

    private double xScale;
    private double yScale;

    private double min;
    private double max;

    private boolean patterns;

    private double horizontalLineYValue;

    private YFormat yFormat;

    private int fontSize;

    private double yLabelShift;

    private boolean noRotate;

    protected BarGraph(String xLabel, String yLabel) {
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        this.xScale = 1.0;
        this.yScale = 1.0;

        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;

        this.horizontalLineYValue = Double.NEGATIVE_INFINITY;

        this.yFormat = YFormat.DOUBLE_SCIENTIFIC;

        this.fontSize = 14;

        this.yLabelShift = 2.0;
    }

    /**
     * Save itself in the specified file.
     *
     * @param pw the print writer
     */
    protected abstract void save(PrintWriter pw);

    @Override
    public void plot(String outputFileNamePrefix) {
        try {
            File tempFile = File.createTempFile("archimulator", "bargraph");
            try {
                PrintWriter pw = new PrintWriter(tempFile.getAbsolutePath());

                pw.println("fontsz=" + fontSize);
                pw.println("legendfontsz=8");

                pw.println("yformat=" + yFormat.getFormatString());

                if(noRotate) {
                    pw.println("=norotate");
                }

                pw.println("=nogridy");
                pw.println("=noupperright");
                pw.println("=nolegend");
//                pw.println("legendx=right");
//                pw.println("legendy=center");
//                pw.println("=nolegoutline");
//                pw.println("legendfill=");

                pw.println("=nocommas");

//                pw.println("xticshift=0,-0.7");

                pw.println("ylabelshift=" + yLabelShift + ",0");

                if(patterns) {
                    pw.println("=patterns");
                }

                if(horizontalLineYValue != Double.NEGATIVE_INFINITY) {
                    pw.println("horizline=" + horizontalLineYValue);
                }

                pw.println("xlabel=" + getxLabel());
                pw.println("ylabel=" + getyLabel());

                pw.println("xscale=" + getxScale());
                pw.println("yscale=" + getyScale());

                Pair<Double, Double> automaticYMinMax = calcYMinMax();

                if(getMin() == Double.POSITIVE_INFINITY) {
                    if(automaticYMinMax != null) {
                        setMin(automaticYMinMax.getFirst());
                    }
                }

                if(getMax() == Double.NEGATIVE_INFINITY) {
                    if(automaticYMinMax != null) {
                        setMax(automaticYMinMax.getSecond());
                    }
                }

                if(getMin() == getMax()) {
                    double value = getMin();

                    setMin(value - 10);
                    setMax(value + 10);
                }

                if(getMin() != Double.POSITIVE_INFINITY) {
                    if(this.yFormat.isDouble()) {
                        pw.println("min=" + getMin());
                    }
                    else {
                        pw.println("min=" + ((Double)getMin()).longValue());
                    }
                }

                if(getMax() != Double.NEGATIVE_INFINITY) {
                    if(this.yFormat.isDouble()) {
                        pw.println("max=" + getMax());
                    }
                    else {
                        pw.println("max=" + ((Double)getMax()).longValue());
                    }
                }

                pw.println();

                save(pw);

                pw.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            File file = new File(outputFileNamePrefix +
                    ".eps");

            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new RuntimeException();
                }
            }

            CommandLineHelper.invokeShellCommand("perl tools/bargraph/bargraph.pl " +
                    tempFile.getAbsolutePath() +
                    " > " +
                    outputFileNamePrefix +
                    ".eps", true);

            tempFile.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Pair<Double, Double> calcYMinMax();

    /**
     * Get the x label.
     *
     * @return the x label
     */
    public String getxLabel() {
        return xLabel;
    }

    /**
     * Get the y label.
     *
     * @return the y label
     */
    public String getyLabel() {
        return yLabel;
    }

    /**
     * Get the x scale.
     *
     * @return the x scale
     */
    public double getxScale() {
        return xScale;
    }

    /**
     * Set the x scale.
     *
     * @param xScale the x scale
     */
    public void setxScale(double xScale) {
        this.xScale = xScale;
    }

    /**
     * Get the y scale.
     *
     * @return the y scale
     */
    public double getyScale() {
        return yScale;
    }

    /**
     * Set the y scale.
     *
     * @param yScale the y scale
     */
    public void setyScale(double yScale) {
        this.yScale = yScale;
    }

    /**
     * Get the y min.
     *
     * @return the y min
     */
    public double getMin() {
        return min;
    }

    /**
     * Set the y min.
     *
     * @param min the y min
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * Get the y max.
     *
     * @return the y max
     */
    public double getMax() {
        return max;
    }

    /**
     * Set the y max.
     *
     * @param max the y max
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * Get a value indicating whether patterns are used or not.
     *
     * @return a value indicating whether patterns are used or not
     */
    public boolean isPatterns() {
        return patterns;
    }

    /**
     * Set a value indicating whether patterns are used or not.
     *
     * @param patterns a value indicating whether patterns are used or not
     */
    public void setPatterns(boolean patterns) {
        this.patterns = patterns;
    }

    /**
     * Get the y value of the horizontal line.
     *
     * @return the y value of the horizontal line
     */
    public double getHorizontalLineYValue() {
        return horizontalLineYValue;
    }

    /**
     * Set the y value of the horizontal line.
     *
     * @param horizontalLineYValue the y value of the horizontal line
     */
    public void setHorizontalLineYValue(double horizontalLineYValue) {
        this.horizontalLineYValue = horizontalLineYValue;
    }

    /**
     * Get the y format.
     *
     * @return the y format
     */
    public YFormat getyFormat() {
        return yFormat;
    }

    /**
     * Set the y format.
     *
     * @param yFormat the y format
     */
    public void setyFormat(YFormat yFormat) {
        this.yFormat = yFormat;
    }

    /**
     * Get the font size.
     *
     * @return the font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Set the font size.
     *
     * @param fontSize the font size
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Get the y label shift.
     *
     * @return the y label shift
     */
    public double getyLabelShift() {
        return yLabelShift;
    }

    /**
     * Set the y label shift.
     *
     * @param yLabelShift the y label shift
     */
    public void setyLabelShift(double yLabelShift) {
        this.yLabelShift = yLabelShift;
    }

    public boolean isNoRotate() {
        return noRotate;
    }

    public void setNoRotate(boolean noRotate) {
        this.noRotate = noRotate;
    }
}
