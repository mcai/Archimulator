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
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

public class SimulatedProgramServiceImpl extends AbstractService implements SimulatedProgramService {
    private Dao<SimulatedProgram, Long> simulatedPrograms;

    @SuppressWarnings("unchecked")
    public SimulatedProgramServiceImpl() {
        super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(SimulatedProgram.class));

        this.simulatedPrograms = createDao(SimulatedProgram.class);

        if (this.getFirstSimulatedProgram() == null) {
            this.addSimulatedProgram(new SimulatedProgram(
                    "mst_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
                    "mst.mips",
                    "4000"));

            this.addSimulatedProgram(new SimulatedProgram(
                    "mst_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                    "mst.mips",
                    "4000", "", true));

            this.addSimulatedProgram(new SimulatedProgram(
                    "em3d_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
                    "em3d.mips",
                    "400000 128 75 1"));

            this.addSimulatedProgram(new SimulatedProgram(
                    "em3d_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
                    "em3d.mips",
                    "400000 128 75 1", "", true));

            this.addSimulatedProgram(new SimulatedProgram(
                    "429_mcf_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in"));

            this.addSimulatedProgram(new SimulatedProgram(
                    "429_mcf_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in", "", true));
        }
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

    @Override
    public void lockSimulatedProgram(SimulatedProgram simulatedProgram) {
        long simulatedProgramId = simulatedProgram.getId();
        while (getSimulatedProgramById(simulatedProgramId).getLocked()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        simulatedProgram.setLocked(true);
        updateSimulatedProgram(simulatedProgram);
    }

    @Override
    public void unlockSimulatedProgram(SimulatedProgram simulatedProgram) {
        simulatedProgram.setLocked(false);
        updateSimulatedProgram(simulatedProgram);
    }
}
