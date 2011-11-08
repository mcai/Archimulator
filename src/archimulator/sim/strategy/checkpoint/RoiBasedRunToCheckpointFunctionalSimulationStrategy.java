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
package archimulator.sim.strategy.checkpoint;

import archimulator.sim.SimulationStartingImage;
import archimulator.sim.event.PseudocallEncounteredEvent;
import archimulator.sim.strategy.SequentialSimulationStrategy;
import archimulator.util.action.Action1;

import java.util.concurrent.CyclicBarrier;

public class RoiBasedRunToCheckpointFunctionalSimulationStrategy extends SequentialSimulationStrategy {
    private int pthreadSpawnedIndex;
    private SimulationStartingImage simulationStartingImage;

    private boolean pthreadHasSpawned;

    public RoiBasedRunToCheckpointFunctionalSimulationStrategy(CyclicBarrier phaser, SimulationStartingImage simulationStartingImage) {
        this(phaser, 3720, simulationStartingImage);
    }

    public RoiBasedRunToCheckpointFunctionalSimulationStrategy(CyclicBarrier phaser, int pthreadSpawnedIndex, SimulationStartingImage simulationStartingImage) {
        super(phaser);

        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
        this.simulationStartingImage = simulationStartingImage;
    }

    @Override
    public boolean canDoFastForwardOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean canDoCacheWarmupOneCycle() {
        return !pthreadHasSpawned;
    }

    @Override
    public boolean canDoMeasurementOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public void beginSimulation() {
        this.pthreadHasSpawned = false;

        this.getSimulation().getProcessor().getKernel().getBlockingEventDispatcher().addListener(PseudocallEncounteredEvent.class, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
                if (event.getArg() == RoiBasedRunToCheckpointFunctionalSimulationStrategy.this.pthreadSpawnedIndex) {
                    pthreadHasSpawned = true;
                }
            }
        });
    }

    @Override
    public void endSimulation() {
        this.simulationStartingImage.setKernel(this.getSimulation().getProcessor().getKernel());
        this.simulationStartingImage.setCacheHierarchy(this.getSimulation().getProcessor().getCacheHierarchy());
    }

    @Override
    public boolean isSupportFastForward() {
        return false;
    }

    @Override
    public boolean isSupportCacheWarmup() {
        return true;
    }

    @Override
    public boolean isSupportMeasurement() {
        return false;
    }
}
