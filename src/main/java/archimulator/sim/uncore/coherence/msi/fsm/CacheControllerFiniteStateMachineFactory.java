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
package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.event.cache.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.fsm.FiniteStateMachineFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * L1 cache controller finite state machine factory.
 *
 * @author Min Cai
 */
public class CacheControllerFiniteStateMachineFactory extends FiniteStateMachineFactory<CacheControllerState, CacheControllerEventType, CacheControllerFiniteStateMachine> {
    /**
     * Create an L1 cache controller finite state machine factory.
     */
    private CacheControllerFiniteStateMachineFactory() {
        Action1<CacheControllerFiniteStateMachine> actionWhenStateChanged = fsm -> {
            if (fsm.getPreviousState() != fsm.getState()) {
                if (fsm.getState().isStable()) {
                    Action onCompletedCallback = fsm.getOnCompletedCallback();
                    if (onCompletedCallback != null) {
                        fsm.setOnCompletedCallback(null);
                        onCompletedCallback.apply();
                    }
                }

                List<Action> stalledEventsToProcess = new ArrayList<>();
                stalledEventsToProcess.addAll(fsm.getStalledEvents());
                fsm.getStalledEvents().clear();

                for (Action stalledEvent : stalledEventsToProcess) {
                    stalledEvent.apply();
                }
            }
        };

        this.inState(CacheControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, (fsm, sender, eventType, params) -> {
                    LoadEvent event = (LoadEvent) params;
                    fsm.sendGetSToDir(event, event.getTag());
                    fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                    fsm.getLine().setAccess(event.getAccess());
                    fsm.getLine().setTag(event.getTag());
                    fsm.setOnCompletedCallback(() -> {
                        fsm.getCacheController().getCache().getReplacementPolicy().handleInsertionOnMiss(event.getAccess(), fsm.getSet(), fsm.getWay());
                        event.getOnCompletedCallback().apply();
                    });
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.STORE, (fsm, sender, eventType, params) -> {
                    StoreEvent event = (StoreEvent) params;
                    fsm.sendGetMToDir(event, event.getTag());
                    fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                    fsm.getLine().setAccess(event.getAccess());
                    fsm.getLine().setTag(event.getTag());
                    fsm.setOnCompletedCallback(() -> {
                        fsm.getCacheController().getCache().getReplacementPolicy().handleInsertionOnMiss(event.getAccess(), fsm.getSet(), fsm.getWay());
                        event.getOnCompletedCallback().apply();
                    });
                }, CacheControllerState.IM_AD);

        this.inState(CacheControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, (fsm, sender, eventType, params) -> {
                    LoadEvent event = (LoadEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.STORE, (fsm, sender, eventType, params) -> {
                    StoreEvent event = (StoreEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    event.getOnStalledCallback().apply();
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.INV, (fsm, sender, eventType, params) -> {
                    InvEvent event = (InvEvent) params;
                    fsm.stall(sender, event);
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACKS_EQ_0, (fsm, sender, eventType, event) -> {
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, (fsm, sender, eventType, event) -> {
                }, CacheControllerState.S);

        this.inState(CacheControllerState.IM_AD)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, (fsm, sender, eventType, params) -> {
                    LoadEvent event = (LoadEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.STORE, (fsm, sender, eventType, params) -> {
                    StoreEvent event = (StoreEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    event.getOnStalledCallback().apply();
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.FWD_GETS, (fsm, sender, eventType, params) -> {
                    FwdGetSEvent event = (FwdGetSEvent) params;
                    fsm.stall(sender, event);
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.FWD_GETM, (fsm, sender, eventType, params) -> {
                    FwdGetMEvent event = (FwdGetMEvent) params;
                    fsm.stall(sender, event);
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACKS_EQ_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirAcksEq0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirAcksEq0Event event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACKS_GT_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirAcksGt0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirAcksGt0Event event) {
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromOwnerEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromOwnerEvent event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvAckEvent event) {
                        fsm.decrementInvAcks();
                    }
                }, CacheControllerState.IM_AD);

        this.inState(CacheControllerState.IM_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvAckEvent event) {
                        fsm.decrementInvAcks();
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.LAST_INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LastInvAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LastInvAckEvent event) {
                    }
                }, CacheControllerState.M);

        this.inState(CacheControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), event.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.sendGetMToDir(event, event.getTag());
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), true);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        fsm.sendPutSToDir(event, fsm.getLine().getTag());
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                        fsm.getCacheController().incrementNumEvictions();
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvEvent event) {
                        CacheController req = event.getRequester();
                        fsm.sendInvAckToRequester(event, req, event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAckToDir(event, event.getTag(), 8);
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.SM_AD)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), event.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvEvent event) {
                        CacheController req = event.getRequester();
                        fsm.sendInvAckToRequester(event, req, event.getTag());
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAckToDir(event, event.getTag(), 8);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACKS_EQ_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirAcksEq0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirAcksEq0Event event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACKS_GT_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirAcksGt0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirAcksGt0Event event) {
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromOwnerEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromOwnerEvent event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvAckEvent event) {
                        fsm.decrementInvAcks();
                    }
                }, CacheControllerState.SM_AD);

        this.inState(CacheControllerState.SM_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), event.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvAckEvent event) {
                        fsm.decrementInvAcks();
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.LAST_INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LastInvAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LastInvAckEvent event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.stall(sender, event);
                    }
//                }, CacheControllerState.IM_A);
                }, CacheControllerState.SM_A);

        this.inState(CacheControllerState.M)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), event.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), event.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        fsm.sendPutMAndDataToDir(event, fsm.getLine().getTag());
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                        fsm.getCacheController().incrementNumEvictions();
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetSEvent event) {
                        fsm.sendDataToRequesterAndDir(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetMEvent event) {
                        fsm.sendDataToRequester(event, event.getRequester(), event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAckToDir(event, event.getTag(), fsm.getCacheController().getCache().getLineSize() + 8);
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.MI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAckToDir(event, event.getTag(), 8);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetSEvent event) {
                        fsm.sendDataToRequesterAndDir(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, FwdGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, FwdGetMEvent event) {
                        fsm.sendDataToRequester(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, PutAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, PutAckEvent event) {
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.SI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvEvent event) {
                        fsm.sendInvAckToRequester(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAckToDir(event, event.getTag(), 8);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, PutAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, PutAckEvent event) {
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.II_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, PutAckEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, PutAckEvent event) {
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);
    }

    private static CacheControllerFiniteStateMachineFactory singleton;

    /**
     * Get the L1 cache controller finite state machine factory singleton.
     *
     * @return the L1 cache controller finite state machine factory singleton
     */
    public static CacheControllerFiniteStateMachineFactory getSingleton() {
        if (singleton == null) {
            singleton = new CacheControllerFiniteStateMachineFactory();
        }

        return singleton;
    }
}
