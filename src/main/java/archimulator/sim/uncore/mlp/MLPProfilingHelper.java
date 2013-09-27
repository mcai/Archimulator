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
package archimulator.sim.uncore.mlp;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.math.Quantizer;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Memory level parallelism (MLP) profiling helper.
 *
 * @author Min Cai
 */
public class MLPProfilingHelper {
    /**
     * L2 miss MLP profiled event.
     */
    public class L2MissMLPProfiledEvent extends SimulationEvent {
        private int set;
        private int way;
        private PendingL2Miss pendingL2Miss;

        /**
         * Create an L2 miss MLP profiled event.
         *
         * @param set the set
         * @param way the way
         * @param pendingL2Miss the pending L2 miss
         */
        public L2MissMLPProfiledEvent(int set, int way, PendingL2Miss pendingL2Miss) {
            super(l2CacheController);
            this.set = set;
            this.way = way;
            this.pendingL2Miss = pendingL2Miss;
        }

        /**
         * Get the L2 cache controller.
         *
         * @return the L2 cache controller
         */
        public DirectoryController getL2CacheController() {
            return l2CacheController;
        }

        /**
         * Get the set.
         *
         * @return the set
         */
        public int getSet() {
            return set;
        }

        /**
         * Get the way.
         *
         * @return the way
         */
        public int getWay() {
            return way;
        }

        /**
         * Get the pending L2 miss.
         *
         * @return the pending L2 miss
         */
        public PendingL2Miss getPendingL2Miss() {
            return pendingL2Miss;
        }
    }

    private DirectoryController l2CacheController;

    private Quantizer mlpCostQuantizer;

    private Map<Integer, PendingL2Miss> pendingL2Misses;

    private SummaryStatistics statL2CacheMissNumCycles;
    private SummaryStatistics statL2CacheMissMlpCosts;

    /**
     * Create an MLP profiling helper.
     *
     * @param simulation the simulation
     */
    public MLPProfilingHelper(Simulation simulation) {
        this.l2CacheController = simulation.getProcessor().getMemoryHierarchy().getL2CacheController();

        this.mlpCostQuantizer = new Quantizer(7, 40);

        this.pendingL2Misses = new LinkedHashMap<Integer, PendingL2Miss>();

        this.statL2CacheMissNumCycles = new SummaryStatistics();
        this.statL2CacheMissMlpCosts = new SummaryStatistics();

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(MLPProfilingHelper.this.l2CacheController) && !event.isHitInCache()) {
                    profileBeginServicingL2CacheMiss(event.getAccess());
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, new Action1<LastLevelCacheControllerLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheControllerLineInsertEvent event) {
                if (event.getCacheController().equals(MLPProfilingHelper.this.l2CacheController)) {
                    profileEndServicingL2CacheMiss(event.getSet(), event.getWay(), event.getAccess());
                }
            }
        });

        l2CacheController.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                updateL2CacheMlpCostsPerCycle();
            }
        });
    }

    /**
     * To be invoked per cycle for updating MLP-costs for in-flight L2 cache accesses.
     */
    private void updateL2CacheMlpCostsPerCycle() {
        for (PendingL2Miss pendingL2Miss : this.pendingL2Misses.values()) {
            pendingL2Miss.setMlpCost(pendingL2Miss.getMlpCost() + (double) 1 / this.pendingL2Misses.size());
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache request.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2CacheMiss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = new PendingL2Miss(access, this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle());
        this.pendingL2Misses.put(tag, pendingL2Miss);
    }

    /**
     * Profile the end of servicing an L2 cache request.
     *
     * @param set the set
     * @param way the way
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2CacheMiss(int set, int way, MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
        pendingL2Miss.setEndCycle(this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle());

        this.pendingL2Misses.remove(tag);

        this.statL2CacheMissNumCycles.addValue(pendingL2Miss.getNumCycles());
        this.statL2CacheMissMlpCosts.addValue(pendingL2Miss.getMlpCost());

        this.l2CacheController.getBlockingEventDispatcher().dispatch(new L2MissMLPProfiledEvent(set, way, pendingL2Miss));
    }

    /**
     * Get the MLP-cost quantizer.
     *
     * @return the MLP-cost quantizer
     */
    public Quantizer getMlpCostQuantizer() {
        return mlpCostQuantizer;
    }

    /**
     * Get the summary statistics of the number of cycles for the L2 cache misses.
     *
     * @return the summary statistics of the number of cycles for the L2 cache misses
     */
    public SummaryStatistics getStatL2CacheMissNumCycles() {
        return statL2CacheMissNumCycles;
    }

    /**
     * Get the summary statistics of the MLP-costs for the L2 cache misses.
     *
     * @return the summary statistics of the MLP-costs for the L2 cache misses
     */
    public SummaryStatistics getStatL2CacheMissMlpCosts() {
        return statL2CacheMissMlpCosts;
    }
}
