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
package archimulator.sim.uncore.cache.replacement.mlpAware;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.mlp.MLPProfilingHelper;
import net.pickapack.action.Action1;
import net.pickapack.math.Quantizer;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Linear MLP-aware least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class LinearMLPAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private Cache<Boolean> mirrorCache;

    private Map<Integer, Long> numL2MissesPerMlpCostQuantum;

    /**
     * Create a linear-MLP aware least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public LinearMLPAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<Boolean>(cache, cache.getName() + ".reuseDistancePredictionPolicy.mirrorCache", cache.getGeometry(), new ValueProviderFactory<Boolean, ValueProvider<Boolean>>() {
            @Override
            public ValueProvider<Boolean> createValueProvider(Object... args) {
                return new BooleanValueProvider();
            }
        });

        this.numL2MissesPerMlpCostQuantum = new TreeMap<Integer, Long>();

        cache.getBlockingEventDispatcher().addListener(MLPProfilingHelper.L2MissMLPProfiledEvent.class, new Action1<MLPProfilingHelper.L2MissMLPProfiledEvent>() {
            @Override
            public void apply(MLPProfilingHelper.L2MissMLPProfiledEvent event) {
                CacheLine<Boolean> mirrorLine = mirrorCache.getLine(event.getSet(), event.getWay());
                BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
                double mlpCost = event.getPendingL2Miss().getMlpCost();
                stateProvider.mlpCost = mlpCost;

                int quantizedMlpCost = getCache().getSimulation().getMlpProfilingHelper().getMlpCostQuantizer().quantize((int) mlpCost);
                if(!numL2MissesPerMlpCostQuantum.containsKey(quantizedMlpCost)) {
                    numL2MissesPerMlpCostQuantum.put(quantizedMlpCost, 0L);
                }

                numL2MissesPerMlpCostQuantum.put(quantizedMlpCost, numL2MissesPerMlpCostQuantum.get(quantizedMlpCost) + 1);
            }
        });
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param tag    the tag
     * @return the newly created cache access object
     */
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int victimLinearSum = Integer.MAX_VALUE;
        int victimWay = 0;

        for (CacheLine<Boolean> mirrorLine : this.mirrorCache.getLines(set)) {
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            int way = mirrorLine.getWay();

            int recency = getCache().getAssociativity() - getStackPosition(set, way);
            int quantizedMlpCost = getCache().getSimulation().getMlpProfilingHelper().getMlpCostQuantizer().quantize((int) stateProvider.mlpCost);

            int linearSum = recency + lambda * quantizedMlpCost;

            if(linearSum < victimLinearSum) {
                victimLinearSum = linearSum;
                victimWay = way;
            }
        }

        return new CacheAccess<StateT>(this.getCache(), access, set, victimWay, tag);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        Quantizer mlpCostQuantizer = getCache().getSimulation().getMlpProfilingHelper().getMlpCostQuantizer();
        reportNode.getChildren().add(new ReportNode(reportNode, "mlpCostQuantizer.maxValue", mlpCostQuantizer.getMaxValue() + ""));
        reportNode.getChildren().add(new ReportNode(reportNode, "mlpCostQuantizer.quantum", mlpCostQuantizer.getQuantum() + ""));
        for(int i = 0; i < mlpCostQuantizer.getMaxValue(); i++) {
            reportNode.getChildren().add(new ReportNode(reportNode, "numL2MissesPerMlpCostQuantum[" + i + "]", numL2MissesPerMlpCostQuantum.get(i) + ""));
        }
    }

    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private double mlpCost;

        /**
         * Create a boolean value provider.
         */
        private BooleanValueProvider() {
            this.state = true;
        }

        /**
         * Get the state.
         *
         * @return the state
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial state.
         *
         * @return the initial state
         */
        @Override
        public Boolean getInitialValue() {
            return true;
        }
    }

    private static final int lambda = 4;
}
