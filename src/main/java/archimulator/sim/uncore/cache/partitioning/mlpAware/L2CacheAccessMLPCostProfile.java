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
package archimulator.sim.uncore.cache.partitioning.mlpAware;

import java.util.ArrayList;
import java.util.List;

/**
 * L2 cache access MLP-cost profile.
 *
 * @author Min Cai
 */
public class L2CacheAccessMLPCostProfile {
    private List<Integer> hitCounters;
    private int missCounter;

    /**
     * Create an L2 cache access MLP-cost profile.
     *
     * @param associativity the associativity
     */
    public L2CacheAccessMLPCostProfile(int associativity) {
        this.hitCounters = new ArrayList<Integer>();

        for (int i = 0; i < associativity; i++) {
            this.hitCounters.add(0);
        }
    }

    /**
     * Increment the hit counter for the specified stack distance.
     *
     * @param stackDistance the stack distance
     */
    public void incrementCounter(int stackDistance) {
        if (stackDistance == -1) {
            this.missCounter++;
        } else {
            this.hitCounters.set(stackDistance, this.hitCounters.get(stackDistance) + 1);
        }
    }

    /**
     * Decrement the hit counter for the specified stack distance.
     *
     * @param stackDistance the stack distance
     */
    public void decrementCounter(int stackDistance) {
        if (stackDistance == -1) {
            this.missCounter--;
        } else {
            this.hitCounters.set(stackDistance, this.hitCounters.get(stackDistance) - 1);
        }
    }

    /**
     * Get the value of N, which is the number of L2 accesses with stack distance greater than or equal to the specified stack distance.
     *
     * @param stackDistance the stack distance
     * @return the value of N, which is the number of L2 accesses with stack distance greater than or equal to the specified stack distance
     */
    public int getN(int stackDistance) {
        if (stackDistance == -1) {
            return this.missCounter;
        }

        int n = 0;

        for (int i = stackDistance; i < this.hitCounters.size(); i++) {
            n += this.hitCounters.get(i);
        }

        return n;
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
}
