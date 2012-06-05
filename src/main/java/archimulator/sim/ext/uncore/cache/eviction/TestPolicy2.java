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

import archimulator.sim.core.BasicThread;
import archimulator.sim.ext.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.ext.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;

public class TestPolicy2<StateT extends Serializable> extends LRUPolicy<StateT> {
    private Cache<Boolean> mirrorCache;
    private Predictor<Boolean> replacementOwnershipPredictor;

    public TestPolicy2(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".testEvictionPolicy2.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });

        this.replacementOwnershipPredictor = new CacheBasedPredictor<Boolean>(cache, cache.getName() + ".replacemeentOwnershipPredictor", new CacheGeometry(16 * 16 * getCache().getLineSize(), 16, getCache().getLineSize()), 1, 3);
    }

    @Override
    public CacheMiss<StateT> handleReplacement(CacheReference reference) {
        if (BasicThread.isMainThread(reference.getAccess().getThread().getId())) {
            return super.handleReplacement(reference);
        } else {
            int set = reference.getSet();

            for (int i = this.getCache().getAssociativity() - 1; i >= 0; i--) {
                int way = this.getWayInStackPosition(set, i);
                CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
                BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
                if (!stateProvider.ownedByMainThread) {
                    return new CacheMiss<StateT>(this.getCache(), reference, way);
                }
            }

            return new CacheMiss<StateT>(this.getCache(), reference, getCache().getAssociativity() - 1); //TODO: or just bypass? i'm not sure the performance impact!
        }
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT> hit) {
        super.handlePromotionOnHit(hit);

        this.handleLineReference(hit.getReference().getSet(), hit.getWay(), hit.getReference().getAccess().getThread().getId());
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT> miss) {
        super.handleInsertionOnMiss(miss);

        CacheLine<Boolean> line = this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.pc = miss.getReference().getAccess().getVirtualPc();

        this.handleLineReference(miss.getReference().getSet(), miss.getWay(), miss.getReference().getAccess().getThread().getId());
    }

    private void handleLineReference(int set, int way, int threadId) {
        CacheLine<Boolean> line = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        int pc = stateProvider.pc;

//        this.replacementOwnershipPredictor.update(pc, BasicThread.isMainThread(threadId));

        if (BasicThread.isMainThread(threadId)) {
            this.replacementOwnershipPredictor.update(pc, true);
        }

        stateProvider.ownedByMainThread = this.replacementOwnershipPredictor.predict(pc, false);
    }

    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private int pc;
        private boolean ownedByMainThread;

        public BooleanValueProvider() {
            this.state = true;
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
