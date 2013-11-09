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
package archimulator.sim.uncore.helperThread.hotspot;

import archimulator.sim.analysis.BasicBlock;
import archimulator.sim.analysis.Function;
import archimulator.sim.analysis.Instruction;
import archimulator.sim.common.Simulation;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.isa.event.FunctionalCallEvent;
import archimulator.sim.os.Process;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.stackDistanceProfile.StackDistanceProfilingHelper;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestState;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Map;
import java.util.TreeMap;

/**
 * Hotspot profiling helper.
 *
 * @author Min Cai
 */
public class HotspotProfilingHelper implements Reportable {
    private DirectoryController l2CacheController;

    private Map<String, Map<String, Long>> numCallsPerFunctions;
    private Map<Integer, LoadInstructionEntry> loadsInHotspotFunction;

    private SummaryStatistics statL2CacheHitStackDistances;
    private SummaryStatistics statL2CacheMissStackDistances;

    private SummaryStatistics statL2CacheHitHotspotInterThreadStackDistances;
    private SummaryStatistics statL2CacheMissHotspotStackDistances;

    /**
     * Create a hotpot profiling helper.
     *
     * @param simulation the simulation object
     */
    public HotspotProfilingHelper(Simulation simulation) {
        this.l2CacheController = simulation.getProcessor().getMemoryHierarchy().getL2CacheController();

        this.numCallsPerFunctions = new TreeMap<>();
        this.loadsInHotspotFunction = new TreeMap<>();

        this.statL2CacheHitStackDistances = new SummaryStatistics();
        this.statL2CacheMissStackDistances = new SummaryStatistics();

        this.statL2CacheHitHotspotInterThreadStackDistances = new SummaryStatistics();
        this.statL2CacheMissHotspotStackDistances = new SummaryStatistics();

        this.scanLoadInstructionsInHotspotFunction(simulation.getProcessor().getCores().get(0).getThreads().get(0).getContext().getProcess());

        simulation.getBlockingEventDispatcher().addListener(FunctionalCallEvent.class, event -> {
            String callerFunctionName = event.getContext().getProcess().getFunctionNameFromPc(event.getFunctionCallContext().getPc());
            String calleeFunctionName = event.getContext().getProcess().getFunctionNameFromPc(event.getFunctionCallContext().getTargetPc());
            if (callerFunctionName != null) {
                if (!numCallsPerFunctions.containsKey(callerFunctionName)) {
                    numCallsPerFunctions.put(callerFunctionName, new TreeMap<>());
                }

                if (!numCallsPerFunctions.get(callerFunctionName).containsKey(calleeFunctionName)) {
                    numCallsPerFunctions.get(callerFunctionName).put(calleeFunctionName, 0L);
                }

                numCallsPerFunctions.get(callerFunctionName).put(calleeFunctionName, numCallsPerFunctions.get(callerFunctionName).get(calleeFunctionName) + 1);
            }
        });

        simulation.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            DynamicInstruction dynamicInstruction = event.getAccess().getDynamicInstruction();

            if (dynamicInstruction != null && dynamicInstruction.getThread().getContext().getThreadId() == HelperThreadingHelper.getMainThreadId()) {
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
        });

        simulation.getBlockingEventDispatcher().addListener(StackDistanceProfilingHelper.StackDistanceProfiledEvent.class, event -> {
            if(event.getCacheController() == l2CacheController) {
                MemoryHierarchyAccess access = event.getAccess();

                if (event.isHitInCache() && HelperThreadingHelper.isMainThread(access.getThread())) {
                    HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper = l2CacheController.getSimulation().getHelperThreadL2CacheRequestProfilingHelper();

                    if (helperThreadL2CacheRequestProfilingHelper != null) {
                        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState =
                                helperThreadL2CacheRequestProfilingHelper.getHelperThreadL2CacheRequestStates().get(event.getSet()).get(event.getWay());

                        if (HelperThreadingHelper.isHelperThread(helperThreadL2CacheRequestState.getThreadId())) {
                            l2CacheController.getBlockingEventDispatcher().dispatch(new L2CacheHitHotspotInterThreadStackDistanceMeterEvent(
                                    l2CacheController,
                                    access.getVirtualPc(),
                                    access.getPhysicalAddress(),
                                    access.getThread().getId(),
                                    access.getThread().getContext().getProcess().getFunctionNameFromPc(access.getVirtualPc()),
                                    new L2CacheHitHotspotInterThreadStackDistanceMeterEvent.L2CacheHitHotspotInterThreadStackDistanceMeterEventValue(helperThreadL2CacheRequestState, event.getStackDistance())
                            ));
                        }
                    }
                }

                if (!event.isHitInCache() && HelperThreadingHelper.isMainThread(access.getThread())) {
                    l2CacheController.getBlockingEventDispatcher().dispatch(new L2CacheMissHotspotStackDistanceMeterEvent(
                            l2CacheController,
                            access.getVirtualPc(),
                            access.getPhysicalAddress(),
                            access.getThread().getId(),
                            access.getThread().getContext().getProcess().getFunctionNameFromPc(access.getVirtualPc()),
                            new L2CacheMissHotspotStackDistanceMeterEvent.L2CacheMissHotspotStackDistanceMeterEventValue(event.getStackDistance())
                    ));
                }

                if (event.isHitInCache()) {
                    statL2CacheHitStackDistances.addValue(event.getStackDistance());
                } else {
                    statL2CacheMissStackDistances.addValue(event.getStackDistance());
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(
                L2CacheHitHotspotInterThreadStackDistanceMeterEvent.class,
                event -> statL2CacheHitHotspotInterThreadStackDistances.addValue(event.getValue().getStackDistance())
        );

        simulation.getBlockingEventDispatcher().addListener(
                L2CacheMissHotspotStackDistanceMeterEvent.class,
                event -> statL2CacheMissHotspotStackDistances.addValue(event.getValue().getStackDistance())
        );
    }

    /**
     * Scan the load instructions in the first identified hotspot function in the specified process.
     *
     * @param process the process
     */
    private void scanLoadInstructionsInHotspotFunction(Process process) {
        Function hotspotFunction = process.getHotspotFunction();

        if (hotspotFunction != null) {
            for (BasicBlock basicBlock : hotspotFunction.getBasicBlocks()) {
                for (Instruction instruction : basicBlock.getInstructions()) {
                    if (instruction.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.LOAD) {
                        this.loadsInHotspotFunction.put(instruction.getPc(), new LoadInstructionEntry(instruction));
                    }
                }
            }
        }
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "hotspot") {{
            getChildren().add(new ReportNode(this, "numCallsPerFunctions", getNumCallsPerFunctions() + "")); //TODO
            getChildren().add(new ReportNode(this, "loadsInHotspotFunction", getLoadsInHotspotFunction() + "")); //TODO

            getChildren().add(new ReportNode(this, "statL2CacheHitStackDistances", getStatL2CacheHitStackDistances() + "")); //TODO
            getChildren().add(new ReportNode(this, "statL2CacheMissStackDistances", getStatL2CacheMissStackDistances() + "")); //TODO
            getChildren().add(new ReportNode(this, "statL2CacheHitHotspotInterThreadStackDistances", getStatL2CacheHitHotspotInterThreadStackDistances() + "")); //TODO
            getChildren().add(new ReportNode(this, "statL2CacheMissHotspotStackDistances", getStatL2CacheMissStackDistances() + "")); //TODO
        }});
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
     * Get the summary statistics on the L2 cache hit stack distances.
     *
     * @return the summary statistics on the L2 cache hit stack distances
     */
    public SummaryStatistics getStatL2CacheHitStackDistances() {
        return statL2CacheHitStackDistances;
    }

    /**
     * Get the summary statistics on the L2 cache miss stack distances.
     *
     * @return the summary statistics on the L2 cache miss stack distances
     */
    public SummaryStatistics getStatL2CacheMissStackDistances() {
        return statL2CacheMissStackDistances;
    }

    /**
     * Get the summary statistics on the L2 cache hit hotspot inter-thread stack distances.
     *
     * @return the summary statistics on the L2 cache hit hotspot inter-thread stack distances
     */
    public SummaryStatistics getStatL2CacheHitHotspotInterThreadStackDistances() {
        return statL2CacheHitHotspotInterThreadStackDistances;
    }

    /**
     * Get the summary statistics on the L2 cache miss hotspot stack distances.
     *
     * @return the summary statistics on the L2 cache miss hotspot stack distances
     */
    public SummaryStatistics getStatL2CacheMissHotspotStackDistances() {
        return statL2CacheMissHotspotStackDistances;
    }
}
