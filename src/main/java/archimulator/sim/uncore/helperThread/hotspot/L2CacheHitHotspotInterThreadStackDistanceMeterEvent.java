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
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestState;

import java.io.Serializable;

/**
 * L2 cache hit Hotspot inter-thread stack distance meter event.
 *
 * @author Min Cai
 */
public class L2CacheHitHotspotInterThreadStackDistanceMeterEvent extends SimulationMeterEvent<L2CacheHitHotspotInterThreadStackDistanceMeterEvent.L2CacheHitHotspotInterThreadStackDistanceMeterEventValue> {
    /**
     * Create an L2 cache hit hotspot inter-thread stack distance meter event.
     *
     * @param sender       the sender simulation object
     * @param pc           the value of the program counter (PC)
     * @param address      the data access address
     * @param threadId     the thread ID
     * @param functionName the function symbol name
     * @param value        the value
     */
    public L2CacheHitHotspotInterThreadStackDistanceMeterEvent(SimulationObject sender, int pc, int address, int threadId, String functionName, L2CacheHitHotspotInterThreadStackDistanceMeterEventValue value) {
        super(sender, "L2CacheHitHotspotInterThreadStackDistanceMeterEvent", pc, address, threadId, functionName, value);
    }

    /**
     * L2 cache hit Hotspot inter-thread stack distance meter event value.
     */
    public static class L2CacheHitHotspotInterThreadStackDistanceMeterEventValue implements Serializable {
        private HelperThreadL2CacheRequestState helperThreadL2CacheRequestState;
        private long stackDistance;

        /**
         * Create an L2 cache hit hotspot inter-thread stack distance meter event value.
         *
         * @param helperThreadL2CacheRequestState
         *                      the helper thread L2 cache request state
         * @param stackDistance the stack distance
         */
        public L2CacheHitHotspotInterThreadStackDistanceMeterEventValue(HelperThreadL2CacheRequestState helperThreadL2CacheRequestState, long stackDistance) {
            this.helperThreadL2CacheRequestState = helperThreadL2CacheRequestState;
            this.stackDistance = stackDistance;
        }

        /**
         * Get the helper thread L2 cache request state.
         *
         * @return the helper thread L2 cache request state
         */
        public HelperThreadL2CacheRequestState getHelperThreadL2CacheRequestState() {
            return helperThreadL2CacheRequestState;
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
            return String.format("{htRequestState=%s, stackDistance=%d}", helperThreadL2CacheRequestState, stackDistance);
        }
    }
}
