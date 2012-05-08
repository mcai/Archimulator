package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.AbstractEvictFlow;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;

public class FirstLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public FirstLevelCacheFindAndLockFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(cache, access, tag, cacheAccessType);
    }

    @Override
    protected AbstractEvictFlow createEvictFlow(MemoryHierarchyAccess access) {
        return new EvictFlow((FirstLevelCache) getCache(), access, getCacheAccess());
    }
}
