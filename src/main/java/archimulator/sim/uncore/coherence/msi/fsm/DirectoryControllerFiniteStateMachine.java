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

    public DirectoryControllerState getOldState() {
        return oldState;
    }

    public List<Action> getStalledEvents() {
        return stalledEvents;
    }

    public void onEventGetS(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onStalledCallback) {
        Params params = new Params();
        GetSEvent getSEvent = new GetSEvent(directoryController, producerFlow, req, tag, set, way, onStalledCallback);
        params.put("event", getSEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, getSEvent);
    }

    public void onEventGetM(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onStalledCallback) {
        Params params = new Params();
        GetMEvent getMEvent = new GetMEvent(directoryController, producerFlow, req, tag, set, way, onStalledCallback);
        params.put("event", getMEvent);
        this.fireTransition(req + "." + String.format("0x%08x", tag), params, getMEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        Params params = new Params();
        ReplacementEvent replacementEvent = new ReplacementEvent(directoryController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback);
        params.put("event", replacementEvent);
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), params, replacementEvent);
    }

    public void onEventRecallAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
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
        Params params = new Params();
        DataEvent dataEvent = new DataEvent(directoryController, producerFlow, sender, tag);
        params.put("event", dataEvent);
        this.fireTransition(sender + "." + String.format("0x%08x", tag), params, dataEvent);
    }

    public void fireTransition(Object sender, Params params, DirectoryControllerEvent event) {
        event.onCompleted();
        directoryController.getFsmFactory().fireTransition(this, sender, event.getType(), params);
    }

    public void stall(final Object sender, final Params params, final DirectoryControllerEvent event) {
        Action action = new Action() {
            @Override
            public void apply() {
                fireTransition(sender, params, event);
            }
        };
        stall(action);
    }

    public void stall(Action action) {
        stalledEvents.add(action);
    }

    public void sendDataToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag, int numAcks) {
        directoryController.transfer(req, directoryController.getCache().getLineSize() + 8, new DataMessage(directoryController, producerFlow, directoryController, tag, numAcks));
    }

    public void sendPutAckToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        sendPutAckToReq(producerFlow, directoryController, req, tag);
    }

    public static void sendPutAckToReq(CacheCoherenceFlow producerFlow, DirectoryController directoryController, final CacheController req, final int tag) {
        directoryController.transfer(req, 8, new PutAckMessage(directoryController, producerFlow, tag));
    }

    public void copyDataToMemory(int tag) {
        directoryController.getNext().memWriteRequestReceive(directoryController, tag, new Action() {
            @Override
            public void apply() {
            }
        });
    }

    public void sendFwdGetSToOwner(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        final CacheController owner = getDirectoryEntry().getOwner();
        directoryController.transfer(owner, 8, new FwdGetSMessage(directoryController, producerFlow, req, tag));
    }

    public void sendFwdGetMToOwner(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        final CacheController owner = getDirectoryEntry().getOwner();
        directoryController.transfer(owner, 8, new FwdGetMMessage(directoryController, producerFlow, req, tag));
    }

    public void sendInvToSharers(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        for (final CacheController sharer : this.getDirectoryEntry().getSharers()) {
            if (req != sharer) {
                directoryController.transfer(sharer, 8, new InvMessage(directoryController, producerFlow, req, tag));
            }
        }
    }

    public void sendRecallToOwner(CacheCoherenceFlow producerFlow, int tag) {
        final CacheController owner = getDirectoryEntry().getOwner();
        directoryController.transfer(owner, 8, new RecallMessage(directoryController, producerFlow, tag));
    }

    public void sendRecallToSharers(CacheCoherenceFlow producerFlow, int tag) {
        for (final CacheController sharer : this.getDirectoryEntry().getSharers()) {
            directoryController.transfer(sharer, 8, new RecallMessage(directoryController, producerFlow, tag));
        }
    }

    public void setNumRecallAcks(int numRecallAcks) {
        this.numRecallAcks = numRecallAcks;
    }

    public void decrementRecallAck() {
        this.numRecallAcks--;
    }

    public void addReqAndOwnerToSharers(CacheController req) {
        this.getDirectoryEntry().getSharers().add(req);
        this.getDirectoryEntry().getSharers().add(this.getDirectoryEntry().getOwner());
    }

    public void addReqToSharers(CacheController req) {
        this.getDirectoryEntry().getSharers().add(req);
    }

    public void removeReqFromSharers(CacheController req) {
        this.getDirectoryEntry().getSharers().remove(req);
    }

    public void setOwnerToReq(CacheController req) {
        this.getDirectoryEntry().setOwner(req);
    }

    public void clearSharers() {
        this.getDirectoryEntry().getSharers().clear();
    }

    public void clearOwner() {
        this.getDirectoryEntry().setOwner(null);
    }

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public void setOnCompletedCallback(Action onCompletedCallback) {
        if (this.onCompletedCallback != null && onCompletedCallback != null) {
            throw new IllegalArgumentException();
        }

        this.onCompletedCallback = onCompletedCallback;
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

    public DirectoryController getDirectoryController() {
        return directoryController;
    }
}