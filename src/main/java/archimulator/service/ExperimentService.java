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
import archimulator.model.ExperimentPack;
import archimulator.model.SimulatedProgram;
import net.pickapack.service.Service;

import java.util.List;

public interface ExperimentService extends Service {
    List<Experiment> getAllExperiments();

    Experiment getExperimentById(long id);

    List<Experiment> getExperimentsByTitle(String title);

    Experiment getLatestExperimentByTitle(String title);

    List<Experiment> getExperimentsBySimulatedProgram(SimulatedProgram simulatedProgram);

    List<Experiment> getExperimentsByArchitecture(Architecture architecture);

    List<Experiment> getExperimentsByParent(ExperimentPack parent);

    void addExperiment(Experiment experiment);

    void removeExperimentById(long id);

    void updateExperiment(Experiment experiment);

    void dumpExperiment(Experiment experiment);

    Experiment getFirstExperimentToRun();

    void waitForExperimentStopped(Experiment experiment);

    List<ExperimentPack> getAllExperimentPacks();

    ExperimentPack getExperimentPackById(long id);

    ExperimentPack getExperimentPackByTitle(String title);

    void addExperimentPack(ExperimentPack experimentPack);

    void removeExperimentPack(long id);

    void updateExperimentPack(ExperimentPack experimentPack);

    void waitForExperimentPackStopped(ExperimentPack experimentPack);
}
