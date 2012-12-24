/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.uncore.cache.replacement.helperThread;

import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestQuality;

import java.io.Serializable;

/**
 * Helper thread aware least recently used (LRU) policy.
 *
 * @author Min Cai
 * @param <StateT> the state type of the parent evictable cache
 */
public class HelperThreadAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private boolean useBreakdown;

    /**
     * Create a helper thread aware least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     * @param useBreakdown a value indicating whether using the helper thread L2 cache request breakdown based enhancement or not
     */
    public HelperThreadAwareLRUPolicy(EvictableCache<StateT> cache, boolean useBreakdown) {
        super(cache);
        this.useBreakdown = useBreakdown;
    }

    /**
     * Handle promotion on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @param way the way
     */
    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (isAwarenessEnabled() && access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsMainThread(access) && lineFoundIsHelperThread(set, way)) {
            this.setLRU(set, way);  // HT-MT inter-thread hit, never used again: low locality => Demote to LRU position
            return;
        }

        super.handlePromotionOnHit(access, set, way);
    }

    /**
     * Handle insertion on a cache miss.
     *
     * @param access the memory hierarchy access
     * @param set the set
     * @param way the way
     */
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (isAwarenessEnabled()) {
            if (access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsMainThread(access)) {
                //TODO: setLRU(..) only when the access is from delinquent PC only
                this.setLRU(set, way); // MT miss, prevented from thrashing: low locality => insert in LRU position
                return;
            }
            else if (access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsHelperThread(access)) {
                if(!this.useBreakdown) {
                    setMRU(set, way); // HT miss: expected high HT-MT inter-thread reuse in most cases => insert in MRU position
                    return;
                }

                Predictor<HelperThreadL2CacheRequestQuality> predictor = getCache().getSimulation().getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestQualityPredictor();
                HelperThreadL2CacheRequestQuality predictedQuality = predictor.predict(access.getVirtualPc(), HelperThreadL2CacheRequestQuality.UGLY);

                switch (predictedQuality) {
                    case REDUNDANT_HIT_TO_TRANSIENT_TAG:
                        this.setMRU(set, way);
                        break;
                    case REDUNDANT_HIT_TO_CACHE:
                        this.setMRU(set, way);
                        break;
                    case TIMELY:
                        this.setMRU(set, way);
                        break;
                    case LATE:
                        this.setMRU(set, way);
                        break;
                    case BAD:
                        this.setLRU(set, way);
                        break;
                    case UGLY:
                        this.setMRU(set, way); //TODO: it is evicted too early? setMRU : setLRU
                        break;
                    case INVALID:
                        this.setLRU(set, way); //TODO: set to middle stack position
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

                return;
            }
        }

        super.handleInsertionOnMiss(access, set, way);
    }

    /**
     * Get a value indicating whether the helper thread L2 cache request profiling is enabled or not.
     *
     * @return a value indicating whether the helper thread L2 cache request profiling is enabled or not
     */
    private boolean isAwarenessEnabled() {
        return getCache().getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled();
    }

    /**
     * Get a value indicating whether the requester of the specified memory hierarchy access is the main thread or not.
     *
     * @param access the memory hierarchy access
     * @return a value indicating whether the requester of the specified memory hierarchy access is the main thread or not
     */
    private boolean requesterIsMainThread(MemoryHierarchyAccess access) {
        return BasicThread.isMainThread(access.getThread());
    }

    /**
     * Get a value indicating whether the requester of the specified memory hierarchy access is the helper thread or not.
     *
     * @param access the memory hierarchy access
     * @return a value indicating whether the requester of the specified memory hierarchy access is the helper thread or not
     */
    private boolean requesterIsHelperThread(MemoryHierarchyAccess access) {
        return BasicThread.isHelperThread(access.getThread());
    }

    /**
     * Get a value indicating whether the line found in the specified set index and way is brought by the helper thread or not.
     *
     * @param set the set index
     * @param way the way
     * @return a value indicating whether the line found in the specified set index and way is brought by the helper thread or not
     */
    private boolean lineFoundIsHelperThread(int set, int way) {
        return BasicThread.isHelperThread(getCache().getSimulation().getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestStates().get(set).get(way).getThreadId());
    }

    /**
     * Get a value indicating whether the helper thread L2 cache request breakdown based enhancement is used or not.
     *
     * @return a value indicating whether the helper thread L2 cache request breakdown based enhancement is used or not
     */
    public boolean isUseBreakdown() {
        return useBreakdown;
    }
}
