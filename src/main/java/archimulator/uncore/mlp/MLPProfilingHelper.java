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
package archimulator.uncore.mlp;

import archimulator.common.Simulation;
import archimulator.common.SimulationEvent;
import archimulator.common.SimulationType;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.uncore.coherence.msi.controller.DirectoryController;
import archimulator.util.math.Quantizer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Memory level parallelism (MLP) profiling helper.
 *
 * @author Min Cai
 */
public class MLPProfilingHelper implements Reportable {
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
            super(l2Controller);
            this.set = set;
            this.way = way;
            this.pendingL2Miss = pendingL2Miss;
        }

        /**
         * Get the L2 cache controller.
         *
         * @return the L2 cache controller
         */
        public DirectoryController getL2Controller() {
            return l2Controller;
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

    private DirectoryController l2Controller;

    private Quantizer mlpCostQuantizer;

    private Map<Integer, PendingL2Miss> pendingL2Misses;

    private Map<Integer, Long> numL2MissesPerMlpCostQuantum;

    private Map<Integer, Long> numCyclesPerMlp;

    /**
     * Create an MLP profiling helper.
     *
     * @param simulation the simulation
     */
    public MLPProfilingHelper(Simulation simulation) {
        this.l2Controller = simulation.getProcessor().getMemoryHierarchy().getL2Controller();

        this.mlpCostQuantizer = new Quantizer(7, 40);

        this.pendingL2Misses = new LinkedHashMap<>();

        this.numL2MissesPerMlpCostQuantum = new TreeMap<>();

        this.numCyclesPerMlp = new TreeMap<>();

        this.l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController() == MLPProfilingHelper.this.l2Controller && !event.isHitInCache()) {
                profileBeginServicingL2Miss(event.getAccess());
            }
        });

        this.l2Controller.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, event -> {
            if (event.getCacheController() == MLPProfilingHelper.this.l2Controller) {
                profileEndServicingL2Miss(event.getSet(), event.getWay(), event.getAccess());
            }
        });

        this.l2Controller.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            if (simulation.getType() != SimulationType.FAST_FORWARD) {
                updateL2MlpCostsPerCycle();
            }
        });

        this.l2Controller.getBlockingEventDispatcher().addListener(MLPProfilingHelper.L2MissMLPProfiledEvent.class, event -> {
            double mlpCost = event.getPendingL2Miss().getMlpCost();

            int quantizedMlpCost = getMlpCostQuantizer().quantize((int) mlpCost);
            if (!numL2MissesPerMlpCostQuantum.containsKey(quantizedMlpCost)) {
                numL2MissesPerMlpCostQuantum.put(quantizedMlpCost, 0L);
            }

            numL2MissesPerMlpCostQuantum.put(quantizedMlpCost, numL2MissesPerMlpCostQuantum.get(quantizedMlpCost) + 1);
        });
    }

    /**
     * To be invoked per cycle for updating MLP-costs for in-flight L2 cache accesses.
     */
    private void updateL2MlpCostsPerCycle() {
        int mlp = this.pendingL2Misses.size();

        if(!this.pendingL2Misses.isEmpty()) {
            for (PendingL2Miss pendingL2Miss : this.pendingL2Misses.values()) {
                pendingL2Miss.setMlpCost(pendingL2Miss.getMlpCost() + (double) 1 / mlp);
            }

            if(!this.numCyclesPerMlp.containsKey(mlp)) {
                this.numCyclesPerMlp.put(mlp, 0L);
            }
            this.numCyclesPerMlp.put(mlp, this.numCyclesPerMlp.get(mlp) + 1);
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache request.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2Miss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = new PendingL2Miss(access, this.l2Controller.getCycleAccurateEventQueue().getCurrentCycle());
        this.pendingL2Misses.put(tag, pendingL2Miss);
    }

    /**
     * Profile the end of servicing an L2 cache request.
     *
     * @param set the set
     * @param way the way
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2Miss(int set, int way, MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
        pendingL2Miss.setEndCycle(this.l2Controller.getCycleAccurateEventQueue().getCurrentCycle());

        this.pendingL2Misses.remove(tag);

        this.l2Controller.getBlockingEventDispatcher().dispatch(new L2MissMLPProfiledEvent(set, way, pendingL2Miss));
    }

    @Override
    public void dumpStats(final ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "mlpCostQuantizer") {{
            getChildren().add(new ReportNode(this, "maxValue", mlpCostQuantizer.getMaxValue() + ""));
            getChildren().add(new ReportNode(this, "quantum", mlpCostQuantizer.getQuantum() + ""));
        }});

        for(int i = 0; i < mlpCostQuantizer.getMaxValue(); i++) {
            reportNode.getChildren().add(new ReportNode(reportNode, "numL2MissesPerMlpCostQuantum[" + i + "]", (numL2MissesPerMlpCostQuantum.containsKey(i) ? numL2MissesPerMlpCostQuantum.get(i) : 0) + ""));
        }

        for(int i : numCyclesPerMlp.keySet()) {
            reportNode.getChildren().add(new ReportNode(reportNode, "numCyclesPerMlp[" + i + "]", numCyclesPerMlp.get(i) + ""));
        }
    }

    /**
     * Get the MLP-cost quantizer.
     *
     * @return the MLP-cost quantizer
     */
    public Quantizer getMlpCostQuantizer() {
        return mlpCostQuantizer;
    }
}
