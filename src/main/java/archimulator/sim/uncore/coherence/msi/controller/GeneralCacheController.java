package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;

import java.io.Serializable;

public abstract class GeneralCacheController<StateT extends Serializable> extends Controller {
    public GeneralCacheController(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config);
    }

    public boolean isLastLevelCache() {
        return this instanceof DirectoryController;
    }

    public abstract EvictableCache<StateT> getCache();
}
