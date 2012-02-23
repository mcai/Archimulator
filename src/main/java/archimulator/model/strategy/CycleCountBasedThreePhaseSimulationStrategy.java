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
package archimulator.model.strategy;

import archimulator.model.event.PseudocallEncounteredEvent;
import archimulator.util.action.Action1;

import java.util.concurrent.CyclicBarrier;

public class CycleCountBasedThreePhaseSimulationStrategy extends SequentialSimulationStrategy {
    private int pthreadSpawnedIndex;
    private long maxCyclesInCacheWarmup;
    private long maxCyclesInMeasurement;

    private long numCyclesInCacheWarmup;
    private long numCyclesInMeasurement;

    private boolean pthreadHasSpawned = false;

    public CycleCountBasedThreePhaseSimulationStrategy(CyclicBarrier phaser) {
        this(phaser, 3720, 1000000000, 1000000000);
    }

    public CycleCountBasedThreePhaseSimulationStrategy(CyclicBarrier phaser, int pthreadSpawnedIndex, long maxCyclesInCacheWarmup, long maxCyclesInMeasurement) {
        super(phaser);

        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
        this.maxCyclesInCacheWarmup = maxCyclesInCacheWarmup;
        this.maxCyclesInMeasurement = maxCyclesInMeasurement;
    }

    @Override
    public void beginSimulation() {
        this.numCyclesInCacheWarmup = this.maxCyclesInCacheWarmup;
        this.numCyclesInMeasurement = this.maxCyclesInMeasurement;

        this.getSimulation().getBlockingEventDispatcher().addListener(PseudocallEncounteredEvent.class, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
                if (event.getArg() == CycleCountBasedThreePhaseSimulationStrategy.this.pthreadSpawnedIndex) {
                    pthreadHasSpawned = true;
                }
            }
        });
    }

    @Override
    public void endSimulation() {
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

    public final boolean canDoFastForwardOneCycle() {
        return !pthreadHasSpawned;
    }

    public final boolean canDoCacheWarmupOneCycle() {
        return --this.numCyclesInCacheWarmup >= 0;
    }

    public final boolean canDoMeasurementOneCycle() {
        return --this.numCyclesInMeasurement >= 0;
    }

    public boolean isPthreadHasSpawned() {
        return pthreadHasSpawned;
    }
}
