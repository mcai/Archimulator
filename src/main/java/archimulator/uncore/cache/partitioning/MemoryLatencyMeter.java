/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.partitioning;

/**
 * Memory latency meter.
 *
 * @author Min Cai
 */
public class MemoryLatencyMeter {
    private long numSamples;
    private long totalCycles;

    /**
     * Record new sample.
     *
     * @param latency the memory latency in cycles
     */
    public void newSample(int latency) {
        this.numSamples++;
        this.totalCycles += latency;
    }

    /**
     * Get the average memory latency in cycles.
     *
     * @return the average memory latency in cycles
     */
    public int getAverageLatency() {
        return numSamples == 0 ? 0 : (int) (totalCycles / numSamples);
    }

    /**
     * Get the number of samples recorded.
     *
     * @return the number of samples recorded
     */
    public long getNumSamples() {
        return numSamples;
    }

    /**
     * Get the accumulated total number of cycles recorded.
     *
     * @return the accumulated total number of cycles recorded
     */
    public long getTotalCycles() {
        return totalCycles;
    }
}
