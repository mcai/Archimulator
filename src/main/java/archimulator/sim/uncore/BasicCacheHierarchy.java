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

import archimulator.sim.base.event.SimulationEvent;
import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;
import archimulator.sim.uncore.dram.*;
import archimulator.sim.uncore.net.L1sToL2Net;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.action.Action;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicCacheHierarchy extends BasicSimulationObject implements CacheHierarchy {
    private MemoryController memoryController;
    private DirectoryController l2CacheController;
    private List<CacheController> l1ICacheControllers;
    private List<CacheController> l1DCacheControllers;

    private List<TranslationLookasideBuffer> itlbs;
    private List<TranslationLookasideBuffer> dtlbs;

    private L1sToL2Net l1sToL2Network;
    private L2ToMemNet l2ToMemNetwork;

    private Map<Controller, Map<Controller, PointToPointReorderBuffer>> p2pReorderBuffers;

    public BasicCacheHierarchy(BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, ProcessorConfig processorConfig) {
        super(blockingEventDispatcher, cycleAccurateEventQueue);

        switch (processorConfig.getMemoryHierarchyConfig().getMemoryController().getType()) {
            case SIMPLE:
                this.memoryController = new SimpleMemoryController(this, (SimpleMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMemoryController());
                break;
            case BASIC:
                this.memoryController = new BasicMemoryController(this, (BasicMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMemoryController());
                break;
            default:
                this.memoryController = new FixedLatencyMemoryController(this, (FixedLatencyMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMemoryController());
                break;
        }

        this.l2CacheController = new DirectoryController(this, "llc", processorConfig.getMemoryHierarchyConfig().getL2CacheController());
        this.l2CacheController.setNext(this.memoryController);

        this.l1ICacheControllers = new ArrayList<CacheController>();
        this.l1DCacheControllers = new ArrayList<CacheController>();

        this.itlbs = new ArrayList<TranslationLookasideBuffer>();
        this.dtlbs = new ArrayList<TranslationLookasideBuffer>();

        for (int i = 0; i < processorConfig.getNumCores(); i++) {
            CacheController l1ICacheController = new CacheController(this, "c" + i + ".icache", processorConfig.getMemoryHierarchyConfig().getL1ICacheController());
            l1ICacheController.setNext(this.l2CacheController);
            this.l1ICacheControllers.add(l1ICacheController);

            CacheController l1DCacheController = new CacheController(this, "c" + i + ".dcache", processorConfig.getMemoryHierarchyConfig().getL1DCacheController());
            l1DCacheController.setNext(this.l2CacheController);
            this.l1DCacheControllers.add(l1DCacheController);

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
    }

    public void dumpCacheControllerFsmStats() {
        for(CacheController l1ICacheController : this.l1ICacheControllers) {
            System.out.println("Cache Controller " + l1ICacheController.getName() + " FSM: ");
            System.out.println("------------------------------------------------------------------------");
            l1ICacheController.getFsmFactory().dump();
        }

        System.out.println();
        System.out.println();

        for(CacheController l1DCacheController : this.l1DCacheControllers) {
            System.out.println("Cache Controller " + l1DCacheController.getName() + " FSM: ");
            System.out.println("------------------------------------------------------------------------");
            l1DCacheController.getFsmFactory().dump();
        }

        System.out.println();
        System.out.println();

        System.out.println("Directory Controller " + this.l2CacheController.getName() + " FSM: ");
        System.out.println("------------------------------------------------------------------------");
        this.l2CacheController.getFsmFactory().dump();
    }

    @Override
    public void dumpCacheControllerFsmStats(Map<String, Object> stats) {
        for(CacheController l1ICacheController : this.l1ICacheControllers) {
            l1ICacheController.getFsmFactory().dump(l1ICacheController.getName(), stats);
        }

        for(CacheController l1DCacheController : this.l1DCacheControllers) {
            l1DCacheController.getFsmFactory().dump(l1DCacheController.getName(), stats);
        }
        this.l2CacheController.getFsmFactory().dump(this.l2CacheController.getName(), stats);
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
                p2pReorderBuffers.get(from).get(to).onDestinationArrived(message);
            }
        });
    }

    public MemoryController getMemoryController() {
        return memoryController;
    }

    public DirectoryController getL2CacheController() {
        return l2CacheController;
    }

    public List<CacheController> getL1ICacheControllers() {
        return l1ICacheControllers;
    }

    public List<CacheController> getL1DCacheControllers() {
        return l1DCacheControllers;
    }

    public List<GeneralCacheController> getCacheControllers() {
        List<GeneralCacheController> cacheControllers = new ArrayList<GeneralCacheController>();
        cacheControllers.add(l2CacheController);
        cacheControllers.addAll(getL1ICacheControllers());
        cacheControllers.addAll(getL1DCacheControllers());
        return cacheControllers;
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
