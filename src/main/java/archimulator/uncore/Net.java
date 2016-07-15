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
package archimulator.uncore;

import archimulator.common.CPUExperiment;
import archimulator.common.Simulation;
import archimulator.common.SimulationObject;
import archimulator.uncore.MemoryDevice;

/**
 * Net.
 *
 * @author Min Cai
 */
public interface Net extends SimulationObject<CPUExperiment, Simulation> {
    /**
     * Transfer a message of the specified size from the source device to the destination device.
     *
     * @param deviceFrom        the source device
     * @param deviceTo   the destination device
     * @param size                the size
     * @param onCompletedCallback the callback action performed when the transfer is completed
     */
    void transfer(MemoryDevice deviceFrom, MemoryDevice deviceTo, int size, Runnable onCompletedCallback);
}
