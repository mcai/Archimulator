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
package archimulator.sim.uncore.cache.replacement.reuseDistancePrediction;

import net.pickapack.math.Quantizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Reuse distance sampler.
 *
 * @author Min Cai
 */
public class ReuseDistanceSampler {
    private ReuseDistanceMonitor reuseDistanceMonitor;

    private List<ReuseDistanceSamplerEntry> entries;

    private int samplingPeriod;
    private int samplingCounter;

    private Quantizer reuseDistanceQuantizer;

    /**
     * Create a reuse distance sampler.
     *
     * @param reuseDistanceMonitor the parent reuse distance monitor
     * @param samplingPeriod the sampling period
     * @param maxReuseDistance the maximum reuse distance
     * @param reuseDistanceQuantizer the reuse distance quantizer
     */
    public ReuseDistanceSampler(ReuseDistanceMonitor reuseDistanceMonitor, int samplingPeriod, int maxReuseDistance, Quantizer reuseDistanceQuantizer) {
        this.reuseDistanceMonitor = reuseDistanceMonitor;
        this.samplingPeriod = samplingPeriod;
        this.reuseDistanceQuantizer = reuseDistanceQuantizer;

        this.samplingCounter = 0;

        this.entries = new ArrayList<ReuseDistanceSamplerEntry>();
        for (int i = 0; i < maxReuseDistance / this.samplingPeriod; i++) {
            this.entries.add(new ReuseDistanceSamplerEntry());
        }
    }

    /**
     * Update.
     *
     * @param pc the value of the program counter (PC)
     * @param address the address
     */
    public void update(int pc, int address) {
        for (int i = 0; i < this.entries.size(); i++) {
            ReuseDistanceSamplerEntry entry = this.entries.get(i);
            if (entry.isValid() && entry.getAddress() == address) {
                entry.setValid(false);
                int reuseDistance = (i + 8) * this.samplingPeriod;
                reuseDistanceMonitor.getPredictor().update(entry.getPc(), this.reuseDistanceQuantizer.quantize(reuseDistance));
                break;
            }
        }

        if (this.samplingCounter == 0) {
            ReuseDistanceSamplerEntry victimEntry = this.entries.get(this.entries.size() - 1);
            if (victimEntry.isValid()) {
                reuseDistanceMonitor.getPredictor().update(victimEntry.getPc(), this.reuseDistanceQuantizer.getMaxValue());
            }

            this.entries.remove(victimEntry);
            this.entries.add(0, victimEntry);

            victimEntry.setValid(true);
            victimEntry.setPc(pc);
            victimEntry.setAddress(address);

            this.samplingCounter = this.samplingPeriod - 1;
        } else {
            samplingCounter--;
        }
    }

    /**
     * Get the parent reuse distance monitor.
     *
     * @return the parent reuse distance monitor
     */
    public ReuseDistanceMonitor getReuseDistanceMonitor() {
        return reuseDistanceMonitor;
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
}
