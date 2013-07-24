package archimulator.sim.uncore.cache.replacement.reuseDistancePrediction;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;
import net.pickapack.math.Quantizer;

/**
 * Helper thread aware reuse distance sampler.
 *
 * @author Min Cai
 */
public class HelperThreadAwareReuseDistanceSampler extends ReuseDistanceSampler {
    /**
     * Create a helper thread aware reuse distance sampler.
     *
     * @param parent                 the parent simulation object
     * @param samplingPeriod         the sampling period
     * @param maxReuseDistance       the maximum reuse distance
     * @param reuseDistanceQuantizer the reuse distance quantizer
     */
    public HelperThreadAwareReuseDistanceSampler(SimulationObject parent, String name, int samplingPeriod, int maxReuseDistance, Quantizer reuseDistanceQuantizer) {
        super(parent, name, samplingPeriod, maxReuseDistance, reuseDistanceQuantizer);
    }

    @Override
    public void update(int threadId, int pc, int address) {
        for (int i = 0; i < this.entries.size(); i++) {
            ReuseDistanceSamplerEntry entry = this.entries.get(i);
            if (entry.isValid() && entry.getAddress() == address) {
                entry.setValid(false);

                int foundEntryThreadId = entry.getThreadId();

                if(HelperThreadingHelper.isHelperThread(foundEntryThreadId) && HelperThreadingHelper.isMainThread(threadId)) {
                    this.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestReuseDistanceSampledEvent(this, foundEntryThreadId, threadId , entry.getPc(), this.reuseDistanceQuantizer.quantize(i * this.samplingPeriod)));
                }

                this.getBlockingEventDispatcher().dispatch(new ReuseDistanceSampledEvent(this, foundEntryThreadId, threadId , entry.getPc(), this.reuseDistanceQuantizer.quantize(i * this.samplingPeriod)));
                break;
            }
        }

        if (this.samplingCounter == 0) {
            ReuseDistanceSamplerEntry victimEntry = this.entries.get(this.entries.size() - 1);
            if (victimEntry.isValid()) {
                int victimEntryThreadId = victimEntry.getThreadId();

                if(HelperThreadingHelper.isHelperThread(victimEntryThreadId)) {
                    this.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestReuseDistanceSampledEvent(this, victimEntryThreadId, -1, victimEntry.getPc(), this.reuseDistanceQuantizer.getMaxValue()));
                }

                this.getBlockingEventDispatcher().dispatch(new ReuseDistanceSampledEvent(this, victimEntryThreadId, -1, victimEntry.getPc(), this.reuseDistanceQuantizer.getMaxValue()));
            }

            this.entries.remove(victimEntry);
            this.entries.add(0, victimEntry);

            victimEntry.setValid(true);
            victimEntry.setThreadId(threadId);
            victimEntry.setPc(pc);
            victimEntry.setAddress(address);

            this.samplingCounter = this.samplingPeriod - 1;
        } else {
            samplingCounter--;
        }
    }

    /**
     * Helper thread L2 request reuse distance sampled event.
     */
    public class HelperThreadL2RequestReuseDistanceSampledEvent extends ReuseDistanceSampledEvent {
        /**
         * Create a helper thread L2 request reuse distance sampled event.
         *
         * @param sender           the sender simulation object
         * @param leaderThreadId   the leader thread ID
         * @param followerThreadId the follower thread ID
         * @param pc               the value of the program counter (PC)
         * @param reuseDistance    the reuse distance
         */
        public HelperThreadL2RequestReuseDistanceSampledEvent(SimulationObject sender, int leaderThreadId, int followerThreadId, int pc, int reuseDistance) {
            super(sender, leaderThreadId, followerThreadId, pc, reuseDistance);
        }
    }
}
