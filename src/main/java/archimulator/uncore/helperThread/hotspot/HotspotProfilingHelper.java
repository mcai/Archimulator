/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.helperThread.hotspot;

import archimulator.analysis.BasicBlock;
import archimulator.analysis.Function;
import archimulator.common.Simulation;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.core.DynamicInstruction;
import archimulator.isa.StaticInstructionType;
import archimulator.isa.event.FunctionCallEvent;
import archimulator.os.Process;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.stackDistanceProfile.StackDistanceProfilingHelper;
import archimulator.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.uncore.coherence.msi.controller.DirectoryController;
import archimulator.uncore.helperThread.HelperThreadL2RequestProfilingHelper;
import archimulator.uncore.helperThread.HelperThreadL2RequestState;
import archimulator.uncore.helperThread.HelperThreadingHelper;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Hotspot profiling helper.
 *
 * @author Min Cai
 */
public class HotspotProfilingHelper implements Reportable {
    private DirectoryController l2Controller;

    private Map<String, Map<String, Long>> numCallsPerFunctions;
    private Map<Integer, LoadInstructionEntry> loadsInHotspotFunction;

    private SummaryStatistics statL2HitStackDistances;
    private SummaryStatistics statL2MissStackDistances;

    private SummaryStatistics statL2HitHotspotInterThreadStackDistances;
    private SummaryStatistics statL2MissHotspotStackDistances;

    /**
     * Create a hotpot profiling helper.
     *
     * @param simulation the simulation object
     */
    public HotspotProfilingHelper(Simulation simulation) {
        this.l2Controller = simulation.getProcessor().getMemoryHierarchy().getL2Controller();

        this.numCallsPerFunctions = new TreeMap<>();
        this.loadsInHotspotFunction = new TreeMap<>();

        this.statL2HitStackDistances = new SummaryStatistics();
        this.statL2MissStackDistances = new SummaryStatistics();

        this.statL2HitHotspotInterThreadStackDistances = new SummaryStatistics();
        this.statL2MissHotspotStackDistances = new SummaryStatistics();

        if (simulation.getProcessor().getCores().get(0).getThreads().get(0).getContext() == null) {
            return;
        }

        this.scanLoadInstructionsInHotspotFunctions(simulation.getProcessor().getCores().get(0).getThreads().get(0).getContext().getProcess());

        simulation.getBlockingEventDispatcher().addListener(FunctionCallEvent.class, event -> {
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
                    } else if (event.getCacheController() == l2Controller) {
                        loadInstructionEntry.setL2Accesses(loadInstructionEntry.getL2Accesses() + 1);
                        if (event.isHitInCache()) {
                            loadInstructionEntry.setL2Hits(loadInstructionEntry.getL2Hits() + 1);
                        }
                    }
                }
            }
        });

        //TODO: to be removed out of HotspotProfilingHelper!!!
        simulation.getBlockingEventDispatcher().addListener(StackDistanceProfilingHelper.StackDistanceProfiledEvent.class, event -> {
            if (event.getCacheController() == l2Controller) {
                MemoryHierarchyAccess access = event.getAccess();

                if (event.isHitInCache() && HelperThreadingHelper.isMainThread(access.getThread())) {
                    HelperThreadL2RequestProfilingHelper helperThreadL2RequestProfilingHelper = l2Controller.getSimulation().getHelperThreadL2RequestProfilingHelper();

                    if (helperThreadL2RequestProfilingHelper != null) {
                        HelperThreadL2RequestState helperThreadL2RequestState =
                                helperThreadL2RequestProfilingHelper.getHelperThreadL2RequestStates().get(event.getSet()).get(event.getWay());

                        if (HelperThreadingHelper.isHelperThread(helperThreadL2RequestState.getThreadId())) {
                            l2Controller.getBlockingEventDispatcher().dispatch(new L2HitHotspotInterThreadStackDistanceMeterEvent(
                                    l2Controller,
                                    access.getVirtualPc(),
                                    access.getPhysicalAddress(),
                                    access.getThread().getId(),
                                    access.getThread().getContext().getProcess().getFunctionNameFromPc(access.getVirtualPc()),
                                    new L2HitHotspotInterThreadStackDistanceMeterEvent.L2HitHotspotInterThreadStackDistanceMeterEventValue(helperThreadL2RequestState, event.getStackDistance())
                            ));
                        }
                    }
                }

                if (!event.isHitInCache() && HelperThreadingHelper.isMainThread(access.getThread())) {
                    l2Controller.getBlockingEventDispatcher().dispatch(new L2MissHotspotStackDistanceMeterEvent(
                            l2Controller,
                            access.getVirtualPc(),
                            access.getPhysicalAddress(),
                            access.getThread().getId(),
                            access.getThread().getContext().getProcess().getFunctionNameFromPc(access.getVirtualPc()),
                            new L2MissHotspotStackDistanceMeterEvent.L2MissHotspotStackDistanceMeterEventValue(event.getStackDistance())
                    ));
                }

                if (event.isHitInCache()) {
                    statL2HitStackDistances.addValue(event.getStackDistance());
                } else {
                    statL2MissStackDistances.addValue(event.getStackDistance());
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(
                L2HitHotspotInterThreadStackDistanceMeterEvent.class,
                event -> statL2HitHotspotInterThreadStackDistances.addValue(event.getValue().getStackDistance())
        );

        simulation.getBlockingEventDispatcher().addListener(
                L2MissHotspotStackDistanceMeterEvent.class,
                event -> statL2MissHotspotStackDistances.addValue(event.getValue().getStackDistance())
        );
    }

    /**
     * Scan the load instructions in all the identified hotspot functions in the specified process.
     *
     * @param process the process
     */
    private void scanLoadInstructionsInHotspotFunctions(Process process) {
        List<Function> hotspotFunctions = process.getHotspotFunctions();

        for (Function hotspotFunction : hotspotFunctions) {
            for (BasicBlock basicBlock : hotspotFunction.getBasicBlocks()) {
                basicBlock.getInstructions().stream()
                        .filter(instruction -> instruction.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.LOAD)
                        .forEach(instruction -> this.loadsInHotspotFunction.put(instruction.getPc(), new LoadInstructionEntry(instruction)));
            }
        }
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "hotspot") {{
            for(String function : getNumCallsPerFunctions().keySet()) {
                Map<String, Long> calls = getNumCallsPerFunctions().get(function);

                for(String callee : calls.keySet()) {
                    getChildren().add(
                            new ReportNode(
                                    this,
                                    String.format("numCallsPerFunctions/%s/%s", function, callee),
                                    String.format("%s", calls.get(callee))
                            )
                    );
                }
            }

            for(int pc : getLoadsInHotspotFunction().keySet()) {
                LoadInstructionEntry loadInstructionEntry = getLoadsInHotspotFunction().get(pc);

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/instruction", pc),
                                String.format("%s", loadInstructionEntry.getInstruction())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l1DAccesses", pc),
                                String.format("%s", loadInstructionEntry.getL1DAccesses())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l1DHitRatio", pc),
                                String.format("%s", loadInstructionEntry.getL1DHitRatio())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l1DHits", pc),
                                String.format("%s", loadInstructionEntry.getL1DHits())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l1DMisses", pc),
                                String.format("%s", loadInstructionEntry.getL1DMisses())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l2Accesses", pc),
                                String.format("%s", loadInstructionEntry.getL2Accesses())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l2HitRatio", pc),
                                String.format("%s", loadInstructionEntry.getL2HitRatio())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l2Hits", pc),
                                String.format("%s", loadInstructionEntry.getL2Hits())
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("loadsInHotspotFunction/0x%08x/l2Misses", pc),
                                String.format("%s", loadInstructionEntry.getL2Misses())
                        )
                );
            }

            getChildren().add(
                    new ReportNode(
                            this,
                            "statL2HitStackDistances",
                            String.format("%s", getStatL2HitStackDistances())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "statL2MissStackDistances",
                            String.format("%s", getStatL2MissStackDistances())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "statL2HitHotspotInterThreadStackDistances",
                            String.format("%s", getStatL2HitHotspotInterThreadStackDistances())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "statL2MissHotspotStackDistances",
                            String.format("%s", getStatL2MissStackDistances())
                    )
            );
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
    public SummaryStatistics getStatL2HitStackDistances() {
        return statL2HitStackDistances;
    }

    /**
     * Get the summary statistics on the L2 cache miss stack distances.
     *
     * @return the summary statistics on the L2 cache miss stack distances
     */
    public SummaryStatistics getStatL2MissStackDistances() {
        return statL2MissStackDistances;
    }

    /**
     * Get the summary statistics on the L2 cache hit hotspot inter-thread stack distances.
     *
     * @return the summary statistics on the L2 cache hit hotspot inter-thread stack distances
     */
    public SummaryStatistics getStatL2HitHotspotInterThreadStackDistances() {
        return statL2HitHotspotInterThreadStackDistances;
    }

    /**
     * Get the summary statistics on the L2 cache miss hotspot stack distances.
     *
     * @return the summary statistics on the L2 cache miss hotspot stack distances
     */
    public SummaryStatistics getStatL2MissHotspotStackDistances() {
        return statL2MissHotspotStackDistances;
    }
}
