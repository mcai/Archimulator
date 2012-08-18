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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachineFactory;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.sim.uncore.dram.MemoryController;
import archimulator.sim.uncore.net.Net;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action2;

import java.util.ArrayList;
import java.util.List;

public class DirectoryController extends GeneralCacheController {
    private CacheGeometry cacheGeometry;
    private EvictableCache<DirectoryControllerState> cache;
    private List<CacheController> cacheControllers;
    private DirectoryControllerFiniteStateMachineFactory fsmFactory;

    public DirectoryController(CacheHierarchy cacheHierarchy, final String name) {
        super(cacheHierarchy, name);

        this.cacheGeometry = new CacheGeometry(getExperiment().getArchitecture().getL2Size(), getExperiment().getArchitecture().getL2Assoc(), getExperiment().getArchitecture().getL2LineSize());

        ValueProviderFactory<DirectoryControllerState, ValueProvider<DirectoryControllerState>> cacheLineStateProviderFactory = new ValueProviderFactory<DirectoryControllerState, ValueProvider<DirectoryControllerState>>() {
            @Override
            public ValueProvider<DirectoryControllerState> createValueProvider(Object... args) {
                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new DirectoryControllerFiniteStateMachine(name, set, way, DirectoryController.this);
            }
        };

        this.cache = new EvictableCache<DirectoryControllerState>(cacheHierarchy, name, getGeometry(), getReplacementPolicyType(), cacheLineStateProviderFactory);
        this.cacheControllers = new ArrayList<CacheController>();

        this.fsmFactory = new DirectoryControllerFiniteStateMachineFactory(new Action1<DirectoryControllerFiniteStateMachine>() {
            @Override
            public void apply(DirectoryControllerFiniteStateMachine fsm) {
                if (fsm.getPreviousState() != fsm.getState() && fsm.getState().isStable()) {
                    Action onCompletedCallback = fsm.getOnCompletedCallback();
                    if (onCompletedCallback != null) {
                        fsm.setOnCompletedCallback(null);
                        onCompletedCallback.apply();
                    }
                }

                if (fsm.getPreviousState() != fsm.getState()) {
                    List<Action> stalledEventsToProcess = new ArrayList<Action>();
                    for (Action stalledEvent : fsm.getStalledEvents()) {
                        stalledEventsToProcess.add(stalledEvent);
                    }

                    fsm.getStalledEvents().clear();

                    for (Action stalledEvent : stalledEventsToProcess) {
                        stalledEvent.apply();
                    }
                }
            }
        });
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return to instanceof MemoryController ? this.getCacheHierarchy().getL2ToMemNetwork() : this.getCacheHierarchy().getL1sToL2Network();
    }

    public void setNext(MemoryController next) {
        super.setNext(next);
    }

    public MemoryController getNext() {
        return (MemoryController) super.getNext();
    }

    @Override
    public void receive(CoherenceMessage message) {
        switch (message.getType()) {
            case GETS:
                onGetS((GetSMessage) message);
                break;
            case GETM:
                onGetM((GetMMessage) message);
                break;
            case RECALL_ACK:
                onRecallAck((RecallAckMessage) message);
                break;
            case PUTS:
                onPutS((PutSMessage) message);
                break;
            case PUTM_AND_DATA:
                onPutMAndData((PutMAndDataMessage) message);
                break;
            case DATA:
                onData((DataMessage) message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void onGetS(final GetSMessage message) {
        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                onGetS(message);
            }
        };

        this.access(message, message.getAccess(), message.getReq(), message.getTag(), new Action2<Integer, Integer>() {
            @Override
            public void apply(Integer set, Integer way) {
                CacheLine<DirectoryControllerState> line = getCache().getLine(set, way);
                DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventGetS(message, message.getReq(), message.getTag(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    private void onGetM(final GetMMessage message) {
        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                onGetM(message);
            }
        };

        this.access(message, message.getAccess(), message.getReq(), message.getTag(), new Action2<Integer, Integer>() {
            @Override
            public void apply(Integer set, Integer way) {
                CacheLine<DirectoryControllerState> line = getCache().getLine(set, way);
                DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventGetM(message, message.getReq(), message.getTag(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    private void onRecallAck(RecallAckMessage message) {
        CacheController sender = message.getSender();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);
        CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
        DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventRecallAck(message, sender, tag);
    }

    private void onPutS(PutSMessage message) {
        CacheController req = message.getReq();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);

        if (way == -1) {
            DirectoryControllerFiniteStateMachine.sendPutAckToReq(message, this, req, tag);
        } else {
            CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            fsm.onEventPutS(message, req, tag);
        }
    }

    private void onPutMAndData(PutMAndDataMessage message) {
        CacheController req = message.getReq();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);

        if (tag == -1) {
            DirectoryControllerFiniteStateMachine.sendPutAckToReq(message, this, req, tag);
        } else {
            CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            fsm.onEventPutMAndData(message, req, tag);
        }
    }

    private void onData(DataMessage message) {
        CacheController sender = (CacheController) message.getSender();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);
        CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
        DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventData(message, sender, tag);
    }

    private void access(CacheCoherenceFlow producerFlow, MemoryHierarchyAccess access, CacheController req, final int tag, final Action2<Integer, Integer> onReplacementCompletedCallback, final Action onReplacementStalledCallback) {
        final int set = this.cache.getSet(tag);

        for (CacheLine<DirectoryControllerState> line : this.cache.getLines(set)) {
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            if (line.getState() == DirectoryControllerState.MI_A || line.getState() == DirectoryControllerState.SI_A && fsm.getEvicterTag() == tag) {
                fsm.stall(onReplacementStalledCallback);
                return;
            }
        }

        final CacheAccess<DirectoryControllerState> cacheAccess = this.cache.newAccess(access, tag);
        if (cacheAccess.isHitInCache()) {
            onReplacementCompletedCallback.apply(set, cacheAccess.getWay());
        } else {
            if (cacheAccess.isReplacement()) {
                CacheLine<DirectoryControllerState> line = this.getCache().getLine(set, cacheAccess.getWay());
                DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventReplacement(producerFlow, req, tag, cacheAccess,
                        new Action() {
                            @Override
                            public void apply() {
                                onReplacementCompletedCallback.apply(set, cacheAccess.getWay());
                            }
                        },
                        new Action() {
                            @Override
                            public void apply() {
                                getCycleAccurateEventQueue().schedule(DirectoryController.this, onReplacementStalledCallback, 1);
                            }
                        }
                );
            } else {
                onReplacementCompletedCallback.apply(set, cacheAccess.getWay());
            }
        }
    }

    @Override
    public EvictableCache<DirectoryControllerState> getCache() {
        return cache;
    }

    public List<CacheController> getCacheControllers() {
        return cacheControllers;
    }

    public DirectoryControllerFiniteStateMachineFactory getFsmFactory() {
        return fsmFactory;
    }

    @Override
    public CacheGeometry getGeometry() {
        return cacheGeometry;
    }

    @Override
    public int getHitLatency() {
        return getExperiment().getArchitecture().getL2HitLatency();
    }

    @Override
    public CacheReplacementPolicyType getReplacementPolicyType() {
        return getExperiment().getArchitecture().getL2ReplacementPolicyType();
    }

    @Override
    public String toString() {
        return this.getCache().getName();
    }
}
