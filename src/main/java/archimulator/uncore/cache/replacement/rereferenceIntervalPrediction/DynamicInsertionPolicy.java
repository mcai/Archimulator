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
package archimulator.uncore.cache.replacement.rereferenceIntervalPrediction;

import archimulator.uncore.cache.Cache;
import archimulator.uncore.cache.setDueling.AbstractSetDuelingUnit;
import archimulator.uncore.cache.setDueling.MainThreadL2MissBasedSetDuelingUnit;

import java.util.Random;

/**
 * Dynamic insertion policy.
 *
 * @author Min Cai
 */
public class DynamicInsertionPolicy {
    private AbstractSetDuelingUnit setDuelingUnit;

    private int bimodalSuggestionThrottle;

    private Random random;

    /**
     * Create a dynamic insertion policy.
     *
     * @param cache                          the parent cache
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public DynamicInsertionPolicy(Cache<?> cache, int numSetsPerSetDuelingMonitor) {
        this.bimodalSuggestionThrottle = 5;

        this.random = new Random(13);

        this.setDuelingUnit = new MainThreadL2MissBasedSetDuelingUnit(cache, 2, numSetsPerSetDuelingMonitor);
    }

    /**
     * Get a value indicating whether it should do normal fill or not.
     *
     * @param set      the set index
     * @param threadId the thread ID
     * @return a value indicating whether it should do normal fill or not
     */
    public boolean shouldDoNormalFill(int set, int threadId) {
        return this.setDuelingUnit.getPolicyId(set, threadId) == 0 || this.bimodalSuggestion(this.bimodalSuggestionThrottle);
    }

    /**
     * Get the bimodal suggestion.
     *
     * @param throttle the throttle
     * @return the bimodal suggestion
     */
    private boolean bimodalSuggestion(int throttle) {
        return this.random.nextInt(100) >= throttle;
    }
}
