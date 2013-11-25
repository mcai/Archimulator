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
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;

import java.util.Map;
import java.util.TreeMap;

/**
 * Cache interaction helper.
 *
 * @author Min Cai
 */
public class CacheInteractionHelper implements Reportable {
    private DirectoryController l2CacheController;

    private Map<Integer, Map<Integer, Long>> numL2CacheInterThreadConstructiveInteractions;
    private Map<Integer, Map<Integer, Long>> numL2CacheInterThreadEvictions;

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

        this.numL2CacheInterThreadConstructiveInteractions = new TreeMap<>();
        this.numL2CacheInterThreadEvictions = new TreeMap<>();

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController().equals(CacheInteractionHelper.this.l2CacheController) && event.isHitInCache()) {
                int set = event.getSet();
                int way = event.getWay();

                int broughterThreadId = l2CacheController.getCache().getLine(set, way).getAccess().getThread().getId();
                int requesterThreadId = event.getAccess().getThread().getId();

                if(broughterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(requesterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(broughterThreadId != requesterThreadId) {
                    if(!numL2CacheInterThreadConstructiveInteractions.containsKey(broughterThreadId)) {
                        numL2CacheInterThreadConstructiveInteractions.put(broughterThreadId, new TreeMap<>());
                    }

                    if(!numL2CacheInterThreadConstructiveInteractions.get(broughterThreadId).containsKey(requesterThreadId)) {
                        numL2CacheInterThreadConstructiveInteractions.get(broughterThreadId).put(requesterThreadId, 0L);
                    }

                    numL2CacheInterThreadConstructiveInteractions.get(broughterThreadId).put(requesterThreadId, numL2CacheInterThreadConstructiveInteractions.get(broughterThreadId).get(requesterThreadId) + 1);
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, event -> {
            if (event.getCacheController() == CacheInteractionHelper.this.l2CacheController) {
                int set = event.getSet();
                int way = event.getWay();

                int broughterThreadId = l2CacheController.getCache().getLine(set, way).getAccess().getThread().getId();
                int requesterThreadId = event.getAccess().getThread().getId();

                if(broughterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(requesterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(broughterThreadId != requesterThreadId) {
                    if(!numL2CacheInterThreadEvictions.containsKey(broughterThreadId)) {
                        numL2CacheInterThreadEvictions.put(broughterThreadId, new TreeMap<>());
                    }

                    if(!numL2CacheInterThreadEvictions.get(broughterThreadId).containsKey(requesterThreadId)) {
                        numL2CacheInterThreadEvictions.get(broughterThreadId).put(requesterThreadId, 0L);
                    }

                    numL2CacheInterThreadEvictions.get(broughterThreadId).put(requesterThreadId, numL2CacheInterThreadEvictions.get(broughterThreadId).get(requesterThreadId) + 1);
                }
            }
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "cacheInteractionHelper") {{
            getChildren().add(new ReportNode(this, "numL2CacheInterThreadConstructiveInteractions", getNumL2CacheInterThreadConstructiveInteractions() + ""));
            getChildren().add(new ReportNode(this, "numL2CacheInterThreadEvictions", getNumL2CacheInterThreadEvictions() + ""));
        }});
    }

    /**
     * Get the number of L2 cache inter-thread constructive interactions.
     *
     * @return the number of L2 cache inter-thread constructive interactions
     */
    public Map<Integer, Map<Integer, Long>> getNumL2CacheInterThreadConstructiveInteractions() {
        return numL2CacheInterThreadConstructiveInteractions;
    }

    /**
     * Get the number of L2 cache inter-thread evictions.
     *
     * @return the number of L2 cache inter-thread evictions
     */
    public Map<Integer, Map<Integer, Long>> getNumL2CacheInterThreadEvictions() {
        return numL2CacheInterThreadEvictions;
    }
}
