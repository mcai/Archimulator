package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryEntry;
import archimulator.sim.uncore.coherence.msi.event.dir.*;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
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

public class DirectoryControllerFiniteStateMachine extends BasicFiniteStateMachine<DirectoryControllerState, DirectoryControllerEventType> implements ValueProvider<DirectoryControllerState> {
    private DirectoryController directoryController;
    private DirectoryEntry directoryEntry;
    private DirectoryControllerState oldState;
    private int set;
    private int way;

    private int numRecallAcks = 0;

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

    public DirectoryControllerFiniteStateMachine(String name, int set, int way, final DirectoryController directoryController) {
        super(name, DirectoryControllerState.I);
        this.set = set;
        this.way = way;
        this.directoryController = directoryController;
        this.directoryEntry = new DirectoryEntry();

        this.addListener(ExitStateEvent.class, new Action1<ExitStateEvent>() {
            @Override
            public void apply(ExitStateEvent exitStateEvent) {
                oldState = getState();
            }
        });
    }

    public void onEventGetS(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onStalledCallback) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        Params params = new Params();
        GetSEvent getSEvent = new GetSEvent(directoryController, producerFlow, req, tag, set, way, onStalledCallback);
        params.put("event", getSEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, getSEvent);
    }

    public void onEventGetM(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onStalledCallback) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        Params params = new Params();
        GetMEvent getMEvent = new GetMEvent(directoryController, producerFlow, req, tag, set, way, onStalledCallback);
        params.put("event", getMEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, getMEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        if (tag == CacheLine.INVALID_TAG || onCompletedCallback == null) {
            throw new IllegalArgumentException();
        }

        Params params = new Params();
        ReplacementEvent replacementEvent = new ReplacementEvent(directoryController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback);
        params.put("event", replacementEvent);
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), params, replacementEvent);
    }

    public void onEventRecallAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        if (sender == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        Params params = new Params();
        RecallAckEvent recallAckEvent = new RecallAckEvent(directoryController, producerFlow, sender, tag);
        params.put("event", recallAckEvent);
        this.fireTransition(sender + "." + String.format("0x%08x", tag), params, recallAckEvent);

        if (this.numRecallAcks == 0) {
            Params params2 = new Params();
            LastRecallAckEvent lastRecallAckEvent = new LastRecallAckEvent(directoryController, producerFlow, tag);
            params2.put("event", lastRecallAckEvent);
            this.fireTransition(sender + "." + String.format("0x%08x", tag), params2, lastRecallAckEvent);
        }
    }

    public void onEventPutS(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        if (this.getDirectoryEntry().getSharers().size() > 1) {
            Params params = new Params();
            PutSNotLastEvent putSNotLastEvent = new PutSNotLastEvent(directoryController, producerFlow, req, tag);
            params.put("event", putSNotLastEvent);
            this.fireTransition(req + "." + String.format("0x%08x", tag), params, putSNotLastEvent);
        } else {
            Params params = new Params();
            PutSLastEvent putSLastEvent = new PutSLastEvent(directoryController, producerFlow, req, tag);
            params.put("event", putSLastEvent);
            this.fireTransition(req + "." + String.format("0x%08x", tag), params, putSLastEvent);
        }
    }

    public void onEventPutMAndData(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        if (req == this.getDirectoryEntry().getOwner()) {
            Params params = new Params();
            PutMAndDataFromOwnerEvent putMAndDataFromOwnerEvent = new PutMAndDataFromOwnerEvent(directoryController, producerFlow, req, tag);
            params.put("event", putMAndDataFromOwnerEvent);
            this.fireTransition(req + "." + String.format("0x%08x", tag), params, putMAndDataFromOwnerEvent);
        } else {
            Params params = new Params();
            PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = new PutMAndDataFromNonOwnerEvent(directoryController, producerFlow, req, tag);
            params.put("event", putMAndDataFromNonOwnerEvent);
            this.fireTransition(req + "." + String.format("0x%08x", tag), params, putMAndDataFromNonOwnerEvent);
        }
    }

    public void onEventData(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        if (sender == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        Params params = new Params();
        DataEvent dataEvent = new DataEvent(directoryController, producerFlow, sender, tag);
        params.put("event", dataEvent);
        this.fireTransition(sender + "." + String.format("0x%08x", tag), params, dataEvent);
    }

    private void fireTransition(Object sender, Params params, DirectoryControllerEvent event) {
        event.onCompleted();
        fsmFactory.fireTransition(this, sender, event.getType(), params);
    }

    private void stall(final Object sender, final Params params, final DirectoryControllerEvent event) {
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

    private void sendDataToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag, int numAcks) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        directoryController.transfer(req, directoryController.getCache().getLineSize() + 8, new DataMessage(directoryController, producerFlow, directoryController, tag, numAcks));
    }

    private void sendPutAckToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        sendPutAckToReq(producerFlow, directoryController, req, tag);
    }

    public static void sendPutAckToReq(CacheCoherenceFlow producerFlow, DirectoryController directoryController, final CacheController req, final int tag) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        directoryController.transfer(req, 8, new PutAckMessage(directoryController, producerFlow, tag));
    }

    private void copyDataToMemory(int tag) {
        //TODO: add dram read latency
    }

    private void sendFwdGetSToOwner(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        final CacheController owner = getDirectoryEntry().getOwner();

        if (owner == null) {
            throw new IllegalArgumentException();
        }

        directoryController.transfer(owner, 8, new FwdGetSMessage(directoryController, producerFlow, req, tag));
    }

    private void sendFwdGetMToOwner(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        final CacheController owner = getDirectoryEntry().getOwner();

        if (owner == null) {
            throw new IllegalArgumentException();
        }

        directoryController.transfer(owner, 8, new FwdGetMMessage(directoryController, producerFlow, req, tag));
    }

    private void sendInvToSharers(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        for (final CacheController sharer : this.getDirectoryEntry().getSharers()) {
            if (req != sharer) {
                directoryController.transfer(sharer, 8, new InvMessage(directoryController, producerFlow, req, tag));
            }
        }
    }

    private void sendRecallToOwner(CacheCoherenceFlow producerFlow, int tag) {
        if (tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        final CacheController owner = getDirectoryEntry().getOwner();

        if (owner == null) {
            throw new IllegalArgumentException();
        }

        directoryController.transfer(owner, 8, new RecallMessage(directoryController, producerFlow, tag));
    }

    private void sendRecallToSharers(CacheCoherenceFlow producerFlow, int tag) {
        if (tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        for (final CacheController sharer : this.getDirectoryEntry().getSharers()) {
            directoryController.transfer(sharer, 8, new RecallMessage(directoryController, producerFlow, tag));
        }
    }

    private void decrementRecallAck(CacheController sender, int tag) {
        if (sender == null || tag == CacheLine.INVALID_TAG || this.numRecallAcks <= 0) {
            throw new IllegalArgumentException();
        }

        this.numRecallAcks--;
    }

    private void addReqAndOwnerToSharers(CacheController req) {
        if (req == null) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().add(req);
        this.getDirectoryEntry().getSharers().add(this.getDirectoryEntry().getOwner());
    }

    private void addReqToSharers(CacheController req) {
        if (req == null) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().add(req);
    }

    private void removeReqFromSharers(CacheController req) {
        if (req == null) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().remove(req);
    }

    private void setOwnerToReq(CacheController req) {
        if (req == null) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().setOwner(req);
    }

    private void clearSharers() {
        this.getDirectoryEntry().getSharers().clear();
    }

    private void clearOwner() {
        this.getDirectoryEntry().setOwner(null);
    }

    @Override
    public DirectoryControllerState get() {
        return this.getState();
    }

    @Override
    public DirectoryControllerState getInitialValue() {
        return DirectoryControllerState.I;
    }

    public DirectoryEntry getDirectoryEntry() {
        return directoryEntry;
    }

    public CacheLine<DirectoryControllerState> getLine() {
        return this.directoryController.getCache().getLine(this.getSet(), this.getWay());
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    private static Action1<DirectoryControllerFiniteStateMachine> actionWhenStateChanged = new Action1<DirectoryControllerFiniteStateMachine>() {
        @Override
        public void apply(DirectoryControllerFiniteStateMachine fsm) {
            if (fsm.oldState != fsm.getState() && fsm.getState().isStable()) {
                final Action onCompletedCallback = fsm.getOnCompletedCallback();
                if (onCompletedCallback != null) {
                    fsm.setOnCompletedCallback(null);
                    onCompletedCallback.apply();
                }
            }

            if (fsm.oldState != fsm.getState()) {
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

    public static FiniteStateMachineFactory<DirectoryControllerState, DirectoryControllerEventType, DirectoryControllerFiniteStateMachine> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<DirectoryControllerState, DirectoryControllerEventType, DirectoryControllerFiniteStateMachine>();

        fsmFactory.inState(DirectoryControllerState.I)
                .setOnCompletedCallback(actionWhenStateChanged)
                .onCondition(DirectoryControllerEventType.GETS, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        //TODO: add dram read latency
                        GetSEvent getSEvent = params.get(GetSEvent.class, "event");
                        final CacheController req = getSEvent.getReq();
                        final int tag = getSEvent.getTag();

                        fsm.sendDataToReq(getSEvent, req, tag, 0);
                        fsm.addReqToSharers(req);
                        fsm.getLine().setTag(getSEvent.getTag());
                        fsm.directoryController.getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                    }
                }, DirectoryControllerState.S)
                .onCondition(DirectoryControllerEventType.GETM, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
                    @Override
                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
                        //TODO: add dram read latency
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
                        fsm.setOwnerToReq(req);
                        fsm.getLine().setTag(getMEvent.getTag());
                        fsm.directoryController.getCache().handleInsertionOnMiss(fsm.getSet(), fsm.getWay());
                    }
                }, DirectoryControllerState.M)
        //TODO: hint: select one victim line, and to be integrated into FSM, etc.
//                .onCondition(DirectoryControllerEventType.PUTS_NOT_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
//                    @Override
//                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
//                        PutSNotLastEvent putSNotLastEvent = params.get(PutSNotLastEvent.class, "event");
//                        final CacheController req = putSNotLastEvent.getReq();
//                        final int tag = putSNotLastEvent.getTag();
//
//                        fsm.sendPutAckToReq(req, tag);
//                    }
//                }, DirectoryControllerState.I)
//                .onCondition(DirectoryControllerEventType.PUTS_LAST, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
//                    @Override
//                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
//                        PutSLastEvent putSLastEvent = params.get(PutSLastEvent.class, "event");
//                        final CacheController req = putSLastEvent.getReq();
//                        final int tag = putSLastEvent.getTag();
//
//                        fsm.sendPutAckToReq(req, tag);
//                    }
//                }, DirectoryControllerState.I)
//                .onCondition(DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER, new Action4<DirectoryControllerFiniteStateMachine, Object, DirectoryControllerEventType, Params>() {
//                    @Override
//                    public void apply(DirectoryControllerFiniteStateMachine fsm, Object sender, DirectoryControllerEventType eventType, Params params) {
//                        PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = params.get(PutMAndDataFromNonOwnerEvent.class, "event");
//                        final CacheController req = putMAndDataFromNonOwnerEvent.getReq();
//                        final int tag = putMAndDataFromNonOwnerEvent.getTag();
//
//                        fsm.sendPutAckToReq(req, tag);
//                    }
//                }, DirectoryControllerState.I)
        ;

        fsmFactory.inState(DirectoryControllerState.S)
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
                        CacheLine<DirectoryControllerState> line = fsm.directoryController.getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.numRecallAcks = fsm.getDirectoryEntry().getSharers().size();
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

        fsmFactory.inState(DirectoryControllerState.M)
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
                        CacheLine<DirectoryControllerState> line = fsm.directoryController.getCache().getLine(replacementEvent.getSet(), replacementEvent.getWay());
                        fsm.numRecallAcks = 1;
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

        fsmFactory.inState(DirectoryControllerState.S_D)
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

        fsmFactory.inState(DirectoryControllerState.MI_A)
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
                        RecallAckEvent recallAckEvent = params.get(RecallAckEvent.class, "event");
                        final int tag = recallAckEvent.getTag();
                        fsm.decrementRecallAck(recallAckEvent.getSender(), tag);
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

        fsmFactory.inState(DirectoryControllerState.SI_A)
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
                        RecallAckEvent recallAckEvent = params.get(RecallAckEvent.class, "event");
                        final int tag = recallAckEvent.getTag();
                        fsm.decrementRecallAck(recallAckEvent.getSender(), tag);
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