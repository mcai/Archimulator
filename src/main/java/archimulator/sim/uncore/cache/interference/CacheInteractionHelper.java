/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
    private DirectoryController l2Controller;

    private Map<Integer, Map<Integer, Long>> numL2InterThreadConstructiveInteractions;
    private Map<Integer, Map<Integer, Long>> numL2InterThreadEvictions;

    /**
     * Create a cache interaction helper.
     *
     * @param simulation simulation
     */
    public CacheInteractionHelper(Simulation simulation) {
        this(simulation.getProcessor().getMemoryHierarchy().getL2Controller());
    }

    /**
     * Create a cache interaction helper.
     *
     * @param l2Controller L2 cache (directory) controller
     */
    public CacheInteractionHelper(final DirectoryController l2Controller) {
        this.l2Controller = l2Controller;

        this.numL2InterThreadConstructiveInteractions = new TreeMap<>();
        this.numL2InterThreadEvictions = new TreeMap<>();

        l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController().equals(CacheInteractionHelper.this.l2Controller) && event.isHitInCache()) {
                int set = event.getSet();
                int way = event.getWay();

                int broughterThreadId = l2Controller.getCache().getLine(set, way).getAccess().getThread().getId();
                int requesterThreadId = event.getAccess().getThread().getId();

                if(broughterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(requesterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(broughterThreadId != requesterThreadId) {
                    if(!numL2InterThreadConstructiveInteractions.containsKey(broughterThreadId)) {
                        numL2InterThreadConstructiveInteractions.put(broughterThreadId, new TreeMap<>());
                    }

                    if(!numL2InterThreadConstructiveInteractions.get(broughterThreadId).containsKey(requesterThreadId)) {
                        numL2InterThreadConstructiveInteractions.get(broughterThreadId).put(requesterThreadId, 0L);
                    }

                    numL2InterThreadConstructiveInteractions.get(broughterThreadId).put(requesterThreadId, numL2InterThreadConstructiveInteractions.get(broughterThreadId).get(requesterThreadId) + 1);
                }
            }
        });

        l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, event -> {
            if (event.getCacheController() == CacheInteractionHelper.this.l2Controller) {
                int set = event.getSet();
                int way = event.getWay();

                int broughterThreadId = l2Controller.getCache().getLine(set, way).getAccess().getThread().getId();
                int requesterThreadId = event.getAccess().getThread().getId();

                if(broughterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(requesterThreadId == -1) {
                    throw new IllegalArgumentException();
                }

                if(broughterThreadId != requesterThreadId) {
                    if(!numL2InterThreadEvictions.containsKey(broughterThreadId)) {
                        numL2InterThreadEvictions.put(broughterThreadId, new TreeMap<>());
                    }

                    if(!numL2InterThreadEvictions.get(broughterThreadId).containsKey(requesterThreadId)) {
                        numL2InterThreadEvictions.get(broughterThreadId).put(requesterThreadId, 0L);
                    }

                    numL2InterThreadEvictions.get(broughterThreadId).put(requesterThreadId, numL2InterThreadEvictions.get(broughterThreadId).get(requesterThreadId) + 1);
                }
            }
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "cacheInteractionHelper") {{
            for(int threadFrom : numL2InterThreadConstructiveInteractions.keySet()) {
                Map<Integer, Long> constructiveInteractionsPerThread = numL2InterThreadConstructiveInteractions.get(threadFrom);
                for(int threadTo : constructiveInteractionsPerThread.keySet()) {
                    getChildren().add(
                            new ReportNode(
                                    this,
                                    "numL2InterThreadConstructiveInteractions[" + threadFrom + "," + threadTo + "]",
                                    constructiveInteractionsPerThread.get(threadTo) + ""
                            )
                    );
                }
            }

            for(int threadFrom : numL2InterThreadEvictions.keySet()) {
                Map<Integer, Long> evictionsPerThread = numL2InterThreadEvictions.get(threadFrom);
                for(int threadTo : evictionsPerThread.keySet()) {
                    getChildren().add(
                            new ReportNode(
                                    this,
                                    "numL2InterThreadEvictions[" + threadFrom + "," + threadTo + "]",
                                    evictionsPerThread.get(threadTo) + ""
                            )
                    );
                }
            }
        }});
    }

    /**
     * Get the number of L2 cache inter-thread constructive interactions.
     *
     * @return the number of L2 cache inter-thread constructive interactions
     */
    public Map<Integer, Map<Integer, Long>> getNumL2InterThreadConstructiveInteractions() {
        return numL2InterThreadConstructiveInteractions;
    }

    /**
     * Get the number of L2 cache inter-thread evictions.
     *
     * @return the number of L2 cache inter-thread evictions
     */
    public Map<Integer, Map<Integer, Long>> getNumL2InterThreadEvictions() {
        return numL2InterThreadEvictions;
    }
}
