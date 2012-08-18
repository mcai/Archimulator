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
package archimulator.sim.uncore.dram;

import archimulator.sim.uncore.CacheHierarchy;
import net.pickapack.action.Action;

public class SimpleMemoryController extends MemoryController {
    public SimpleMemoryController(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy);
    }

    @Override
    protected void access(int address, Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, onCompletedCallback, this.getLatency());
    }

    private int getLatency() {
        int chunks = (this.getLineSize() - (this.getBusWidth() - 1)) / this.getBusWidth();
        if ((chunks <= 0)) {
            throw new IllegalArgumentException();
        }

        return this.getMemoryLatency() + (this.getMemoryTrunkLatency() * (chunks - 1));
    }

    public int getMemoryLatency() {
        return getExperiment().getArchitecture().getSimpleMainMemoryMemoryLatency();
    }

    public int getMemoryTrunkLatency() {
        return getExperiment().getArchitecture().getSimpleMainMemoryMemoryTrunkLatency();
    }

    public int getBusWidth() {
        return getExperiment().getArchitecture().getSimpleMainMemoryBusWidth();
    }
}
