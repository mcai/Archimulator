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
package archimulator.mem.cache.eviction;

import archimulator.mem.cache.*;
import archimulator.util.action.Function2;

import java.io.Serializable;

public class LeastFrequentlyUsedEvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends EvictionPolicy<StateT, LineT> {
    private MirrorCache mirrorCache;

    public LeastFrequentlyUsedEvictionPolicy(EvictableCache<StateT, LineT> cache) {
        super(cache);

        this.mirrorCache = new MirrorCache();
    }

    @Override
    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
        int minFrequency = Integer.MAX_VALUE;
        int victimWay = getCache().getAssociativity() - 1;

        for (MirrorCacheLine mirrorCacheLine : this.mirrorCache.getLines(reference.getSet())) {
            int frequency = mirrorCacheLine.frequency;

            if (frequency < minFrequency) {
                minFrequency = frequency;
                victimWay = mirrorCacheLine.getWay();
            }
        }

        return new CacheMiss<StateT, LineT>(this.getCache(), reference, victimWay);
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        this.mirrorCache.getLine(hit.getLine()).frequency++;
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        this.mirrorCache.getLine(miss.getLine()).frequency = 0;
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private int frequency;

        private MirrorCacheLine(int set, int way) {
            super(set, way, true);
        }
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache() {
            super(getCache(), getCache().getName() + ".leastFrequentlyUsedEvictionPolicy.mirrorCache", getCache().getGeometry(), new Function2<Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Integer set, Integer way) {
                    return new MirrorCacheLine(set, way);
                }
            });
        }

        private MirrorCacheLine getLine(CacheLine<?> ownerCacheLine) {
            return this.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
        }
    }

    public static final EvictionPolicyFactory FACTORY = new EvictionPolicyFactory() {
        public String getName() {
            return "LEAST_FREQUENTLY_USED";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new LeastFrequentlyUsedEvictionPolicy<StateT, LineT>(cache);
        }
    };
}
