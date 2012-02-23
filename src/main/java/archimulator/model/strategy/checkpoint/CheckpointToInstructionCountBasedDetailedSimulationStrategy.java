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
package archimulator.model.strategy.checkpoint;

import archimulator.model.simulation.SimulationStartingImage;
import archimulator.model.strategy.SequentialSimulationStrategy;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.CacheHierarchy;

import java.util.concurrent.CyclicBarrier;

public class CheckpointToInstructionCountBasedDetailedSimulationStrategy extends SequentialSimulationStrategy {
    private long maxInsts;
    private SimulationStartingImage simulationStartingImage;

    public CheckpointToInstructionCountBasedDetailedSimulationStrategy(CyclicBarrier phaser, long maxInsts, SimulationStartingImage simulationStartingImage) {
        super(phaser);

        this.maxInsts = maxInsts;
        this.simulationStartingImage = simulationStartingImage;
    }

    @Override
    public boolean canDoFastForwardOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean canDoCacheWarmupOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean canDoMeasurementOneCycle() {
        return this.getSimulation().getProcessor().getCores().get(0).getThreads().get(0).getTotalInsts() < this.maxInsts;
    }

    @Override
    public void beginSimulation() {
    }

    @Override
    public void endSimulation() {
    }

    @Override
    public boolean isSupportFastForward() {
        return false;
    }

    @Override
    public boolean isSupportCacheWarmup() {
        return false;
    }

    @Override
    public boolean isSupportMeasurement() {
        return true;
    }

    @Override
    public Kernel prepareKernel() {
        return this.simulationStartingImage.getKernel();
    }

    @Override
    public CacheHierarchy prepareCacheHierarchy() {
        return this.simulationStartingImage.getCacheHierarchy();
    }
}
