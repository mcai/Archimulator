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
import archimulator.sim.common.Simulation;
import archimulator.sim.core.BasicThread;
import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.isa.event.FunctionalCallEvent;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.Process;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action1;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Hotspot profiling helper.
 *
 * @author Min Cai
 */
public class HotspotProfilingHelper {
    private Simulation simulation;
    private DirectoryController l2CacheController;

    private Map<String, Map<String, Long>> numCallsPerFunctions;
    private Map<Integer, LoadInstructionEntry> loadsInHotspotFunction;

    private DescriptiveStatistics statL2CacheHitReuseDistances;
    private DescriptiveStatistics statL2CacheMissReuseDistances;

    /**
     * Create a hotpot profiling helper.
     *
     * @param simulation the simulation object
     */
    public HotspotProfilingHelper(Simulation simulation) {
        this.simulation = simulation;
        this.l2CacheController = simulation.getProcessor().getCacheHierarchy().getL2CacheController();

        this.numCallsPerFunctions = new TreeMap<String, Map<String, Long>>();
        this.loadsInHotspotFunction = new TreeMap<Integer, LoadInstructionEntry>();

        this.statL2CacheHitReuseDistances = new DescriptiveStatistics();
        this.statL2CacheMissReuseDistances = new DescriptiveStatistics();

        this.scanLoadInstructionsInHotspotFunction(this.simulation.getProcessor().getCores().get(0).getThreads().get(0).getContext().getProcess());

        this.simulation.getBlockingEventDispatcher().addListener(FunctionalCallEvent.class, new Action1<FunctionalCallEvent>() {
            @Override
            public void apply(FunctionalCallEvent event) {
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
        });

        this.simulation.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                DynamicInstruction dynamicInstruction = event.getAccess().getDynamicInstruction();

                if (dynamicInstruction != null && dynamicInstruction.getThread().getContext().getThreadId() == BasicThread.getMainThreadId()) {
                    if (loadsInHotspotFunction.containsKey(dynamicInstruction.getPc())) {
                        LoadInstructionEntry loadInstructionEntry = loadsInHotspotFunction.get(dynamicInstruction.getPc());
                        if (event.getCacheController().getName().equals("c0/dcache")) {
                            loadInstructionEntry.l1DAccesses++;
                            if (event.isHitInCache()) {
                                loadInstructionEntry.l1DHits++;
                            }
                        } else if (event.getCacheController() == l2CacheController) {
                            loadInstructionEntry.l2Accesses++;
                            if (event.isHitInCache()) {
                                loadInstructionEntry.l2Hits++;
                            }
                        }
                    }
                }

                if (event.getCacheController() == l2CacheController) {
                    profileReuseDistance(event.isHitInCache(), event.getAccess());
                }
            }
        });
    }

    /**
     * Scan the load instructions in the first identified hotspot function in the specified process.
     *
     * @param process the process
     */
    private void scanLoadInstructionsInHotspotFunction(Process process) {
        Function hotspotFunction = process.getHotspotFunction();

        if(hotspotFunction != null) {
            for (BasicBlock basicBlock : hotspotFunction.getBasicBlocks()) {
                for (Instruction instruction : basicBlock.getInstructions()) {
                    if (instruction.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.LOAD) {
                        this.loadsInHotspotFunction.put(instruction.getPc(), new LoadInstructionEntry(instruction));
                    }
                }
            }
        }
    }

    /**
     * Profile the reuse distance for an access.
     *
     * @param hitInCache a value indicating whether the access hits in the cache or not
     * @param access the memory hierarchy access
     */
    private void profileReuseDistance(boolean hitInCache, MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.l2CacheController.getCache().getSet(tag);

        Stack<Integer> lruStack = this.l2CacheController.getCache().get(set).getLruStack();

        int position = lruStack.search(tag);

        if(position == -1) {
            lruStack.push(tag);
            this.statL2CacheMissReuseDistances.addValue(position);
        }
        else {
            lruStack.remove((Integer) tag);
            lruStack.push(tag);

            if(hitInCache) {
                this.statL2CacheHitReuseDistances.addValue(position);
            }
            else {
                this.statL2CacheMissReuseDistances.addValue(position);
            }
        }
    }

    /**
     * Get the number of calls per functions.
     *
     * @return the number of calls per functions
     */
    public Map<String, Map<String, Long>> getNumCallsPerFunctions() {
        return numCallsPerFunctions;
    }

    /**
     * Get the loads in the first identified hotspot function in the first context/process.
     *
     * @return the loads in the first identified hotspot function in the first context/process
     */
    public Map<Integer, LoadInstructionEntry> getLoadsInHotspotFunction() {
        return loadsInHotspotFunction;
    }

    /**
     * Get the descriptive statistics on the L2 cache hit reuse distances.
     *
     * @return the descriptive statistics on the L2 cache hit reuse distances
     */
    public DescriptiveStatistics getStatL2CacheHitReuseDistances() {
        return statL2CacheHitReuseDistances;
    }

    /**
     * Get the descriptive statistics on the L2 cache miss reuse distances.
     *
     * @return the descriptive statistics on the L2 cache miss reuse distances
     */
    public DescriptiveStatistics getStatL2CacheMissReuseDistances() {
        return statL2CacheMissReuseDistances;
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
