/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.coherence.msi.fsm;

import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.event.directory.*;
import archimulator.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.fsm.FiniteStateMachineFactory;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.not;

/**
 * Directory controller finite state machine factory.
 *
 * @author Min Cai
 */
public class DirectoryControllerFiniteStateMachineFactory extends FiniteStateMachineFactory<DirectoryControllerState, DirectoryControllerEventType, DirectoryControllerFiniteStateMachine> {
    /**
     * Create a directory controller finite state machine factory.
     */
    private DirectoryControllerFiniteStateMachineFactory() {
        Action1<DirectoryControllerFiniteStateMachine> actionWhenStateChanged = fsm -> {
            if (fsm.getPreviousState() != fsm.getState() && fsm.getState().isStable()) {
                Action onCompletedCallback = fsm.getOnCompletedCallback();
                if (onCompletedCallback != null) {
                    fsm.setOnCompletedCallback(null);
                    onCompletedCallback.apply();
                }
            }

            if (fsm.getPreviousState() != fsm.getState()) {
                List<Action> stalledEventsToProcess = new ArrayList<>();
                for (Action stalledEvent : fsm.getStalledEvents()) {
                    stalledEventsToProcess.add(stalledEvent);
                }

                fsm.getStalledEvents().clear();

                for (Action stalledEvent : stalledEventsToProcess) {
                    stalledEvent.apply();
                }
            }
        };

        this.inState(DirectoryControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;

                    fsm.getDirectoryController().incrementNumPendingMemoryAccesses();

                    fsm.getDirectoryController().transfer(fsm.getDirectoryController().getNext(), 8, () -> {
                        fsm.getDirectoryController().getNext().memReadRequestReceive(fsm.getDirectoryController(), event.getTag(), () -> {
                            fsm.getDirectoryController().getCycleAccurateEventQueue().schedule(fsm.getDirectoryController(), () -> {
                                fsm.getDirectoryController().decrementNumPendingMemoryAccesses();

                                DataFromMemEvent dataFromMemEvent = new DataFromMemEvent(fsm.getDirectoryController(), event, event.getRequester(), event.getTag(), event.getAccess());
                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", event.getTag()), dataFromMemEvent);
                            }, fsm.getDirectoryController().getHitLatency());
                        });
                    });
                    fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                    fsm.getLine().setAccess(event.getAccess());
                    fsm.getLine().setTag(event.getTag());
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;

                    fsm.getDirectoryController().incrementNumPendingMemoryAccesses();

                    fsm.getDirectoryController().transfer(fsm.getDirectoryController().getNext(), 8, () -> {
                        fsm.getDirectoryController().getNext().memReadRequestReceive(fsm.getDirectoryController(), event.getTag(), () -> {
                            fsm.getDirectoryController().getCycleAccurateEventQueue().schedule(fsm.getDirectoryController(), () -> {
                                fsm.getDirectoryController().decrementNumPendingMemoryAccesses();

                                DataFromMemEvent dataFromMemEvent = new DataFromMemEvent(fsm.getDirectoryController(), event, event.getRequester(), event.getTag(), event.getAccess());
                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", event.getTag()), dataFromMemEvent);
                            }, fsm.getDirectoryController().getHitLatency());
                        });
                    });
                    fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                    fsm.getLine().setAccess(event.getAccess());
                    fsm.getLine().setTag(event.getTag());
                }, DirectoryControllerState.IM_D);

        this.inState(DirectoryControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.stall(sender, event);
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.stall(sender, event);
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.stall(sender, event);
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEM, (fsm, sender, eventType, params) -> {
                    DataFromMemEvent event = (DataFromMemEvent) params;
                    fsm.sendDataToRequester(event, event.getRequester(), event.getTag(), 0);
                    fsm.addRequesterToSharers(event.getRequester());
                    fsm.fireCacheLineInsertEvent(event.getAccess(), event.getTag(), fsm.getVictimTag());
                    fsm.setEvicterTag(CacheLine.INVALID_TAG);
                    fsm.setVictimTag(CacheLine.INVALID_TAG);
                    fsm.getDirectoryController().getCache().getReplacementPolicy().handleInsertionOnMiss(event.getAccess(), fsm.getSet(), fsm.getWay());
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.IM_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                    fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.stall(sender, event);
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.stall(sender, event);
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.stall(sender, event);
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEM, (fsm, sender, eventType, params) -> {
                    DataFromMemEvent event = (DataFromMemEvent) params;
                    fsm.sendDataToRequester(event, event.getRequester(), event.getTag(), 0);
                    fsm.setOwnerToRequester(event.getRequester());
                    fsm.fireCacheLineInsertEvent(event.getAccess(), event.getTag(), fsm.getVictimTag());
                    fsm.setEvicterTag(CacheLine.INVALID_TAG);
                    fsm.setVictimTag(CacheLine.INVALID_TAG);
                    fsm.getDirectoryController().getCache().getReplacementPolicy().handleInsertionOnMiss(event.getAccess(), fsm.getSet(), fsm.getWay());
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.sendDataToRequester(event, event.getRequester(), event.getTag(), 0);
                    fsm.addRequesterToSharers(event.getRequester());
                    fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.sendDataToRequester(event, event.getRequester(), event.getTag(), select(fsm.getDirectoryEntry().getSharers(), having(on(CacheController.class), not(event.getRequester()))).size());
                    fsm.sendInvToSharers(event, event.getRequester(), event.getTag());
                    fsm.clearSharers();
                    fsm.setOwnerToRequester(event.getRequester());
                    fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.setNumRecallAcks(fsm.getDirectoryEntry().getSharers().size());
                    fsm.sendRecallToSharers(event, fsm.getLine().getTag());
                    fsm.clearSharers();
                    fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                    fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                    fsm.setEvicterTag(event.getTag());
                    fsm.setVictimTag(fsm.getLine().getTag());
                    fsm.getDirectoryController().incrementNumEvictions();
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.removeRequesterFromSharers(event.getRequester());
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.removeRequesterFromSharers(event.getRequester());
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                    fsm.firePutSOrPutMAndDataFromOwnerEvent(event.getAccess(), event.getTag());
                    fsm.getLine().setAccess(null);
                    fsm.getLine().setTag(CacheLine.INVALID_TAG);
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.removeRequesterFromSharers(event.getRequester());
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.M)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.sendFwdGetSToOwner(event, event.getRequester(), event.getTag());
                    fsm.addRequesterAndOwnerToSharers(event.getRequester());
                    fsm.clearOwner();
                    fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.sendFwdGetMToOwner(event, event.getRequester(), event.getTag());
                    fsm.setOwnerToRequester(event.getRequester());
                    fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.setNumRecallAcks(1);
                    fsm.sendRecallToOwner(event, fsm.getLine().getTag());
                    fsm.clearOwner();
                    fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                    fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                    fsm.setEvicterTag(event.getTag());
                    fsm.setVictimTag(fsm.getLine().getTag());
                    fsm.getDirectoryController().incrementNumEvictions();
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_OWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromOwnerEvent event = (PutMAndDataFromOwnerEvent) params;
                    fsm.copyDataToMem(event.getTag());
                    fsm.clearOwner();
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                    fsm.firePutSOrPutMAndDataFromOwnerEvent(event.getAccess(), event.getTag());
                    fsm.getLine().setAccess(null);
                    fsm.getLine().setTag(CacheLine.INVALID_TAG);
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.removeRequesterFromSharers(event.getRequester());
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.removeRequesterFromSharers(event.getRequester());
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.removeRequesterFromSharers(event.getRequester());
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.DATA, (fsm, sender, eventType, params) -> {
                    DataEvent event = (DataEvent) params;
                    fsm.copyDataToMem(event.getTag());
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.MI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.RECALL_ACK, (fsm, sender, eventType, params) -> {
                    fsm.decrementRecallAcks();
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.LAST_RECALL_ACK, (fsm, sender, eventType, params) -> {
                    LastRecallAckEvent event = (LastRecallAckEvent) params;
                    fsm.copyDataToMem(event.getTag());
                    fsm.getLine().setAccess(null);
                    fsm.getLine().setTag(CacheLine.INVALID_TAG);
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.MI_A);

        this.inState(DirectoryControllerState.SI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, (fsm, sender, eventType, params) -> {
                    GetSEvent event = (GetSEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.GETM, (fsm, sender, eventType, params) -> {
                    GetMEvent event = (GetMEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, (fsm, sender, eventType, params) -> {
                    ReplacementEvent event = (ReplacementEvent) params;
                    fsm.stall(event.getOnStalledCallback());
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.RECALL_ACK, (fsm, sender, eventType, params) -> {
                    fsm.decrementRecallAcks();
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.LAST_RECALL_ACK, (fsm, sender, eventType, params) -> {
                    fsm.getLine().setAccess(null);
                    fsm.getLine().setTag(CacheLine.INVALID_TAG);
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, (fsm, sender, eventType, params) -> {
                    PutSNotLastEvent event = (PutSNotLastEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, (fsm, sender, eventType, params) -> {
                    PutSLastEvent event = (PutSLastEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, (fsm, sender, eventType, params) -> {
                    PutMAndDataFromNonOwnerEvent event = (PutMAndDataFromNonOwnerEvent) params;
                    fsm.sendPutAckToReq(event, event.getRequester(), event.getTag());
                }, DirectoryControllerState.SI_A);
    }

    private static DirectoryControllerFiniteStateMachineFactory singleton;

    /**
     * Get the directory controller finite state machine factory singleton.
     *
     * @return the directory controller finite state machine factory singleton
     */
    public static DirectoryControllerFiniteStateMachineFactory getSingleton() {
        if (singleton == null) {
            singleton = new DirectoryControllerFiniteStateMachineFactory();
        }

        return singleton;
    }
}
