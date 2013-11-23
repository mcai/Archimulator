package archimulator.sim.uncore.cache.replacement.helperThread;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;

import java.io.Serializable;

import static archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper.getThreadIdentifier;

/**
 * Helper thread aware least recently used (LRU) policy 3.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class HelperThreadAwareLRUPolicy3<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Create a helper thread aware least recently used (LRU) policy 3 for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public HelperThreadAwareLRUPolicy3(EvictableCache<StateT> cache) {
        super(cache);
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
            int way = this.getWayInStackPosition(set, stackPosition);
            CacheLine<StateT> line = this.getCache().getLine(set, way);
            if (line.getAccess() != null && getThreadIdentifier(line.getAccess().getThread()) != getThreadIdentifier(access.getThread())) {
                return new CacheAccess<>(this.getCache(), access, set, way, tag);
            }
        }

        return new CacheAccess<>(this.getCache(), access, set, this.getLRU(set), tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        super.handlePromotionOnHit(access, set, way);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        super.handleInsertionOnMiss(access, set, way);
    }
}
