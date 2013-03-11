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
package archimulator.sim.uncore.cache.stackDistanceProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack distance profile.
 *
 * @author Min Cai
 */
public class StackDistanceProfile {
    private List<Integer> hitCounters;
    private int missCounter;

    /**
     * Create a stack distance profile.
     *
     * @param associativity the associativity
     */
    public StackDistanceProfile(int associativity) {
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
    public void incrementHitCounter(int stackDistance) {
        this.getHitCounters().set(stackDistance, this.getHitCounters().get(stackDistance) + 1);
    }

    /**
     * Increment the miss counter.
     */
    public void incrementMissCounter() {
        this.missCounter++;
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
