package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.event.dir.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.Params;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.fsm.FiniteStateMachineFactory;

public class DirectoryControllerFiniteStateMachineFactory extends FiniteStateMachineFactory<DirectoryControllerState, DirectoryControllerEventType, DirectoryControllerFiniteStateMachine> {
    public DirectoryControllerFiniteStateMachineFactory(Action1<DirectoryControllerFiniteStateMachine> actionWhenStateChanged) {
        this.inState(DirectoryControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(final DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        final GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        final CacheController req = getSEvent.getReq();
                        final int tag = getSEvent.getTag();

                        fsm.getDirectoryController().transfer(fsm.getDirectoryController().getNext(), 8, new Action() {
                            @Override
                            public void apply() {
                                fsm.getDirectoryController().getNext().memReadRequestReceive(fsm.getDirectoryController(), tag, new Action() {
                                    @Override
                                    public void apply() {
                                        fsm.getDirectoryController().getCycleAccurateEventQueue().schedule(fsm.getDirectoryController(), new Action() {
                                            @Override
                                            public void apply() {
                                                Params params1 = new Params();
                                                DataFromMemoryEvent dataFromMemoryEvent = new DataFromMemoryEvent(fsm.getDirectoryController(), getSEvent, req, tag);
                                                params1.put("event", dataFromMemoryEvent);
                                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", tag), params1, dataFromMemoryEvent);
                                            }
                                        }, fsm.getDirectoryController().getHitLatency());
                                    }
                                });
                            }
                        });
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(final DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        final GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        final CacheController req = getMEvent.getReq();
                        final int tag = getMEvent.getTag();

                        fsm.getDirectoryController().transfer(fsm.getDirectoryController().getNext(), 8, new Action() {
                            @Override
                            public void apply() {
                                fsm.getDirectoryController().getNext().memReadRequestReceive(fsm.getDirectoryController(), tag, new Action() {
                                    @Override
                                    public void apply() {
                                        fsm.getDirectoryController().getCycleAccurateEventQueue().schedule(fsm.getDirectoryController(), new Action() {
                                            @Override
                                            public void apply() {
                                                Params params1 = new Params();
                                                DataFromMemoryEvent dataFromMemoryEvent = new DataFromMemoryEvent(fsm.getDirectoryController(), getMEvent, req, tag);
                                                params1.put("event", dataFromMemoryEvent);
                                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", tag), params1, dataFromMemoryEvent);
                                            }
                                        }, fsm.getDirectoryController().getHitLatency());
                                    }
                                });
                            }
                        });
                    }
                }, DirectoryControllerState.IM_D);

        this.inState(DirectoryControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
                        fsm.stall(sender, params, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSNotLastEvent = params.get(PutSLastEvent.class, "event");
                        fsm.stall(sender, params, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
                        fsm.stall(sender, params, putMAndDataFromNonOwnerEvent);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEMORY, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        DataFromMemoryEvent dataFromMemoryEvent = params.get(DataFromMemoryEvent.class, "event");
                        fsm.sendDataToReq(dataFromMemoryEvent, dataFromMemoryEvent.getReq(), dataFromMemoryEvent.getTag(), 0);
                        fsm.addReqToSharers(dataFromMemoryEvent.getReq());
                        fsm.getLine().setTag(dataFromMemoryEvent.getTag());
                        fsm.getDirectoryController().getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.IM_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
                        fsm.stall(sender, params, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSNotLastEvent = params.get(PutSLastEvent.class, "event");
                        fsm.stall(sender, params, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
                        fsm.stall(sender, params, putMAndDataFromNonOwnerEvent);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEMORY, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        DataFromMemoryEvent dataFromMemoryEvent = params.get(DataFromMemoryEvent.class, "event");
                        fsm.sendDataToReq(dataFromMemoryEvent, dataFromMemoryEvent.getReq(), dataFromMemoryEvent.getTag(), 0);
                        fsm.setOwnerToReq(dataFromMemoryEvent.getReq());
                        fsm.getLine().setTag(dataFromMemoryEvent.getTag());
                        fsm.getDirectoryController().getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                    }
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        final CacheController req = getSEvent.getReq();
                        final int tag = getSEvent.getTag();

                        fsm.sendDataToReq(getSEvent, req, tag, 0);
                        fsm.addReqToSharers(req);
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        final CacheController req = getMEvent.getReq();
                        final int tag = getMEvent.getTag();

                        int numAcks = 0;
                        for (CacheController sharer : fsm.getDirectoryEntry().getSharers()) {
                            if (sharer != req) {
                                numAcks++;
                            }
                        }

                        fsm.sendDataToReq(getMEvent, req, tag, numAcks);
                        fsm.sendInvToSharers(getMEvent, req, tag);
                        fsm.clearSharers();
                        fsm.setOwnerToReq(req);
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        CacheLine<DirectoryControllerState> line = fsm.getDirectoryController().getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.setNumRecallAcks(fsm.getDirectoryEntry().getSharers().size());
                        fsm.sendRecallToSharers(replacementEvent, line.getTag());
                        fsm.clearSharers();
                        fsm.setOnCompletedCallback(replacementEvent.getOnCompletedCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = params.get(PutSLastEvent.class, "event");
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.M)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        final CacheController req = getSEvent.getReq();
                        final int tag = getSEvent.getTag();

                        fsm.sendFwdGetSToOwner(getSEvent, req, tag);
                        fsm.addReqAndOwnerToSharers(req);
                        fsm.clearOwner();
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        final CacheController req = getMEvent.getReq();
                        final int tag = getMEvent.getTag();

                        fsm.sendFwdGetMToOwner(getMEvent, req, tag);
                        fsm.setOwnerToReq(req);
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        CacheLine<DirectoryControllerState> line = fsm.getDirectoryController().getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.setNumRecallAcks(1);
                        fsm.sendRecallToOwner(replacementEvent, line.getTag());
                        fsm.clearOwner();
                        fsm.setOnCompletedCallback(replacementEvent.getOnCompletedCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = params.get(PutSLastEvent.class, "event");
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_OWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromOwnerEvent putMAndDataFromOwnerEvent = params.get(PutMAndDataFromOwnerEvent.class, "event");
                        final CacheController req = putMAndDataFromOwnerEvent.getReq();
                        final int tag = putMAndDataFromOwnerEvent.getTag();

                        fsm.copyDataToMemory(tag);
                        fsm.clearOwner();
                        fsm.sendPutAckToReq(putMAndDataFromOwnerEvent, req, tag);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();

                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = params.get(PutSLastEvent.class, "event");
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.DATA, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        DataEvent dataEvent = params.get(DataEvent.class, "event");
                        final int tag = dataEvent.getTag();

                        fsm.copyDataToMemory(tag);
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.MI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        fsm.stall(sender, params, replacementEvent);
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        fsm.decrementRecallAck();
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.LAST_RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        LastRecallAckEvent lastRecallAckEvent = params.get(LastRecallAckEvent.class, "event");
                        final int tag = lastRecallAckEvent.getTag();
                        fsm.copyDataToMemory(tag);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();
//
                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.MI_A);

        this.inState(DirectoryControllerState.SI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = params.get(GetMEvent.class, "event");
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        fsm.stall(sender, params, replacementEvent);
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        fsm.decrementRecallAck();
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.LAST_RECALL_ACK, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

//                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = params.get(PutSLastEvent.class, "event");
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

//                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
//                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        //TODO: is it correct?
                        throw new UnsupportedOperationException();
//                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
//                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
//                        final int tag = putMAndDataFromNonOwnerEvent.getTag();
//
//                        fsm.sendPutAckToReq(req, tag);
                    }
                }, DirectoryControllerState.SI_A);
        
    }
}
