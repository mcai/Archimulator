/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache.replacement.costAware.mlp;

import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.replacement.costAware.CostSensitiveLRUPolicy;
import archimulator.uncore.coherence.event.GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.uncore.mlp.MLPProfilingHelper;

import java.io.Serializable;

/**
 * MLP sensitive least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class MLPSensitiveLRUPolicy<StateT extends Serializable> extends CostSensitiveLRUPolicy<StateT> {
    /**
     * Create a MLP sensitive least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public MLPSensitiveLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

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
