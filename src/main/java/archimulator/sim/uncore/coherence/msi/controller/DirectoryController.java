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

import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.coherence.msi.event.directory.DirectoryControllerEventType;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachineFactory;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.sim.uncore.dram.MemoryController;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;
import net.pickapack.action.Action2;

import java.util.ArrayList;
import java.util.List;

/**
 * Directory controller.
 *
 * @author Min Cai
 */
public class DirectoryController extends GeneralCacheController<DirectoryControllerState, DirectoryControllerEventType> {
    private CacheGeometry cacheGeometry;
    private EvictableCache<DirectoryControllerState> cache;
    private List<CacheController> cacheControllers;
    private DirectoryControllerFiniteStateMachineFactory fsmFactory;

    private int numPendingMemoryAccesses;

    /**
     * Create a directory controller.
     *
     * @param memoryHierarchy the memory hierarchy
     * @param name            the name
     */
    public DirectoryController(MemoryHierarchy memoryHierarchy, final String name) {
        super(memoryHierarchy, name);

        this.cacheGeometry = new CacheGeometry(
                getExperiment().getArchitecture().getL2Size(),
                getExperiment().getArchitecture().getL2Associativity(),
                getExperiment().getArchitecture().getL2LineSize()
        );

        this.cache = new EvictableCache<>(
                memoryHierarchy,
                name,
                getGeometry(),
                getReplacementPolicyType(),
                args -> {
                    int set = (Integer) args[0];
                    int way = (Integer) args[1];

                    return new DirectoryControllerFiniteStateMachine(name, set, way, this);
                }
        );

        this.cacheControllers = new ArrayList<>();

        this.fsmFactory = DirectoryControllerFiniteStateMachineFactory.getSingleton();
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return to instanceof MemoryController ? this.getMemoryHierarchy().getL2ToMemNet() : this.getMemoryHierarchy().getL1sToL2Net();
    }

    public MemoryController getNext() {
        return (MemoryController) super.getNext();
    }

    public void setNext(MemoryDevice next) {
        if (!(next instanceof MemoryController)) {
            throw new IllegalArgumentException();
        }

        super.setNext(next);
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

    /**
     * Act on a "GetS" message.
     *
     * @param message the "GetS" message
     */
    private void onGetS(final GetSMessage message) {
        final Action onStalledCallback = () -> onGetS(message);

        this.access(message, message.getAccess(), message.getRequester(), message.getTag(), (set, way) -> {
            CacheLine<DirectoryControllerState> line = getCache().getLine(set, way);
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            fsm.onEventGetS(message, message.getRequester(), message.getTag(), onStalledCallback);
        }, onStalledCallback);
    }

    /**
     * Act on a "GetM" message.
     *
     * @param message the "GetM" message
     */
    private void onGetM(final GetMMessage message) {
        final Action onStalledCallback = () -> onGetM(message);

        this.access(message, message.getAccess(), message.getRequester(), message.getTag(), (set, way) -> {
            CacheLine<DirectoryControllerState> line = getCache().getLine(set, way);
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            fsm.onEventGetM(message, message.getRequester(), message.getTag(), onStalledCallback);
        }, onStalledCallback);
    }

    /**
     * Act on a "recall acknowledgement" message.
     *
     * @param message the "recall acknowledgement" message
     */
    private void onRecallAck(RecallAckMessage message) {
        CacheController sender = message.getSender();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);
        CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
        DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventRecallAck(message, sender, tag);
    }

    /**
     * Act on a "PutS" message.
     *
     * @param message the "PutS" message
     */
    private void onPutS(PutSMessage message) {
        CacheController req = message.getRequester();
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

    /**
     * Act on a "PutM and data" message.
     *
     * @param message the "PutM and data" message
     */
    private void onPutMAndData(PutMAndDataMessage message) {
        CacheController req = message.getRequester();
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

    /**
     * Act on a "data" message.
     *
     * @param message the "data" message
     */
    private void onData(DataMessage message) {
        CacheController sender = (CacheController) message.getSender();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);
        CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
        DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventData(message, sender, tag);
    }

    /**
     * Perform the memory hierarchy access.
     *
     * @param producerFlow                   the producer flow
     * @param access                         the access
     * @param req                            the requester controller
     * @param tag                            the tag
     * @param onReplacementCompletedCallback the callback action performed when the replacement is completed
     * @param onReplacementStalledCallback   the callback action performed when the replacement is stalled
     */
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
                fsm.onEventReplacement(
                        producerFlow,
                        req,
                        tag,
                        cacheAccess,
                        () -> onReplacementCompletedCallback.apply(set, cacheAccess.getWay()),
                        () -> getCycleAccurateEventQueue().schedule(DirectoryController.this, onReplacementStalledCallback, 1)
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

    /**
     * Get the list of upper level L1 cache controllers.
     *
     * @return the list of upper level L1 cache controllers
     */
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

    /**
     * Increment the number of pending memory accesses.
     */
    public void incrementNumPendingMemoryAccesses() {
        this.numPendingMemoryAccesses++;
    }

    /**
     * Decrement the number of pending memory accesses.
     */
    public void decrementNumPendingMemoryAccesses() {
        this.numPendingMemoryAccesses--;
    }

    /**
     * Get the number of pending memory accesses.
     *
     * @return the number of pending memory accesses
     */
    public int getNumPendingMemoryAccesses() {
        return numPendingMemoryAccesses;
    }
}
