package archimulator.sim.uncore.coherence.event;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;

public class LastLevelCacheLineInsertEvent extends CoherentCacheEvent {
    private final MemoryHierarchyAccess access;
    private final int tag;
    private final int set;
    private final int way;
    private int victimTag;

    public LastLevelCacheLineInsertEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, int tag, int set, int way, int victimTag) {
        super(cacheController);
        this.tag = tag;
        this.set = set;
        this.way = way;
        this.access = access;
        this.victimTag = victimTag;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public int getTag() {
        return tag;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public int getVictimTag() {
        return victimTag;
    }

    public boolean isEviction() {
        return victimTag != CacheLine.INVALID_TAG;
    }

    @Override
    public String toString() {
        return String.format("LastLevelCacheLineInsertEvent{access=%s, tag=0x%08x, set=%d, way=%d, cache.name=%s}", access, tag, set, way, getCacheController().getCache().getName());
    }
}
