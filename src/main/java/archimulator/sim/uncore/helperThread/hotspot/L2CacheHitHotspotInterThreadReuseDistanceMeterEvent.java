package archimulator.sim.uncore.helperThread.hotspot;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.common.meter.SimulationMeterEvent;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestState;

import java.io.Serializable;

/**
 * L2 cache hit Hotspot inter-thread reuse distance meter event.
 */
public class L2CacheHitHotspotInterThreadReuseDistanceMeterEvent extends SimulationMeterEvent<L2CacheHitHotspotInterThreadReuseDistanceMeterEvent.L2CacheHitHotspotInterThreadReuseDistanceMeterEventValue> {
    /**
     * Create an L2 cache hit hotspot inter-thread reuse distance meter event.
     *
     * @param sender       the sender simulation object
     * @param pc           the value of the program counter (PC)
     * @param address      the data access address
     * @param threadId     the thread ID
     * @param functionName the function symbol name
     * @param value        the value
     */
    public L2CacheHitHotspotInterThreadReuseDistanceMeterEvent(SimulationObject sender, int pc, int address, int threadId, String functionName, L2CacheHitHotspotInterThreadReuseDistanceMeterEventValue value) {
        super(sender, "L2CacheHitHotspotInterThreadReuseDistanceMeterEvent", pc, address, threadId, functionName, value);
    }

    /**
     * L2 cache hit Hotspot inter-thread reuse distance meter event value.
     */
    public static class L2CacheHitHotspotInterThreadReuseDistanceMeterEventValue implements Serializable {
        private HelperThreadL2CacheRequestState helperThreadL2CacheRequestState;
        private long reuseDistance;

        /**
         * Create an L2 cache hit hotspot inter-thread reuse distance meter event value.
         *
         * @param helperThreadL2CacheRequestState the helper thread L2 cache request state
         * @param reuseDistance the reuse distance
         */
        public L2CacheHitHotspotInterThreadReuseDistanceMeterEventValue(HelperThreadL2CacheRequestState helperThreadL2CacheRequestState, long reuseDistance) {
            this.helperThreadL2CacheRequestState = helperThreadL2CacheRequestState;
            this.reuseDistance = reuseDistance;
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
         * Get the reuse distance.
         *
         * @return the reuse distance
         */
        public long getReuseDistance() {
            return reuseDistance;
        }

        @Override
        public String toString() {
            return String.format("{htRequestState=%s, reuseDistance=%d}", helperThreadL2CacheRequestState, reuseDistance);
        }
    }
}
