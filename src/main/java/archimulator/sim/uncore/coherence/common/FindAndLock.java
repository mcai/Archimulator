package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.LockableCacheLineReplacementState;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.util.action.Action;

//TODO
public class FindAndLock {
    private CoherentCache cache;
    private MemoryHierarchyAccess access;
    private int tag;
    private CacheAccessType cacheAccessType;
    private CacheAccess<MESIState, LockableCacheLine> cacheAccess;

    public FindAndLock(CoherentCache cache, final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
        this.cacheAccessType = cacheAccessType;
    }

    public void run() {
        if (this.doLockingProcess()) {
            if (cacheAccess.isEviction()) {
                this.evict(access);
            }
        }
    }

    private boolean doLockingProcess() {
        if (this.cacheAccess == null) {
            this.cacheAccess = this.getCache().getCache().newAccess(this.getCache(), access, tag, cacheAccessType);
        }

        if (this.cacheAccess.isHitInCache() || !this.cacheAccess.isBypass()) {
            if (this.cacheAccess.getLine().isLocked() && !cacheAccessType.isUpward()) {
                if (this.cacheAccess.isHitInCache()) {
                    this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(this.getCache(), tag, access, this.cacheAccess.getLine()));
                }
            }

            if (this.cacheAccess.getLine().isLocked() && cacheAccessType.isDownward()) {
//                throw new CoherentCacheException();
                return false;
            } else {
                Action action = new Action() {
                    public void apply() {
                        doLockingProcess();
                    }
                };

                boolean result = this.cacheAccess.isHitInCache() ? this.cacheAccess.getLine().beginHit(action, tag) :
                        (this.cacheAccess.isEviction() ? this.cacheAccess.getLine().beginEvict(action, tag) : this.cacheAccess.getLine().beginFill(action, tag));

                if (result) {
                    if (!cacheAccessType.isUpward()) {
                        this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCache(), tag, access, this.cacheAccess.getLine(), this.cacheAccess.isHitInCache(), this.cacheAccess.isEviction(), this.cacheAccess.getReference().getAccessType()));
                    }

                    if (this.cacheAccess.isEviction()) {
                        getCache().incEvictions();
                    }
                }
            }
        }

        if (this.cacheAccess.getLine().getReplacementState() == LockableCacheLineReplacementState.HITTING || this.cacheAccess.getLine().getReplacementState() == LockableCacheLineReplacementState.FILLING || this.cacheAccess.isBypass()) {
            this.getCache().updateStats(cacheAccessType, this.cacheAccess);
            this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(this.getCache(), access, this.cacheAccess));
        }

        return true;
    }

    private void evict(MemoryHierarchyAccess access) {
        final boolean hasData = cacheAccess.getLine().getState() == MESIState.MODIFIED;

        final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;


//        getCache().sendRequest(getCache().getNext(), new EvictMessage(access, cacheAccess.getLine().getTag(), hasData, new Action1<EvictMessage>() {
//            public void apply(EvictMessage evictMessage) {
//                if (evictMessage.isError()) {
//                    error = true;
//                } else {
//                    completed = true;
//                }
//            }
//        }), size);

        cacheAccess.getLine().endEvict();
    }

//    public CoherentCache getCache() {
//        return cache;
//    }

    public CacheAccess<MESIState, LockableCacheLine> getCacheAccess() {
        return cacheAccess;
    }

    //TODO
    public FirstLevelCache getCache() {
        return (FirstLevelCache) cache;
    }
}
