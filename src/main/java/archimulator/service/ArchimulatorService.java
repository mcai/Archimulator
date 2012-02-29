package archimulator.service;

import archimulator.model.experiment.ExperimentBuilder;

import java.sql.SQLException;

public interface ArchimulatorService {
    void stop();

    void clearData() throws SQLException;

    void addExperimentProfile(ExperimentBuilder.ExperimentProfile experimentProfile) throws SQLException;

    ExperimentBuilder.ExperimentProfile retrieveOneExperimentProfileToRun() throws SQLException;

    void notifyExperimentStopped(long experimentProfileId) throws SQLException;

    boolean isRunningExperimentEnabled();

    void setRunningExperimentEnabled(boolean runningExperimentEnabled);
}
