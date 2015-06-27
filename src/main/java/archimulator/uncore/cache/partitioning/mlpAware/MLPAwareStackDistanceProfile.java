/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.partitioning.mlpAware;

import java.util.ArrayList;
import java.util.List;

/**
 * MLP-aware stack distance profile.
 *
 * @author Min Cai
 */
public class MLPAwareStackDistanceProfile {
    private List<Integer> hitCounters;
    private int missCounter;

    /**
     * Create an MLP-aware stack distance profile.
     *
     * @param associativity the associativity
     */
    public MLPAwareStackDistanceProfile(int associativity) {
        this.hitCounters = new ArrayList<>();

        for (int i = 0; i < associativity; i++) {
            this.hitCounters.add(0);
        }
    }

    /**
     * Increment the hit counter for the specified stack distance.
     *
     * @param stackDistance    the stack distance
     * @param quantizedMlpCost the quantized MLP-cost
     */
    public void incrementHitCounter(int stackDistance, int quantizedMlpCost) {
        this.getHitCounters().set(stackDistance, this.getHitCounters().get(stackDistance) + quantizedMlpCost);
    }

    /**
     * Increment the miss counter.
     *
     * @param quantizedMlpCost the quantized MLP-cost
     */
    public void incrementMissCounter(int quantizedMlpCost) {
        this.missCounter += quantizedMlpCost;
    }

    /**
     * Begins new interval.
     */
    public void newInterval() {
        for(int i = 0; i < this.hitCounters.size(); i++) {
            this.hitCounters.set(i, (int) (this.hitCounters.get(i) * RHO));
        }

        this.missCounter *= RHO;
    }

    /**
     * Get the list of hit counters.
     *
     * @return the list of hit counters
     */
    public List<Integer> getHitCounters() {
        return hitCounters;
    }

    /**
     * Get the miss counter.
     *
     * @return the miss counter
     */
    public int getMissCounter() {
        return missCounter;
    }

    private static final double RHO = 0.5f;
}
