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
package archimulator.sim.base.simulation.strategy;

import archimulator.sim.base.event.PseudocallEncounteredEvent;
import archimulator.util.action.Action1;

import java.util.concurrent.CyclicBarrier;

public class InstructionCountBasedThreePhaseSimulationStrategy extends SequentialSimulationStrategy {
    private int pthreadSpawnedIndex;
    private long maxInstsInCacheWarmup;
    private long maxInstsInMeasurement;

    private boolean pthreadHasSpawned;

    public InstructionCountBasedThreePhaseSimulationStrategy(CyclicBarrier phaser) {
        this(phaser, 3720, 1000000000, 1000000000);
    }

    public InstructionCountBasedThreePhaseSimulationStrategy(CyclicBarrier phaser, int pthreadSpawnedIndex, long maxInstsInCacheWarmup, long maxInstsInMeasurement) {
        super(phaser);

        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
        this.maxInstsInCacheWarmup = maxInstsInCacheWarmup;
        this.maxInstsInMeasurement = maxInstsInMeasurement;
    }

    @Override
    public void beginSimulation() {
        pthreadHasSpawned = false;

        this.getSimulation().getBlockingEventDispatcher().addListener(PseudocallEncounteredEvent.class, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
                if (event.getArg() == InstructionCountBasedThreePhaseSimulationStrategy.this.pthreadSpawnedIndex) {
                    pthreadHasSpawned = true;
                }
            }
        });
    }

    @Override
    public void endSimulation() {
    }

    public final boolean canDoFastForwardOneCycle() {
        return !pthreadHasSpawned;
    }

    public final boolean canDoCacheWarmupOneCycle() {
        return this.getSimulation().getProcessor().getCores().get(0).getThreads().get(0).getTotalInsts() < this.maxInstsInCacheWarmup;
    }

    public final boolean canDoMeasurementOneCycle() {
        return this.getSimulation().getProcessor().getCores().get(0).getThreads().get(0).getTotalInsts() < this.maxInstsInMeasurement;
    }

    @Override
    public boolean isSupportFastForward() {
        return true;
    }

    @Override
    public boolean isSupportCacheWarmup() {
        return true;
    }

    @Override
    public boolean isSupportMeasurement() {
        return true;
    }
}
