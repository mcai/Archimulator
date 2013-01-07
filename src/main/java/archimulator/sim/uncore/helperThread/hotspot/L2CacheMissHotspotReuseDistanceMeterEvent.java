/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.helperThread.hotspot;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.common.meter.SimulationMeterEvent;

import java.io.Serializable;

/**
 * L2 cache miss Hotspot reuse distance meter event.
 *
 * @author Min Cai
 */
public class L2CacheMissHotspotReuseDistanceMeterEvent extends SimulationMeterEvent<L2CacheMissHotspotReuseDistanceMeterEvent.L2CacheMissHotspotReuseDistanceMeterEventValue> {
    /**
     * Create an L2 cache miss hotspot reuse distance meter event.
     *
     * @param sender       the sender simulation object
     * @param pc           the value of the program counter (PC)
     * @param address      the data access address
     * @param threadId     the thread ID
     * @param functionName the function symbol name
     * @param value        the value
     */
    public L2CacheMissHotspotReuseDistanceMeterEvent(SimulationObject sender, int pc, int address, int threadId, String functionName, L2CacheMissHotspotReuseDistanceMeterEventValue value) {
        super(sender, "L2CacheMissHotspotReuseDistanceMeterEvent", pc, address, threadId, functionName, value);
    }

    /**
     * L2 cache miss Hotspot reuse distance meter event value.
     */
    public static class L2CacheMissHotspotReuseDistanceMeterEventValue implements Serializable {
        private long reuseDistance;

        /**
         * Create an L2 cache miss hotspot reuse distance meter event value.
         *
         * @param reuseDistance the reuse distance
         */
        public L2CacheMissHotspotReuseDistanceMeterEventValue(long reuseDistance) {
            this.reuseDistance = reuseDistance;
        }

        /**
         * Get the reuse distance.
         *
         * @return the reuse distance
         */
        public long getReuseDistance() {
            return reuseDistance;
        }

        @Override
        public String toString() {
            return String.format("{reuseDistance=%d}", reuseDistance);
        }
    }
}
