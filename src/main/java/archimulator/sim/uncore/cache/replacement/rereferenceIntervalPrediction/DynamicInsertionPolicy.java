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
package archimulator.sim.uncore.cache.replacement.rereferenceIntervalPrediction;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.MainThreadL2MissBasedSetDuelingUnit;
import archimulator.sim.uncore.cache.SetDuelingUnit;

import java.util.Random;

/**
 * Dynamic insertion policy.
 *
 * @author Min Cai
 */
public class DynamicInsertionPolicy {
    private SetDuelingUnit setDuelingUnit;

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

        this.setDuelingUnit = new MainThreadL2MissBasedSetDuelingUnit(cache, cache.getExperiment().getArchitecture().getNumCores(), 2, numSetsPerSetDuelingMonitor);
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
