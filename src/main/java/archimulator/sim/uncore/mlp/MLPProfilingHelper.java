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

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.LinkedHashMap;
import java.util.Map;

//TODO: integration into Simulation and statistics reporting
//TODO: MLP quantizer, MLP-cost predictor and MLP-cost based LLC replacement policy
/**
 * Memory level parallelism (MLP) profiling helper.
 *
 * @author Min Cai
 */
public class MLPProfilingHelper {
    private DirectoryController l2CacheController;

    private Map<Integer, PendingL2Miss> pendingL2Misses;

    private SummaryStatistics statL2CacheMissNumCycles;
    private SummaryStatistics statL2CacheMissMlpCosts;
    private SummaryStatistics statL2CacheMissAverageMlps;

    /**
     * Create an MLP profiling helper.
     *
     * @param l2CacheController the L2 cache controller
     */
    public MLPProfilingHelper(final DirectoryController l2CacheController) {
        this.l2CacheController = l2CacheController;

        this.pendingL2Misses = new LinkedHashMap<Integer, PendingL2Miss>();

        this.statL2CacheMissNumCycles = new SummaryStatistics();
        this.statL2CacheMissMlpCosts = new SummaryStatistics();
        this.statL2CacheMissAverageMlps = new SummaryStatistics();

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(MLPProfilingHelper.this.l2CacheController)) {
                    profileBeginServicingL2Request(event.getAccess());
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, new Action1<LastLevelCacheControllerLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheControllerLineInsertEvent event) {
                if (event.getCacheController().equals(MLPProfilingHelper.this.l2CacheController)) {
                    profileEndServicingL2Request(event.getAccess());
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
     * To be invoked per cycle for updating MLP costs for in-flight L2 cache accesses.
     */
    private void updateL2CacheMlpCostsPerCycle() {
        for (Integer tag : this.pendingL2Misses.keySet()) {
            PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
            pendingL2Miss.setMlpCost(pendingL2Miss.getMlpCost() + 1 / (double) this.pendingL2Misses.size());
            pendingL2Miss.setNumMlpSamples(pendingL2Miss.getNumMlpSamples() + 1);
            pendingL2Miss.setMlpSum(pendingL2Miss.getMlpSum() + this.pendingL2Misses.size());
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache request.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2Request(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = new PendingL2Miss(access, l2CacheController.getCycleAccurateEventQueue().getCurrentCycle());
        this.pendingL2Misses.put(tag, pendingL2Miss);
    }

    /**
     * Profile the end of servicing an L2 cache request.
     *
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2Request(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
        pendingL2Miss.setEndCycle(this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle());

        this.pendingL2Misses.remove(tag);

        this.statL2CacheMissNumCycles.addValue(pendingL2Miss.getNumCycles());
        this.statL2CacheMissMlpCosts.addValue(pendingL2Miss.getMlpCost());
        this.statL2CacheMissAverageMlps.addValue(pendingL2Miss.getAverageMlp());

        //TODO: error, tracking pending accesses precisely from directory controller/fsm/fsmFactory!!!
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
     * Get the summary statistics of the MLP costs for the L2 cache misses.
     *
     * @return the summary statistics of the MLP costs for the L2 cache misses
     */
    public SummaryStatistics getStatL2CacheMissMlpCosts() {
        return statL2CacheMissMlpCosts;
    }

    /**
     * Get the summary statistics of the average MLPs for the L2 cache misses.
     *
     * @return the summary statistics of the average MLPs for the L2 cache misses
     */
    public SummaryStatistics getStatL2CacheMissAverageMlps() {
        return statL2CacheMissAverageMlps;
    }
}
