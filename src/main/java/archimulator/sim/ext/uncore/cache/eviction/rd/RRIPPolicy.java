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
package archimulator.sim.ext.uncore.cache.eviction.rd;

import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;

public class RRIPPolicy<StateT extends Serializable> extends EvictionPolicy<StateT> {
    private int predictedRereferenceIntervalMaxValue;
    private DynamicInsertionPolicy insertionPolicy;
    private Cache<Boolean> mirrorCache;

    public RRIPPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.predictedRereferenceIntervalMaxValue = 3;

        ValueProviderFactory<Boolean, ValueProvider<Boolean>> valueProviderFactory = new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        };

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".rereferenceIntervalPredictionEvictionPolicy.mirrorCache", cache.getGeometry(), valueProviderFactory);

        this.insertionPolicy = new DynamicInsertionPolicy(cache, 4, ((1 << 10) - 1), 8); //TODO: parameter passing
    }

    @Override
    public CacheMiss<StateT> handleReplacement(CacheReference reference) {
        int set = reference.getSet();

        do {
            /* Search for victim whose predicted rereference interval value is furthest in future */
            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
                BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
                if (stateProvider.predictedRereferenceInterval == this.predictedRereferenceIntervalMaxValue) {
                    return new CacheMiss<StateT>(this.getCache(), reference, way);
                }
            }

            /* If victim is not found, then move all rereference prediction values into future and then repeat the search again */
            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
                BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
                stateProvider.predictedRereferenceInterval++;
            }
        } while (true);
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT> hit) {
        // Set the line's predicted rereference interval value to near-immediate (0)
        CacheLine<Boolean> line = this.mirrorCache.getLine(hit.getReference().getSet(), hit.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.predictedRereferenceInterval = 0;
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT> miss) {
        this.insertionPolicy.recordMiss(miss.getReference().getSet());

        if (this.insertionPolicy.shouldDoNormalFill(miss.getReference().getAccess().getThread().getId(), miss.getReference().getSet())) {
            CacheLine<Boolean> line = this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay());
            BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
            stateProvider.predictedRereferenceInterval = this.predictedRereferenceIntervalMaxValue - 1;
        }
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private int predictedRereferenceInterval;

        public BooleanValueProvider() {
            this.state = true;
            this.predictedRereferenceInterval = predictedRereferenceIntervalMaxValue;
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
