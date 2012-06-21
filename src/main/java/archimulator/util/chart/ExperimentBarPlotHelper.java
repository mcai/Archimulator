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
import archimulator.sim.base.simulation.SimulatedProgram;
import com.caucho.hessian.client.HessianProxyFactory;
import net.pickapack.DateHelper;
import net.pickapack.StorageUnit;
import net.pickapack.action.Function1;
import net.pickapack.action.Predicate;
import net.pickapack.chart.BarPlot;
import net.pickapack.chart.BarPlotFrame;
import net.pickapack.chart.SubBarPlot;
import org.jfree.ui.RefineryUtilities;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;

public class ExperimentBarPlotHelper {
    private static Double getExperimentProfileStat(ExperimentProfile experimentProfile, ArchimulatorService archimulatorService, String key) {
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

    private static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    private static BarPlot<ExperimentProfile> createHTLLCRequestDistributionBarPlot(final ArchimulatorService archimulatorService) throws SQLException {
        BarPlot<ExperimentProfile> barPlot = new BarPlot<ExperimentProfile>("HT LLC Request Distribution", "# HT LLC Requests", new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED && experimentProfile.getTitle().contains("_LRU") && experimentProfile.getTitle().contains("mst_ht");
            }
        }, archimulatorService.getExperimentProfilesAsList());

        Function1<ExperimentProfile, String> getTitleCallback = new Function1<ExperimentProfile, String>() {
            @Override
            public String apply(ExperimentProfile experimentProfile) {
//                return "Exp #" + experimentProfile.getId();
                SimulatedProgram simulatedProgram = experimentProfile.getContextConfigs().get(0).getSimulatedProgram();
                return simulatedProgram.getHtLookahead() + ", " + simulatedProgram.getHtStride() + ", " + StorageUnit.toString(experimentProfile.getProcessorProfile().getL2Size()) + ", " + experimentProfile.getProcessorProfile().getL2Associativity();
            }
        };

        barPlot.getSubBarPlots().add(new SubBarPlot<ExperimentProfile>("Total HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numTotalHtRequests");
            }
        }, getTitleCallback));

        barPlot.getSubBarPlots().add(new SubBarPlot<ExperimentProfile>("Good HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numGoodHtRequests");
            }
        }, getTitleCallback));
        barPlot.getSubBarPlots().add(new SubBarPlot<ExperimentProfile>("Bad HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numBadHtRequests");
            }
        }, getTitleCallback));
        barPlot.getSubBarPlots().add(new SubBarPlot<ExperimentProfile>("Ugly HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numUglyHtRequests");
            }
        }, getTitleCallback));
        barPlot.getSubBarPlots().add(new SubBarPlot<ExperimentProfile>("Late HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numLateHtRequests");
            }
        }, getTitleCallback));
        return barPlot;
    }

    public static void main(String[] args) throws MalformedURLException, SQLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(20000);
        factory.setOverloadEnabled(true);

        ArchimulatorService archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, ManagementStartup.SERVICE_URL);

        BarPlot<ExperimentProfile> barPlot = createHTLLCRequestDistributionBarPlot(archimulatorService);

        BarPlotFrame<ExperimentProfile> barPlotFrame = new BarPlotFrame<ExperimentProfile>(barPlot, "Experiment", 500, 470);
        barPlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(barPlotFrame);
        barPlotFrame.setVisible(true);
    }
}
