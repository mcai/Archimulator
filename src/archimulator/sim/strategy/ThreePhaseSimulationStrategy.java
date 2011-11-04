/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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

import java.util.concurrent.CyclicBarrier;

public abstract class ThreePhaseSimulationStrategy extends SequentialSimulationStrategy {
    protected State state;

    protected ThreePhaseSimulationStrategy(CyclicBarrier phaser) {
        super(phaser);

        this.setState(State.FAST_FORWARD);
    }

    protected void switchToWarmup() {
        this.setState(State.CACHE_WARMUP);
    }

    protected void switchToMeasurement() {
        this.setState(State.MEASUREMENT);
        this.resetStat();
    }

    @Override
    public void beginSimulation() {
        this.state = State.FAST_FORWARD;
    }

    @Override
    public void endSimulation() {
        if (this.state != State.STOPPED) {
            this.setState(State.STOPPED);
        }
    }

    protected abstract void resetStat();

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

    public void setState(State state) {
        this.state = state;
    }

    protected static enum State {
        FAST_FORWARD,
        CACHE_WARMUP,
        MEASUREMENT,
        STOPPED
    }
}
