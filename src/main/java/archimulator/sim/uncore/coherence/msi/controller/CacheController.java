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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.core.Thread;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.coherence.msi.event.cache.CacheControllerEventType;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.flow.LoadFlow;
import archimulator.sim.uncore.coherence.msi.flow.StoreFlow;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachineFactory;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;
import net.pickapack.action.Action2;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache controller.
 *
 * @author Min Cai
 */
public abstract class CacheController extends GeneralCacheController<CacheControllerState, CacheControllerEventType> {
    private EvictableCache<CacheControllerState> cache;
    private Map<Integer, MemoryHierarchyAccess> pendingAccesses;
    private EnumMap<MemoryHierarchyAccessType, Integer> pendingAccessesPerType;
    private CacheControllerFiniteStateMachineFactory fsmFactory;

    /**
     * Create a cache controller.
     *
     * @param memoryHierarchy the memory hierarchy
     * @param name            the name
     */
    public CacheController(MemoryHierarchy memoryHierarchy, final String name) {
        super(memoryHierarchy, name);

        ValueProviderFactory<CacheControllerState, ValueProvider<CacheControllerState>> cacheLineStateProviderFactory = new ValueProviderFactory<CacheControllerState, ValueProvider<CacheControllerState>>() {
            @Override
            public ValueProvider<CacheControllerState> createValueProvider(Object... args) {
                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new CacheControllerFiniteStateMachine(name, set, way, CacheController.this);
            }
        };

        this.cache = new EvictableCache<CacheControllerState>(memoryHierarchy, name, getGeometry(), getReplacementPolicyType(), cacheLineStateProviderFactory);

        this.pendingAccesses = new HashMap<Integer, MemoryHierarchyAccess>();

        this.pendingAccessesPerType = new EnumMap<MemoryHierarchyAccessType, Integer>(MemoryHierarchyAccessType.class);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.IFETCH, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.LOAD, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.STORE, 0);

        this.fsmFactory = CacheControllerFiniteStateMachineFactory.getSingleton();
    }

    /**
     * Get a value indicating whether the specified memory hierarchy access can be performed now.
     *
     * @param type        the type of the memory hierarchy access
     * @param physicalTag the physical tag
     * @return a value indicating whether the specified memory hierarchy access can be performed now
     */
    public boolean canAccess(MemoryHierarchyAccessType type, int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);
        return access == null ?
                this.pendingAccessesPerType.get(type) < (type == MemoryHierarchyAccessType.STORE ? this.getNumWritePorts() : this.getNumReadPorts()) :
                type != MemoryHierarchyAccessType.STORE && access.getType() != MemoryHierarchyAccessType.STORE;
    }

    /**
     * Find the pending memory hierarchy access matching the specified physical tag.
     *
     * @param physicalTag the physical tag
     * @return the pending memory hierarchy access matching the specified physical tag if any exists; otherwise null
     */
    public MemoryHierarchyAccess findAccess(int physicalTag) {
        return this.pendingAccesses.containsKey(physicalTag) ? this.pendingAccesses.get(physicalTag) : null;
    }

    /**
     * Begin a memory hierarchy access.
     *
     * @param dynamicInstruction  the dynamic instruction
     * @param thread              the thread
     * @param type                the type of the memory hierarchy access
     * @param virtualPc           the virtual address of the program counter (PC)'s value
     * @param physicalAddress     the physical address
     * @param physicalTag         the physical tag
     * @param onCompletedCallback the callback action performed when the memory hierarchy access is completed
     * @return the newly created memory hierarchy access
     */
    public MemoryHierarchyAccess beginAccess(DynamicInstruction dynamicInstruction, Thread thread, MemoryHierarchyAccessType type, int virtualPc, int physicalAddress, int physicalTag, Action onCompletedCallback) {
        MemoryHierarchyAccess newAccess = new MemoryHierarchyAccess(dynamicInstruction, thread, type, virtualPc, physicalAddress, physicalTag, onCompletedCallback);

        MemoryHierarchyAccess access = this.findAccess(physicalTag);

        if (access != null) {
            access.getAliases().add(0, newAccess);
        } else {
            this.pendingAccesses.put(physicalTag, newAccess);
            this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) + 1);
        }

        return newAccess;
    }

    /**
     * End the memory hierarchy access matching the specified physical tag.
     *
     * @param physicalTag the physical tag
     */
    public void endAccess(int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);

        access.complete();

        for (MemoryHierarchyAccess alias : access.getAliases()) {
            alias.complete();
        }

        MemoryHierarchyAccessType type = access.getType();
        this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) - 1);

        this.pendingAccesses.remove(physicalTag);
    }

    /**
     * Get the net for the specified destination device.
     *
     * @param to the destination device
     * @return the net for the specified destination device
     */
    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getMemoryHierarchy().getL1sToL2Net();
    }

    /**
     * Receive an "instruction fetch" memory hierarchy access.
     *
     * @param access              the memory hierarchy access
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                onLoad(access, access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    /**
     * Receive a "load" memory hierarchy access.
     *
     * @param access              the memory hierarchy access
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public void receiveLoad(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                onLoad(access, access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    /**
     * Receive a "store" memory hierarchy access.
     *
     * @param access              the memory hierarchy access
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public void receiveStore(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                onStore(access, access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    /**
     * Get the next level directory controller.
     *
     * @return the next level directory controller
     */
    public DirectoryController getNext() {
        return (DirectoryController) super.getNext();
    }

    /**
     * Set the next level directory controller.
     *
     * @param next the next level directory controller
     */
    public void setNext(MemoryDevice next) {
        if (!(next instanceof DirectoryController)) {
            throw new IllegalArgumentException();
        }

        ((DirectoryController) next).getCacheControllers().add(this);
        super.setNext(next);
    }

    @Override
    public void receive(CoherenceMessage message) {
        switch (message.getType()) {
            case FWD_GETS:
                onFwdGetS((FwdGetSMessage) message);
                break;
            case FWD_GETM:
                onFwdGetM((FwdGetMMessage) message);
                break;
            case INV:
                onInv((InvMessage) message);
                break;
            case RECALL:
                onRecall((RecallMessage) message);
                break;
            case PUT_ACK:
                onPutAck((PutAckMessage) message);
                break;
            case DATA:
                onData((DataMessage) message);
                break;
            case INV_ACK:
                onInvAck((InvAckMessage) message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Act on a "load" memory hierarchy access.
     *
     * @param access              the memory hierarchy access
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public void onLoad(MemoryHierarchyAccess access, int tag, Action onCompletedCallback) {
        onLoad(access, tag, new LoadFlow(this, tag, onCompletedCallback, access));
    }

    /**
     * Act on a "load" memory hierarchy access.
     *
     * @param access   the memory hierarchy access
     * @param tag      the tag
     * @param loadFlow the load flow
     */
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
                fsm.onEventLoad(loadFlow, tag, loadFlow.getOnCompletedCallback(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    /**
     * Act on a "store" memory hierarchy access.
     *
     * @param access              the memory hierarchy access
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public void onStore(MemoryHierarchyAccess access, int tag, Action onCompletedCallback) {
        onStore(access, tag, new StoreFlow(this, tag, onCompletedCallback, access));
    }

    /**
     * Act on a "store" memory hierarchy access.
     *
     * @param access    the memory hierarchy access
     * @param tag       the tag
     * @param storeFlow the associated store flow
     */
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
                fsm.onEventStore(storeFlow, tag, storeFlow.getOnCompletedCallback(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    /**
     * Act on a "forwarded GetS" message.
     *
     * @param message the "forwarded GetS" message
     */
    private void onFwdGetS(FwdGetSMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventFwdGetS(message, message.getRequester(), message.getTag());
    }

    /**
     * Act on a "forwarded GetM" message.
     *
     * @param message the "forwarded GetM" message
     */
    private void onFwdGetM(FwdGetMMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventFwdGetM(message, message.getRequester(), message.getTag());
    }

    /**
     * Act on an "invalidation" message.
     *
     * @param message the "invalidation" message
     */
    private void onInv(InvMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventInv(message, message.getRequester(), message.getTag());
    }

    /**
     * Act on a "recall" message.
     *
     * @param message the "recall" message
     */
    private void onRecall(RecallMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventRecall(message, message.getTag());
    }

    /**
     * Act on a "Put Acknowledgement" message.
     *
     * @param message the "Put Acknowledgement" message
     */
    private void onPutAck(PutAckMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventPutAck(message, message.getTag());
    }

    /**
     * Act on a "data" message.
     *
     * @param message the "data" message
     */
    private void onData(DataMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventData(message, message.getSender(), message.getTag(), message.getNumInvAcks());
    }

    /**
     * Act on an "invalidation acknowledgement" message.
     *
     * @param message the "invalidation acknowledgement" message
     */
    private void onInvAck(InvAckMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventInvAck(message, message.getSender(), message.getTag());
    }

    /**
     * Perform the memory hierarchy access.
     *
     * @param producerFlow                   the producer cache coherence flow
     * @param access                         the memory hierarchy access
     * @param tag                            the tag
     * @param onReplacementCompletedCallback the callback action performed when the line replacement is completed
     * @param onReplacementStalledCallback   the callback action performed when the line replacement is stalled
     */
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

    @Override
    public EvictableCache<CacheControllerState> getCache() {
        return cache;
    }

    /**
     * Get the lower level directory controller.
     *
     * @return the lower level directory controller
     */
    public DirectoryController getDirectoryController() {
        return this.getNext();
    }

    @Override
    public CacheControllerFiniteStateMachineFactory getFsmFactory() {
        return fsmFactory;
    }

    /**
     * Get the number of read ports.
     *
     * @return the number of read ports
     */
    public abstract int getNumReadPorts();

    /**
     * Get the number of write ports.
     *
     * @return the number of write ports
     */
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
