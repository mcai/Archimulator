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

import net.pickapack.model.ModelElement;
import archimulator.model.SimulatedProgram;
import com.j256.ormlite.dao.Dao;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

public class SimulatedProgramServiceImpl extends AbstractService implements SimulatedProgramService {
    private Dao<SimulatedProgram, Long> simulatedPrograms;

    @SuppressWarnings("unchecked")
    public SimulatedProgramServiceImpl() {
        super(ServiceManager.DATABASE_DIRECTORY, ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(SimulatedProgram.class));

        this.simulatedPrograms = createDao(SimulatedProgram.class);
    }

    @Override
    public List<SimulatedProgram> getAllSimulatedPrograms() {
        return this.getAllItems(this.simulatedPrograms);
    }

    @Override
    public SimulatedProgram getSimulatedProgramById(long id) {
        return this.getItemById(this.simulatedPrograms, id);
    }

    @Override
    public SimulatedProgram getSimulatedProgramByTitle(String title) {
        return this.getFirstItemByTitle(this.simulatedPrograms, title);
    }

    @Override
    public SimulatedProgram getFirstSimulatedProgram() {
        return this.getFirstItem(this.simulatedPrograms);
    }

    @Override
    public long addSimulatedProgram(SimulatedProgram simulatedProgram) {
        return this.addItem(this.simulatedPrograms, SimulatedProgram.class, simulatedProgram);
    }

    @Override
    public void removeSimulatedProgramById(long id) {
        this.removeItemById(this.simulatedPrograms, SimulatedProgram.class, id);
    }

    @Override
    public void clearSimulatedPrograms() {
        this.clearItems(this.simulatedPrograms, SimulatedProgram.class);
    }

    @Override
    public void updateSimulatedProgram(SimulatedProgram simulatedProgram) {
        this.updateItem(this.simulatedPrograms, SimulatedProgram.class, simulatedProgram);
    }
}
