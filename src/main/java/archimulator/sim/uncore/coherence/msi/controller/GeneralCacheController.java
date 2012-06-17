package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import net.pickapack.action.Action1;

import java.io.Serializable;
import java.util.Map;

public abstract class GeneralCacheController<StateT extends Serializable> extends Controller {
    private long numDownwardReadHits;
    private long numDownwardReadMisses;
    private long numDownwardWriteHits;
    private long numDownwardWriteMisses;

    private long numEvictions;

    private double warmupRatio;

    public GeneralCacheController(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config);

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numDownwardReadHits = 0;
                numDownwardReadMisses = 0;
                numDownwardWriteHits = 0;
                numDownwardWriteMisses = 0;

                numEvictions = 0;

                warmupRatio = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            @Override
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        if(getNumDownwardAccesses() > 0) {
            stats.put(this.getName() + ".hitRatio", String.valueOf(getHitRatio()));
            stats.put(this.getName() + ".numDownwardAccesses", String.valueOf(getNumDownwardAccesses()));
            stats.put(this.getName() + ".numDownwardHits", String.valueOf(getNumDownwardHits()));
            stats.put(this.getName() + ".numDownwardMisses", String.valueOf(getNumDownwardMisses()));

            stats.put(this.getName() + ".numDownwardReadHits", String.valueOf(numDownwardReadHits));
            stats.put(this.getName() + ".numDownwardReadMisses", String.valueOf(numDownwardReadMisses));
            stats.put(this.getName() + ".numDownwardWriteHits", String.valueOf(numDownwardWriteHits));
            stats.put(this.getName() + ".numDownwardWriteMisses", String.valueOf(numDownwardWriteMisses));

            stats.put(this.getName() + ".numEvictions", String.valueOf(numEvictions));

            stats.put(this.getName() + ".warmupRatio", String.valueOf(warmupRatio));
        }
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

        warmupRatio = cache.getWarmupRatio();
    }

    public void incNumEvictions() {
        this.numEvictions++;
    }

    private double getHitRatio() {
        return getNumDownwardAccesses() > 0 ? (double) getNumDownwardHits() / (getNumDownwardAccesses()) : 0.0;
    }

    private long getNumDownwardHits() {
        return numDownwardReadHits + numDownwardWriteHits;
    }

    private long getNumDownwardMisses() {
        return numDownwardReadMisses + numDownwardWriteMisses;
    }

    private long getNumDownwardAccesses() {
        return getNumDownwardHits() + getNumDownwardMisses();
    }
}
