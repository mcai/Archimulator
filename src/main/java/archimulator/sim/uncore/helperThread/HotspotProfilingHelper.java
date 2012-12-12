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
import archimulator.sim.isa.FunctionalCallEvent;
import archimulator.sim.isa.PseudoCall;
import archimulator.sim.isa.PseudoCallEncounteredEvent;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.Context;
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
    private Map<String, Map<String, Long>> numCallsPerFunctions;
    private Context context;

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
        this.numCallsPerFunctions = new TreeMap<String, Map<String, Long>>();

        Processor processor = this.l2CacheController.getSimulation().getProcessor();

        this.context = processor.getCores().get(0).getThreads().get(0).getContext();

        Function hotspotFunction = this.context.getProcess().getHotspotFunction();
        if (hotspotFunction != null) {
            this.scanLoadInstructionsInHotspotFunction(hotspotFunction);
        }

        l2CacheController.getBlockingEventDispatcher().addListener(FunctionalCallEvent.class, new Action1<FunctionalCallEvent>() {
            @Override
            public void apply(FunctionalCallEvent event) {
//                if (event.getContext() == context)
                {
                    String callerFunctionName = event.getContext().getProcess().getFunctionNameFromPc(event.getFunctionCallContext().getPc());
                    String calleeFunctionName = event.getContext().getProcess().getFunctionNameFromPc(event.getFunctionCallContext().getTargetPc());
                    if(callerFunctionName != null) {
                        if(!numCallsPerFunctions.containsKey(callerFunctionName)) {
                            numCallsPerFunctions.put(callerFunctionName, new TreeMap<String, Long>());
                        }

                        if(!numCallsPerFunctions.get(callerFunctionName).containsKey(calleeFunctionName)) {
                            numCallsPerFunctions.get(callerFunctionName).put(calleeFunctionName, 0L);
                        }

                        numCallsPerFunctions.get(callerFunctionName).put(calleeFunctionName, numCallsPerFunctions.get(callerFunctionName).get(calleeFunctionName) + 1);
                    }
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(PseudoCallEncounteredEvent.class, new Action1<PseudoCallEncounteredEvent>() {
            @Override
            public void apply(PseudoCallEncounteredEvent event) {
                int imm = event.getPseudoCall().getImm();
                if(imm == PseudoCall.PSEUDOCALL_HOTSPOT_FUNCTION_BEGIN || imm == PseudoCall.PSEUDOCALL_HOTSPOT_FUNCTION_END) {
                    System.out.println(event.getContext().getFunctionCallContextStack());
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                DynamicInstruction dynamicInstruction = event.getAccess().getDynamicInstruction();
                if (dynamicInstruction != null && dynamicInstruction.getThread().getContext().getThreadId() == BasicThread.getMainThreadId()) {
                    if (loadsInHotspotFunction.containsKey(dynamicInstruction.getPc())) {
                        LoadInstructionEntry loadInstructionEntry = loadsInHotspotFunction.get(dynamicInstruction.getPc());
                        if (event.getCacheController().getName().equals("c0/dcache")) {
                            loadInstructionEntry.l1DAccesses++;
                            if (event.isHitInCache()) {
                                loadInstructionEntry.l1DHits++;
                            }
                        } else if (event.getCacheController().equals(HotspotProfilingHelper.this.l2CacheController)) {
                            loadInstructionEntry.l2Accesses++;
                            if (event.isHitInCache()) {
                                loadInstructionEntry.l2Hits++;
                            }
                        }
                    }
                }
            }
        });
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
    /**
     * Dump the statistics.
     */
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

        Logger.infof(
                Logger.ROI,
                "{%s} number of dynamic instructions per function",
                this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle(),
                this.l2CacheController.getSimulation().getExperiment().getTitle()
        );

        for (String callerFunctionName : this.numCallsPerFunctions.keySet()) {
            Map<String, Long> numCallsPerCallee = this.numCallsPerFunctions.get(callerFunctionName);

            Logger.infof(Logger.ROI, "%s:", this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle(), callerFunctionName);

            for(String calleeFunctionName : numCallsPerCallee.keySet()) {
                Logger.infof(Logger.ROI, "\t=> %s: %d", this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle(), calleeFunctionName, numCallsPerCallee.get(calleeFunctionName));
            }

            Logger.info(Logger.ROI, "", this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle());
        }
    }

    /**
     * The load instruction entry.
     */
    public class LoadInstructionEntry {
        private Instruction instruction;
        private int l1DAccesses;
        private int l1DHits;
        private int l2Accesses;
        private int l2Hits;

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
         * Get the number of L1D accesses.
         *
         * @return the number of L1D accesses
         */
        public int getL1DAccesses() {
            return l1DAccesses;
        }

        /**
         * Get the number of L1D hits.
         *
         * @return the number of L1D hits
         */
        public int getL1DHits() {
            return l1DHits;
        }

        /**
         * Get the number of L1D misses.
         *
         * @return the number of L1D misses
         */
        public int getL1DMisses() {
            return l1DAccesses - l1DHits;
        }

        /**
         * Get the L1D hit ratio.
         *
         * @return the L1D hit ratio
         */
        public double getL1DHitRatio() {
            return this.l1DAccesses > 0 ? (double) this.l1DHits / this.l1DAccesses : 0.0;
        }

        /**
         * Get the number of L2 accesses.
         *
         * @return the number of L2 accesses
         */
        public int getL2Accesses() {
            return l2Accesses;
        }

        /**
         * Get the number of L2 hits.
         *
         * @return the number of L2 hits
         */
        public int getL2Hits() {
            return l2Hits;
        }

        /**
         * Get the number of L2 misses.
         *
         * @return the number of L2 misses
         */
        public int getL2Misses() {
            return l2Accesses - l2Hits;
        }

        /**
         * Get the L2 hit ratio.
         *
         * @return the L2 hit ratio
         */
        public double getL2HitRatio() {
            return this.l2Accesses > 0 ? (double) this.l2Hits / this.l2Accesses : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "LoadInstructionEntry{instruction=%s, " +
                            "l1d.accesses=%d, l1d.hits=%d, l1d.misses=%d, l1d.hitRatio=%.4f, " +
                            "l2.accesses=%d, l2.hits=%d, l2.misses=%d, l2.hitRatio=%.4f}",
                    instruction,
                    l1DAccesses, l1DHits, getL1DMisses(), getL1DHitRatio(),
                    l2Accesses, l2Hits, getL2Misses(), getL2HitRatio()
            );
        }
    }
}
