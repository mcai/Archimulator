package archimulator.service;

import archimulator.model.experiment.ExperimentBuilder;

import java.sql.SQLException;
import java.util.List;

public interface ArchimulatorService {
    void stop();

    void clearData() throws SQLException;

    void addExperimentProfile(ExperimentBuilder.ExperimentProfile experimentProfile) throws SQLException;

    List<ExperimentBuilder.ExperimentProfile> getExperimentProfilesAsList() throws SQLException;

    ExperimentBuilder.ExperimentProfile retrieveOneExperimentProfileToRun() throws SQLException;

    void notifyExperimentStopped(long experimentProfileId) throws SQLException;

    boolean isRunningExperimentEnabled();

    void setRunningExperimentEnabled(boolean runningExperimentEnabled);

    void setUserPassword(String userId, String password) throws SQLException;

    boolean authenticateUser(String userId, String password) throws SQLException;
}
