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

import archimulator.model.SimulatedProgram;
import net.pickapack.service.Service;

import java.util.List;

public interface SimulatedProgramService extends Service {
    List<SimulatedProgram> getAllSimulatedPrograms();

    List<SimulatedProgram> getAllSimulatedPrograms(long first, long count);

    long getNumAllSimulatedPrograms();

    SimulatedProgram getSimulatedProgramById(long id);

    SimulatedProgram getSimulatedProgramByTitle(String title);

    SimulatedProgram getFirstSimulatedProgram();

    long addSimulatedProgram(SimulatedProgram simulatedProgram);

    void removeSimulatedProgramById(long id);

    void clearSimulatedPrograms();

    void updateSimulatedProgram(SimulatedProgram simulatedProgram);

    void lockSimulatedProgram(SimulatedProgram simulatedProgram);

    void unlockSimulatedProgram(SimulatedProgram simulatedProgram);
}
