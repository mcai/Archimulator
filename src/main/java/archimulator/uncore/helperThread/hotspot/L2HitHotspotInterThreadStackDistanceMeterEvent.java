/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.helperThread.hotspot;

import archimulator.common.SimulationObject;
import archimulator.common.meter.SimulationMeterEvent;
import archimulator.uncore.helperThread.HelperThreadL2RequestState;

import java.io.Serializable;

/**
 * L2 cache hit Hotspot inter-thread stack distance meter event.
 *
 * @author Min Cai
 */
public class L2HitHotspotInterThreadStackDistanceMeterEvent extends SimulationMeterEvent<L2HitHotspotInterThreadStackDistanceMeterEvent.L2HitHotspotInterThreadStackDistanceMeterEventValue> {
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
    public L2HitHotspotInterThreadStackDistanceMeterEvent(SimulationObject sender, int pc, int address, int threadId, String functionName, L2HitHotspotInterThreadStackDistanceMeterEventValue value) {
        super(sender, "L2HitHotspotInterThreadStackDistanceMeterEvent", pc, address, threadId, functionName, value);
    }

    /**
     * L2 cache hit Hotspot inter-thread stack distance meter event value.
     */
    public static class L2HitHotspotInterThreadStackDistanceMeterEventValue implements Serializable {
        private HelperThreadL2RequestState helperThreadL2RequestState;
        private long stackDistance;

        /**
         * Create an L2 cache hit hotspot inter-thread stack distance meter event value.
         *
         * @param helperThreadL2RequestState
         *                      the helper thread L2 cache request state
         * @param stackDistance the stack distance
         */
        public L2HitHotspotInterThreadStackDistanceMeterEventValue(HelperThreadL2RequestState helperThreadL2RequestState, long stackDistance) {
            this.helperThreadL2RequestState = helperThreadL2RequestState;
            this.stackDistance = stackDistance;
        }

        /**
         * Get the helper thread L2 cache request state.
         *
         * @return the helper thread L2 cache request state
         */
        public HelperThreadL2RequestState getHelperThreadL2RequestState() {
            return helperThreadL2RequestState;
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
            return String.format("{htRequestState=%s, stackDistance=%d}", helperThreadL2RequestState, stackDistance);
        }
    }
}
