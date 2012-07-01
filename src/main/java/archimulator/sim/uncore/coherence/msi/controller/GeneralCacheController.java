package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import net.pickapack.action.Action1;

import java.io.Serializable;

public abstract class GeneralCacheController<StateT extends Serializable> extends Controller {
    private long numDownwardReadHits;
    private long numDownwardReadMisses;
    private long numDownwardWriteHits;
    private long numDownwardWriteMisses;

    private long numEvictions;

    private double occupancyRatio;

    public GeneralCacheController(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config);

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numDownwardReadHits = 0;
                numDownwardReadMisses = 0;
                numDownwardWriteHits = 0;
                numDownwardWriteMisses = 0;

                numEvictions = 0;

                occupancyRatio = 0;
            }
        });
    }

    public abstract EvictableCache<StateT> getCache();

    public void updateStats(EvictableCache<?> cache, boolean read, boolean hitInCache) {
        if (read) {
            if (hitInCache) {
                numDownwardReadHits++;
            } else {
                numDownwardReadMisses++;
            }
        } else {
            if (hitInCache) {
                numDownwardWriteHits++;
            } else {
                numDownwardWriteMisses++;
            }
        }

        occupancyRatio = cache.getOccupancyRatio();
    }

    public void incNumEvictions() {
        this.numEvictions++;
    }

    public double getHitRatio() {
        return getNumDownwardAccesses() > 0 ? (double) getNumDownwardHits() / (getNumDownwardAccesses()) : 0.0;
    }

    public long getNumDownwardHits() {
        return numDownwardReadHits + numDownwardWriteHits;
    }

    public long getNumDownwardMisses() {
        return numDownwardReadMisses + numDownwardWriteMisses;
    }

    public long getNumDownwardAccesses() {
        return getNumDownwardHits() + getNumDownwardMisses();
    }

    public long getNumDownwardReadHits() {
        return numDownwardReadHits;
    }

    public long getNumDownwardReadMisses() {
        return numDownwardReadMisses;
    }

    public long getNumDownwardWriteHits() {
        return numDownwardWriteHits;
    }

    public long getNumDownwardWriteMisses() {
        return numDownwardWriteMisses;
    }

    public long getNumEvictions() {
        return numEvictions;
    }

    public double getOccupancyRatio() {
        return occupancyRatio;
    }
}
