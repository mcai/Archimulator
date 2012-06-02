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
import net.pickapack.fsm.BasicFiniteStateMachine;
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

    public void fireTransition(Object sender, Params params, CacheControllerEvent event) {
        event.onCompleted();
        cacheController.getFsmFactory().fireTransition(this, sender, event.getType(), params);
    }

    public void sendGetSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetSMessage(cacheController, producerFlow, cacheController, tag));
    }

    public void sendGetMToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetMMessage(cacheController, producerFlow, cacheController, tag));
    }

    public void sendPutSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new PutSMessage(cacheController, producerFlow, cacheController, tag));
    }

    public void sendPutMAndDataToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new PutMAndDataMessage(cacheController, producerFlow, cacheController, tag));
    }

    public void sendDataToReqAndDir(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, 10, new DataMessage(cacheController, producerFlow, cacheController, tag, 0));
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0));
    }

    public void sendDataToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0));
    }

    public void sendInvAckToReq(CacheCoherenceFlow producerFlow, final CacheController req, final int tag) {
        cacheController.transfer(req, 8, new InvAckMessage(cacheController, producerFlow, cacheController, tag));
    }

    public void sendRecallAckToDir(CacheCoherenceFlow producerFlow, final int tag, int size) {
        cacheController.transfer(cacheController.getDirectoryController(), size, new RecallAckMessage(cacheController, producerFlow, cacheController, tag));
    }

    public void decrementInvAck() {
        this.numInvAcks--;
    }

    public void hit(int set, int way) {
        this.cacheController.getCache().handlePromotionOnHit(set, way);
    }

    public void stall(final Object sender, final Params params, final CacheControllerEvent event) {
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
