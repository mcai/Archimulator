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
package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.CacheMiss;
import archimulator.sim.uncore.cache.CacheReference;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;

import java.io.Serializable;

public class ReuseDistancePredictionEvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends AbstractReuseDistancePredictionEvictionPolicy<StateT, LineT> {
    private boolean selectiveCaching;

    public ReuseDistancePredictionEvictionPolicy(EvictableCache<StateT, LineT> cache, boolean selectiveCaching) {
        super(cache);

        this.selectiveCaching = selectiveCaching;
    }

    @Override
    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
        CacheMiss<StateT, LineT> miss = handleReplacementBasedOnReuseDistancePrediction(reference, this.selectiveCaching);

        if (miss.isBypass()) {
            this.updateOnEveryAccess(reference.getAccess().getVirtualPc(), reference.getAddress(), reference.getAccessType());
        }

        return miss;
    }

    public boolean isSelectiveCaching() {
        return selectiveCaching;
    }

    public static final EvictionPolicyFactory FACTORY_WITHOUT_SELECTIVE_CACHING = new EvictionPolicyFactory() {
        public String getName() {
            return "REUSE_DISTANCE_PREDICTION";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new ReuseDistancePredictionEvictionPolicy<StateT, LineT>(cache, false);
        }
    };

    public static final EvictionPolicyFactory FACTORY_WITH_SELECTIVE_CACHING = new EvictionPolicyFactory() {
        public String getName() {
            return "REUSE_DISTANCE_PREDICTION_WITH_SELECTIVE_CACHING";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new ReuseDistancePredictionEvictionPolicy<StateT, LineT>(cache, true);
        }
    };
}