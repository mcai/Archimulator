/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.dram;

import archimulator.uncore.MemoryHierarchy;
import archimulator.util.action.Action;

/**
 * Fixed latency memory controller.
 *
 * @author Min Cai
 */
public class FixedLatencyMemoryController extends MemoryController {
    /**
     * Create a fixed latency memory controller.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    public FixedLatencyMemoryController(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);
    }

    /**
     * Access the specified address.
     *
     * @param address             the address
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    @Override
    protected void access(int address, Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, onCompletedCallback, this.getLatency());
    }

    /**
     * Get the fixed latency.
     *
     * @return the fixed latency
     */
    public int getLatency() {
        return getExperiment().getFixedLatencyMemoryControllerLatency();
    }
}
