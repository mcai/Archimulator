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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestQuality;

import java.io.Serializable;

/**
 *
 * @author Min Cai
 * @param <StateT>
 */
public class HelperThreadAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private boolean useBreakdown;

    /**
     *
     * @param cache
     * @param useBreakdown
     */
    public HelperThreadAwareLRUPolicy(EvictableCache<StateT> cache, boolean useBreakdown) {
        super(cache);
        this.useBreakdown = useBreakdown;
    }

    /**
     *
     * @param access
     * @param set
     * @param way
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
     *
     * @param access
     * @param set
     * @param way
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

    private boolean isAwarenessEnabled() {
        return getCache().getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled();
    }

    private boolean requesterIsMainThread(MemoryHierarchyAccess access) {
        return BasicThread.isMainThread(access.getThread());
    }

    private boolean requesterIsHelperThread(MemoryHierarchyAccess access) {
        return BasicThread.isHelperThread(access.getThread());
    }

    private boolean lineFoundIsHelperThread(int set, int way) {
        return BasicThread.isHelperThread(getCache().getSimulation().getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestStates().get(set).get(way).getThreadId());
    }

    /**
     *
     * @return
     */
    public boolean isUseBreakdown() {
        return useBreakdown;
    }
}
