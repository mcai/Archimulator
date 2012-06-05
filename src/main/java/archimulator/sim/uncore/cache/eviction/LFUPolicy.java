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
package archimulator.sim.uncore.cache.eviction;

import archimulator.sim.uncore.cache.*;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;

public class LFUPolicy<StateT extends Serializable> extends EvictionPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    public LFUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".lfuPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });
    }

    @Override
    public CacheMiss<StateT> handleReplacement(CacheReference reference) {
        int minFrequency = Integer.MAX_VALUE;
        int victimWay = getCache().getAssociativity() - 1;

        for (CacheLine<Boolean> mirrorCacheLine : this.mirrorCache.getLines(reference.getSet())) {
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorCacheLine.getStateProvider();
            int frequency = stateProvider.frequency;

            if (frequency < minFrequency) {
                minFrequency = frequency;
                victimWay = mirrorCacheLine.getWay();
            }
        }

        return new CacheMiss<StateT>(this.getCache(), reference, victimWay);
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT> hit) {
        CacheLine<Boolean> line = this.getMirrorCacheLine(hit.getLine());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.frequency++;
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT> miss) {
        CacheLine<Boolean> line = this.getMirrorCacheLine(miss.getLine());
        BooleanValueProvider stateProvider = (BooleanValueProvider) line.getStateProvider();
        stateProvider.frequency = 0;
    }

    private static class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private int frequency;

        private BooleanValueProvider() {
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

    private CacheLine<Boolean> getMirrorCacheLine(CacheLine<?> ownerCacheLine) {
        return this.mirrorCache.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
    }
}
