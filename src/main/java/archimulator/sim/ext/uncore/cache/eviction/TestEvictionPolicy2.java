package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.sim.core.BasicThread;
import archimulator.sim.ext.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.ext.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.util.action.Function3;

import java.io.Serializable;

public class TestEvictionPolicy2<StateT extends Serializable, LineT extends CacheLine<StateT>> extends LeastRecentlyUsedEvictionPolicy<StateT, LineT> {
    private MirrorCache mirrorCache;
    private Predictor<Boolean> replacementOwnershipPredictor;

    public TestEvictionPolicy2(EvictableCache<StateT, LineT> cache) {
        super(cache);

        this.mirrorCache = new MirrorCache();
        this.replacementOwnershipPredictor = new CacheBasedPredictor<Boolean>(cache, cache.getName() + ".replacemeentOwnershipPredictor", new CacheGeometry(16 * 16 * getCache().getLineSize(), 16, getCache().getLineSize()), 1, 3);
    }

    @Override
    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
        if (BasicThread.isMainThread(reference.getAccess().getThread().getId())) {
            return super.handleReplacement(reference);
        } else {
            int set = reference.getSet();

            for (int i = this.getCache().getAssociativity() - 1; i >= 0; i--) {
                int way = this.getWayInStackPosition(set, i);
                if (!this.mirrorCache.getLine(set, way).ownedByMainThread) {
                    return new CacheMiss<StateT, LineT>(this.getCache(), reference, way);
                }
            }

            return new CacheMiss<StateT, LineT>(this.getCache(), reference, getCache().getAssociativity() - 1); //TODO: or just bypass? i'm not sure the performance impact!
        }
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        super.handlePromotionOnHit(hit);

        this.handleLineReference(hit.getReference().getSet(), hit.getWay(), hit.getReference().getAccess().getThread().getId());
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        super.handleInsertionOnMiss(miss);

        this.mirrorCache.getLine(miss.getReference().getSet(), miss.getWay()).pc = miss.getReference().getAccess().getVirtualPc();

        this.handleLineReference(miss.getReference().getSet(), miss.getWay(), miss.getReference().getAccess().getThread().getId());
    }

    private void handleLineReference(int set, int way, int threadId) {
        int pc = this.mirrorCache.getLine(set, way).pc;

//        this.replacementOwnershipPredictor.update(pc, BasicThread.isMainThread(threadId));

        if (BasicThread.isMainThread(threadId)) {
            this.replacementOwnershipPredictor.update(pc, true);
        }

        this.mirrorCache.getLine(set, way).ownedByMainThread = this.replacementOwnershipPredictor.predict(pc, false);
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private int pc;
        private boolean ownedByMainThread;

        private MirrorCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, true);
        }
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache() {
            super(getCache(), getCache().getName() + ".testEvictionPolicy2.mirrorCache", getCache().getGeometry(), new Function3<Cache<?, ?>, Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                    return new MirrorCacheLine(cache, set, way);
                }
            });
        }
    }

    public static final EvictionPolicyFactory FACTORY = new EvictionPolicyFactory() {
        public String getName() {
            return "TEST_2";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new TestEvictionPolicy2<StateT, LineT>(cache);
        }
    };
}