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
import archimulator.util.action.Function1;
import archimulator.util.action.Predicate;
import com.caucho.hessian.client.HessianProxyFactory;
import org.jfree.ui.RefineryUtilities;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentBarPlot {
    private String title;
    private String titleY;
    private boolean stacked;
    private Predicate<ExperimentProfile> experimentProfilePred;
    private List<ExperimentProfile> experimentProfiles;
    private List<ExperimentSubBarPlot> subBarPlots;

    public ExperimentBarPlot(String title, String titleY, boolean stacked, Predicate<ExperimentProfile> experimentProfilePred, List<ExperimentProfile> experimentProfiles) {
        this.title = title;
        this.titleY = titleY;
        this.stacked = stacked;
        this.experimentProfilePred = experimentProfilePred;
        this.experimentProfiles = experimentProfiles;
        this.subBarPlots = new ArrayList<ExperimentSubBarPlot>();
    }

    public String getTitle() {
        return title;
    }

    public String getTitleY() {
        return titleY;
    }

    public boolean isStacked() {
        return stacked;
    }

    public Predicate<ExperimentProfile> getExperimentProfilePred() {
        return experimentProfilePred;
    }

    public List<ExperimentProfile> getExperimentProfiles() {
        return experimentProfiles;
    }

    public List<ExperimentSubBarPlot> getSubBarPlots() {
        return subBarPlots;
    }

    public static class ExperimentSubBarPlot {
        private String title;
        private Function1<ExperimentProfile, Double> getValueCallback;

        public ExperimentSubBarPlot(String title, Function1<ExperimentProfile, Double> getValueCallback) {
            this.title = title;
            this.getValueCallback = getValueCallback;
        }

        public String getTitle() {
            return title;
        }

        public Function1<ExperimentProfile, Double> getGetValueCallback() {
            return getValueCallback;
        }
    }

    private static Double getExperimentProfileStat(ExperimentProfile experimentProfile, ArchimulatorService archimulatorService, String key) {
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

    private static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    public static void main(String[] args) throws MalformedURLException, SQLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(20000);
        factory.setOverloadEnabled(true);

        final ArchimulatorService archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, GuestStartup.SERVICE_URL);

        ExperimentBarPlot experimentBarPlot = new ExperimentBarPlot("HT LLC Request Distribution", "# HT LLC Requests", true, new Predicate<ExperimentProfile>() {
            @Override
            public boolean apply(ExperimentProfile experimentProfile) {
                return experimentProfile.getState() == ExperimentProfileState.STOPPED && experimentProfile.getTitle().contains("HT");
            }
        }, archimulatorService.getExperimentProfilesAsList());

        experimentBarPlot.getSubBarPlots().add(new ExperimentBarPlot.ExperimentSubBarPlot("Good HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numGoodHtRequests");
            }
        }));
        experimentBarPlot.getSubBarPlots().add(new ExperimentBarPlot.ExperimentSubBarPlot("Bad HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numBadHtRequests");
            }
        }));
        experimentBarPlot.getSubBarPlots().add(new ExperimentBarPlot.ExperimentSubBarPlot("Ugly HT LLC Request", new Function1<ExperimentProfile, Double>() {
            @Override
            public Double apply(ExperimentProfile experimentProfile) {
                return getExperimentProfileStat(experimentProfile, archimulatorService, "checkpointedSimulation/phase1.llcHTRequestProfilingCapability.llc.numUglyHtRequests");
            }
        }));

        ExperimentBarPlotFrame experimentBarPlotFrame = new ExperimentBarPlotFrame(experimentBarPlot);
        experimentBarPlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(experimentBarPlotFrame);
        experimentBarPlotFrame.setVisible(true);
    }
}
