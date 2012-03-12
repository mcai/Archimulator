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
package archimulator.service;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.model.simulation.SimulatedProgram;
import com.caucho.hessian.server.HessianServlet;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public List<SimulatedProgram> getSimulatedProgramsAsList() throws SQLException {
        return this.getProxy().getSimulatedProgramsAsList();
    }

    @Override
    public SimulatedProgram getSimulatedProgramById(long simulatedProgramId) throws SQLException {
        return this.getProxy().getSimulatedProgramById(simulatedProgramId);
    }

    @Override
    public void addSimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException {
        this.getProxy().addSimulatedProgram(simulatedProgram);
    }

    @Override
    public void removeSimulatedProgramById(long simulatedProgramId) throws SQLException {
        this.getProxy().removeSimulatedProgramById(simulatedProgramId);
    }

    @Override
    public void updateSimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException {
        this.getProxy().updateSimulatedProgram(simulatedProgram);
    }

    @Override
    public List<ProcessorProfile> getProcessorProfilesAsList() throws SQLException {
        return this.getProxy().getProcessorProfilesAsList();
    }

    @Override
    public ProcessorProfile getProcessorProfileById(long processorProfileId) throws SQLException {
        return this.getProxy().getProcessorProfileById(processorProfileId);
    }

    @Override
    public void addProcessorProfile(ProcessorProfile processorProfile) throws SQLException {
        this.getProxy().addProcessorProfile(processorProfile);
    }

    @Override
    public void removeProcessorProfileById(long processorProfileId) throws SQLException {
        this.getProxy().removeProcessorProfileById(processorProfileId);
    }

    @Override
    public void updateProcessorProfile(ProcessorProfile processorProfile) throws SQLException {
        this.getProxy().updateProcessorProfile(processorProfile);
    }

    @Override
    public void addExperimentProfile(ExperimentProfile experimentProfile) throws SQLException {
        this.getProxy().addExperimentProfile(experimentProfile);
    }

    @Override
    public void removeExperimentProfileById(long experimentProfileId) throws SQLException {
        this.getProxy().removeExperimentProfileById(experimentProfileId);
    }

    @Override
    public void updateExperimentProfile(ExperimentProfile experimentProfile) throws SQLException {
        this.getProxy().updateExperimentProfile(experimentProfile);
    }

    @Override
    public List<ExperimentProfile> getExperimentProfilesAsList() throws SQLException {
        return this.getProxy().getExperimentProfilesAsList();
    }

    @Override
    public ExperimentProfile getExperimentProfileById(long experimentProfileId) throws SQLException {
        return this.getProxy().getExperimentProfileById(experimentProfileId);
    }

    @Override
    public ExperimentProfile retrieveOneExperimentProfileToRun(String simulatorUserId) throws SQLException {
        return this.getProxy().retrieveOneExperimentProfileToRun(simulatorUserId);
    }

    @Override
    public void notifyExperimentPaused(long experimentProfileId) throws SQLException {
        this.getProxy().notifyExperimentPaused(experimentProfileId);
    }

    @Override
    public void notifyExperimentResumed(long experimentProfileId) throws SQLException {
        this.getProxy().notifyExperimentResumed(experimentProfileId);
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

    @Override
    public void setUserPassword(String userId, String password) throws SQLException {
        this.getProxy().setUserPassword(userId, password);
    }

    @Override
    public boolean authenticateUser(String userId, String password) throws SQLException {
        return this.getProxy().authenticateUser(userId, password);
    }

    @Override
    public void pauseExperimentById(long experimentProfileId) throws SQLException {
        this.getProxy().pauseExperimentById(experimentProfileId);
    }

    @Override
    public void resumeExperimentById(long experimentProfileId) throws SQLException {
        this.getProxy().resumeExperimentById(experimentProfileId);
    }

    @Override
    public void stopExperimentById(long experimentProfileId) throws SQLException {
        this.getProxy().stopExperimentById(experimentProfileId);
    }

    @Override
    public void notifyPollStatsCompletedEvent(long experimentProfileId, Map<String, Object> stats) throws SQLException {
        this.getProxy().notifyPollStatsCompletedEvent(experimentProfileId, stats);
    }

    @Override
    public void notifyDumpStatsCompletedEvent(long experimentProfileId, Map<String, Object> stats) throws SQLException {
        this.getProxy().notifyDumpStatsCompletedEvent(experimentProfileId, stats);
    }

    @Override
    public Map<String, Object> getExperimentStatsById(long experimentProfileId) throws SQLException {
        return this.getProxy().getExperimentStatsById(experimentProfileId);
    }

    @Override
    public Set<String> getUserIds() {
        return this.getProxy().getUserIds();
    }

    @Override
    public void send(String fromUserId, String toUserId, String message) {
        this.getProxy().send(fromUserId, toUserId, message);
    }

    @Override
    public String receive(String userId) {
        return this.getProxy().receive(userId);
    }

    private ArchimulatorService getProxy() {
        return ArchimulatorServletContextListener.getArchimulatorService(this.getServletContext());
    }
}
