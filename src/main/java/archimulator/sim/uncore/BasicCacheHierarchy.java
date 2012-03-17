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
import archimulator.sim.base.simulation.Logger;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.coherence.FirstLevelCache;
import archimulator.sim.uncore.coherence.LastLevelCache;
import archimulator.sim.uncore.dram.*;
import archimulator.sim.uncore.net.L1sToL2Net;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.ArrayList;
import java.util.List;

public class BasicCacheHierarchy extends BasicSimulationObject implements CacheHierarchy {
    private MainMemory mainMemory;
    private LastLevelCache l2Cache;
    private List<FirstLevelCache> instructionCaches;
    private List<FirstLevelCache> dataCaches;

    private List<TranslationLookasideBuffer> itlbs;
    private List<TranslationLookasideBuffer> dtlbs;

    private L1sToL2Net l1sToL2Network;
    private L2ToMemNet l2ToMemNetwork;

    public BasicCacheHierarchy(BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, Logger logger, ProcessorConfig processorConfig) {
        super(blockingEventDispatcher, cycleAccurateEventQueue, logger);

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

        this.l2Cache = new LastLevelCache(this, "llc", processorConfig.getMemoryHierarchyConfig().getL2Cache());
        this.l2Cache.setNext(this.mainMemory);

        this.instructionCaches = new ArrayList<FirstLevelCache>();
        this.dataCaches = new ArrayList<FirstLevelCache>();

        this.itlbs = new ArrayList<TranslationLookasideBuffer>();
        this.dtlbs = new ArrayList<TranslationLookasideBuffer>();

        for (int i = 0; i < processorConfig.getNumCores(); i++) {
            FirstLevelCache instructionCache = new FirstLevelCache(this, "c" + i + ".icache", processorConfig.getMemoryHierarchyConfig().getInstructionCache());
            instructionCache.setNext(this.l2Cache);
            this.l2Cache.addShadowTagDirectoryForL1(instructionCache);
            this.instructionCaches.add(instructionCache);

            FirstLevelCache dataCache = new FirstLevelCache(this, "c" + i + ".dcache", processorConfig.getMemoryHierarchyConfig().getDataCache());
            dataCache.setNext(this.l2Cache);
            this.l2Cache.addShadowTagDirectoryForL1(dataCache);
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

    public void dumpState() {
        for (FirstLevelCache cache : this.instructionCaches) {
            cache.dumpState();
        }

        for (FirstLevelCache cache : this.dataCaches) {
            cache.dumpState();
        }

        this.l2Cache.dumpState();

        this.mainMemory.dumpState();
    }

    public MainMemory getMainMemory() {
        return mainMemory;
    }

    public LastLevelCache getL2Cache() {
        return l2Cache;
    }

    public List<FirstLevelCache> getInstructionCaches() {
        return instructionCaches;
    }

    public List<FirstLevelCache> getDataCaches() {
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
