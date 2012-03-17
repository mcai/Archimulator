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

import java.util.concurrent.CyclicBarrier;

public class CycleCountBasedTwoPhaseSimulationStrategy extends SequentialSimulationStrategy {
    private long maxCyclesInFastForward;
    private long maxCyclesInMeasurement;

    private long numCyclesInFastForward;
    private long numCyclesInMeasurement;

    public CycleCountBasedTwoPhaseSimulationStrategy(CyclicBarrier phaser, long maxCyclesInFastForward, long maxCyclesInMeasurement) {
        super(phaser);

        this.maxCyclesInFastForward = maxCyclesInFastForward;
        this.maxCyclesInMeasurement = maxCyclesInMeasurement;
    }

    @Override
    public boolean canDoFastForwardOneCycle() {
        return --this.numCyclesInFastForward >= 0;
    }

    @Override
    public boolean canDoCacheWarmupOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean canDoMeasurementOneCycle() {
        return --this.numCyclesInMeasurement >= 0;
    }

    @Override
    public void beginSimulation() {
        this.numCyclesInFastForward = this.maxCyclesInFastForward;
        this.numCyclesInMeasurement = this.maxCyclesInMeasurement;
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
        return true;
    }
}
