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

import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.MyCycleAccurateEventQueue;
import archimulator.sim.uncore.dram.*;
import archimulator.sim.uncore.net.L1sToL2Net;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;

import java.util.ArrayList;
import java.util.List;

public class BasicCacheHierarchy extends BasicSimulationObject implements CacheHierarchy {
    private MainMemory mainMemory;
    private DirectoryController l2Cache;
    private List<CacheController> instructionCaches;
    private List<CacheController> dataCaches;

    private List<TranslationLookasideBuffer> itlbs;
    private List<TranslationLookasideBuffer> dtlbs;

    private L1sToL2Net l1sToL2Network;
    private L2ToMemNet l2ToMemNetwork;

    public BasicCacheHierarchy(BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, MyCycleAccurateEventQueue cycleAccurateEventQueue, ProcessorConfig processorConfig) {
        super(blockingEventDispatcher, cycleAccurateEventQueue);

        switch (processorConfig.getMemoryHierarchyConfig().getMainMemory().getType()) {
            case SIMPLE:
                this.mainMemory = new SimpleMainMemory(this, (SimpleMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMainMemory());
                break;
            case BASIC:
                this.mainMemory = new BasicMainMemory(this, (BasicMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMainMemory());
                break;
            default:
                this.mainMemory = new FixedLatencyMainMemory(this, (FixedLatencyMainMemoryConfig) processorConfig.getMemoryHierarchyConfig().getMainMemory());
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
    }

    public MainMemory getMainMemory() {
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
