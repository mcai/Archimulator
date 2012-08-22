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
package archimulator.util.chart;

import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.service.ExperimentService;
import archimulator.service.ServiceManager;
import net.pickapack.StorageUnit;
import net.pickapack.action.Function1;
import net.pickapack.action.Predicate;
import net.pickapack.chart.BarPlot;
import net.pickapack.chart.BarPlotFrame;
import net.pickapack.chart.SubBarPlot;
import org.jfree.ui.RefineryUtilities;

public class ExperimentBarPlotHelper {
    private static Double getExperimentProfileStat(Experiment experiment, String key) {
        String value = experiment.getStats().get(key);
        if (value != null) {
            value = value.replaceAll(",", "");
            return (double) (int) (Double.valueOf(value).doubleValue());
        }
        return 0.0;
    }

    private static BarPlot<Experiment> createHTLLCRequestDistributionBarPlot(final ExperimentService experimentService) {
        BarPlot<Experiment> barPlot = new BarPlot<Experiment>("HT LLC Request Distribution", "# HT LLC Requests", new Predicate<Experiment>() {
            @Override
            public boolean apply(Experiment experiment) {
                return experiment.getState() == ExperimentState.COMPLETED && experiment.getTitle().contains("_LRU") && experiment.getTitle().contains("mst_ht");
            }
        }, experimentService.getAllExperiments());

        Function1<Experiment, String> getTitleCallback = new Function1<Experiment, String>() {
            @Override
            public String apply(Experiment experiment) {
                ContextMapping contextMapping = experiment.getContextMappings().get(0);
                return contextMapping.getHelperThreadLookahead() + ", " + contextMapping.getHelperThreadStride() + ", " + StorageUnit.toString(experiment.getArchitecture().getL2Size()) + ", " + experiment.getArchitecture().getL2Associativity();
            }
        };

        barPlot.getSubBarPlots().add(new SubBarPlot<Experiment>("Total HT LLC Request", new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return getExperimentProfileStat(experiment, "detailed/htllcRequestProfilingHelper/numTotalHTLLCRequests");
            }
        }, getTitleCallback));

        barPlot.getSubBarPlots().add(new SubBarPlot<Experiment>("Good HT LLC Request", new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experimentProfile) {
                return getExperimentProfileStat(experimentProfile, "detailed/htllcRequestProfilingHelper/numTimelyHTLLCRequests");
            }
        }, getTitleCallback));
        barPlot.getSubBarPlots().add(new SubBarPlot<Experiment>("Late HT LLC Request", new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experimentProfile) {
                return getExperimentProfileStat(experimentProfile, "detailed/htllcRequestProfilingHelper/numLateHTLLCRequests");
            }
        }, getTitleCallback));
        barPlot.getSubBarPlots().add(new SubBarPlot<Experiment>("Bad HT LLC Request", new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experimentProfile) {
                return getExperimentProfileStat(experimentProfile, "detailed/htllcRequestProfilingHelper/numBadHTLLCRequests");
            }
        }, getTitleCallback));
        barPlot.getSubBarPlots().add(new SubBarPlot<Experiment>("Ugly HT LLC Request", new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experimentProfile) {
                return getExperimentProfileStat(experimentProfile, "detailed/htllcRequestProfilingHelper/numUglyHTLLCRequests");
            }
        }, getTitleCallback));
        return barPlot;
    }

    public static void main(String[] args) {
        ExperimentService experimentService = ServiceManager.getExperimentService();

        BarPlot<Experiment> barPlot = createHTLLCRequestDistributionBarPlot(experimentService);
        BarPlotFrame<Experiment> barPlotFrame = new BarPlotFrame<Experiment>(barPlot, "Experiment", 500, 470);
        barPlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(barPlotFrame);
        barPlotFrame.setVisible(true);
    }
}
