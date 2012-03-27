package archimulator.util.chart;

import archimulator.client.GuestStartup;
import archimulator.service.ArchimulatorService;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.util.action.Function;
import com.caucho.hessian.client.HessianProxyFactory;
import org.jfree.ui.RefineryUtilities;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimulationPlot {
    private String title;
    private List<SimulationSubPlot> subPlots;

    public SimulationPlot(String title) {
        this.title = title;
        this.subPlots = new ArrayList<SimulationSubPlot>();
    }

    public String getTitle() {
        return title;
    }

    public List<SimulationSubPlot> getSubPlots() {
        return subPlots;
    }

    public static class SimulationSubPlot {
        private String titleY;
        private List<SimulationSubPlotLine> lines;
        
        public SimulationSubPlot(String titleY) {
            this.titleY = titleY;
            this.lines = new ArrayList<SimulationSubPlotLine>();
        }

        public String getTitleY() {
            return titleY;
        }

        public List<SimulationSubPlotLine> getLines() {
            return lines;
        }
    }
    
    public static class SimulationSubPlotLine {
        private String title;
        private Function<Double> getValueCallback;

        public SimulationSubPlotLine(String title, Function<Double> getValueCallback) {
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

    private static void addSimulationSubPlotInstsPerSecond(final ArchimulatorService archimulatorService, SimulationPlot simulationPlot) throws SQLException {
        SimulationSubPlot simulationSubPlotCyclesPerSecond = new SimulationSubPlot("Insts per Second");

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
        for(final ExperimentProfile experimentProfile : experimentProfiles) {
            if(experimentProfile.getState() == ExperimentProfileState.RUNNING) {
                simulationSubPlotCyclesPerSecond.getLines().add(new SimulationSubPlotLine("Exp #" + experimentProfile.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        try {
                            String str = (String) archimulatorService.getExperimentStatsById(experimentProfile.getId()).get("detailedSimulation.instsPerSecond");
                            str = str.replaceAll(",", "");
                            return (double)(int)(Double.valueOf(str).doubleValue());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }
        }
        simulationPlot.getSubPlots().add(simulationSubPlotCyclesPerSecond);
    }

    private static void addSimulationSubPlotCyclesPerSecond(final ArchimulatorService archimulatorService, SimulationPlot simulationPlot) throws SQLException {
        SimulationSubPlot simulationSubPlotCyclesPerSecond = new SimulationSubPlot("Cycles per Second");

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
        for(final ExperimentProfile experimentProfile : experimentProfiles) {
            if(experimentProfile.getState() == ExperimentProfileState.RUNNING) {
                simulationSubPlotCyclesPerSecond.getLines().add(new SimulationSubPlotLine("Exp #" + experimentProfile.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        try {
                            String str = (String) archimulatorService.getExperimentStatsById(experimentProfile.getId()).get("detailedSimulation.cyclesPerSecond");
                            str = str.replaceAll(",", "");
                            return (double)(int)(Double.valueOf(str).doubleValue());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }
        }
        simulationPlot.getSubPlots().add(simulationSubPlotCyclesPerSecond);
    }
    
    public static void main(String[] args) throws MalformedURLException, SQLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(20000);
        factory.setOverloadEnabled(true);

        final ArchimulatorService archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, GuestStartup.SERVICE_URL);

        SimulationPlot simulationPlot = new SimulationPlot("Experiment Stats - Archimulator");

        addSimulationSubPlotInstsPerSecond(archimulatorService, simulationPlot);
        addSimulationSubPlotCyclesPerSecond(archimulatorService, simulationPlot);

        SimulationPlotFrame simulationPlotFrame = new SimulationPlotFrame(simulationPlot);
        simulationPlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(simulationPlotFrame);
        simulationPlotFrame.setVisible(true);
    }
}
