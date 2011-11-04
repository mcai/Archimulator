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
