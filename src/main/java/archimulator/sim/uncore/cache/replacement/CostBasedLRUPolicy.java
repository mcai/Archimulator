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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;

/**
 * Cost based least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class CostBasedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    protected Cache<Boolean> mirrorCache;
    protected int lambda;

    /**
     * Create a cost based least recently used (LRU) policy.
     *
     * @param cache the parent evictable cache
     * @param lambda the lambda value
     */
    public CostBasedLRUPolicy(EvictableCache<StateT> cache, int lambda) {
        super(cache);
        this.lambda = lambda;
        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".costBasedLRUPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param tag    the tag
     * @return the newly created cache access object
     */
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int victimLinearSum = Integer.MAX_VALUE;
        int victimWay = 0;

        for(int way = 0; way < this.getCache().getAssociativity(); way++) {
            int recency = this.getCache().getAssociativity() - getStackPosition(set, way);
            int quantizedCost = this.getQuantizedCost(this.getCost(set, way));

            int linearSum = recency + lambda * quantizedCost;

            if (linearSum < victimLinearSum) {
                victimLinearSum = linearSum;
                victimWay = way;
            }
        }

        return new CacheAccess<StateT>(this.getCache(), access, set, victimWay, tag);
    }

    /**
     * Set the cost for the given set and way.
     *
     * @param set the set
     * @param way the way
     * @param cost the cost
     */
    public void setCost(int set, int way, double cost) {
        CacheLine<Boolean> mirrorLine = mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.cost = cost;
    }

    /**
     * Get the cost for the given set and way.
     *
     * @param set the set
     * @param way the way
     * @return the cost for the given set and way
     */
    public double getCost(int set, int way) {
        CacheLine<Boolean> mirrorLine = mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        return stateProvider.cost;
    }

    /**
     * Get the quantized cost for the specified cost.
     *
     * @param cost the cost
     * @return the quantized cost for the specified cost
     */
    protected abstract int getQuantizedCost(double cost);

    /**
     * Boolean value provider.
     */
    protected class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        protected double cost;

        /**
         * Create a boolean value provider.
         */
        protected BooleanValueProvider() {
            this.state = true;
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
            return true;
        }
    }
}