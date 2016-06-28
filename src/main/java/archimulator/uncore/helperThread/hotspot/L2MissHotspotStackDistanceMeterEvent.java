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
package archimulator.uncore.helperThread.hotspot;

import archimulator.common.SimulationObject;
import archimulator.common.meter.SimulationMeterEvent;

import java.io.Serializable;

/**
 * L2 cache miss Hotspot stack distance meter event.
 *
 * @author Min Cai
 */
public class L2MissHotspotStackDistanceMeterEvent extends SimulationMeterEvent<L2MissHotspotStackDistanceMeterEvent.L2MissHotspotStackDistanceMeterEventValue> {
    /**
     * Create an L2 cache miss hotspot stack distance meter event.
     *
     * @param sender       the sender simulation object
     * @param pc           the value of the program counter (PC)
     * @param address      the data access address
     * @param threadId     the thread ID
     * @param functionName the function symbol name
     * @param value        the value
     */
    public L2MissHotspotStackDistanceMeterEvent(SimulationObject sender, int pc, int address, int threadId, String functionName, L2MissHotspotStackDistanceMeterEventValue value) {
        super(sender, "L2MissHotspotStackDistanceMeterEvent", pc, address, threadId, functionName, value);
    }

    /**
     * L2 cache miss Hotspot stack distance meter event value.
     */
    public static class L2MissHotspotStackDistanceMeterEventValue implements Serializable {
        private long stackDistance;

        /**
         * Create an L2 cache miss hotspot stack distance meter event value.
         *
         * @param stackDistance the stack distance
         */
        public L2MissHotspotStackDistanceMeterEventValue(long stackDistance) {
            this.stackDistance = stackDistance;
        }

        /**
         * Get the stack distance.
         *
         * @return the stack distance
         */
        public long getStackDistance() {
            return stackDistance;
        }

        @Override
        public String toString() {
            return String.format("{stackDistance=%d}", stackDistance);
        }
    }
}
