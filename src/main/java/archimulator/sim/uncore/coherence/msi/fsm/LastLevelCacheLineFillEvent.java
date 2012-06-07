package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.event.CoherentCacheEvent;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;

public class LastLevelCacheLineFillEvent extends CoherentCacheEvent {
    private MemoryHierarchyAccess access;
    private CacheAccess<?> cacheAccess;

    public LastLevelCacheLineFillEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, CacheAccess<DirectoryControllerState> cacheAccess) {
        super(cacheController);

        this.cacheAccess = cacheAccess;
        this.access = access;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public CacheAccess<?> getCacheAccess() {
        return cacheAccess;
    }

    @Override
    public String toString() {
        return String.format("LastLevelCacheLineFillEvent{access=%s, cache.name=%s}", access, getCacheController().getCache().getName());
    }
}
