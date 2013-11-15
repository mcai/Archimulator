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
package archimulator.sim.uncore.cache;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyFactory;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;

/**
 * Basic cache which supports eviction (replacement).
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class BasicEvictableCache<StateT extends Serializable> extends BasicCache<StateT> implements EvictableCache<StateT> {
    /**
     * The replacement policy.
     */
    protected CacheReplacementPolicy<StateT> replacementPolicy;

    /**
     * Create a basic evictable cache.
     *
     * @param parent                        the parent simulation object.
     * @param name                          the name
     * @param geometry                      the geometry
     * @param replacementPolicyType         the replacement policy type
     * @param cacheLineStateProviderFactory the cache line state provider factory
     */
    public BasicEvictableCache(SimulationObject parent, String name, CacheGeometry geometry, CacheReplacementPolicyType replacementPolicyType, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent, name, geometry, cacheLineStateProviderFactory);

        this.replacementPolicy = CacheReplacementPolicyFactory.createCacheReplacementPolicy(replacementPolicyType, this);
    }

    /**
     * Get the replacement policy.
     *
     * @return the replacement policy
     */
    public CacheReplacementPolicy<StateT> getReplacementPolicy() {
        return replacementPolicy;
    }
}
