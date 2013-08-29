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
import archimulator.sim.uncore.dram.BasicMemoryController;
import archimulator.sim.uncore.dram.MemoryController;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Function1;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DRAM Bank level parallelism (BLP) profiling helper.
 *
 * @author Min Cai
 */
public class BLPProfilingHelper {
    /**
     * DRAM Bank access BLP profiled event
     */
    public class BankAccessBLPProfiledEvent extends SimulationEvent {
        private PendingDramBankAccess pendingDramBankAccess;

        /**
         * Create a DRAM bank access BLP profiled event.
         *
         * @param pendingDramBankAccess the pending bank access
         */
        public BankAccessBLPProfiledEvent(PendingDramBankAccess pendingDramBankAccess) {
            super(memoryController);
            this.pendingDramBankAccess = pendingDramBankAccess;
        }

        /**
         * Get the pending DRAM bank access.
         *
         * @return the pending DRAM bank access
         */
        public PendingDramBankAccess getPendingDramBankAccess() {
            return pendingDramBankAccess;
        }

        /**
         * Get the memory controller.
         *
         * @return the memory controller
         */
        public MemoryController getMemoryController() {
            return memoryController;
        }
    }

    private MemoryController memoryController;

    private Function1<Integer, Integer> blpCostQuantizer;

    private Map<Integer, PendingDramBankAccess> pendingDRAMBankAccesses;

    private SummaryStatistics statDramBankAccessNumCycles;
    private SummaryStatistics statDramBankAccessBlpCosts;

    /**
     * Create a BLP profiling helper.
     *
     * @param simulation the simulation
     */
    public BLPProfilingHelper(Simulation simulation) {
        this.memoryController = simulation.getProcessor().getMemoryHierarchy().getMemoryController();

        this.blpCostQuantizer = new Function1<Integer, Integer>() {
            @Override
            public Integer apply(Integer rawValue) {
                if (rawValue < 0) {
                    throw new IllegalArgumentException();
                }

                if (rawValue <= 42) {
                    return 0;
                } else if (rawValue <= 85) {
                    return 1;
                } else if (rawValue <= 128) {
                    return 2;
                } else if (rawValue <= 170) {
                    return 3;
                } else if (rawValue <= 213) {
                    return 4;
                } else if (rawValue <= 246) {
                    return 5;
                } else if (rawValue <= 300) {
                    return 6;
                } else {
                    return 7;
                }
            }
        };

        this.pendingDRAMBankAccesses = new LinkedHashMap<Integer, PendingDramBankAccess>();

        this.statDramBankAccessNumCycles = new SummaryStatistics();
        this.statDramBankAccessBlpCosts = new SummaryStatistics();

        memoryController.getBlockingEventDispatcher().addListener(BasicMemoryController.BeginAccessEvent.class, new Action1<BasicMemoryController.BeginAccessEvent>() {
            @Override
            public void apply(BasicMemoryController.BeginAccessEvent event) {
                profileBeginServicingL2CacheMiss(event);
            }
        });

        memoryController.getBlockingEventDispatcher().addListener(BasicMemoryController.EndAccessEvent.class, new Action1<BasicMemoryController.EndAccessEvent>() {
            @Override
            public void apply(BasicMemoryController.EndAccessEvent event) {
                profileEndServicingL2CacheMiss(event);
            }
        });

        memoryController.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                updateBlpCostsPerCycle();
            }
        });
    }


    /**
     * To be invoked per cycle for updating BLP-costs for in-flight DRAM bank accesses.
     */
    private void updateBlpCostsPerCycle() {
        for (PendingDramBankAccess pendingDramBankAccess : this.pendingDRAMBankAccesses.values()) {
            pendingDramBankAccess.setBlpCost(pendingDramBankAccess.getBlpCost() + (double) 1 / this.pendingDRAMBankAccesses.size());
        }
    }

    /**
     * Profile the beginning of servicing a DRAM bank access.
     *
     * @param event the begin access event
     */
    private void profileBeginServicingL2CacheMiss(BasicMemoryController.BeginAccessEvent event) {
        PendingDramBankAccess pendingDramBankAccess = new PendingDramBankAccess(event.getAddress(), event.getBank(), memoryController.getCycleAccurateEventQueue().getCurrentCycle());

        if(this.pendingDRAMBankAccesses.containsKey(event.getAddress())) {
            throw new IllegalArgumentException(event + "");
        }

        this.pendingDRAMBankAccesses.put(event.getAddress(), pendingDramBankAccess);
    }

    /**
     * Profile the end of servicing a DRAM bank access.
     *
     * @param event the end access event
     */
    private void profileEndServicingL2CacheMiss(BasicMemoryController.EndAccessEvent event) {
        PendingDramBankAccess pendingDramBankAccess = this.pendingDRAMBankAccesses.get(event.getAddress());
        pendingDramBankAccess.setEndCycle(this.memoryController.getCycleAccurateEventQueue().getCurrentCycle());

        this.pendingDRAMBankAccesses.remove(event.getAddress());

        this.statDramBankAccessNumCycles.addValue(pendingDramBankAccess.getNumCycles());
        this.statDramBankAccessBlpCosts.addValue(pendingDramBankAccess.getBlpCost());

        this.memoryController.getBlockingEventDispatcher().dispatch(new BankAccessBLPProfiledEvent(pendingDramBankAccess));
    }

    /**
     * Get the BLP-cost quantizer.
     *
     * @return the BLP-cost quantizer
     */
    public Function1<Integer, Integer> getBlpCostQuantizer() {
        return blpCostQuantizer;
    }

    /**
     * Get the summary statistics of the number of cycles for the DRAM bank accesses.
     *
     * @return the summary statistics of the number of cycles for the DRAM bank accesses
     */
    public SummaryStatistics getStatDramBankAccessNumCycles() {
        return statDramBankAccessNumCycles;
    }

    /**
     * Get the summary statistics of the MLP-costs for the DRAM bank accesses.
     *
     * @return the summary statistics of the MLP-costs for the DRAM bank accesses
     */
    public SummaryStatistics getStatDramBankAccessBlpCosts() {
        return statDramBankAccessBlpCosts;
    }
}
