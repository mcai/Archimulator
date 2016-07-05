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
 * Simple memory controller.
 *
 * @author Min Cai
 */
public class SimpleMemoryController extends MemoryController {
    private int latency;

    /**
     * Create a simple memory controller.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    public SimpleMemoryController(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);

        int busWidth = getExperiment().getConfig().getSimpleMemoryControllerBusWidth();
        int memoryLatency = getExperiment().getConfig().getSimpleMemoryControllerMemoryLatency();
        int memoryTrunkLatency = getExperiment().getConfig().getSimpleMemoryControllerMemoryTrunkLatency();

        int numChunks = (this.getLineSize() - (busWidth - 1)) / busWidth;
        if ((numChunks <= 0)) {
            throw new IllegalArgumentException();
        }

        this.latency = memoryLatency + (memoryTrunkLatency * (numChunks - 1));
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
     * Get the latency.
     *
     * @return the latency
     */
    private int getLatency() {
        return latency;
    }
}
