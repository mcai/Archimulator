/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement.costAware;

import archimulator.uncore.cache.BasicCache;
import archimulator.uncore.cache.Cache;
import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.uncore.cache.prediction.Predictor;
import archimulator.uncore.cache.replacement.LRUPolicy;
import archimulator.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.ValueProvider;

import java.io.Serializable;

/**
 * Abstract cost aware least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class AbstractCostAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    protected Cache<Boolean> mirrorCache;
    private Predictor<Double> costPredictor;

    /**
     * Create an abstract cost aware least recently used (LRU) policy.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public AbstractCostAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new BasicCache<>(cache, cache.getName() + ".abstractCostAwareLRUPolicy.mirrorCache", cache.getGeometry(), args -> new BooleanValueProvider());

        this.costPredictor = new CacheBasedPredictor<>(
                cache,
                cache.getName() + ".abstractCostAwareLRUPolicy.costPredictor",
                16,
                1,
                3,
                Double.MAX_VALUE
        );
    }

    /**
     * Set the cost for the given set and way.
     *
     * @param set the set
     * @param way the way
     * @param cost the cost
     */
    @SuppressWarnings("unchecked")
    public void setCost(int set, int way, double cost) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.cost = cost;

        this.costPredictor.update(this.getCache().getLine(set, way).getAccess().getVirtualPc(), cost);
    }

    /**
     * Get the cost for the given set and way.
     *
     * @param set the set
     * @param way the way
     * @return the cost for the given set and way
     */
    @SuppressWarnings("unchecked")
    public double getCost(int set, int way) {
        if(!isStable(set, way)) {
            return this.costPredictor.predict(this.getCache().getLine(set, way).getAccess().getVirtualPc());
        }

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        return stateProvider.cost;
    }

    /**
     * Get a value indicating whether the specified line is in the stable state or not.
     *
     * @param set the set
     * @param way the way
     * @return a value indicating whether the specified line is in the stable state or not
     */
    @SuppressWarnings("unchecked")
    protected boolean isStable(int set, int way) {
        StateT state = this.getCache().getLine(set, way).getState();

        return !(state instanceof DirectoryControllerState) || ((DirectoryControllerState) state).isStable();
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
