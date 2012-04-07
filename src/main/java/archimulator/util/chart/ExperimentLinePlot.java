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

import archimulator.client.GuestStartup;
import archimulator.service.ArchimulatorService;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.util.DateHelper;
import archimulator.util.action.Function;
import archimulator.util.action.Predicate;
import com.caucho.hessian.client.HessianProxyFactory;
import org.jfree.ui.RefineryUtilities;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentLinePlot {
    private String title;
    private List<ExperimentSubLinePlot> subLinePlots;

    public ExperimentLinePlot(String title) {
        this.title = title;
        this.subLinePlots = new ArrayList<ExperimentSubLinePlot>();
    }

    public String getTitle() {
        return title;
    }

    public List<ExperimentSubLinePlot> getSubLinePlots() {
        return subLinePlots;
    }

    public static class ExperimentSubLinePlot {
        private String titleY;
        private List<ExperimentSubLinePlotLine> lines;
        
        public ExperimentSubLinePlot(String titleY) {
            this.titleY = titleY;
            this.lines = new ArrayList<ExperimentSubLinePlotLine>();
        }

        public String getTitleY() {
            return titleY;
        }

        public List<ExperimentSubLinePlotLine> getLines() {
            return lines;
        }
    }
    
    public static class ExperimentSubLinePlotLine {
        private String title;
        private Function<Double> getValueCallback;

        public ExperimentSubLinePlotLine(String title, Function<Double> getValueCallback) {
            this.title = title;
            this.getValueCallback = getValueCallback;
        }

        public String getTitle() {
            return title;
        }

        public Function<Double> getGetValueCallback() {
            return getValueCallback;
        }
    }

    public static void addExperimentSubLinePlot(final ArchimulatorService archimulatorService, ExperimentLinePlot experimentLinePlot, String titleY, final String key) throws SQLException {
        addExperimentSubLinePlot(archimulatorService, experimentLinePlot, titleY, key, new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return true;
            }
        });
    }

    public static void addExperimentSubLinePlot(final ArchimulatorService archimulatorService, ExperimentLinePlot experimentLinePlot, String titleY, final String key, Predicate<ExperimentProfile> experimentProfilePred) throws SQLException {
        ExperimentSubLinePlot experimentSubLinePlot = new ExperimentSubLinePlot(titleY);

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
        for(final ExperimentProfile experimentProfile : experimentProfiles) {
            if(experimentProfilePred.apply(experimentProfile)) {
                experimentSubLinePlot.getLines().add(new ExperimentSubLinePlotLine("Exp #" + experimentProfile.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        try {
                            String value = archimulatorService.getExperimentStatById(experimentProfile.getId(), key);
                            if(value != null) {
                                value = value.replaceAll(",", "");
                                return (double)(int)(Double.valueOf(value).doubleValue());
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
        experimentLinePlot.getSubLinePlots().add(experimentSubLinePlot);
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

        ArchimulatorService archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, GuestStartup.SERVICE_URL);

        ExperimentLinePlot experimentLinePlot = new ExperimentLinePlot("Experiment Stats - Archimulator");

        addExperimentSubLinePlot(archimulatorService, experimentLinePlot, "Insts per Second", "checkpointedSimulation/phase1.instsPerSecond", new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED;
            }
        });
        addExperimentSubLinePlot(archimulatorService, experimentLinePlot, "Cycles per Second", "checkpointedSimulation/phase1.cyclesPerSecond", new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED;
            }
        });

        ExperimentLinePlotFrame experimentLinePlotFrame = new ExperimentLinePlotFrame(experimentLinePlot);
        experimentLinePlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(experimentLinePlotFrame);
        experimentLinePlotFrame.setVisible(true);
    }
}
