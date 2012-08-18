package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.event.dir.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.fsm.FiniteStateMachineAction;
import net.pickapack.fsm.FiniteStateMachineFactory;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;

public class DirectoryControllerFiniteStateMachineFactory extends FiniteStateMachineFactory<DirectoryControllerState, DirectoryControllerEventType, DirectoryControllerFiniteStateMachine> {
    //TODO...
    public static FiniteStateMachineAction<DirectoryControllerFiniteStateMachine, DirectoryControllerEventType, DirectoryControllerEvent> ACTION_SET_ACCESS =
            new FiniteStateMachineAction<DirectoryControllerFiniteStateMachine, DirectoryControllerEventType, DirectoryControllerEvent>("SET_ACCESS") {
        @Override
        public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, final DirectoryControllerEvent event) {
            fsm.getLine().setAccess(event.getAccess());
        }
    };
    public static FiniteStateMachineAction<DirectoryControllerFiniteStateMachine, DirectoryControllerEventType, DirectoryControllerEvent> ACTION_SET_TAG =
            new FiniteStateMachineAction<DirectoryControllerFiniteStateMachine, DirectoryControllerEventType, DirectoryControllerEvent>("SET_TAG") {
        @Override
        public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, final DirectoryControllerEvent event) {
            fsm.getLine().setTag(event.getTag());
        }
    };

    public DirectoryControllerFiniteStateMachineFactory(Action1<DirectoryControllerFiniteStateMachine> actionWhenStateChanged) {
        this.inState(DirectoryControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(final DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, final GetSEvent event) {
                        fsm.getDirectoryController().transfer(fsm.getDirectoryController().getNext(), 8, new Action() {
                            @Override
                            public void apply() {
                                fsm.getDirectoryController().getNext().memReadRequestReceive(fsm.getDirectoryController(), event.getTag(), new Action() {
                                    @Override
                                    public void apply() {
                                        fsm.getDirectoryController().getCycleAccurateEventQueue().schedule(fsm.getDirectoryController(), new Action() {
                                            @Override
                                            public void apply() {
                                                DataFromMemoryEvent dataFromMemoryEvent = new DataFromMemoryEvent(fsm.getDirectoryController(), event, event.getReq(), event.getTag(), event.getAccess());
                                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", event.getTag()), dataFromMemoryEvent);
                                            }
                                        }, fsm.getDirectoryController().getHitLatency());
                                    }
                                });
                            }
                        });
                        fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                        fsm.getLine().setAccess(event.getAccess());
                        fsm.getLine().setTag(event.getTag());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(final DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, final GetMEvent event) {
                        fsm.getDirectoryController().transfer(fsm.getDirectoryController().getNext(), 8, new Action() {
                            @Override
                            public void apply() {
                                fsm.getDirectoryController().getNext().memReadRequestReceive(fsm.getDirectoryController(), event.getTag(), new Action() {
                                    @Override
                                    public void apply() {
                                        fsm.getDirectoryController().getCycleAccurateEventQueue().schedule(fsm.getDirectoryController(), new Action() {
                                            @Override
                                            public void apply() {
                                                DataFromMemoryEvent dataFromMemoryEvent = new DataFromMemoryEvent(fsm.getDirectoryController(), event, event.getReq(), event.getTag(), event.getAccess());
                                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", event.getTag()), dataFromMemoryEvent);
                                            }
                                        }, fsm.getDirectoryController().getHitLatency());
                                    }
                                });
                            }
                        });
                        fsm.fireServiceNonblockingRequestEvent(event.getAccess(), event.getTag(), false);
                        fsm.getLine().setAccess(event.getAccess());
                        fsm.getLine().setTag(event.getTag());
                    }
                }, DirectoryControllerState.IM_D);

        this.inState(DirectoryControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, ReplacementEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.stall(sender, event);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.stall(sender, event);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.stall(sender, event);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEMORY, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, DataFromMemoryEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, DataFromMemoryEvent event) {
                        fsm.sendDataToReq(event, event.getReq(), event.getTag(), 0);
                        fsm.addReqToSharers(event.getReq());
                        fsm.fireCacheLineInsertEvent(event.getAccess(), event.getTag(), fsm.getVictimTag());
                        fsm.setEvicterTag(CacheLine.INVALID_TAG);
                        fsm.setVictimTag(CacheLine.INVALID_TAG);
                        fsm.getDirectoryController().getCache().getReplacementPolicy().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.IM_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(event.getAccess(), event.getTag());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, ReplacementEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.stall(sender, event);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.stall(sender, event);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.stall(sender, event);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEMORY, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, DataFromMemoryEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, DataFromMemoryEvent event) {
                        fsm.sendDataToReq(event, event.getReq(), event.getTag(), 0);
                        fsm.setOwnerToReq(event.getReq());
                        fsm.fireCacheLineInsertEvent(event.getAccess(), event.getTag(), fsm.getVictimTag());
                        fsm.setEvicterTag(CacheLine.INVALID_TAG);
                        fsm.setVictimTag(CacheLine.INVALID_TAG);
                        fsm.getDirectoryController().getCache().getReplacementPolicy().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                    }
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.sendDataToReq(event, event.getReq(), event.getTag(), 0);
                        fsm.addReqToSharers(event.getReq());
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.sendDataToReq(event, event.getReq(), event.getTag(), select(fsm.getDirectoryEntry().getSharers(), having(on(CacheController.class), not(event.getReq()))).size());
                        fsm.sendInvToSharers(event, event.getReq(), event.getTag());
                        fsm.clearSharers();
                        fsm.setOwnerToReq(event.getReq());
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, ReplacementEvent event) {
                        fsm.setNumRecallAcks(fsm.getDirectoryEntry().getSharers().size());
                        fsm.sendRecallToSharers(event, fsm.getLine().getTag());
                        fsm.clearSharers();
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                        fsm.setEvicterTag(event.getTag());
                        fsm.setVictimTag(fsm.getLine().getTag());
                        fsm.getDirectoryController().incNumEvictions();
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.removeReqFromSharers(event.getReq());
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.removeReqFromSharers(event.getReq());
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                        fsm.firePutSOrPutMAndDataFromOwnerEvent(event.getAccess(), event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.removeReqFromSharers(event.getReq());
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.M)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.sendFwdGetSToOwner(event, event.getReq(), event.getTag());
                        fsm.addReqAndOwnerToSharers(event.getReq());
                        fsm.clearOwner();
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.sendFwdGetMToOwner(event, event.getReq(), event.getTag());
                        fsm.setOwnerToReq(event.getReq());
                        fsm.hit(event.getAccess(), event.getTag(), event.getSet(), event.getWay());
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, ReplacementEvent event) {
                        fsm.setNumRecallAcks(1);
                        fsm.sendRecallToOwner(event, fsm.getLine().getTag());
                        fsm.clearOwner();
                        fsm.setOnCompletedCallback(event.getOnCompletedCallback());
                        fsm.fireReplacementEvent(event.getAccess(), event.getTag());
                        fsm.setEvicterTag(event.getTag());
                        fsm.setVictimTag(fsm.getLine().getTag());
                        fsm.getDirectoryController().incNumEvictions();
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_OWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromOwnerEvent event) {
                        fsm.copyDataToMemory(event.getTag());
                        fsm.clearOwner();
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                        fsm.firePutSOrPutMAndDataFromOwnerEvent(event.getAccess(), event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.removeReqFromSharers(event.getReq());
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.removeReqFromSharers(event.getReq());
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.removeReqFromSharers(event.getReq());
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.DATA, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, DataEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, DataEvent event) {
                        fsm.copyDataToMemory(event.getTag());
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.MI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, ReplacementEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, RecallAckEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, RecallAckEvent event) {
                        fsm.decrementRecallAck();
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.LAST_RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, LastRecallAckEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, LastRecallAckEvent event) {
                        fsm.copyDataToMemory(event.getTag());
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.MI_A);

        this.inState(DirectoryControllerState.SI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetSEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetSEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, GetMEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, GetMEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, ReplacementEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, ReplacementEvent event) {
                        fsm.stall(event.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, RecallAckEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, RecallAckEvent event) {
                        fsm.decrementRecallAck();
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.LAST_RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, LastRecallAckEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, LastRecallAckEvent event) {
                        fsm.getLine().setAccess(null);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSNotLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSNotLastEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutSLastEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutSLastEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, PutMAndDataFromNonOwnerEvent>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, PutMAndDataFromNonOwnerEvent event) {
                        fsm.sendPutAckToReq(event, event.getReq(), event.getTag());
                    }
                }, DirectoryControllerState.SI_A);
        
    }
}
