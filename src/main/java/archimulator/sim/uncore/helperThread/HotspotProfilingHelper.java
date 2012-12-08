/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.helperThread;

import archimulator.sim.analysis.BasicBlock;
import archimulator.sim.analysis.Function;
import archimulator.sim.analysis.Instruction;
import archimulator.sim.common.Logger;
import archimulator.sim.common.Simulation;
import archimulator.sim.core.BasicThread;
import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.core.Processor;
import archimulator.sim.isa.PseudoCall;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.BasicProcess;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action1;

import java.util.Map;
import java.util.TreeMap;

/**
 * Hotspot profiling helper.
 *
 * @author Min Cai
 */
public class HotspotProfilingHelper {
    private DirectoryController l2CacheController;
    private Map<Integer, LoadInstructionEntry> loadsInHotspotFunction;

    /**
     * Create a hotpot profiling helper.
     *
     * @param simulation the simulation object
     */
    public HotspotProfilingHelper(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2CacheController());
    }

    /**
     * Create a hotspot profiling helper.
     *
     * @param l2CacheController the L2 cache controller
     */
    public HotspotProfilingHelper(final DirectoryController l2CacheController) {
        this.l2CacheController = l2CacheController;

        this.loadsInHotspotFunction = new TreeMap<Integer, LoadInstructionEntry>();
        Processor processor = this.l2CacheController.getSimulation().getProcessor();

        BasicProcess process = (BasicProcess) processor.getCores().get(0).getThreads().get(0).getContext().getProcess();

        Function hotspotFunction = this.getHotspotFunction(process);
        if (hotspotFunction != null) {
            this.scanLoadInstructionsInHotspotFunction(hotspotFunction);
        }

        l2CacheController.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HotspotProfilingHelper.this.l2CacheController)) {
                    DynamicInstruction dynamicInstruction = event.getAccess().getDynamicInstruction();

                    if (dynamicInstruction != null && loadsInHotspotFunction.containsKey(dynamicInstruction.getPc()) && dynamicInstruction.getThread().getContext().getThreadId() == BasicThread.getMainThreadId()) {
                        loadsInHotspotFunction.get(dynamicInstruction.getPc()).accesses++;
                        if (event.isHitInCache()) {
                            loadsInHotspotFunction.get(dynamicInstruction.getPc()).hits++;
                        }
                    }
                }
            }
        });
    }

    /**
     * Get the first hotspot function in the process.
     *
     * @param process the process
     * @return the first hotspot function in the process if any exists; otherwise null
     */
    private Function getHotspotFunction(BasicProcess process) {
        for (Function function : process.getElfAnalyzer().getProgram().getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                for (Instruction instruction : basicBlock.getInstructions()) {
                    PseudoCall pseudoCall = StaticInstruction.getPseudoCall(instruction.getStaticInstruction().getMachineInstruction());
                    if (pseudoCall != null && pseudoCall.getImm() == PSEUDOCALL_HOTSPOT_FUNCTION_BEGINNING) {
                        Logger.infof(
                                Logger.ROI,
                                "{%s} a hotspot function is detected: %s",
                                this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle(),
                                this.l2CacheController.getSimulation().getExperiment().getTitle(),
                                function
                        );
                        return function;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Scan the load instructions included in the specified function object.
     *
     * @param function the function object
     */
    private void scanLoadInstructionsInHotspotFunction(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Instruction instruction : basicBlock.getInstructions()) {
                if (instruction.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.LOAD) {
                    this.loadsInHotspotFunction.put(instruction.getPc(), new LoadInstructionEntry(instruction));
                }
            }
        }
    }

    //TODO: to be refactored out.
    public void dumpStats() {
        Logger.infof(
                Logger.ROI,
                "{%s} profiling of load instructions in the first hotspot function",
                this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle(),
                this.l2CacheController.getSimulation().getExperiment().getTitle()
        );

        for (int pc : this.loadsInHotspotFunction.keySet()) {
            Logger.infof(Logger.ROI, "%s", this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle(), this.loadsInHotspotFunction.get(pc));
        }
    }

    /**
     * The load instruction entry.
     */
    public class LoadInstructionEntry {
        private Instruction instruction;
        private int accesses;
        private int hits;

        /**
         * Create a load instruction entry from the specified instruction object.
         *
         * @param instruction the instruction object
         */
        private LoadInstructionEntry(Instruction instruction) {
            this.instruction = instruction;
        }

        /**
         * Get the instruction object.
         *
         * @return the instruction object
         */
        public Instruction getInstruction() {
            return instruction;
        }

        /**
         * Get the number of accesses.
         *
         * @return the number of accesses
         */
        public int getAccesses() {
            return accesses;
        }

        /**
         * Get the number of hits.
         *
         * @return the number of hits
         */
        public int getHits() {
            return hits;
        }

        /**
         * Get the number of misses.
         *
         * @return the number of misses
         */
        public int getMisses() {
            return accesses - hits;
        }

        /**
         * Get the hit ratio.
         *
         * @return the hit ratio
         */
        public double getHitRatio() {
            return this.accesses > 0 ? (double) this.hits / this.accesses : 0.0;
        }

        @Override
        public String toString() {
            return String.format("LoadInstructionEntry{instruction=%s, accesses=%d, hits=%d, misses=%d, hitRatio=%.4f}", instruction, accesses, hits, getMisses(), getHitRatio());
        }
    }

    /**
     * The immediate value of a pseudocall instruction indicating the beginning of a hotspot function.
     */
    public static final int PSEUDOCALL_HOTSPOT_FUNCTION_BEGINNING = 3721;
}
