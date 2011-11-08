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
package archimulator.sim.strategy;

import archimulator.mem.BasicCacheHierarchy;
import archimulator.mem.CacheHierarchy;
import archimulator.os.Kernel;
import archimulator.sim.Simulation;

public abstract class SimulationStrategy {
    private Simulation simulation;

    public SimulationStrategy() {
    }

    public abstract void execute();

    public void doHouseKeeping() {
        this.simulation.getProcessor().getKernel().advanceOneCycle();
        this.simulation.getProcessor().updateContextToThreadAssignments();
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public abstract boolean isSupportFastForward();

    public abstract boolean isSupportCacheWarmup();

    public abstract boolean isSupportMeasurement();

    public Kernel prepareKernel() {
        return this.simulation.createKernel();
    }

    public CacheHierarchy prepareCacheHierarchy() {
        return new BasicCacheHierarchy(this.simulation.getBlockingEventDispatcher(), this.simulation.getCycleAccurateEventQueue(), this.simulation.getLogger(), this.simulation.getConfig().getProcessorConfig());
    }
}
