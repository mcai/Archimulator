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

import archimulator.sim.event.PseudocallEncounteredEvent;
import archimulator.util.action.Action1;

import java.util.concurrent.CyclicBarrier;

public class RoiBasedThreePhaseSimulationStrategy extends ThreePhaseSimulationStrategy {
    private int maxRoiExecutionsInCacheWarmup;
    private int maxRoiExecutionsInMeasurement;
    private int pthreadSpawnedIndex;
    private int roiEntryIndex;
    private int roiExit0Index;
    private int roiExit1Index;

    private boolean inRoi;

    private long numRoiEntries;
    private long numRoiExits;

    private boolean pthreadHasSpawned;

    public RoiBasedThreePhaseSimulationStrategy(CyclicBarrier phaser) {
        this(phaser, 1000000, 2000000, 3720, 3721, 3722, 3723);
    }

    public RoiBasedThreePhaseSimulationStrategy(CyclicBarrier phaser, int maxRoiExecutionsInCacheWarmup, int maxRoiExecutionsInMeasurement, int pthreadSpawnedIndex, int roiEntryIndex, int roiExit0Index, int roiExit1Index) {
        super(phaser);

        this.maxRoiExecutionsInCacheWarmup = maxRoiExecutionsInCacheWarmup;
        this.maxRoiExecutionsInMeasurement = maxRoiExecutionsInMeasurement;
        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
        this.roiEntryIndex = roiEntryIndex;
        this.roiExit0Index = roiExit0Index;
        this.roiExit1Index = roiExit1Index;
    }

    private void enterRoi() {
        this.numRoiEntries++;

        inRoi = true;

        if (pthreadHasSpawned && state == State.FAST_FORWARD) {
            switchToWarmup();
        }
    }

    private void exitRoi() {
        this.numRoiExits++;

        inRoi = false;

        if (state == State.CACHE_WARMUP && this.numRoiExits >= this.maxRoiExecutionsInCacheWarmup) {
            switchToMeasurement();
        } else if (state == State.MEASUREMENT && this.numRoiExits >= this.maxRoiExecutionsInMeasurement) {
            endSimulation();
        }
    }

    @Override
    protected void resetStat() {
        this.numRoiEntries = 0;
        this.numRoiExits = 0;
    }

    @Override
    public void beginSimulation() {
        super.beginSimulation();

        this.inRoi = false;

        this.numRoiEntries = 0;
        this.numRoiExits = 0;

        this.pthreadHasSpawned = false;

        this.getSimulation().getBlockingEventDispatcher().addListener(PseudocallEncounteredEvent.class, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
                if (event.getArg() == RoiBasedThreePhaseSimulationStrategy.this.pthreadSpawnedIndex) {
                    pthreadHasSpawned = true;
                } else if (pthreadHasSpawned) {
                    if (event.getArg() == RoiBasedThreePhaseSimulationStrategy.this.roiEntryIndex) {
                        enterRoi();
                    } else if (event.getArg() == RoiBasedThreePhaseSimulationStrategy.this.roiExit0Index || event.getArg() == RoiBasedThreePhaseSimulationStrategy.this.roiExit1Index) {
                        exitRoi();
                    }
                }
            }
        });
    }

    @Override
    public void endSimulation() {
        super.endSimulation();
    }

    public final boolean canDoFastForwardOneCycle() {
        return state == State.FAST_FORWARD;
    }

    public final boolean canDoCacheWarmupOneCycle() {
        return state == State.CACHE_WARMUP;
    }

    public final boolean canDoMeasurementOneCycle() {
        return state == State.MEASUREMENT;
    }
}
