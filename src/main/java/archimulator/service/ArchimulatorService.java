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

import archimulator.model.experiment.builder.ExperimentProfile;
import archimulator.model.experiment.builder.ProcessorProfile;
import archimulator.model.simulation.SimulatedProgram;

import java.sql.SQLException;
import java.util.List;

public interface ArchimulatorService {
    void stop();

    void clearData() throws SQLException;
    
    List<SimulatedProgram> getSimulatedProgramsAsList() throws SQLException;
    
    SimulatedProgram getSimulatedProgramById(long simulatedProgramId) throws SQLException;
    
    void addSimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException;
    
    void removeSimulatedProgramById(long simulatedProgramId) throws SQLException;
    
    void updateSimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException;

    List<ProcessorProfile> getProcessorProfilesAsList() throws SQLException;
    
    ProcessorProfile getProcessorProfileById(long processorProfileId) throws SQLException;
    
    void addProcessorProfile(ProcessorProfile processorProfile) throws SQLException;
    
    void removeProcessorProfileById(long processorProfileId) throws SQLException;
    
    void updateProcessorProfile(ProcessorProfile processorProfile) throws SQLException;

    List<ExperimentProfile> getExperimentProfilesAsList() throws SQLException;

    ExperimentProfile getExperimentProfileById(long experimentProfileId) throws SQLException;

    void addExperimentProfile(ExperimentProfile experimentProfile) throws SQLException;

    void removeExperimentProfileById(long experimentProfileId) throws SQLException;

    void updateExperimentProfile(ExperimentProfile experimentProfile) throws SQLException;

    ExperimentProfile retrieveOneExperimentProfileToRun() throws SQLException;

    void notifyExperimentStopped(long experimentProfileId) throws SQLException;

    boolean isRunningExperimentEnabled();

    void setRunningExperimentEnabled(boolean runningExperimentEnabled);

    void setUserPassword(String userId, String password) throws SQLException;

    boolean authenticateUser(String userId, String password) throws SQLException;
}
