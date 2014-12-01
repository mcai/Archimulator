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
 * Simple bar graph.
 *
 * @author Min cai
 */
public class SimpleBarGraph extends BarGraph {
    private List<Pair<String, Number>> bars;

    /**
     * Create a simple bar graph.
     *
     * @param xLabel the x label
     * @param yLabel the y label
     */
    public SimpleBarGraph(String xLabel, String yLabel) {
        super(xLabel, yLabel);
        this.bars = new ArrayList<>();
    }

    @Override
    protected void save(PrintWriter pw) {
        pw.println("=table");

        for(Pair<String, Number> bar : bars) {
//            if(bar.getSecond().doubleValue() == 0) {
//                bar.setSecond(bar.getSecond().doubleValue() + 0.001);
//            }

            pw.println(bar.getFirst() + "\t" + bar.getSecond());
        }
    }

    @Override
    protected Pair<Double, Double> calcYMinMax() {
        Function1<Pair<String, Number>, Number> func = Pair<String, Number>::getSecond;

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
     * Get the list of bars.
     *
     * @return the list of bars
     */
    public List<Pair<String, Number>> getBars() {
        return bars;
    }
}
