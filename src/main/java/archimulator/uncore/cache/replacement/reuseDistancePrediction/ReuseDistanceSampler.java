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
package archimulator.uncore.cache.replacement.reuseDistancePrediction;

import archimulator.common.BasicSimulationObject;
import archimulator.common.SimulationEvent;
import archimulator.common.SimulationObject;
import archimulator.util.math.Quantizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Reuse distance sampler.
 *
 * @author Min Cai
 */
public class ReuseDistanceSampler extends BasicSimulationObject {
    private String name;

    protected List<ReuseDistanceSamplerEntry> entries;

    protected int samplingPeriod;
    protected int samplingCounter;

    protected Quantizer reuseDistanceQuantizer;

    /**
     * Create a reuse distance sampler.
     *
     * @param parent                 the parent simulation object
     * @param samplingPeriod         the sampling period
     * @param maxReuseDistance       the maximum reuse distance
     * @param reuseDistanceQuantizer the reuse distance quantizer
     */
    public ReuseDistanceSampler(SimulationObject parent, String name, int samplingPeriod, int maxReuseDistance, Quantizer reuseDistanceQuantizer) {
        super(parent);

        this.name = name;
        this.samplingPeriod = samplingPeriod;
        this.reuseDistanceQuantizer = reuseDistanceQuantizer;

        this.samplingCounter = 0;

        this.entries = new ArrayList<>();
        for (int i = 0; i < maxReuseDistance / this.samplingPeriod; i++) {
            this.entries.add(new ReuseDistanceSamplerEntry());
        }
    }

    /**
     * Update.
     *
     * @param threadId the thread ID
     * @param pc       the value of the program counter (PC)
     * @param address  the address
     */
    public void update(int threadId, int pc, int address) {
        for (int i = 0; i < this.entries.size(); i++) {
            ReuseDistanceSamplerEntry entry = this.entries.get(i);
            if (entry.isValid() && entry.getAddress() == address) {
                entry.setValid(false);
                this.getBlockingEventDispatcher().dispatch(new ReuseDistanceSampledEvent(this, entry.getThreadId(), threadId, entry.getPc(), this.reuseDistanceQuantizer.quantize(i * this.samplingPeriod)));
                break;
            }
        }

        if (this.samplingCounter == 0) {
            ReuseDistanceSamplerEntry victimEntry = this.entries.get(this.entries.size() - 1);
            if (victimEntry.isValid()) {
                this.getBlockingEventDispatcher().dispatch(new ReuseDistanceSampledEvent(this, victimEntry.getThreadId(), -1, victimEntry.getPc(), this.reuseDistanceQuantizer.getMaxValue()));
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

    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the list of entries.
     *
     * @return the list of entries
     */
    public List<ReuseDistanceSamplerEntry> getEntries() {
        return entries;
    }

    /**
     * Get the sampling period.
     *
     * @return the sampling period
     */
    public int getSamplingPeriod() {
        return samplingPeriod;
    }

    /**
     * Get the value of the sampling counter.
     *
     * @return the value of the sampling counter
     */
    public int getSamplingCounter() {
        return samplingCounter;
    }

    /**
     * Get the reuse distance quantizer.
     *
     * @return the reuse distance quantizer
     */
    public Quantizer getReuseDistanceQuantizer() {
        return reuseDistanceQuantizer;
    }

    /**
     * The event fired when a reuse distance is sampled.
     */
    public class ReuseDistanceSampledEvent extends SimulationEvent {
        private int leaderThreadId;
        private int followerThreadId;
        private int pc;
        private int reuseDistance;

        /**
         * Create a event when a reuse distance is sampled.
         *
         * @param sender           the sender simulation object
         * @param leaderThreadId   the leader thread ID
         * @param followerThreadId the follower thread ID
         * @param pc               the value of the program counter (PC)
         * @param reuseDistance    the reuse distance
         */
        public ReuseDistanceSampledEvent(SimulationObject sender, int leaderThreadId, int followerThreadId, int pc, int reuseDistance) {
            super(sender);
            this.leaderThreadId = leaderThreadId;
            this.followerThreadId = followerThreadId;
            this.pc = pc;
            this.reuseDistance = reuseDistance;
        }

        /**
         * Get the leader thread ID.
         *
         * @return the leader thread ID
         */
        public int getLeaderThreadId() {
            return leaderThreadId;
        }

        /**
         * Get the follower thread ID.
         *
         * @return the follower thread ID
         */
        public int getFollowerThreadId() {
            return followerThreadId;
        }

        /**
         * Get the value of the program counter (PC).
         *
         * @return the value of the program counter (PC)
         */
        public int getPc() {
            return pc;
        }

        /**
         * Get the reuse distance.
         *
         * @return the reuse distance
         */
        public int getReuseDistance() {
            return reuseDistance;
        }
    }
}
