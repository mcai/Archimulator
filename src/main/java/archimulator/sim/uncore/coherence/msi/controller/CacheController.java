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

import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.uncore.*;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.flow.LoadFlow;
import archimulator.sim.uncore.coherence.msi.flow.StoreFlow;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachineFactory;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import archimulator.sim.uncore.net.Net;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action2;

import java.util.*;

public abstract class CacheController extends GeneralCacheController {
    private EvictableCache<CacheControllerState> cache;
    private Map<Integer, MemoryHierarchyAccess> pendingAccesses;
    private EnumMap<MemoryHierarchyAccessType, Integer> pendingAccessesPerType;
    private CacheControllerFiniteStateMachineFactory fsmFactory;

    public CacheController(CacheHierarchy cacheHierarchy, final String name) {
        super(cacheHierarchy, name);

        ValueProviderFactory<CacheControllerState, ValueProvider<CacheControllerState>> cacheLineStateProviderFactory = new ValueProviderFactory<CacheControllerState, ValueProvider<CacheControllerState>>() {
            @Override
            public ValueProvider<CacheControllerState> createValueProvider(Object... args) {
                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new CacheControllerFiniteStateMachine(name, set, way, CacheController.this);
            }
        };

        this.cache = new EvictableCache<CacheControllerState>(cacheHierarchy, name, getGeometry(), getReplacementPolicyType(), cacheLineStateProviderFactory);

        this.pendingAccesses = new HashMap<Integer, MemoryHierarchyAccess>();

        this.pendingAccessesPerType = new EnumMap<MemoryHierarchyAccessType, Integer>(MemoryHierarchyAccessType.class);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.IFETCH, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.LOAD, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.STORE, 0);

        this.fsmFactory = new CacheControllerFiniteStateMachineFactory(new Action1<CacheControllerFiniteStateMachine>() {
            @Override
            public void apply(CacheControllerFiniteStateMachine fsm) {
                if (fsm.getPreviousState() != fsm.getState()) {
                    if (fsm.getState().isStable()) {
                        Action onCompletedCallback = fsm.getOnCompletedCallback();
                        if (onCompletedCallback != null) {
                            fsm.setOnCompletedCallback(null);
                            onCompletedCallback.apply();
                        }
                    }

                    List<Action> stalledEventsToProcess = new ArrayList<Action>();
                    stalledEventsToProcess.addAll(fsm.getStalledEvents());
                    fsm.getStalledEvents().clear();

                    for (Action stalledEvent : stalledEventsToProcess) {
                        stalledEvent.apply();
                    }
                }
            }
        });
    }

    public boolean canAccess(MemoryHierarchyAccessType type, int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);
        return access == null ?
                this.pendingAccessesPerType.get(type) < (type == MemoryHierarchyAccessType.STORE ? this.getNumWritePorts() : this.getNumReadPorts()) :
                type != MemoryHierarchyAccessType.STORE && access.getType() != MemoryHierarchyAccessType.STORE;
    }

    public MemoryHierarchyAccess findAccess(int physicalTag) {
        return this.pendingAccesses.containsKey(physicalTag) ? this.pendingAccesses.get(physicalTag) : null;
    }

    public MemoryHierarchyAccess beginAccess(DynamicInstruction dynamicInstruction, MemoryHierarchyThread thread, MemoryHierarchyAccessType type, int virtualPc, int physicalAddress, int physicalTag, Action onCompletedCallback) {
        MemoryHierarchyAccess newAccess = new MemoryHierarchyAccess(dynamicInstruction, thread, type, virtualPc, physicalAddress, physicalTag, onCompletedCallback, this.getCycleAccurateEventQueue().getCurrentCycle());

        MemoryHierarchyAccess access = this.findAccess(physicalTag);

        if (access != null) {
            access.getAliases().add(0, newAccess);
        } else {
            this.pendingAccesses.put(physicalTag, newAccess);
            this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) + 1);
        }

        return newAccess;
    }

    public void endAccess(int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);

        access.complete(this.getCycleAccurateEventQueue().getCurrentCycle());

        for (MemoryHierarchyAccess alias : access.getAliases()) {
            alias.complete(this.getCycleAccurateEventQueue().getCurrentCycle());
        }

        MemoryHierarchyAccessType type = access.getType();
        this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) - 1);

        this.pendingAccesses.remove(physicalTag);
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getCacheHierarchy().getL1sToL2Network();
    }

    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                onLoad(access, access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    public void receiveLoad(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                onLoad(access, access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    public void receiveStore(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                onStore(access, access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    public void setNext(DirectoryController next) {
        next.getCacheControllers().add(this);
        super.setNext(next);
    }

    public DirectoryController getNext() {
        return (DirectoryController) super.getNext();
    }

    @Override
    public void receive(CoherenceMessage message) {
        switch (message.getType()) {
            case FORWARD_GETS:
                onFwdGetS((ForwardGetSMessage) message);
                break;
            case FORWARD_GETM:
                onFwdGetM((ForwardGetMMessage) message);
                break;
            case INVALIDATION:
                onInvalidation((InvalidationMessage) message);
                break;
            case RECALL:
                onRecall((RecallMessage) message);
                break;
            case PUT_ACKNOWLEDGEMENT:
                onPutAck((PutAcknowledgementMessage) message);
                break;
            case DATA:
                onData((DataMessage) message);
                break;
            case INVALIDATION_ACKNOWLEDGEMENT:
                onInvalidationAcknowledgement((InvalidationAcknowledgementMessage) message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void onLoad(MemoryHierarchyAccess access, int tag, Action onCompletedCallback) {
        onLoad(access, tag, new LoadFlow(this, tag, onCompletedCallback, access));
    }

    private void onLoad(final MemoryHierarchyAccess access, final int tag, final LoadFlow loadFlow) {
        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                onLoad(access, tag, loadFlow);
            }
        };

        this.access(loadFlow, access, tag, new Action2<Integer, Integer>() {
            @Override
            public void apply(Integer set, Integer way) {
                CacheLine<CacheControllerState> line = getCache().getLine(set, way);
                CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventLoad(loadFlow, tag, loadFlow.getOnCompletedCallback2(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    public void onStore(MemoryHierarchyAccess access, int tag, Action onCompletedCallback) {
        onStore(access, tag, new StoreFlow(this, tag, onCompletedCallback, access));
    }

    private void onStore(final MemoryHierarchyAccess access, final int tag, final StoreFlow storeFlow) {
        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                onStore(access, tag, storeFlow);
            }
        };

        this.access(storeFlow, access, tag, new Action2<Integer, Integer>() {
            @Override
            public void apply(Integer set, Integer way) {
                CacheLine<CacheControllerState> line = getCache().getLine(set, way);
                CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventStore(storeFlow, tag, storeFlow.getOnCompletedCallback2(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    private void onFwdGetS(ForwardGetSMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventForwardGetS(message, message.getRequester(), message.getTag());
    }

    private void onFwdGetM(ForwardGetMMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventForwardGetM(message, message.getRequester(), message.getTag());
    }

    private void onInvalidation(InvalidationMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventInvalidation(message, message.getRequester(), message.getTag());
    }

    private void onRecall(RecallMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventRecall(message, message.getTag());
    }

    private void onPutAck(PutAcknowledgementMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventPutAcknowledgement(message, message.getTag());
    }

    private void onData(DataMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventData(message, message.getSender(), message.getTag(), message.getNumInvalidationAcknowledgements());
    }

    private void onInvalidationAcknowledgement(InvalidationAcknowledgementMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventInvalidationAcknowledgement(message, message.getSender(), message.getTag());
    }

    @Override
    public EvictableCache<CacheControllerState> getCache() {
        return cache;
    }

    private void access(CacheCoherenceFlow producerFlow, MemoryHierarchyAccess access, int tag, final Action2<Integer, Integer> onReplacementCompletedCallback, final Action onReplacementStalledCallback) {
        final int set = this.cache.getSet(tag);

        final CacheAccess<CacheControllerState> cacheAccess = this.getCache().newAccess(access, tag);
        if (cacheAccess.isHitInCache()) {
            onReplacementCompletedCallback.apply(set, cacheAccess.getWay());
        } else {
            if (cacheAccess.isReplacement()) {
                CacheLine<CacheControllerState> line = this.getCache().getLine(set, cacheAccess.getWay());
                CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventReplacement(producerFlow, tag, cacheAccess,
                        new Action() {
                            @Override
                            public void apply() {
                                onReplacementCompletedCallback.apply(set, cacheAccess.getWay());
                            }
                        },
                        new Action() {
                            @Override
                            public void apply() {
                                getCycleAccurateEventQueue().schedule(CacheController.this, onReplacementStalledCallback, 1);
                            }
                        }
                );
            } else {
                onReplacementCompletedCallback.apply(set, cacheAccess.getWay());
            }
        }
    }

    public DirectoryController getDirectoryController() {
        return this.getNext();
    }

    public CacheControllerFiniteStateMachineFactory getFsmFactory() {
        return fsmFactory;
    }

    public abstract int getNumReadPorts();

    public abstract int getNumWritePorts();

    @Override
    public abstract int getHitLatency();

    @Override
    public abstract CacheReplacementPolicyType getReplacementPolicyType();

    @Override
    public String toString() {
        return this.getCache().getName();
    }
}
