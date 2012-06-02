package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.event.cache.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import net.pickapack.Params;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.fsm.FiniteStateMachineFactory;

public class CacheControllerFiniteStateMachineFactory extends FiniteStateMachineFactory<CacheControllerState, CacheControllerEventType, CacheControllerFiniteStateMachine> {
    public CacheControllerFiniteStateMachineFactory(Action1<CacheControllerFiniteStateMachine> actionWhenStateChanged) {
        this.inState(CacheControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        final LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.sendGetSToDir(loadEvent, loadEvent.getTag());
                        fsm.getLine().setTag(loadEvent.getTag());
                        fsm.setOnCompletedCallback(new Action() {
                            @Override
                            public void apply() {
                                fsm.getCacheController().getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                                loadEvent.getOnCompletedCallback().apply();
                            }
                        });
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        final StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.sendGetMToDir(storeEvent, storeEvent.getTag());
                        fsm.getLine().setTag(storeEvent.getTag());
                        fsm.setOnCompletedCallback(new Action() {
                            @Override
                            public void apply() {
                                fsm.getCacheController().getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                                storeEvent.getOnCompletedCallback().apply();
                            }
                        });
                    }
                }, CacheControllerState.IM_AD);

        this.inState(CacheControllerState.IS_D)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, final CacheControllerEventType eventType, final Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.stall(loadEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, final CacheControllerEventType eventType, final Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, final CacheControllerEventType eventType, final Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(final CacheControllerFiniteStateMachine fsm, Object sender, final CacheControllerEventType eventType, final Params params) {
                        InvEvent invEvent = params.get(InvEvent.class, "event");
                        fsm.stall(sender, params, invEvent);
                    }
                }, CacheControllerState.IS_D)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACK_EQ_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.S);

        this.inState(CacheControllerState.IM_AD)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.stall(loadEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetSEvent fwdGetSEvent = params.get(FwdGetSEvent.class, "event");
                        fsm.stall(sender, params, fwdGetSEvent);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetMEvent fwdGetMEvent = params.get(FwdGetMEvent.class, "event");
                        fsm.stall(sender, params, fwdGetMEvent);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACK_EQ_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACK_GT_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.decrementInvAck();
                    }
                }, CacheControllerState.IM_AD);

        this.inState(CacheControllerState.IM_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.stall(loadEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetSEvent fwdGetSEvent = params.get(FwdGetSEvent.class, "event");
                        fsm.stall(sender, params, fwdGetSEvent);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetMEvent fwdGetMEvent = params.get(FwdGetMEvent.class, "event");
                        fsm.stall(sender, params, fwdGetMEvent);
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.decrementInvAck();
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.LAST_INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M);

        this.inState(CacheControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), loadEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.sendGetMToDir(storeEvent, storeEvent.getTag());
                        fsm.setOnCompletedCallback(storeEvent.getOnCompletedCallback());
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        CacheLine<CacheControllerState> line = fsm.getCacheController().getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.sendPutSToDir(replacementEvent, line.getTag());
                        fsm.setOnCompletedCallback(replacementEvent.getOnCompletedCallback());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        InvEvent invEvent = params.get(InvEvent.class, "event");
                        final CacheController req = invEvent.getReq();
                        int tag = invEvent.getTag();
                        fsm.sendInvAckToReq(invEvent, req, tag);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        RecallEvent recallEvent = params.get(RecallEvent.class, "event");
                        int tag = recallEvent.getTag();
                        fsm.sendRecallAckToDir(recallEvent, tag, 8);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.SM_AD)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), loadEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetSEvent fwdGetSEvent = params.get(FwdGetSEvent.class, "event");
                        fsm.stall(sender, params, fwdGetSEvent);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetMEvent fwdGetMEvent = params.get(FwdGetMEvent.class, "event");
                        fsm.stall(sender, params, fwdGetMEvent);
                    }
                }, CacheControllerState.SM_AD)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        InvEvent invEvent = params.get(InvEvent.class, "event");
                        final CacheController req = invEvent.getReq();
                        int tag = invEvent.getTag();
                        fsm.sendInvAckToReq(invEvent, req, tag);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        RecallEvent recallEvent = params.get(RecallEvent.class, "event");
                        int tag = recallEvent.getTag();
                        fsm.sendRecallAckToDir(recallEvent, tag, 8);
                    }
                }, CacheControllerState.IM_AD)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACK_EQ_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.DATA_FROM_DIR_ACK_GT_0, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.DATA_FROM_OWNER, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.decrementInvAck();
                    }
                }, CacheControllerState.SM_AD);

        this.inState(CacheControllerState.SM_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), loadEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetSEvent fwdGetSEvent = params.get(FwdGetSEvent.class, "event");
                        fsm.stall(sender, params, fwdGetSEvent);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetMEvent fwdGetMEvent = params.get(FwdGetMEvent.class, "event");
                        fsm.stall(sender, params, fwdGetMEvent);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.decrementInvAck();
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.LAST_INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M);

        this.inState(CacheControllerState.M)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), loadEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.hit(storeEvent.getSet(), storeEvent.getWay());
                        fsm.getCacheController().getCycleAccurateEventQueue().schedule(fsm.getCacheController(), storeEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        CacheLine<CacheControllerState> line = fsm.getCacheController().getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.sendPutMAndDataToDir(replacementEvent, line.getTag());
                        fsm.setOnCompletedCallback(replacementEvent.getOnCompletedCallback());
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetSEvent fwdGetSEvent = params.get(FwdGetSEvent.class, "event");
                        final CacheController req = fwdGetSEvent.getReq();
                        int tag = fwdGetSEvent.getTag();
                        fsm.sendDataToReqAndDir(fwdGetSEvent, req, tag);
                    }
                }, CacheControllerState.S)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetMEvent fwdGetMEvent = params.get(FwdGetMEvent.class, "event");
                        final CacheController req = fwdGetMEvent.getReq();
                        int tag = fwdGetMEvent.getTag();
                        fsm.sendDataToReq(fwdGetMEvent, req, tag);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        RecallEvent recallEvent = params.get(RecallEvent.class, "event");
                        int tag = recallEvent.getTag();
                        fsm.sendRecallAckToDir(recallEvent, tag, fsm.getCacheController().getCache().getLineSize() + 8);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.MI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.stall(loadEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.MI_A)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        RecallEvent recallEvent = params.get(RecallEvent.class, "event");
                        int tag = recallEvent.getTag();
                        fsm.sendRecallAckToDir(recallEvent, tag, 8);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.FWD_GETS, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetSEvent fwdGetSEvent = params.get(FwdGetSEvent.class, "event");
                        final CacheController req = fwdGetSEvent.getReq();
                        int tag = fwdGetSEvent.getTag();
                        fsm.sendDataToReqAndDir(fwdGetSEvent, req, tag);
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.FWD_GETM, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        FwdGetMEvent fwdGetMEvent = params.get(FwdGetMEvent.class, "event");
                        final CacheController req = fwdGetMEvent.getReq();
                        int tag = fwdGetMEvent.getTag();
                        fsm.sendDataToReq(fwdGetMEvent, req, tag);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.SI_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.stall(loadEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.SI_A)
                .onCondition(CacheControllerEventType.INV, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        InvEvent invEvent = params.get(InvEvent.class, "event");
                        final CacheController req = invEvent.getReq();
                        int tag = invEvent.getTag();
                        fsm.sendInvAckToReq(invEvent, req, tag);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.RECALL, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        RecallEvent recallEvent = params.get(RecallEvent.class, "event");
                        int tag = recallEvent.getTag();
                        fsm.sendRecallAckToDir(recallEvent, tag, 8);
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        this.inState(CacheControllerState.II_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.stall(loadEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.stall(storeEvent.getOnStalledCallback());
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        replacementEvent.getOnStalledCallback().apply();
                    }
                }, CacheControllerState.II_A)
                .onCondition(CacheControllerEventType.PUT_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);
    }
}