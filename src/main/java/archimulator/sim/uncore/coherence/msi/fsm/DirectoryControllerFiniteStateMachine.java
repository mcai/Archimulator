package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryEntry;
import archimulator.sim.uncore.coherence.msi.event.dir.*;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.ValueProvider;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.fsm.BasicFiniteStateMachine;
import net.pickapack.fsm.event.ExitStateEvent;

import java.util.ArrayList;
import java.util.List;

public class DirectoryControllerFiniteStateMachine extends BasicFiniteStateMachine<DirectoryControllerState, DirectoryControllerEventType> implements ValueProvider<DirectoryControllerState> {
    private DirectoryController directoryController;
    private DirectoryEntry directoryEntry;
    private DirectoryControllerState previousState;
    private int set;
    private int way;

    private int numRecallAcks = 0;

    private List<Action> stalledEvents = new ArrayList<Action>();

    private Action onCompletedCallback;

    private List<String> transitionHistory = new ArrayList<String>(10);

    public DirectoryControllerFiniteStateMachine(String name, int set, int way, final DirectoryController directoryController) {
        super(name, DirectoryControllerState.I);
        this.set = set;
        this.way = way;
        this.directoryController = directoryController;
        this.directoryEntry = new DirectoryEntry();

        this.addListener(ExitStateEvent.class, new Action1<ExitStateEvent>() {
            @Override
            public void apply(ExitStateEvent exitStateEvent) {
                previousState = getState();
            }
        });

//        this.addListener(EnterStateEvent.class, new Action1<EnterStateEvent>() {
//            @Override
//            public void apply(EnterStateEvent enterStateEvent) {
//                CacheLine<DirectoryControllerState> line = directoryController.getCache().getLine(getSet(), getWay());
//
//                if (getState() != previousState) {
//                    String transitionText = String.format("[%d] %s.[%d,%d] {%s} %s: %s.%s -> %s (owner: %s, sharers: %s)",
//                            directoryController.getCycleAccurateEventQueue().getCurrentCycle(), getName(), getSet(), getWay(), line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A", previousState, enterStateEvent.getSender() != null ? enterStateEvent.getSender() : "<N/A>", enterStateEvent.getCondition(), getState(),
//                            getDirectoryEntry().getOwner() != null ? getDirectoryEntry().getOwner() : "N/A", getDirectoryEntry().getSharers().toString().replace("[", "").replace("]", ""));
//                    if (transitionHistory.size() >= 10) {
//                        transitionHistory.remove(0);
//                    }
//
//                    transitionHistory.add(transitionText);
//
//                    if (CacheSimulator.logEnabled) {
//                        CacheSimulator.pw.println(transitionText);
//                        CacheSimulator.pw.flush();
//                    }
//                }
//            }
//        });
    }

    public DirectoryControllerState getPreviousState() {
        return previousState;
    }

    public List<Action> getStalledEvents() {
        return stalledEvents;
    }

    public void onEventGetS(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onStalledCallback) {
        GetSEvent getSEvent = new GetSEvent(this.directoryController, producerFlow, req, tag, set, way, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(req + "." + String.format("0x%08x", tag), getSEvent);
    }

    public void onEventGetM(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onStalledCallback) {
        GetMEvent getMEvent = new GetMEvent(this.directoryController, producerFlow, req, tag, set, way, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(req + "." + String.format("0x%08x", tag), getMEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, CacheController req, int tag, Action onCompletedCallback, Action onStalledCallback) {
        ReplacementEvent replacementEvent = new ReplacementEvent(this.directoryController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(req + "." + String.format("0x%08x", tag), replacementEvent);
    }

    public void onEventRecallAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        RecallAckEvent recallAckEvent = new RecallAckEvent(this.directoryController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), recallAckEvent);

        if (this.numRecallAcks == 0) {
            LastRecallAckEvent lastRecallAckEvent = new LastRecallAckEvent(this.directoryController, producerFlow, tag, producerFlow.getAccess());
            this.fireTransition(sender + "." + String.format("0x%08x", tag), lastRecallAckEvent);
        }
    }

    public void onEventPutS(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        if (this.getDirectoryEntry().getSharers().size() > 1) {
            PutSNotLastEvent putSNotLastEvent = new PutSNotLastEvent(this.directoryController, producerFlow, req, tag, producerFlow.getAccess());
            this.fireTransition(req + "." + String.format("0x%08x", tag), putSNotLastEvent);
        } else {
            PutSLastEvent putSLastEvent = new PutSLastEvent(this.directoryController, producerFlow, req, tag, producerFlow.getAccess());
            this.fireTransition(req + "." + String.format("0x%08x", tag), putSLastEvent);
        }
    }

    public void onEventPutMAndData(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        if (req == this.getDirectoryEntry().getOwner()) {
            PutMAndDataFromOwnerEvent putMAndDataFromOwnerEvent = new PutMAndDataFromOwnerEvent(this.directoryController, producerFlow, req, tag, producerFlow.getAccess());
            this.fireTransition(req + "." + String.format("0x%08x", tag), putMAndDataFromOwnerEvent);
        } else {
            PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = new PutMAndDataFromNonOwnerEvent(this.directoryController, producerFlow, req, tag, producerFlow.getAccess());
            this.fireTransition(req + "." + String.format("0x%08x", tag), putMAndDataFromNonOwnerEvent);
        }
    }

    public void onEventData(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        DataEvent dataEvent = new DataEvent(this.directoryController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), dataEvent);
    }

    public void fireTransition(Object sender, DirectoryControllerEvent event) {
        event.onCompleted();
        this.directoryController.getFsmFactory().fireTransition(this, sender, event.getType(), event);
    }

    public void hit(MemoryHierarchyAccess access, int tag, int set, int way) {
        this.directoryController.getCache().getLine(set, way).getCacheAccess().commit();
        this.fireServiceNonblockingRequestEvent(access, tag);
    }

    public void stall(final Object sender, final DirectoryControllerEvent event) {
        Action action = new Action() {
            @Override
            public void apply() {
                fireTransition(sender, event);
            }
        };
        stall(action);
    }

    public void stall(Action action) {
        stalledEvents.add(action);
    }

    public void fireServiceNonblockingRequestEvent(MemoryHierarchyAccess access, int tag) {
        CacheAccess<DirectoryControllerState> cacheAccess = this.getLine().getCacheAccess();
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getDirectoryController(), access, tag, cacheAccess.getLine(), cacheAccess.isHitInCache(), cacheAccess.isEviction(), cacheAccess.getReference().getAccessType()));
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(this.getDirectoryController(), access, cacheAccess));
    }

    public void fireNonblockingRequestHitToTransientTagEvent(MemoryHierarchyAccess access, int tag) {
        CacheAccess<DirectoryControllerState> cacheAccess = this.getLine().getCacheAccess();
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(this.getDirectoryController(), access, tag, cacheAccess.getLine()));
    }

    public void sendDataToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag, int numAcks) {
        this.directoryController.transfer(req, this.directoryController.getCache().getLineSize() + 8, new DataMessage(this.directoryController, producerFlow, this.directoryController, tag, numAcks, producerFlow.getAccess()));
    }

    public void sendPutAckToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        sendPutAckToReq(producerFlow, this.directoryController, req, tag);
    }

    public static void sendPutAckToReq(CacheCoherenceFlow producerFlow, DirectoryController directoryController, final CacheController req, final int tag) {
        directoryController.transfer(req, 8, new PutAckMessage(directoryController, producerFlow, tag, producerFlow.getAccess()));
    }

    public void copyDataToMemory(int tag) {
        this.directoryController.getNext().memWriteRequestReceive(this.directoryController, tag, new Action() {
            @Override
            public void apply() {
            }
        });
    }

    public void sendFwdGetSToOwner(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        final CacheController owner = getDirectoryEntry().getOwner();
        this.directoryController.transfer(owner, 8, new FwdGetSMessage(this.directoryController, producerFlow, req, tag, producerFlow.getAccess()));
    }

    public void sendFwdGetMToOwner(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        final CacheController owner = getDirectoryEntry().getOwner();
        this.directoryController.transfer(owner, 8, new FwdGetMMessage(this.directoryController, producerFlow, req, tag, producerFlow.getAccess()));
    }

    public void sendInvToSharers(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        for (final CacheController sharer : this.getDirectoryEntry().getSharers()) {
            if (req != sharer) {
                this.directoryController.transfer(sharer, 8, new InvMessage(this.directoryController, producerFlow, req, tag, producerFlow.getAccess()));
            }
        }
    }

    public void sendRecallToOwner(CacheCoherenceFlow producerFlow, int tag) {
        final CacheController owner = getDirectoryEntry().getOwner();
        this.directoryController.transfer(owner, 8, new RecallMessage(this.directoryController, producerFlow, tag, producerFlow.getAccess()));
    }

    public void sendRecallToSharers(CacheCoherenceFlow producerFlow, int tag) {
        for (final CacheController sharer : this.getDirectoryEntry().getSharers()) {
            this.directoryController.transfer(sharer, 8, new RecallMessage(this.directoryController, producerFlow, tag, producerFlow.getAccess()));
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