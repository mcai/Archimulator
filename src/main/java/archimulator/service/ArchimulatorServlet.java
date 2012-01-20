package archimulator.service;

import archimulator.model.experiment.ExperimentBuilder;
import com.caucho.hessian.server.HessianServlet;

import java.sql.SQLException;
import java.util.List;

public class ArchimulatorServlet extends HessianServlet implements ArchimulatorService {
    @Override
    public void stop() {
        this.getProxy().stop();
    }

    @Override
    public void clearData() throws SQLException {
        this.getProxy().clearData();
    }

    @Override
    public void addExperimentProfile(ExperimentBuilder.ExperimentProfile experimentProfile) throws SQLException {
        this.getProxy().addExperimentProfile(experimentProfile);
    }

    @Override
    public List<ExperimentBuilder.ExperimentProfile> getExperimentProfilesAsList() throws SQLException {
        return this.getProxy().getExperimentProfilesAsList();
    }

    @Override
    public ExperimentBuilder.ExperimentProfile retrieveOneExperimentProfileToRun() throws SQLException {
        return this.getProxy().retrieveOneExperimentProfileToRun();
    }

    @Override
    public void notifyExperimentStopped(long experimentProfileId) throws SQLException {
        this.getProxy().notifyExperimentStopped(experimentProfileId);
    }

    @Override
    public boolean isRunningExperimentEnabled() {
        return this.getProxy().isRunningExperimentEnabled();
    }

    @Override
    public void setRunningExperimentEnabled(boolean runningExperimentEnabled) {
        this.getProxy().setRunningExperimentEnabled(runningExperimentEnabled);
    }

    private ArchimulatorService getProxy() {
        return ArchimulatorServletContextListener.getArchimulatorService(this.getServletContext());
    }
}
