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

import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.service.ExperimentService;
import archimulator.service.ServiceManager;
import net.pickapack.action.Function;
import net.pickapack.action.Predicate;
import net.pickapack.chart.LinePlot;
import net.pickapack.chart.LinePlotFrame;
import net.pickapack.chart.SubLinePlot;
import net.pickapack.chart.SubLinePlotLine;
import org.jfree.ui.RefineryUtilities;

import java.util.List;

public class ExperimentLinePlotHelper {
    public static void addExperimentSubLinePlot(final ExperimentService experimentService, LinePlot linePlot, String titleY, final String key) {
        addExperimentSubLinePlot(experimentService, linePlot, titleY, key, new Predicate<Experiment>() {
            @Override
            public boolean apply(Experiment experiment) {
                return true;
            }
        });
    }

    public static void addExperimentSubLinePlot(final ExperimentService experimentService, LinePlot linePlot, String titleY, final String key, Predicate<Experiment> experimentPred) {
        SubLinePlot subLinePlot = new SubLinePlot(titleY);

        List<Experiment> experiments = experimentService.getAllExperiments();
        for (final Experiment experiment : experiments) {
            if (experimentPred.apply(experiment)) {
                subLinePlot.getLines().add(new SubLinePlotLine("Exp #" + experiment.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        String value = experiment.getStats().get(key);
                        if (value != null) {
                            value = value.replaceAll(",", "");
                            return (double) (int) (Double.valueOf(value).doubleValue());
                        }
                        return 0.0;
                    }
                }));
            }
        }
        linePlot.getSubLinePlots().add(subLinePlot);
    }

    public static void main(String[] args) {
        ExperimentService experimentService = ServiceManager.getExperimentService();

        LinePlot linePlot = new LinePlot("Experiment Stats - Archimulator");

        addExperimentSubLinePlot(experimentService, linePlot, "Insts per Second", "detailed/instsPerSecond", new Predicate<Experiment>() {
            @Override
            public boolean apply(Experiment experiment) {
                return experiment.getState() == ExperimentState.COMPLETED;
            }
        });
        addExperimentSubLinePlot(experimentService, linePlot, "Cycles per Second", "detailed/cyclesPerSecond", new Predicate<Experiment>() {
            @Override
            public boolean apply(Experiment experimentProfile) {
                return experimentProfile.getState() == ExperimentState.COMPLETED;
            }
        });

        LinePlotFrame linePlotFrame = new LinePlotFrame(linePlot, 500, 470);
        linePlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(linePlotFrame);
        linePlotFrame.setVisible(true);
    }
}
