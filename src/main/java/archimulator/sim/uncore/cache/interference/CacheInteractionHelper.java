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
package archimulator.sim.uncore.cache.interference;

import archimulator.sim.common.Simulation;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action1;

/**
 * Cache interaction helper.
 *
 * @author Min Cai
 */
public class CacheInteractionHelper {
    private DirectoryController l2CacheController;

    private long numL2CacheInterThreadConstructiveInteractions;
    private long numL2CacheInterThreadEvictions;

    /**
     * Create a cache interaction helper.
     *
     * @param simulation simulation
     */
    public CacheInteractionHelper(Simulation simulation) {
        this(simulation.getProcessor().getMemoryHierarchy().getL2CacheController());
    }

    /**
     * Create a cache interaction helper.
     *
     * @param l2CacheController L2 cache (directory) controller
     */
    public CacheInteractionHelper(final DirectoryController l2CacheController) {
        this.l2CacheController = l2CacheController;

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(CacheInteractionHelper.this.l2CacheController) && event.isHitInCache()) {
                    int set = event.getSet();
                    int way = event.getWay();
                    int broughterThreadId = CacheInteractionHelper.this.l2CacheController.getSimulation().getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestStates().get(set).get(way).getThreadId();
                    int requesterThreadId = event.getAccess().getThread().getId();

                    if(broughterThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    if(requesterThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    if(broughterThreadId != requesterThreadId) {
                        numL2CacheInterThreadConstructiveInteractions++;
                    }
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, new Action1<GeneralCacheControllerLineReplacementEvent>() {
            @Override
            public void apply(GeneralCacheControllerLineReplacementEvent event) {
                if (event.getCacheController() == CacheInteractionHelper.this.l2CacheController) {
                    int set = event.getSet();
                    int way = event.getWay();
                    int broughterThreadId = CacheInteractionHelper.this.l2CacheController.getSimulation().getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestStates().get(set).get(way).getThreadId();
                    int requesterThreadId = event.getAccess().getThread().getId();

                    if(broughterThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    if(requesterThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    if(broughterThreadId != requesterThreadId) {
                        numL2CacheInterThreadEvictions++;
                    }
                }
            }
        });
    }

    /**
     * Get the number of L2 cache inter-thread interactions.
     *
     * @return the number of L2 cache inter-thread interactions
     */
    public long getNumL2CacheInterThreadInteractions() {
        return this.numL2CacheInterThreadConstructiveInteractions + this.numL2CacheInterThreadEvictions;
    }

    /**
     * Get the number of L2 cache inter-thread constructive interactions.
     *
     * @return the number of L2 cache inter-thread constructive interactions
     */
    public long getNumL2CacheInterThreadConstructiveInteractions() {
        return numL2CacheInterThreadConstructiveInteractions;
    }

    /**
     * Get the number of L2 cache inter-thread evictions.
     *
     * @return the number of L2 cache inter-thread evictions
     */
    public long getNumL2CacheInterThreadEvictions() {
        return numL2CacheInterThreadEvictions;
    }

    /**
     * Get the ratio of L2 cache inter-thread constructive interactions.
     *
     * @return the ratio of L2 cache inter-thread constructive interactions
     */
    public double getL2CacheInterThreadConstructiveInteractionRatio() {
        return this.getNumL2CacheInterThreadInteractions() == 0 ? 0 : (double) (this.numL2CacheInterThreadConstructiveInteractions) / this.getNumL2CacheInterThreadInteractions();
    }
}
