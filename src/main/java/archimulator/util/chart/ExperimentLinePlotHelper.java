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

import archimulator.client.ManagementStartup;
import archimulator.service.ArchimulatorService;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import com.caucho.hessian.client.HessianProxyFactory;
import net.pickapack.DateHelper;
import net.pickapack.action.Function;
import net.pickapack.action.Predicate;
import net.pickapack.chart.LinePlot;
import net.pickapack.chart.LinePlotFrame;
import net.pickapack.chart.SubLinePlot;
import net.pickapack.chart.SubLinePlotLine;
import org.jfree.ui.RefineryUtilities;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ExperimentLinePlotHelper {
    public static void addExperimentSubLinePlot(final ArchimulatorService archimulatorService, LinePlot linePlot, String titleY, final String key) throws SQLException {
        addExperimentSubLinePlot(archimulatorService, linePlot, titleY, key, new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return true;
            }
        });
    }

    public static void addExperimentSubLinePlot(final ArchimulatorService archimulatorService, LinePlot linePlot, String titleY, final String key, Predicate<ExperimentProfile> experimentProfilePred) throws SQLException {
        SubLinePlot subLinePlot = new SubLinePlot(titleY);

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
        for (final ExperimentProfile experimentProfile : experimentProfiles) {
            if (experimentProfilePred.apply(experimentProfile)) {
                subLinePlot.getLines().add(new SubLinePlotLine("Exp #" + experimentProfile.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        try {
                            String value = archimulatorService.getExperimentStatById(experimentProfile.getId(), key);
                            if (value != null) {
                                value = value.replaceAll(",", "");
                                return (double) (int) (Double.valueOf(value).doubleValue());
                            }
                            return 0.0;
                        } catch (SQLException e) {
                            recordException(e);
                            return 0.0;
                        }
                    }
                }));
            }
        }
        linePlot.getSubLinePlots().add(subLinePlot);
    }

    private static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    public static void main(String[] args) throws MalformedURLException, SQLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(20000);
        factory.setOverloadEnabled(true);

        ArchimulatorService archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, ManagementStartup.SERVICE_URL);

        LinePlot linePlot = new LinePlot("Experiment Stats - Archimulator");

        addExperimentSubLinePlot(archimulatorService, linePlot, "Insts per Second", "checkpointedSimulation/phase1.instsPerSecond", new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED;
            }
        });
        addExperimentSubLinePlot(archimulatorService, linePlot, "Cycles per Second", "checkpointedSimulation/phase1.cyclesPerSecond", new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED;
            }
        });

        LinePlotFrame linePlotFrame = new LinePlotFrame(linePlot, 500, 470);
        linePlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(linePlotFrame);
        linePlotFrame.setVisible(true);
    }
}