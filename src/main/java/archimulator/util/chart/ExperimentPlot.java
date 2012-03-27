package archimulator.util.chart;

import archimulator.client.GuestStartup;
import archimulator.service.ArchimulatorService;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.util.DateHelper;
import archimulator.util.action.Function;
import com.caucho.hessian.client.HessianProxyFactory;
import org.jfree.ui.RefineryUtilities;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExperimentPlot {
    private String title;
    private List<ExperimentSubPlot> subPlots;

    public ExperimentPlot(String title) {
        this.title = title;
        this.subPlots = new ArrayList<ExperimentSubPlot>();
    }

    public String getTitle() {
        return title;
    }

    public List<ExperimentSubPlot> getSubPlots() {
        return subPlots;
    }

    public static class ExperimentSubPlot {
        private String titleY;
        private List<ExperimentSubPlotLine> lines;
        
        public ExperimentSubPlot(String titleY) {
            this.titleY = titleY;
            this.lines = new ArrayList<ExperimentSubPlotLine>();
        }

        public String getTitleY() {
            return titleY;
        }

        public List<ExperimentSubPlotLine> getLines() {
            return lines;
        }
    }
    
    public static class ExperimentSubPlotLine {
        private String title;
        private Function<Double> getValueCallback;

        public ExperimentSubPlotLine(String title, Function<Double> getValueCallback) {
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

    private static void addExperimentSubPlotInstsPerSecond(final ArchimulatorService archimulatorService, ExperimentPlot experimentPlot) throws SQLException {
        ExperimentSubPlot experimentSubPlotCyclesPerSecond = new ExperimentSubPlot("Insts per Second");

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
        for(final ExperimentProfile experimentProfile : experimentProfiles) {
            if(experimentProfile.getState() == ExperimentProfileState.RUNNING) {
                experimentSubPlotCyclesPerSecond.getLines().add(new ExperimentSubPlotLine("Exp #" + experimentProfile.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        try {
                            Map<String,Object> experimentStatsById = archimulatorService.getExperimentStatsById(experimentProfile.getId());
                            String key = "checkpointedSimulation/phase1.instsPerSecond";
                            if(experimentStatsById.containsKey(key)) {
                                String str = (String) experimentStatsById.get(key);
                                str = str.replaceAll(",", "");
                                return (double)(int)(Double.valueOf(str).doubleValue());
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
        experimentPlot.getSubPlots().add(experimentSubPlotCyclesPerSecond);
    }

    private static void addExperimentSubPlotCyclesPerSecond(final ArchimulatorService archimulatorService, ExperimentPlot experimentPlot) throws SQLException {
        ExperimentSubPlot experimentSubPlotCyclesPerSecond = new ExperimentSubPlot("Cycles per Second");

        List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
        for(final ExperimentProfile experimentProfile : experimentProfiles) {
            if(experimentProfile.getState() == ExperimentProfileState.RUNNING) {
                experimentSubPlotCyclesPerSecond.getLines().add(new ExperimentSubPlotLine("Exp #" + experimentProfile.getId(), new Function<Double>() {
                    @Override
                    public Double apply() {
                        try {
                            Map<String, Object> experimentStatsById = archimulatorService.getExperimentStatsById(experimentProfile.getId());
                            String key = "checkpointedSimulation/phase1.cyclesPerSecond";
                            if(experimentStatsById.containsKey(key)) {
                                String str = (String) experimentStatsById.get(key);
                                str = str.replaceAll(",", "");
                                return (double)(int)(Double.valueOf(str).doubleValue());
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
        experimentPlot.getSubPlots().add(experimentSubPlotCyclesPerSecond);
    }

    public static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }
    
    public static void main(String[] args) throws MalformedURLException, SQLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(20000);
        factory.setOverloadEnabled(true);

        final ArchimulatorService archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, GuestStartup.SERVICE_URL);

        ExperimentPlot experimentPlot = new ExperimentPlot("Experiment Stats - Archimulator");

        addExperimentSubPlotInstsPerSecond(archimulatorService, experimentPlot);
        addExperimentSubPlotCyclesPerSecond(archimulatorService, experimentPlot);

        ExperimentPlotFrame experimentPlotFrame = new ExperimentPlotFrame(experimentPlot);
        experimentPlotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(experimentPlotFrame);
        experimentPlotFrame.setVisible(true);
    }
}
