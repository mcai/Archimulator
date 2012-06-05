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

import archimulator.sim.base.event.MyBlockingEventDispatcher;
import archimulator.sim.base.event.PseudocallEncounteredEvent;
import archimulator.sim.base.simulation.Logger;
import net.pickapack.action.Action1;

import java.util.concurrent.CyclicBarrier;

public class RunToEndFunctionalSimulationStrategy extends SequentialSimulationStrategy {
    private int pthreadSpawnedIndex;

    public RunToEndFunctionalSimulationStrategy(CyclicBarrier phaser) {
        this(phaser, 3720);
    }

    public RunToEndFunctionalSimulationStrategy(CyclicBarrier phaser, int pthreadSpawnedIndex) {
        super(phaser);

        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
    }

    @Override
    public boolean canDoFastForwardOneCycle() {
        return true;
    }

    @Override
    public boolean canDoCacheWarmupOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean canDoMeasurementOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public void beginSimulation() {
        this.getSimulation().getBlockingEventDispatcher().addListener2(PseudocallEncounteredEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
                if (event.getImm() == RunToEndFunctionalSimulationStrategy.this.pthreadSpawnedIndex) {
                    Logger.infof(Logger.SIMULATION, "%s encountered pseudocall %d", getSimulation().getCycleAccurateEventQueue().getCurrentCycle(), event.getContext().getThread(getSimulation().getProcessor()).getName(), event.getImm());
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
        return false;
    }

    @Override
    public boolean isSupportMeasurement() {
        return false;
    }
}
