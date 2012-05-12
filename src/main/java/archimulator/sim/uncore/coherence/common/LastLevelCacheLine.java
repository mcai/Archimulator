package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.cache.Cache;

public class LastLevelCacheLine extends LockableCacheLine<LastLevelCacheLineState> {
    public LastLevelCacheLine(Cache<?, ?> cache, int set, int way, LastLevelCacheLineState initialState) {
        super(cache, set, way, initialState);
    }
}
