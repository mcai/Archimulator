/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.cache.replacement.costAware.mlp;

import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.costAware.CostBasedLRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.mlp.MLPProfilingHelper;

import java.io.Serializable;

/**
 * Linear MLP-aware least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class LinearMLPAwareLRUPolicy<StateT extends Serializable> extends CostBasedLRUPolicy<StateT> {
    /**
     * Create a linear-MLP aware least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     * @param lambda the lambda value
     */
    public LinearMLPAwareLRUPolicy(EvictableCache<StateT> cache, int lambda) {
        super(cache, lambda);

        cache.getBlockingEventDispatcher().addListener(
                MLPProfilingHelper.L2MissMLPProfiledEvent.class,
                event -> setCost(event.getSet(), event.getWay(), event.getPendingL2Miss().getMlpCost())
        );

        cache.getBlockingEventDispatcher().addListener(
                GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent.class,
                event -> {
                    if (event.getCacheController().getCache().equals(getCache())) {
                        setCost(event.getSet(), event.getWay(), 0);
                    }
                }
        );
    }

    @Override
    protected int getQuantizedCost(double cost) {
        return getCache().getSimulation().getMlpProfilingHelper().getMlpCostQuantizer().quantize((int) cost);
    }
}
