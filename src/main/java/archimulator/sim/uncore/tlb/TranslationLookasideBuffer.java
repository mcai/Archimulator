/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.tlb;

import archimulator.sim.common.Named;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.action.Action;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

/**
 * Translation lookaside buffer (TLB).
 *
 * @author Min Cai
 */
public class TranslationLookasideBuffer implements Named {
    private String name;

    private EvictableCache<Boolean> cache;

    private long numHits;
    private long numMisses;

    private long numEvictions;

    /**
     * Create a translation lookaside buffer (TLB).
     *
     * @param parent the parent simulation object
     * @param name the name
     */
    public TranslationLookasideBuffer(SimulationObject parent, String name) {
        this.name = name;

        ValueProviderFactory<Boolean, ValueProvider<Boolean>> cacheLineStateProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        };

        this.cache = new EvictableCache<Boolean>(parent, name, new CacheGeometry(parent.getExperiment().getArchitecture().getTlbSize(), parent.getExperiment().getArchitecture().getTlbAssociativity(), parent.getExperiment().getArchitecture().getTlbLineSize()), CacheReplacementPolicyType.LRU, cacheLineStateProviderFactory);
    }

    /**
     * Act on a TLB access.
     *
     * @param access the memory hierarchy access
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public void access(MemoryHierarchyAccess access, Action onCompletedCallback) {
        int set = this.cache.getSet(access.getPhysicalAddress());
        CacheAccess<Boolean> cacheAccess = this.cache.newAccess(access, access.getPhysicalAddress());

        if (cacheAccess.isHitInCache()) {
            getCache().getReplacementPolicy().handlePromotionOnHit(access, set, cacheAccess.getWay());

            this.numHits++;
        } else {
            if (cacheAccess.isReplacement()) {
                this.numEvictions++;
            }

            CacheLine<Boolean> line = this.cache.getLine(set, cacheAccess.getWay());
            BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
            stateProvider.state = true;
            line.setAccess(access);
            line.setTag(access.getPhysicalTag());
            getCache().getReplacementPolicy().handleInsertionOnMiss(access, set, cacheAccess.getWay());

            this.numMisses++;
        }

        access.getThread().getCycleAccurateEventQueue().schedule(this, onCompletedCallback, cacheAccess.isHitInCache() ? this.getHitLatency() : this.getMissLatency());
    }

    /**
     * Get the name of the translation lookaside buffer (TLB).
     *
     * @return the name of the translation lookaside buffer (TLB)
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of misses.
     *
     * @return the number of misses
     */
    public long getNumMisses() {
        return this.numMisses;
    }

    /**
     * Get the number of hits.
     *
     * @return the number of hits
     */
    public long getNumHits() {
        return numHits;
    }

    /**
     * Get the number of accesses.
     *
     * @return the number of accesses
     */
    public long getNumAccesses() {
        return numHits + numMisses;
    }

    /**
     * Get the hit ratio.
     *
     * @return the hit ratio
     */
    public double getHitRatio() {
        return this.getNumAccesses() > 0 ? (double) this.numHits / this.getNumAccesses() : 0.0;
    }

    /**
     * Get the number of evictions.
     *
     * @return the number of evictions
     */
    public long getNumEvictions() {
        return numEvictions;
    }

    /**
     * Get the occupancy ratio.
     *
     * @return the occupancy ratio
     */
    public double getOccupancyRatio() {
        return getCache().getOccupancyRatio();
    }

    /**
     * Get the hit latency.
     *
     * @return the hit latency
     */
    public int getHitLatency() {
        return getCache().getExperiment().getArchitecture().getTlbHitLatency();
    }

    /**
     * Get the miss latency.
     *
     * @return the miss latency
     */
    public int getMissLatency() {
        return getCache().getExperiment().getArchitecture().getTlbMissLatency();
    }

    /**
     * Get the owned evictable cache.
     *
     * @return the owned evictable cache
     */
    public EvictableCache<Boolean> getCache() {
        return cache;
    }

    /**
     * Boolean value provider.
     *
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        protected boolean state;

        /**
         * Create a boolean value provider.
         */
        public BooleanValueProvider() {
            this.state = false;
        }

        /**
         * Get the state.
         *
         * @return the state
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial state.
         *
         * @return the initial state
         */
        @Override
        public Boolean getInitialValue() {
            return false;
        }
    }
}
