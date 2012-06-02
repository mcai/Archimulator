package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.event.cache.*;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import archimulator.util.ValueProvider;
import net.pickapack.Params;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.fsm.BasicFiniteStateMachine;
import net.pickapack.fsm.FiniteStateMachineFactory;
import net.pickapack.fsm.event.ExitStateEvent;

import java.util.ArrayList;
import java.util.List;

public class CacheControllerFiniteStateMachine extends BasicFiniteStateMachine<CacheControllerState, CacheControllerEventType> implements ValueProvider<CacheControllerState> {
    private CacheController cacheController;
    private CacheControllerState previousState;
    private int set;
    private int way;

    private int numInvAcks = 0;

    private List<Action> stalledEvents = new ArrayList<Action>();

    private Action onCompletedCallback;

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public void setOnCompletedCallback(Action onCompletedCallback) {
        if (this.onCompletedCallback != null && onCompletedCallback != null) {
            throw new IllegalArgumentException();
        }

        this.onCompletedCallback = onCompletedCallback;
    }

    public CacheControllerFiniteStateMachine(String name, int set, int way, final CacheController cacheController) {
        super(name, CacheControllerState.I);
        this.set = set;
        this.way = way;
        this.cacheController = cacheController;

        this.addListener(ExitStateEvent.class, new Action1<ExitStateEvent>() {
            @Override
            public void apply(ExitStateEvent exitStateEvent) {
                previousState = getState();
            }
        });
    }

    public void onEventLoad(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        Params params = new Params();
        LoadEvent loadEvent = new LoadEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback);
        params.put("event", loadEvent);
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), params, loadEvent);
    }

    public void onEventStore(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        Params params = new Params();
        StoreEvent storeEvent = new StoreEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback);
        params.put("event", storeEvent);
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), params, storeEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        Params params = new Params();
        ReplacementEvent replacementEvent = new ReplacementEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback);
        params.put("event", replacementEvent);
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), params, replacementEvent);
    }

    public void onEventFwdGetS(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        Params params = new Params();
        FwdGetSEvent fwdGetSEvent = new FwdGetSEvent(cacheController, producerFlow, req, tag);
        params.put("event", fwdGetSEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, fwdGetSEvent);
    }

    public void onEventFwdGetM(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        Params params = new Params();
        FwdGetMEvent fwdGetMEvent = new FwdGetMEvent(cacheController, producerFlow, req, tag);
        params.put("event", fwdGetMEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, fwdGetMEvent);
    }

    public void onEventInv(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        Params params = new Params();
        InvEvent invEvent = new InvEvent(cacheController, producerFlow, req, tag);
        params.put("event", invEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, invEvent);
    }

    public void onEventRecall(CacheCoherenceFlow producerFlow, int tag) {
        Params params = new Params();
        RecallEvent recallEvent = new RecallEvent(cacheController, producerFlow, tag);
        params.put("event", recallEvent);
        this.fireTransition("<dir>" + "." + String.format("0x%08x", tag), params, recallEvent);
    }

    public void onEventPutAck(CacheCoherenceFlow producerFlow, int tag) {
        Params params = new Params();
        PutAckEvent putAckEvent = new PutAckEvent(cacheController, producerFlow, tag);
        params.put("event", putAckEvent);
        this.fireTransition(cacheController.getDirectoryController() + "." + String.format("0x%08x", tag), params, putAckEvent);
    }

    public void onEventData(CacheCoherenceFlow producerFlow, Controller sender, int tag, int numAcks) {
        this.numInvAcks += numAcks;

        if (sender instanceof DirectoryController) {
            if (numAcks == 0) {
                Params params = new Params();
                DataFromDirAckEq0Event dataFromDirAckEq0Event = new DataFromDirAckEq0Event(cacheController, producerFlow, sender, tag);
                params.put("event", dataFromDirAckEq0Event);
                this.fireTransition(sender + "." + String.format("0x%08x", tag), params, dataFromDirAckEq0Event);
            } else {
                Params params = new Params();
                DataFromDirAckGt0Event dataFromDirAckGt0Event = new DataFromDirAckGt0Event(cacheController, producerFlow, sender, tag);
                params.put("event", dataFromDirAckGt0Event);
                this.fireTransition(sender + "." + String.format("0x%08x", tag), params, dataFromDirAckGt0Event);

                if (this.numInvAcks == 0) {
                    onEventLastInvAck(producerFlow, tag);
                }
            }
        } else {
            Params params = new Params();
            DataFromOwnerEvent dataFromOwnerEvent = new DataFromOwnerEvent(cacheController, producerFlow, sender, tag);
            params.put("event", dataFromOwnerEvent);
            this.fireTransition(sender + "." + String.format("0x%08x", tag), params, dataFromOwnerEvent);
        }
    }

    public void onEventInvAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        Params params = new Params();
        InvAckEvent invAckEvent = new InvAckEvent(cacheController, producerFlow, sender, tag);
        params.put("event", invAckEvent);
        this.fireTransition(sender + "." + String.format("0x%08x", tag), params, invAckEvent);

        if (this.numInvAcks == 0) {
            onEventLastInvAck(producerFlow, tag);
        }
    }

    private void onEventLastInvAck(CacheCoherenceFlow producerFlow, int tag) {
        Params params = new Params();
        LastInvAckEvent lastInvAckEvent = new LastInvAckEvent(cacheController, producerFlow, tag);
        params.put("event", lastInvAckEvent);
        this.fireTransition("<N/A>" + "." + String.format("0x%08x", tag), params, lastInvAckEvent);

        this.numInvAcks = 0;
    }

    private void fireTransition(Object sender, Params params, CacheControllerEvent event) {
        event.onCompleted();
        fsmFactory.fireTransition(this, sender, event.getType(), params);
    }

    private void sendGetSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetSMessage(cacheController, producerFlow, cacheController, tag));
    }

    private void sendGetMToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetMMessage(cacheController, producerFlow, cacheController, tag));
    }

    private void sendPutSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new PutSMessage(cacheController, producerFlow, cacheController, tag));
    }

    private void sendPutMAndDataToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new PutMAndDataMessage(cacheController, producerFlow, cacheController, tag));
    }

    private void sendDataToReqAndDir(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, 10, new DataMessage(cacheController, producerFlow, cacheController, tag, 0));
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0));
    }

    private void sendDataToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0));
    }

    private void sendInvAckToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, 8, new InvAckMessage(cacheController, producerFlow, cacheController, tag));
    }

    private void sendRecallAckToDir(CacheCoherenceFlow producerFlow, final int tag, int size) {
        cacheController.transfer(cacheController.getDirectoryController(), size, new RecallAckMessage(cacheController, producerFlow, cacheController, tag));
    }

    private void decrementInvAck(CacheController sender, int tag) {
        this.numInvAcks--;
    }

    private void hit(int set, int way) {
        this.cacheController.getCache().handlePromotionOnHit(set, way);
    }

    private void stall(final Object sender, final Params params, final CacheControllerEvent event) {
        Action action = new Action() {
            @Override
            public void apply() {
                fireTransition(sender, params, event);
            }
        };
        stall(action);
    }

    private void stall(Action action) {
        stalledEvents.add(action);
    }

    @Override
    public CacheControllerState get() {
        return this.getState();
    }

    @Override
    public CacheControllerState getInitialValue() {
        return CacheControllerState.I;
    }

    public CacheLine<CacheControllerState> getLine() {
        return this.cacheController.getCache().getLine(this.getSet(), this.getWay());
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    private static Action1<CacheControllerFiniteStateMachine> actionWhenStateChanged = new Action1<CacheControllerFiniteStateMachine>() {
        @Override
        public void apply(CacheControllerFiniteStateMachine fsm) {
            if (fsm.previousState != fsm.getState()) {
                if (fsm.getState().isStable()) {
                    final Action onCompletedCallback = fsm.getOnCompletedCallback();
                    if (onCompletedCallback != null) {
                        fsm.setOnCompletedCallback(null);
                        onCompletedCallback.apply();
                    }
                }

                List<Action> stalledEventsToProcess = new ArrayList<Action>();
                for (Action stalledEvent : fsm.stalledEvents) {
                    stalledEventsToProcess.add(stalledEvent);
                }

                fsm.stalledEvents.clear();

                for (Action stalledEvent : stalledEventsToProcess) {
                    stalledEvent.apply();
                }
            }
        }
    };

    public static FiniteStateMachineFactory<CacheControllerState, CacheControllerEventType, CacheControllerFiniteStateMachine> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<CacheControllerState, CacheControllerEventType, CacheControllerFiniteStateMachine>();

        fsmFactory.inState(CacheControllerState.I)
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
                                fsm.cacheController.getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
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
                                fsm.cacheController.getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                                storeEvent.getOnCompletedCallback().apply();
                            }
                        });
                    }
                }, CacheControllerState.IM_AD);

        fsmFactory.inState(CacheControllerState.IS_D)
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

        fsmFactory.inState(CacheControllerState.IM_AD)
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
                        InvAckEvent invAckEvent = params.get(InvAckEvent.class, "event");
                        fsm.decrementInvAck(invAckEvent.getSender(), invAckEvent.getTag());
                    }
                }, CacheControllerState.IM_AD);

        fsmFactory.inState(CacheControllerState.IM_A)
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
                        InvAckEvent invAckEvent = params.get(InvAckEvent.class, "event");
                        fsm.decrementInvAck(invAckEvent.getSender(), invAckEvent.getTag());
                    }
                }, CacheControllerState.IM_A)
                .onCondition(CacheControllerEventType.LAST_INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M);

        fsmFactory.inState(CacheControllerState.S)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.cacheController.getCycleAccurateEventQueue().schedule(fsm.cacheController, loadEvent.getOnCompletedCallback(), 0);
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
                        CacheLine<CacheControllerState> line = fsm.cacheController.getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
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

        fsmFactory.inState(CacheControllerState.SM_AD)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.cacheController.getCycleAccurateEventQueue().schedule(fsm.cacheController, loadEvent.getOnCompletedCallback(), 0);
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
                        InvAckEvent invAckEvent = params.get(InvAckEvent.class, "event");
                        fsm.decrementInvAck(invAckEvent.getSender(), invAckEvent.getTag());
                    }
                }, CacheControllerState.SM_AD);

        fsmFactory.inState(CacheControllerState.SM_A)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.cacheController.getCycleAccurateEventQueue().schedule(fsm.cacheController, loadEvent.getOnCompletedCallback(), 0);
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
                        InvAckEvent invAckEvent = params.get(InvAckEvent.class, "event");
                        int tag = invAckEvent.getTag();
                        fsm.decrementInvAck(invAckEvent.getSender(), tag);
                    }
                }, CacheControllerState.SM_A)
                .onCondition(CacheControllerEventType.LAST_INV_ACK, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                    }
                }, CacheControllerState.M);

        fsmFactory.inState(CacheControllerState.M)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(CacheControllerEventType.LOAD, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        LoadEvent loadEvent = params.get(LoadEvent.class, "event");
                        fsm.hit(loadEvent.getSet(), loadEvent.getWay());
                        fsm.cacheController.getCycleAccurateEventQueue().schedule(fsm.cacheController, loadEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.STORE, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        StoreEvent storeEvent = params.get(StoreEvent.class, "event");
                        fsm.hit(storeEvent.getSet(), storeEvent.getWay());
                        fsm.cacheController.getCycleAccurateEventQueue().schedule(fsm.cacheController, storeEvent.getOnCompletedCallback(), 0);
                    }
                }, CacheControllerState.M)
                .onCondition(CacheControllerEventType.REPLACEMENT, new Action4<CacheControllerFiniteStateMachine, Object, CacheControllerEventType, Params>() {
                    @Override
                    public void apply(CacheControllerFiniteStateMachine fsm, Object sender, CacheControllerEventType eventType, Params params) {
                        ReplacementEvent replacementEvent = params.get(ReplacementEvent.class, "event");
                        CacheLine<CacheControllerState> line = fsm.cacheController.getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
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
                        fsm.sendRecallAckToDir(recallEvent, tag, fsm.cacheController.getCache().getLineSize() + 8);
                        fsm.getLine().setTag(CacheLine.INVALID_TAG);
                    }
                }, CacheControllerState.I);

        fsmFactory.inState(CacheControllerState.MI_A)
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

        fsmFactory.inState(CacheControllerState.SI_A)
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

        fsmFactory.inState(CacheControllerState.II_A)
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
