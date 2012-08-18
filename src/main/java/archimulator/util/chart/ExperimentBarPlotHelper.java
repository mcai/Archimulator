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

import archimulator.service.ExperimentService;
import archimulator.service.ServiceManager;
import net.pickapack.dateTime.DateHelper;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Date;

public class ExperimentBarPlotHelper {
    private static Double getExperimentProfileStat(ExperimentService experimentService, String key, long experimentId) {
        try {
            String value = experimentService.getExperimentById(experimentId).getStats().get(key);
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

    //TODO
//    private static BarPlot<ExperimentProfile> createHTLLCRequestDistributionBarPlot(final ExperimentService archimulatorService) throws SQLException {
//        BarPlot<ExperimentProfile> barPlot = new BarPlot<>("HT LLC Request Distribution", "# HT LLC Requests", new Predicate<ExperimentProfile>() {
//            @Override
//            public boolean apply(ExperimentProfile experimentProfile) {
//                return experimentProfile.getState() == ExperimentProfileState.STOPPED && experimentProfile.getTitle().contains("_LRU") && experimentProfile.getTitle().contains("mst_ht");
//            }
//        }, archimulatorService.getExperimentProfilesAsList());
//
//        Function1<ExperimentProfile, String> getTitleCallback = new Function1<ExperimentProfile, String>() {
//            @Override
//            public String apply(ExperimentProfile experimentProfile) {
//                SimulatedProgram simulatedProgram = experimentProfile.getContextMappings().get(0).getSimulatedProgram();
//                return simulatedProgram.getHtLookahead() + ", " + simulatedProgram.getHtStride() + ", " + StorageUnit.toString(PropertiesEnhancedBlockingEventDispatcher.getL2Size()) + ", " + PropertiesEnhancedBlockingEventDispatcher.getL2Assoc();
//            }
//        };
//
//        barPlot.getSubBarPlots().add(new SubBarPlot<>("Total HT LLC Request", new Function1<ExperimentProfile, Double>() {
//            @Override
//            public Double apply(ExperimentProfile experimentProfile) {
//                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numTotalHtRequests");
//            }
//        }, getTitleCallback));
//
//        barPlot.getSubBarPlots().add(new SubBarPlot<>("Good HT LLC Request", new Function1<ExperimentProfile, Double>() {
//            @Override
//            public Double apply(ExperimentProfile experimentProfile) {
//                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numGoodHtRequests");
//            }
//        }, getTitleCallback));
//        barPlot.getSubBarPlots().add(new SubBarPlot<>("Bad HT LLC Request", new Function1<ExperimentProfile, Double>() {
//            @Override
//            public Double apply(ExperimentProfile experimentProfile) {
//                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numBadHtRequests");
//            }
//        }, getTitleCallback));
//        barPlot.getSubBarPlots().add(new SubBarPlot<>("Ugly HT LLC Request", new Function1<ExperimentProfile, Double>() {
//            @Override
//            public Double apply(ExperimentProfile experimentProfile) {
//                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numUglyHtRequests");
//            }
//        }, getTitleCallback));
//        barPlot.getSubBarPlots().add(new SubBarPlot<>("Late HT LLC Request", new Function1<ExperimentProfile, Double>() {
//            @Override
//            public Double apply(ExperimentProfile experimentProfile) {
//                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numLateHtRequests");
//            }
//        }, getTitleCallback));
//        return barPlot;
//    }

    public static void main(String[] args) throws MalformedURLException, SQLException {
        ExperimentService experimentService = ServiceManager.getExperimentService();

        //TODO
//        BarPlot<ExperimentProfile> barPlot = createHTLLCRequestDistributionBarPlot(experimentService);
//        BarPlotFrame<ExperimentProfile> barPlotFrame = new BarPlotFrame<ExperimentProfile>(barPlot, "Experiment", 500, 470);
//        barPlotFrame.pack();
//        RefineryUtilities.centerFrameOnScreen(barPlotFrame);
//        barPlotFrame.setVisible(true);
    }
}
