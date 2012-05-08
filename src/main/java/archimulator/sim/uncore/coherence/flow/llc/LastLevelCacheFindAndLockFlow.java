package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.flow.AbstractEvictFlow;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;

public class LastLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public LastLevelCacheFindAndLockFlow(LastLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(cache, access, tag, cacheAccessType);
    }

    @Override
    protected AbstractEvictFlow createEvictFlow(MemoryHierarchyAccess access) {
        return new MemWriteFlow((LastLevelCache) getCache(), access, getCacheAccess());
    }
}
