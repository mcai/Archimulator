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
 *
 * @author Min Cai
 */
public class TranslationLookasideBuffer {
    private String name;

    private EvictableCache<Boolean> cache;

    private long numHits;
    private long numMisses;

    private long numEvictions;

    /**
     *
     * @param parent
     * @param name
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
     *
     * @param access
     * @param onCompletedCallback
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
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public long getNumMisses() {
        return this.numMisses;
    }

    /**
     *
     * @return
     */
    public double getHitRatio() {
        return this.getNumAccesses() > 0 ? (double) this.numHits / this.getNumAccesses() : 0.0;
    }

    /**
     *
     * @return
     */
    public long getNumAccesses() {
        return numHits + numMisses;
    }

    /**
     *
     * @return
     */
    public long getNumHits() {
        return numHits;
    }

    /**
     *
     * @return
     */
    public long getNumEvictions() {
        return numEvictions;
    }

    /**
     *
     * @return
     */
    public double getOccupancyRatio() {
        return getCache().getOccupancyRatio();
    }

    /**
     *
     * @return
     */
    public EvictableCache<Boolean> getCache() {
        return cache;
    }

    /**
     *
     * @return
     */
    public int getHitLatency() {
        return getCache().getExperiment().getArchitecture().getTlbHitLatency();
    }

    /**
     *
     * @return
     */
    public int getMissLatency() {
        return getCache().getExperiment().getArchitecture().getTlbMissLatency();
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        protected boolean state;

        public BooleanValueProvider() {
            this.state = false;
        }

        @Override
        public Boolean get() {
            return state;
        }

        @Override
        public Boolean getInitialValue() {
            return false;
        }
    }
}
