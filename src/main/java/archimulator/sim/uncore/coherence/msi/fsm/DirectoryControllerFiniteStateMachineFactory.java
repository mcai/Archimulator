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
                        final GetSEvent getSEvent = (GetSEvent) params;
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
                                                DataFromMemoryEvent dataFromMemoryEvent = new DataFromMemoryEvent(fsm.getDirectoryController(), getSEvent, req, tag, getSEvent.getAccess());
                                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", tag), dataFromMemoryEvent);
                                            }
                                        }, fsm.getDirectoryController().getHitLatency());
                                    }
                                });
                            }
                        });

                        fsm.fireServiceNonblockingRequestEvent(getSEvent.getAccess(), getSEvent.getTag());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(final DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        final GetMEvent getMEvent = (GetMEvent) params;
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
                                                DataFromMemoryEvent dataFromMemoryEvent = new DataFromMemoryEvent(fsm.getDirectoryController(), getMEvent, req, tag, getMEvent.getAccess());
                                                fsm.fireTransition(fsm.getDirectoryController().getNext() + "." + String.format("0x%08x", tag), dataFromMemoryEvent);
                                            }
                                        }, fsm.getDirectoryController().getHitLatency());
                                    }
                                });
                            }
                        });

                        fsm.fireServiceNonblockingRequestEvent(getMEvent.getAccess(), getMEvent.getTag());
                    }
                }, DirectoryControllerState.IM_D);

        this.inState(DirectoryControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = (GetSEvent) params;
                        fsm.stall(getSEvent.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(getSEvent.getAccess(), getSEvent.getTag());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
                        fsm.stall(getMEvent.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(getMEvent.getAccess(), getMEvent.getTag());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = (ReplacementEvent) params;
                        fsm.stall(replacementEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        fsm.stall(sender, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSNotLastEvent = (PutSLastEvent) params;
                        fsm.stall(sender, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
                        fsm.stall(sender, putMAndDataFromNonOwnerEvent);
                    }
                }, DirectoryControllerState.IS_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEMORY, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        DataFromMemoryEvent dataFromMemoryEvent = (DataFromMemoryEvent) params;
                        fsm.sendDataToReq(dataFromMemoryEvent, dataFromMemoryEvent.getReq(), dataFromMemoryEvent.getTag(), 0);
                        fsm.addReqToSharers(dataFromMemoryEvent.getReq());
                        fsm.getLine().setTag(dataFromMemoryEvent.getTag());
                        fsm.getDirectoryController().getCache().getLine(fsm.getSet(), fsm.getWay()).getCacheAccess().commit();
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.IM_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = (GetSEvent) params;
                        fsm.stall(getSEvent.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(getSEvent.getAccess(), getSEvent.getTag());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
                        fsm.stall(getMEvent.getOnStalledCallback());
                        fsm.fireNonblockingRequestHitToTransientTagEvent(getMEvent.getAccess(), getMEvent.getTag());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = (ReplacementEvent) params;
                        fsm.stall(replacementEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        fsm.stall(sender, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSNotLastEvent = (PutSLastEvent) params;
                        fsm.stall(sender, putSNotLastEvent);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
                        fsm.stall(sender, putMAndDataFromNonOwnerEvent);
                    }
                }, DirectoryControllerState.IM_D)
                .onCondition(DirectoryControllerEventType.DATA_FROM_MEMORY, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        DataFromMemoryEvent dataFromMemoryEvent = (DataFromMemoryEvent) params;
                        fsm.sendDataToReq(dataFromMemoryEvent, dataFromMemoryEvent.getReq(), dataFromMemoryEvent.getTag(), 0);
                        fsm.setOwnerToReq(dataFromMemoryEvent.getReq());
                        fsm.getLine().setTag(dataFromMemoryEvent.getTag());
                        fsm.getDirectoryController().getCache().getLine(fsm.getSet(), fsm.getWay()).getCacheAccess().commit();
                    }
                }, DirectoryControllerState.M);

        this.inState(DirectoryControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = (GetSEvent) params;
                        final CacheController req = getSEvent.getReq();
                        final int tag = getSEvent.getTag();

                        fsm.sendDataToReq(getSEvent, req, tag, 0);
                        fsm.addReqToSharers(req);
                        fsm.hit(getSEvent.getAccess(), getSEvent.getTag(), getSEvent.getSet(), getSEvent.getWay());
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
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
                        fsm.hit(getMEvent.getAccess(), getMEvent.getTag(), getMEvent.getSet(), getMEvent.getWay());
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = (ReplacementEvent) params;
                        CacheLine<DirectoryControllerState> line = fsm.getDirectoryController().getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.setNumRecallAcks(fsm.getDirectoryEntry().getSharers().size());
                        fsm.sendRecallToSharers(replacementEvent, line.getTag());
                        fsm.clearSharers();
                        fsm.setOnCompletedCallback(replacementEvent.getOnCompletedCallback());
                        fsm.fireServiceNonblockingRequestEvent(replacementEvent.getAccess(), replacementEvent.getTag());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = (PutSLastEvent) params;
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
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
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
                        GetSEvent getSEvent = (GetSEvent) params;
                        final CacheController req = getSEvent.getReq();
                        final int tag = getSEvent.getTag();

                        fsm.sendFwdGetSToOwner(getSEvent, req, tag);
                        fsm.addReqAndOwnerToSharers(req);
                        fsm.clearOwner();
                        fsm.hit(getSEvent.getAccess(), getSEvent.getTag(), getSEvent.getSet(), getSEvent.getWay());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
                        final CacheController req = getMEvent.getReq();
                        final int tag = getMEvent.getTag();

                        fsm.sendFwdGetMToOwner(getMEvent, req, tag);
                        fsm.setOwnerToReq(req);
                        fsm.hit(getMEvent.getAccess(), getMEvent.getTag(), getMEvent.getSet(), getMEvent.getWay());
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = (ReplacementEvent) params;
                        CacheLine<DirectoryControllerState> line = fsm.getDirectoryController().getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.setNumRecallAcks(1);
                        fsm.sendRecallToOwner(replacementEvent, line.getTag());
                        fsm.clearOwner();
                        fsm.setOnCompletedCallback(replacementEvent.getOnCompletedCallback());
                        fsm.fireServiceNonblockingRequestEvent(replacementEvent.getAccess(), replacementEvent.getTag());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = (PutSLastEvent) params;
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                    }
                }, DirectoryControllerState.M)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_OWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromOwnerEvent putMAndDataFromOwnerEvent = (PutMAndDataFromOwnerEvent) params;
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
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
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
                        GetSEvent getSEvent = (GetSEvent) params;
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = (PutSLastEvent) params;
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();

                        fsm.removeReqFromSharers(req);
                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.S_D)
                .onCondition(DirectoryControllerEventType.DATA, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        DataEvent dataEvent = (DataEvent) params;
                        final int tag = dataEvent.getTag();

                        fsm.copyDataToMemory(tag);
                    }
                }, DirectoryControllerState.S);

        this.inState(DirectoryControllerState.MI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = (GetSEvent) params;
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = (ReplacementEvent) params;
                        fsm.stall(sender, replacementEvent);
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
                        LastRecallAckEvent lastRecallAckEvent = (LastRecallAckEvent) params;
                        final int tag = lastRecallAckEvent.getTag();
                        fsm.copyDataToMemory(tag);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, DirectoryControllerState.I)
                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = (PutSLastEvent) params;
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                    }
                }, DirectoryControllerState.MI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();

                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.MI_A);

        this.inState(DirectoryControllerState.SI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetSEvent getSEvent = (GetSEvent) params;
                        fsm.stall(getSEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        GetMEvent getMEvent = (GetMEvent) params;
                        fsm.stall(getMEvent.getOnStalledCallback());
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.REPLACEMENT, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = (ReplacementEvent) params;
                        fsm.stall(sender, replacementEvent);
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
                        PutSNotLastEvent putSNotLastEvent = (PutSNotLastEvent) params;
                        final CacheController req = putSNotLastEvent.getReq();
                        final int tag = putSNotLastEvent.getTag();

                        fsm.sendPutAckToReq(putSNotLastEvent, req, tag);
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutSLastEvent putSLastEvent = (PutSLastEvent) params;
                        final CacheController req = putSLastEvent.getReq();
                        final int tag = putSLastEvent.getTag();

                        fsm.sendPutAckToReq(putSLastEvent, req, tag);
                    }
                }, DirectoryControllerState.SI_A)
                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = (PutMAndDataFromNonOwnerEvent) params;
                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
                        final int tag = putMAndDataFromNonOwnerEvent.getTag();
//
                        fsm.sendPutAckToReq(putMAndDataFromNonOwnerEvent, req, tag);
                    }
                }, DirectoryControllerState.SI_A);
        
    }
}
