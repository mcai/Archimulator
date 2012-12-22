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
package archimulator.sim.uncore.helperThread.hotspot;

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
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestState;
import net.pickapack.action.Action1;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Hotspot profiling helper.
 *
 * @author Min Cai
 */
public class HotspotProfilingHelper {
    private DirectoryController l2CacheController;

    private Map<String, Map<String, Long>> numCallsPerFunctions;
    private Map<Integer, LoadInstructionEntry> loadsInHotspotFunction;

    private SummaryStatistics statL2CacheHitReuseDistances;
    private SummaryStatistics statL2CacheMissReuseDistances;

    private SummaryStatistics statL2CacheHitHotspotInterThreadReuseDistances;
    private SummaryStatistics statL2CacheMissHotspotReuseDistances;

    private Map<Integer, Stack<Integer>> l2CacheLruStacks;

    /**
     * Create a hotpot profiling helper.
     *
     * @param simulation the simulation object
     */
    public HotspotProfilingHelper(Simulation simulation) {
        this.l2CacheController = simulation.getProcessor().getMemoryHierarchy().getL2CacheController();

        this.numCallsPerFunctions = new TreeMap<String, Map<String, Long>>();
        this.loadsInHotspotFunction = new TreeMap<Integer, LoadInstructionEntry>();

        this.statL2CacheHitReuseDistances = new SummaryStatistics();
        this.statL2CacheMissReuseDistances = new SummaryStatistics();

        this.statL2CacheHitHotspotInterThreadReuseDistances = new SummaryStatistics();
        this.statL2CacheMissHotspotReuseDistances = new SummaryStatistics();

        this.l2CacheLruStacks = new TreeMap<Integer, Stack<Integer>>();

        this.scanLoadInstructionsInHotspotFunction(simulation.getProcessor().getCores().get(0).getThreads().get(0).getContext().getProcess());

        simulation.getBlockingEventDispatcher().addListener(FunctionalCallEvent.class, new Action1<FunctionalCallEvent>() {
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

        simulation.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                DynamicInstruction dynamicInstruction = event.getAccess().getDynamicInstruction();

                if (dynamicInstruction != null && dynamicInstruction.getThread().getContext().getThreadId() == BasicThread.getMainThreadId()) {
                    if (loadsInHotspotFunction.containsKey(dynamicInstruction.getPc())) {
                        LoadInstructionEntry loadInstructionEntry = loadsInHotspotFunction.get(dynamicInstruction.getPc());
                        if (event.getCacheController().getName().equals("c0/dcache")) {
                            loadInstructionEntry.setL1DAccesses(loadInstructionEntry.getL1DAccesses() + 1);
                            if (event.isHitInCache()) {
                                loadInstructionEntry.setL1DHits(loadInstructionEntry.getL1DHits() + 1);
                            }
                        } else if (event.getCacheController() == l2CacheController) {
                            loadInstructionEntry.setL2Accesses(loadInstructionEntry.getL2Accesses() + 1);
                            if (event.isHitInCache()) {
                                loadInstructionEntry.setL2Hits(loadInstructionEntry.getL2Hits() + 1);
                            }
                        }
                    }
                }

                if (event.getCacheController() == l2CacheController) {
                    profileReuseDistance(event.isHitInCache(), event.getWay(), event.getAccess());
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(L2CacheHitHotspotInterThreadReuseDistanceMeterEvent.class, new Action1<L2CacheHitHotspotInterThreadReuseDistanceMeterEvent>() {
            @Override
            public void apply(L2CacheHitHotspotInterThreadReuseDistanceMeterEvent event) {
                statL2CacheHitHotspotInterThreadReuseDistances.addValue(event.getValue().getReuseDistance());
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
     * @param way the way
     * @param access the memory hierarchy access
     */
    private void profileReuseDistance(boolean hitInCache, int way, MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.l2CacheController.getCache().getSet(tag);

        Stack<Integer> lruStack = getLruStackForL2Cache(set);

        int position = lruStack.search(tag);

        if (position != -1) {
            lruStack.remove((Integer) tag);
        }
        lruStack.push(tag);

        if(hitInCache && BasicThread.isMainThread(access.getThread())) {
            HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper = this.l2CacheController.getSimulation().getHelperThreadL2CacheRequestProfilingHelper();

            if(helperThreadL2CacheRequestProfilingHelper != null) {
                HelperThreadL2CacheRequestState helperThreadL2CacheRequestState =
                        helperThreadL2CacheRequestProfilingHelper.getHelperThreadL2CacheRequestStates().get(set).get(way);

                if(BasicThread.isHelperThread(helperThreadL2CacheRequestState.getThreadId())) {
                    this.l2CacheController.getBlockingEventDispatcher().dispatch(new L2CacheHitHotspotInterThreadReuseDistanceMeterEvent(
                            this.l2CacheController,
                            access.getVirtualPc(),
                            access.getPhysicalAddress(),
                            access.getThread().getId(),
                            access.getThread().getContext().getProcess().getFunctionNameFromPc(access.getVirtualPc()),
                            new L2CacheHitHotspotInterThreadReuseDistanceMeterEvent.L2CacheHitHotspotInterThreadReuseDistanceMeterEventValue(helperThreadL2CacheRequestState, position)
                    ));
                }
            }
        }

        if(!hitInCache && BasicThread.isMainThread(access.getThread())) {
            this.l2CacheController.getBlockingEventDispatcher().dispatch(new L2CacheMissHotspotReuseDistanceMeterEvent(
                    this.l2CacheController,
                    access.getVirtualPc(),
                    access.getPhysicalAddress(),
                    access.getThread().getId(),
                    access.getThread().getContext().getProcess().getFunctionNameFromPc(access.getVirtualPc()),
                    new L2CacheMissHotspotReuseDistanceMeterEvent.L2CacheMissHotspotReuseDistanceMeterEventValue(position)
            ));
        }

        if(hitInCache) {
            this.statL2CacheHitReuseDistances.addValue(position);
        }
        else {
            this.statL2CacheMissReuseDistances.addValue(position);
        }
    }

    /**
     * Get the LRU stack for the specified set in the L2 cache.
     *
     * @param set the set index
     * @return the LRU stack for the specified set in the L2 cache
     */
    private Stack<Integer> getLruStackForL2Cache(int set) {
        if(!this.l2CacheLruStacks.containsKey(set)) {
            this.l2CacheLruStacks.put(set, new Stack<Integer>());
        }

        return this.l2CacheLruStacks.get(set);
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
     * Get the summary statistics on the L2 cache hit reuse distances.
     *
     * @return the summary statistics on the L2 cache hit reuse distances
     */
    public SummaryStatistics getStatL2CacheHitReuseDistances() {
        return statL2CacheHitReuseDistances;
    }

    /**
     * Get the summary statistics on the L2 cache miss reuse distances.
     *
     * @return the summary statistics on the L2 cache miss reuse distances
     */
    public SummaryStatistics getStatL2CacheMissReuseDistances() {
        return statL2CacheMissReuseDistances;
    }

    /**
     * Get the summary statistics on the L2 cache hit hotspot inter-thread reuse distances.
     *
     * @return the summary statistics on the L2 cache hit hotspot inter-thread reuse distances
     */
    public SummaryStatistics getStatL2CacheHitHotspotInterThreadReuseDistances() {
        return statL2CacheHitHotspotInterThreadReuseDistances;
    }

    /**
     * Get the summary statistics on the L2 cache miss hotspot reuse distances.
     *
     * @return the summary statistics on the L2 cache miss hotspot reuse distances
     */
    public SummaryStatistics getStatL2CacheMissHotspotReuseDistances() {
        return statL2CacheMissHotspotReuseDistances;
    }
}
