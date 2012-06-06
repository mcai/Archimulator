package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.CacheSimulator;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheLineReplacementEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.event.cache.*;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import archimulator.util.ValueProvider;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.fsm.BasicFiniteStateMachine;
import net.pickapack.fsm.event.EnterStateEvent;
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

//    private List<String> transitionHistory = new ArrayList<String>(10);

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

        this.addListener(EnterStateEvent.class, new Action1<EnterStateEvent>() {
            @Override
            public void apply(EnterStateEvent enterStateEvent) {
                CacheLine<CacheControllerState> line = cacheController.getCache().getLine(getSet(), getWay());

//                if (getState() != previousState) {
                    String transitionText = String.format("[%d] %s.[%d,%d] {%s} %s: %s.%s -> %s", cacheController.getCycleAccurateEventQueue().getCurrentCycle(), getName(), getSet(), getWay(), line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A", previousState, enterStateEvent.getSender() != null ? enterStateEvent.getSender() : "<N/A>", enterStateEvent.getCondition(), getState());
//                    if (transitionHistory.size() >= 10) {
//                        transitionHistory.remove(0);
//                    }
//
//                    transitionHistory.add(transitionText);

                    if (CacheSimulator.logEnabled) {
                        CacheSimulator.pw.println(transitionText);
                        CacheSimulator.pw.flush();
                    }
//                }
            }
        });
    }

    public void onEventLoad(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        LoadEvent loadEvent = new LoadEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), loadEvent);
    }

    public void onEventStore(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        StoreEvent storeEvent = new StoreEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), storeEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        ReplacementEvent replacementEvent = new ReplacementEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition("<core>" + "." + String.format("0x%08x", tag), replacementEvent);
    }

    public void onEventFwdGetS(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        FwdGetSEvent fwdGetSEvent = new FwdGetSEvent(cacheController, producerFlow, req, tag, producerFlow.getAccess());
        this.fireTransition(req + "." + String.format("0x%08x", tag), fwdGetSEvent);
    }

    public void onEventFwdGetM(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        FwdGetMEvent fwdGetMEvent = new FwdGetMEvent(cacheController, producerFlow, req, tag, producerFlow.getAccess());
        this.fireTransition(req + "." + String.format("0x%08x", tag), fwdGetMEvent);
    }

    public void onEventInv(CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        InvEvent invEvent = new InvEvent(cacheController, producerFlow, req, tag, producerFlow.getAccess());
        this.fireTransition(req + "." + String.format("0x%08x", tag), invEvent);
    }

    public void onEventRecall(CacheCoherenceFlow producerFlow, int tag) {
        RecallEvent recallEvent = new RecallEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition("<dir>" + "." + String.format("0x%08x", tag), recallEvent);
    }

    public void onEventPutAck(CacheCoherenceFlow producerFlow, int tag) {
        PutAckEvent putAckEvent = new PutAckEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition(cacheController.getDirectoryController() + "." + String.format("0x%08x", tag), putAckEvent);
    }

    public void onEventData(CacheCoherenceFlow producerFlow, Controller sender, int tag, int numAcks) {
        this.numInvAcks += numAcks;

        if (sender instanceof DirectoryController) {
            if (numAcks == 0) {
                DataFromDirAckEq0Event dataFromDirAckEq0Event = new DataFromDirAckEq0Event(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
                this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromDirAckEq0Event);
            } else {
                DataFromDirAckGt0Event dataFromDirAckGt0Event = new DataFromDirAckGt0Event(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
                this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromDirAckGt0Event);

                if (this.numInvAcks == 0) {
                    onEventLastInvAck(producerFlow, tag);
                }
            }
        } else {
            DataFromOwnerEvent dataFromOwnerEvent = new DataFromOwnerEvent(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
            this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromOwnerEvent);
        }
    }

    public void onEventInvAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        InvAckEvent invAckEvent = new InvAckEvent(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), invAckEvent);

        if (this.numInvAcks == 0) {
            onEventLastInvAck(producerFlow, tag);
        }
    }

    private void onEventLastInvAck(CacheCoherenceFlow producerFlow, int tag) {
        LastInvAckEvent lastInvAckEvent = new LastInvAckEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition("<N/A>" + "." + String.format("0x%08x", tag), lastInvAckEvent);

        this.numInvAcks = 0;
    }

    public void fireTransition(Object sender, CacheControllerEvent event) {
        event.onCompleted();
        cacheController.getFsmFactory().fireTransition(this, sender, event.getType(), event);
    }

    public void sendGetSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetSMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendGetMToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetMMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendPutSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new PutSMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendPutMAndDataToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new PutMAndDataMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendDataToReqAndDir(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, 10, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
    }

    public void sendDataToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
    }

    public void sendInvAckToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, 8, new InvAckMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendRecallAckToDir(CacheCoherenceFlow producerFlow, final int tag, int size) {
        cacheController.transfer(cacheController.getDirectoryController(), size, new RecallAckMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void decrementInvAck() {
        this.numInvAcks--;
    }

    public void hit(MemoryHierarchyAccess access, int tag, int set, int way) {
        this.cacheController.getCache().getLine(set, way).getCacheAccess().commit();
        this.fireServiceNonblockingRequestEvent(access, tag);
    }

    public void stall(final Object sender, final CacheControllerEvent event) {
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
        CacheAccess<CacheControllerState> cacheAccess = this.getLine().getCacheAccess();
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCacheController(), access, tag, cacheAccess.getLine(), cacheAccess.isHitInCache(), cacheAccess.isEviction(), cacheAccess.getReference().getAccessType()));
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(this.getCacheController(), access, cacheAccess));
        this.getCacheController().updateStats(cacheAccess);
    }

    public void fireReplacementEvent(MemoryHierarchyAccess access, int tag) {
        CacheAccess<CacheControllerState> cacheAccess = this.getLine().getCacheAccess();
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheLineReplacementEvent(this.getCacheController(), access, tag, cacheAccess.getLine(), cacheAccess.isHitInCache(), cacheAccess.isEviction(), cacheAccess.getReference().getAccessType()));
    }

    public void fireNonblockingRequestHitToTransientTagEvent(MemoryHierarchyAccess access, int tag) {
        CacheAccess<CacheControllerState> cacheAccess = this.getLine().getCacheAccess();
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(this.getCacheController(), access, tag, cacheAccess.getLine()));
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

    public CacheController getCacheController() {
        return cacheController;
    }

    public List<Action> getStalledEvents() {
        return stalledEvents;
    }

    public CacheControllerState getPreviousState() {
        return previousState;
    }
}
