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

import archimulator.model.Architecture;
import archimulator.model.Experiment;
import archimulator.model.SimulatedProgram;
import net.pickapack.service.Service;

import java.sql.SQLException;
import java.util.List;

public interface ExperimentService extends Service {
    List<Experiment> getAllExperiments() throws SQLException;

    Experiment getExperimentById(long id) throws SQLException;

    List<Experiment> getExperimentsByTitle(String title) throws SQLException;

    Experiment getLatestExperimentByTitle(String title) throws SQLException;

    List<Experiment> getExperimentsBySimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException;

    List<Experiment> getExperimentsByArchitecture(Architecture architecture) throws SQLException;

    void addExperiment(Experiment experiment) throws SQLException;

    void removeExperimentById(long id) throws SQLException;

    void updateExperiment(Experiment experiment) throws SQLException;

    void dumpExperiment(Experiment experiment) throws SQLException;

    Experiment getFirstExperimentToRun() throws SQLException;

    void waitForExperimentStopped(Experiment experiment) throws SQLException;
}
