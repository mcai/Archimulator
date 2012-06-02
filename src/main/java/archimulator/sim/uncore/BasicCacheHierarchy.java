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
package archimulator.sim.uncore;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;
import archimulator.sim.uncore.dram.*;
import archimulator.sim.uncore.net.L1sToL2Net;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicCacheHierarchy extends BasicSimulationObject implements CacheHierarchy {
    private MemoryController mainMemory;
    private DirectoryController l2Cache;
    private List<CacheController> instructionCaches;
    private List<CacheController> dataCaches;

    private List<TranslationLookasideBuffer> itlbs;
    private List<TranslationLookasideBuffer> dtlbs;

    private L1sToL2Net l1sToL2Network;
    private L2ToMemNet l2ToMemNetwork;

    private Map<Controller, Map<Controller, PointToPointReorderBuffer>> p2pReorderBuffers;

    public BasicCacheHierarchy(BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, ProcessorConfig processorConfig) {
        super(blockingEventDispatcher, cycleAccurateEventQueue);

        switch (processorConfig.getMemoryHierarchyConfig().getMainMemory().getType()) {
            case SIMPLE:
                this.mainMemory = new SimpleMemoryController(this, (SimpleMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMainMemory());
                break;
            case BASIC:
                this.mainMemory = new BasicMemoryController(this, (BasicMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMainMemory());
                break;
            default:
                this.mainMemory = new FixedLatencyMemoryController(this, (FixedLatencyMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMainMemory());
                break;
        }

        this.l2Cache = new DirectoryController(this, "llc", processorConfig.getMemoryHierarchyConfig().getL2Cache());
        this.l2Cache.setNext(this.mainMemory);

        this.instructionCaches = new ArrayList<CacheController>();
        this.dataCaches = new ArrayList<CacheController>();

        this.itlbs = new ArrayList<TranslationLookasideBuffer>();
        this.dtlbs = new ArrayList<TranslationLookasideBuffer>();

        for (int i = 0; i < processorConfig.getNumCores(); i++) {
            CacheController instructionCache = new CacheController(this, "c" + i + ".icache", processorConfig.getMemoryHierarchyConfig().getInstructionCache());
            instructionCache.setNext(this.l2Cache);
            this.instructionCaches.add(instructionCache);

            CacheController dataCache = new CacheController(this, "c" + i + ".dcache", processorConfig.getMemoryHierarchyConfig().getDataCache());
            dataCache.setNext(this.l2Cache);
            this.dataCaches.add(dataCache);

            for (int j = 0; j < processorConfig.getNumThreadsPerCore(); j++) {
                TranslationLookasideBuffer itlb = new TranslationLookasideBuffer(this, "c" + i + "t" + j + ".itlb", processorConfig.getTlb());
                this.itlbs.add(itlb);

                TranslationLookasideBuffer dtlb = new TranslationLookasideBuffer(this, "c" + i + "t" + j + ".dtlb", processorConfig.getTlb());
                this.dtlbs.add(dtlb);
            }
        }

        this.l1sToL2Network = new L1sToL2Net(this);
        this.l2ToMemNetwork = new L2ToMemNet(this);

        this.p2pReorderBuffers = new HashMap<Controller, Map<Controller, PointToPointReorderBuffer>>();

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        System.out.println("Cache Controller FSM: ");
        System.out.println("------------------------------------------------------------------------");
        CacheControllerFiniteStateMachine.fsmFactory.dump();

        System.out.println();
        System.out.println();

        System.out.println("Directory Controller FSM: ");
        System.out.println("------------------------------------------------------------------------");
        DirectoryControllerFiniteStateMachine.fsmFactory.dump();
    }

    @Override
    public void transfer(final Controller from, final Controller to, int size, final CoherenceMessage message) {
        if(!this.p2pReorderBuffers.containsKey(from)) {
            this.p2pReorderBuffers.put(from, new HashMap<Controller, PointToPointReorderBuffer>());
        }

        if(!this.p2pReorderBuffers.get(from).containsKey(to)) {
            this.p2pReorderBuffers.get(from).put(to, new PointToPointReorderBuffer(from, to));
        }

        this.p2pReorderBuffers.get(from).get(to).transfer(message);

        from.getNet(to).transfer(from, to, size, new Action() {
            @Override
            public void apply() {
                onDestinationArrived(from, to, message);
            }
        });
    }

    private void onDestinationArrived(Controller from, Controller to, CoherenceMessage message) {
        this.p2pReorderBuffers.get(from).get(to).onDestinationArrived(message);
    }

    public MemoryController getMainMemory() {
        return mainMemory;
    }

    public DirectoryController getL2Cache() {
        return l2Cache;
    }

    public List<CacheController> getInstructionCaches() {
        return instructionCaches;
    }

    public List<CacheController> getDataCaches() {
        return dataCaches;
    }

    public List<TranslationLookasideBuffer> getItlbs() {
        return itlbs;
    }

    public List<TranslationLookasideBuffer> getDtlbs() {
        return dtlbs;
    }

    public Net getL1sToL2Network() {
        return l1sToL2Network;
    }

    public L2ToMemNet getL2ToMemNetwork() {
        return l2ToMemNetwork;
    }
}
