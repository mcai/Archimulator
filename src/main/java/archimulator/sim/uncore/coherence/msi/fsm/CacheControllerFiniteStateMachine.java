/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.uncore.coherence.msi.fsm;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.event.CoherentCacheLineReplacementEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.event.cache.*;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.flow.LoadFlow;
import archimulator.sim.uncore.coherence.msi.flow.StoreFlow;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import net.pickapack.util.ValueProvider;
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

    private int numInvalidationAcknowledgements;

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

    public void onEventLoad(LoadFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        LoadEvent loadEvent = new LoadEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(producerFlow.getAccess().getThread().getName() + "." + String.format("0x%08x", tag), loadEvent);
    }

    public void onEventStore(StoreFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        StoreEvent storeEvent = new StoreEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(producerFlow.getAccess().getThread().getName() + "." + String.format("0x%08x", tag), storeEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, int tag, CacheAccess<CacheControllerState> cacheAccess, Action onCompletedCallback, Action onStalledCallback) {
        ReplacementEvent replacementEvent = new ReplacementEvent(cacheController, producerFlow, tag, cacheAccess, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(producerFlow.getAccess().getThread().getName() + "." + String.format("0x%08x", tag), replacementEvent);
    }

    public void onEventForwardGetS(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        ForwardGetSEvent forwardGetSEvent = new ForwardGetSEvent(cacheController, producerFlow, requester, tag, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), forwardGetSEvent);
    }

    public void onEventForwardGetM(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        ForwardGetMEvent forwardGetMEvent = new ForwardGetMEvent(cacheController, producerFlow, requester, tag, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), forwardGetMEvent);
    }

    public void onEventInvalidation(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        InvalidationEvent invalidationEvent = new InvalidationEvent(cacheController, producerFlow, requester, tag, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), invalidationEvent);
    }

    public void onEventRecall(CacheCoherenceFlow producerFlow, int tag) {
        RecallEvent recallEvent = new RecallEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition("<dir>" + "." + String.format("0x%08x", tag), recallEvent);
    }

    public void onEventPutAcknowledgement(CacheCoherenceFlow producerFlow, int tag) {
        PutAcknowledgementEvent putAcknowledgementEvent = new PutAcknowledgementEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition(cacheController.getDirectoryController() + "." + String.format("0x%08x", tag), putAcknowledgementEvent);
    }

    public void onEventData(CacheCoherenceFlow producerFlow, Controller sender, int tag, int numInvalidationAcknowledgements) {
        this.numInvalidationAcknowledgements += numInvalidationAcknowledgements;

        if (sender instanceof DirectoryController) {
            if (numInvalidationAcknowledgements == 0) {
                DataFromDirectoryAcknowledgementsEqual0Event dataFromDirectoryAcknowledgementsEqual0Event = new DataFromDirectoryAcknowledgementsEqual0Event(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
                this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromDirectoryAcknowledgementsEqual0Event);
            } else {
                DataFromDirectoryAcknowledgementsGreaterThan0Event dataFromDirectoryAcknowledgementsGreaterThan0Event = new DataFromDirectoryAcknowledgementsGreaterThan0Event(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
                this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromDirectoryAcknowledgementsGreaterThan0Event);

                if (this.numInvalidationAcknowledgements == 0) {
                    onEventLastInvalidationAcknowledgement(producerFlow, tag);
                }
            }
        } else {
            DataFromOwnerEvent dataFromOwnerEvent = new DataFromOwnerEvent(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
            this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromOwnerEvent);
        }
    }

    public void onEventInvalidationAcknowledgement(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        InvalidationAcknowledgementEvent invalidationAcknowledgementEvent = new InvalidationAcknowledgementEvent(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), invalidationAcknowledgementEvent);

        if (this.numInvalidationAcknowledgements == 0) {
            onEventLastInvalidationAcknowledgement(producerFlow, tag);
        }
    }

    private void onEventLastInvalidationAcknowledgement(CacheCoherenceFlow producerFlow, int tag) {
        LastInvalidationAcknowledgementEvent lastInvalidationAcknowledgementEvent = new LastInvalidationAcknowledgementEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition("<N/A>" + "." + String.format("0x%08x", tag), lastInvalidationAcknowledgementEvent);

        this.numInvalidationAcknowledgements = 0;
    }

    public void fireTransition(Object sender, CacheControllerEvent event) {
        event.onCompleted();
        cacheController.getFsmFactory().fireTransition(this, sender, event.getType(), event);
    }

    public void sendGetSToDirectory(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetSMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendGetMToDirectory(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetMMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendPutSToDirectory(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new PutSMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendPutMAndDataToDirectory(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new PutMAndDataMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendDataToRequesterAndDirectory(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        cacheController.transfer(requester, 10, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
    }

    public void sendDataToRequester(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        cacheController.transfer(requester, cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
    }

    public void sendInvalidationAcknowledgementToRequester(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        cacheController.transfer(requester, 8, new InvalidationAcknowledgementMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void sendRecallAcknowledgementToDirectory(CacheCoherenceFlow producerFlow, int tag, int size) {
        cacheController.transfer(cacheController.getDirectoryController(), size, new RecallAcknowledgementMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    public void decrementInvalidationAcknowledgements() {
        this.numInvalidationAcknowledgements--;
    }

    public void hit(MemoryHierarchyAccess access, int tag, int set, int way) {
        this.cacheController.getCache().getReplacementPolicy().handlePromotionOnHit(set, way);
        this.fireServiceNonblockingRequestEvent(access, tag, true);
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

    public void fireServiceNonblockingRequestEvent(MemoryHierarchyAccess access, int tag, boolean hitInCache) {
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCacheController(), access, tag, getSet(), getWay(), hitInCache));
        this.getCacheController().updateStats(this.getCacheController().getCache(), access.getType().isRead(), hitInCache);
    }

    public void fireReplacementEvent(MemoryHierarchyAccess access, int tag) {
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheLineReplacementEvent(this.getCacheController(), access, tag, getSet(), getWay()));
    }

    public void fireNonblockingRequestHitToTransientTagEvent(MemoryHierarchyAccess access, int tag) {
        this.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(this.getCacheController(), access, tag, getSet(), getWay()));
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
