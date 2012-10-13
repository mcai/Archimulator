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

import archimulator.model.Experiment;
import archimulator.model.metric.ExperimentStat;
import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.*;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;
import archimulator.sim.uncore.dram.BasicMemoryController;
import archimulator.sim.uncore.dram.FixedLatencyMemoryController;
import archimulator.sim.uncore.dram.MemoryController;
import archimulator.sim.uncore.dram.SimpleMemoryController;
import archimulator.sim.uncore.net.L1sToL2Net;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.action.Action;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.fsm.BasicFiniteStateMachine;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Min Cai
 */
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

    /**
     *
     * @param experiment
     * @param simulation
     * @param blockingEventDispatcher
     * @param cycleAccurateEventQueue
     */
    public BasicCacheHierarchy(Experiment experiment, Simulation simulation, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        switch (getExperiment().getArchitecture().getMemoryControllerType()) {
            case SIMPLE:
                this.memoryController = new SimpleMemoryController(this);
                break;
            case BASIC:
                this.memoryController = new BasicMemoryController(this);
                break;
            default:
                this.memoryController = new FixedLatencyMemoryController(this);
                break;
        }

        this.l2CacheController = new DirectoryController(this, "l2");
        this.l2CacheController.setNext(this.memoryController);

        this.l1ICacheControllers = new ArrayList<CacheController>();
        this.l1DCacheControllers = new ArrayList<CacheController>();

        this.itlbs = new ArrayList<TranslationLookasideBuffer>();
        this.dtlbs = new ArrayList<TranslationLookasideBuffer>();

        for (int i = 0; i < getExperiment().getArchitecture().getNumCores(); i++) {
            CacheController l1ICacheController = new L1ICacheController(this, "c" + i + "/icache");
            l1ICacheController.setNext(this.l2CacheController);
            this.l1ICacheControllers.add(l1ICacheController);

            CacheController l1DCacheController = new L1DCacheController(this, "c" + i + "/dcache");
            l1DCacheController.setNext(this.l2CacheController);
            this.l1DCacheControllers.add(l1DCacheController);

            for (int j = 0; j < getExperiment().getArchitecture().getNumThreadsPerCore(); j++) {
                TranslationLookasideBuffer itlb = new TranslationLookasideBuffer(this, "c" + i + "t" + j + "/itlb");
                this.itlbs.add(itlb);

                TranslationLookasideBuffer dtlb = new TranslationLookasideBuffer(this, "c" + i + "t" + j + "/dtlb");
                this.dtlbs.add(dtlb);
            }
        }

        this.l1sToL2Network = new L1sToL2Net(this);
        this.l2ToMemNetwork = new L2ToMemNet(this);

        this.p2pReorderBuffers = new HashMap<Controller, Map<Controller, PointToPointReorderBuffer>>();
    }

    /**
     *
     * @param stats
     */
    @Override
    public void dumpCacheControllerFsmStats(List<ExperimentStat> stats) {
        for (CacheController l1ICacheController : this.l1ICacheControllers) {
            dumpCacheControllerFsmStats(stats, l1ICacheController);
        }

        for (CacheController l1DCacheController : this.l1DCacheControllers) {
            dumpCacheControllerFsmStats(stats, l1DCacheController);
        }
        dumpCacheControllerFsmStats(stats, this.l2CacheController);
    }

    @SuppressWarnings("unchecked")
    private <StateT extends Serializable, ConditionT> void dumpCacheControllerFsmStats(List<ExperimentStat> stats, GeneralCacheController<StateT, ConditionT> cacheController) {
        List<BasicFiniteStateMachine<StateT, ConditionT>> fsms = new ArrayList<BasicFiniteStateMachine<StateT, ConditionT>>();

        for(int set = 0; set < cacheController.getCache().getNumSets(); set++) {
            for(CacheLine<StateT> line : cacheController.getCache().getLines(set)) {
                BasicFiniteStateMachine<StateT, ConditionT> fsm = (BasicFiniteStateMachine<StateT, ConditionT>) line.getStateProvider();
                fsms.add(fsm);
            }
        }

        Map<String, String> statsMap = new LinkedHashMap<String, String>();

        cacheController.getFsmFactory().dump(PREFIX_CC_FSM + cacheController.getName(), fsms, statsMap);

        for(Map.Entry<String, String> entry : statsMap.entrySet()) {
            stats.add(new ExperimentStat(getExperiment(), getSimulation().getPrefix(), entry.getKey(), entry.getValue()));
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param size
     * @param message
     */
    @Override
    public void transfer(final Controller from, final Controller to, int size, final CoherenceMessage message) {
        if (!this.p2pReorderBuffers.containsKey(from)) {
            this.p2pReorderBuffers.put(from, new HashMap<Controller, PointToPointReorderBuffer>());
        }

        if (!this.p2pReorderBuffers.get(from).containsKey(to)) {
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

    /**
     *
     * @return
     */
    public MemoryController getMemoryController() {
        return memoryController;
    }

    /**
     *
     * @return
     */
    public DirectoryController getL2CacheController() {
        return l2CacheController;
    }

    /**
     *
     * @return
     */
    public List<CacheController> getL1ICacheControllers() {
        return l1ICacheControllers;
    }

    /**
     *
     * @return
     */
    public List<CacheController> getL1DCacheControllers() {
        return l1DCacheControllers;
    }

    /**
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<GeneralCacheController> getCacheControllers() {
        List<GeneralCacheController> cacheControllers = new ArrayList<GeneralCacheController>();
        cacheControllers.add(l2CacheController);
        cacheControllers.addAll(getL1ICacheControllers());
        cacheControllers.addAll(getL1DCacheControllers());
        return cacheControllers;
    }

    /**
     *
     * @return
     */
    public List<TranslationLookasideBuffer> getItlbs() {
        return itlbs;
    }

    /**
     *
     * @return
     */
    public List<TranslationLookasideBuffer> getDtlbs() {
        return dtlbs;
    }

    /**
     *
     * @return
     */
    public Net getL1sToL2Network() {
        return l1sToL2Network;
    }

    /**
     *
     * @return
     */
    public L2ToMemNet getL2ToMemNetwork() {
        return l2ToMemNetwork;
    }

    @Override
    public String getName() {
        return "cacheHierarchy";
    }

    public static final String PREFIX_CC_FSM = "ccFsm/";
}
