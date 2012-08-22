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
package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.event.cache.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.fsm.FiniteStateMachineFactory;

public class CacheControllerFiniteStateMachineFactory extends FiniteStateMachineFactory<CacheControllerState, CacheControllerEventType, CacheControllerFiniteStateMachine> {
    public CacheControllerFiniteStateMachineFactory(Action1<CacheControllerFiniteStateMachine> actionWhenStateChanged) {
        this.inState(CacheControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, final LoadEvent event) {
                        fsm.sendGetSToDirectory(event, event.getTag());
                        fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                        fsm.getLine().setAccess(event.getAccess());
                        fsm.getLine().setTag(event.getTag());
                        fsm.setOnCompletedCallback(new Action() {
                            @Override
                            public void apply() {
                                fsm.getCacheController().getCache().getReplacementPolicy().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                                event.getOnCompletedCallback().apply();
                            }
                        });
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, final StoreEvent event) {
                        fsm.sendGetMToDirectory(event, event.getTag());
                        fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                        fsm.getLine().setAccess(event.getAccess());
                        fsm.getLine().setTag(event.getTag());
                        fsm.setOnCompletedCallback(new Action() {
                            @Override
                            public void apply() {
                                fsm.getCacheController().getCache().getReplacementPolicy().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                                event.getOnCompletedCallback().apply();
                            }
                        });
                    }
                }, CacheControllerState.IM_AD);

        this.inState(CacheControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.INVALIDATION, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.DATA_FROM_DIRECTORY_ACKNOWLEDGEMENTS_EQUAL_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirectoryAcknowledgementsEqual0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirectoryAcknowledgementsEqual0Event event) {
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromOwnerEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromOwnerEvent event) {
                    }
                }, CacheControllerState.S);

        this.inState(CacheControllerState.IM_AD)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LoadEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LoadEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, StoreEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, StoreEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        event.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.FORWARD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.FORWARD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.DATA_FROM_DIRECTORY_ACKNOWLEDGEMENTS_EQUAL_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirectoryAcknowledgementsEqual0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirectoryAcknowledgementsEqual0Event event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.DATA_FROM_DIRECTORY_ACKNOWLEDGEMENTS_GREATER_THAN_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirectoryAcknowledgementsGreaterThan0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirectoryAcknowledgementsGreaterThan0Event event) {
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromOwnerEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromOwnerEvent event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.INVALIDATION_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationAcknowledgementEvent event) {
                        fsm.decrementInvalidationAcknowledgements();
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
                .onCondition(CacheControllerEventType.FORWARD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.FORWARD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.INVALIDATION_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationAcknowledgementEvent event) {
                        fsm.decrementInvalidationAcknowledgements();
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.LAST_INVALIDATION_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LastInvalidationAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LastInvalidationAcknowledgementEvent event) {
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
                        fsm.sendGetMToDirectory(event, event.getTag());
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), true);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ReplacementEvent event) {
                        fsm.sendPutSToDirectory(event, fsm.getLine().getTag());
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                        fsm.getCacheController().incrementNumEvictions();
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.INVALIDATION, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationEvent event) {
                        CacheController req = event.getRequester();
                        fsm.sendInvalidationAcknowledgementToRequester(event, req, event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAcknowledgementToDirectory(event, event.getTag(), 8);
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
                .onCondition(CacheControllerEventType.FORWARD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.FORWARD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.INVALIDATION, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationEvent event) {
                        CacheController req = event.getRequester();
                        fsm.sendInvalidationAcknowledgementToRequester(event, req, event.getTag());
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAcknowledgementToDirectory(event, event.getTag(), 8);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.DATA_FROM_DIRECTORY_ACKNOWLEDGEMENTS_EQUAL_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirectoryAcknowledgementsEqual0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirectoryAcknowledgementsEqual0Event event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.DATA_FROM_DIRECTORY_ACKNOWLEDGEMENTS_GREATER_THAN_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromDirectoryAcknowledgementsGreaterThan0Event>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromDirectoryAcknowledgementsGreaterThan0Event event) {
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, DataFromOwnerEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, DataFromOwnerEvent event) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.INVALIDATION_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationAcknowledgementEvent event) {
                        fsm.decrementInvalidationAcknowledgements();
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
                .onCondition(CacheControllerEventType.FORWARD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetSEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.FORWARD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetMEvent event) {
                        fsm.stall(sender, event);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.INVALIDATION_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationAcknowledgementEvent event) {
                        fsm.decrementInvalidationAcknowledgements();
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.LAST_INVALIDATION_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, LastInvalidationAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, LastInvalidationAcknowledgementEvent event) {
                    }
                }, CacheControllerState.M);
//                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
//                    @Override
//                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
//                        int tag = recallEvent.getTag();
//                        fsm.sendRecallAcknowledgementToDirectory(recallEvent, tag, 8);
//                    }
//                }, CacheControllerState.IM_A);

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
                        fsm.sendPutMAndDataToDirectory(event, fsm.getLine().getTag());
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                        fsm.getCacheController().incrementNumEvictions();
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.FORWARD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetSEvent event) {
                        fsm.sendDataToRequesterAndDirectory(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.FORWARD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetMEvent event) {
                        fsm.sendDataToRequester(event, event.getRequester(), event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAcknowledgementToDirectory(event, event.getTag(), fsm.getCacheController().getCache().getLineSize() + 8);
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
                        int tag = event.getTag();
                        fsm.sendRecallAcknowledgementToDirectory(event, tag, 8);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.FORWARD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetSEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetSEvent event) {
                        fsm.sendDataToRequesterAndDirectory(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.FORWARD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, ForwardGetMEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, ForwardGetMEvent event) {
                        fsm.sendDataToRequester(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, PutAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, PutAcknowledgementEvent event) {
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
                .onCondition(CacheControllerEventType.INVALIDATION, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, InvalidationEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, InvalidationEvent event) {
                        fsm.sendInvalidationAcknowledgementToRequester(event, event.getRequester(), event.getTag());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, RecallEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, RecallEvent event) {
                        fsm.sendRecallAcknowledgementToDirectory(event, event.getTag(), 8);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, PutAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, PutAcknowledgementEvent event) {
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
                .onCondition(CacheControllerEventType.PUT_ACKNOWLEDGEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, PutAcknowledgementEvent>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, PutAcknowledgementEvent event) {
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);
    }
}
