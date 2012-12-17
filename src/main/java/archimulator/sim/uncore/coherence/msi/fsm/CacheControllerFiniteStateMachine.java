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
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
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

/**
 * L1 cache controller finite state machine.
 *
 * @author Min Cai
 */
public class CacheControllerFiniteStateMachine extends BasicFiniteStateMachine<CacheControllerState, CacheControllerEventType> implements ValueProvider<CacheControllerState> {
    private CacheController cacheController;
    private CacheControllerState previousState;
    private int set;
    private int way;

    private int numInvAcks;

    private List<Action> stalledEvents = new ArrayList<Action>();

    private Action onCompletedCallback;

    /**
     * Create an L1 cache controller finite state machine.
     *
     * @param name            the name
     * @param set             the set index
     * @param way             the way
     * @param cacheController the L1 cache controller
     */
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

    /**
     * Act on a "load" event.
     *
     * @param producerFlow        the producer cache coherence flow
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the event is completed
     * @param onStalledCallback   the callback action performed when the event is stalled
     */
    public void onEventLoad(LoadFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        LoadEvent loadEvent = new LoadEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(producerFlow.getAccess().getThread().getName() + "." + String.format("0x%08x", tag), loadEvent);
    }

    /**
     * Act on a "store" event.
     *
     * @param producerFlow        the producer cache coherence flow
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the event is completed
     * @param onStalledCallback   the callback action performed when the event is stalled
     */
    public void onEventStore(StoreFlow producerFlow, int tag, Action onCompletedCallback, Action onStalledCallback) {
        StoreEvent storeEvent = new StoreEvent(cacheController, producerFlow, tag, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(producerFlow.getAccess().getThread().getName() + "." + String.format("0x%08x", tag), storeEvent);
    }

    /**
     * Act on a "replacement" event.
     *
     * @param producerFlow        the producer cache coherence flow
     * @param tag                 the tag
     * @param cacheAccess         the cache access
     * @param onCompletedCallback the callback action performed when the replacement is completed
     * @param onStalledCallback   the callback action performed when the replacement is stalled
     */
    public void onEventReplacement(CacheCoherenceFlow producerFlow, int tag, CacheAccess<CacheControllerState> cacheAccess, Action onCompletedCallback, Action onStalledCallback) {
        ReplacementEvent replacementEvent = new ReplacementEvent(cacheController, producerFlow, tag, cacheAccess, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(producerFlow.getAccess().getThread().getName() + "." + String.format("0x%08x", tag), replacementEvent);
    }

    /**
     * Act on a "forwarded GetS" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void onEventFwdGetS(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        FwdGetSEvent fwdGetSEvent = new FwdGetSEvent(cacheController, producerFlow, requester, tag, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), fwdGetSEvent);
    }

    /**
     * Act on a "forwarded GetM" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void onEventFwdGetM(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        FwdGetMEvent fwdGetMEvent = new FwdGetMEvent(cacheController, producerFlow, requester, tag, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), fwdGetMEvent);
    }

    /**
     * Act on an "invalidation" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void onEventInv(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        InvEvent invEvent = new InvEvent(cacheController, producerFlow, requester, tag, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), invEvent);
    }

    /**
     * Act on a "recall" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void onEventRecall(CacheCoherenceFlow producerFlow, int tag) {
        RecallEvent recallEvent = new RecallEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition("<dir>" + "." + String.format("0x%08x", tag), recallEvent);
    }

    /**
     * Act on a "put acknowledgement" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void onEventPutAck(CacheCoherenceFlow producerFlow, int tag) {
        PutAckEvent putAckEvent = new PutAckEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition(cacheController.getDirectoryController() + "." + String.format("0x%08x", tag), putAckEvent);
    }

    /**
     * Act on a "data" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender controller
     * @param tag          the tag
     * @param numInvalidationAcknowledgements
     *                     the number of pending invalidation acknowledgements expected
     */
    public void onEventData(CacheCoherenceFlow producerFlow, Controller sender, int tag, int numInvalidationAcknowledgements) {
        this.numInvAcks += numInvalidationAcknowledgements;

        if (sender instanceof DirectoryController) {
            if (numInvalidationAcknowledgements == 0) {
                DataFromDirAcksEq0Event dataFromDirAcksEq0Event = new DataFromDirAcksEq0Event(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
                this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromDirAcksEq0Event);
            } else {
                DataFromDirAcksGt0Event dataFromDirAcksGt0Event = new DataFromDirAcksGt0Event(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
                this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromDirAcksGt0Event);

                if (this.numInvAcks == 0) {
                    onEventLastInvAck(producerFlow, tag);
                }
            }
        } else {
            DataFromOwnerEvent dataFromOwnerEvent = new DataFromOwnerEvent(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
            this.fireTransition(sender + "." + String.format("0x%08x", tag), dataFromOwnerEvent);
        }
    }

    /**
     * Act on an "invalidation acknowledgement" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender L1 cache controller
     * @param tag          the tag
     */
    public void onEventInvAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        InvAckEvent invAckEvent = new InvAckEvent(cacheController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), invAckEvent);

        if (this.numInvAcks == 0) {
            onEventLastInvAck(producerFlow, tag);
        }
    }

    /**
     * Act on a "last invalidation acknowledgement" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    private void onEventLastInvAck(CacheCoherenceFlow producerFlow, int tag) {
        LastInvAckEvent lastInvAckEvent = new LastInvAckEvent(cacheController, producerFlow, tag, producerFlow.getAccess());
        this.fireTransition("<N/A>" + "." + String.format("0x%08x", tag), lastInvAckEvent);

        this.numInvAcks = 0;
    }

    /**
     * Fire the predefined transition based on the specified sender and event.
     *
     * @param sender the sender
     * @param event  the event
     */
    public void fireTransition(Object sender, CacheControllerEvent event) {
        event.onCompleted();
        cacheController.getFsmFactory().fireTransition(this, sender, event.getType(), event);
    }

    /**
     * Send a "GetS" message to the directory controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void sendGetSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetSMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    /**
     * Send a "GetM" message to the directory controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void sendGetMToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new GetMMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    /**
     * Send a "PutS" message to the directory controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void sendPutSToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), 8, new PutSMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    /**
     * Send a "PutM and data" message to the directory controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void sendPutMAndDataToDir(CacheCoherenceFlow producerFlow, int tag) {
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new PutMAndDataMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    /**
     * Send a "data" message to the requester L1 cache controller and the directory controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendDataToRequesterAndDir(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        cacheController.transfer(requester, 10, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
        cacheController.transfer(cacheController.getDirectoryController(), cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
    }

    /**
     * Send a "data" message to the requester L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendDataToRequester(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        cacheController.transfer(requester, cacheController.getCache().getLineSize() + 8, new DataMessage(cacheController, producerFlow, cacheController, tag, 0, producerFlow.getAccess()));
    }

    /**
     * Send an "invalidation acknowledgement" message to the requester L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendInvAckToRequester(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        cacheController.transfer(requester, 8, new InvAckMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    /**
     * Send a "recall acknowledgement" message to the directory controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     * @param size         the size of the message
     */
    public void sendRecallAckToDir(CacheCoherenceFlow producerFlow, int tag, int size) {
        cacheController.transfer(cacheController.getDirectoryController(), size, new RecallAckMessage(cacheController, producerFlow, cacheController, tag, producerFlow.getAccess()));
    }

    /**
     * Decrement the number of invalidation acknowledgements.
     */
    public void decrementInvAcks() {
        this.numInvAcks--;
    }

    /**
     * Act on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     * @param set    the set index
     * @param way    the way
     */
    public void hit(MemoryHierarchyAccess access, int tag, int set, int way) {
        this.cacheController.getCache().getReplacementPolicy().handlePromotionOnHit(access, set, way);
        this.fireServiceNonblockingRequestEvent(access, tag, true);
    }

    /**
     * Act on a stall.
     *
     * @param sender the sender object
     * @param event  the L1 cache controller event
     */
    public void stall(final Object sender, final CacheControllerEvent event) {
        Action action = new Action() {
            @Override
            public void apply() {
                fireTransition(sender, event);
            }
        };
        stall(action);
    }

    /**
     * Act on a stall.
     *
     * @param action the callback action performed when the stall is awaken
     */
    public void stall(Action action) {
        stalledEvents.add(action);
    }

    /**
     * Fire a "service non-blocking request" event.
     *
     * @param access     the memory hierarchy access
     * @param tag        the tag
     * @param hitInCache a value indicating whether the access hits in the cache or not
     */
    public void fireServiceNonblockingRequestEvent(MemoryHierarchyAccess access, int tag, boolean hitInCache) {
        this.getCacheController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerServiceNonblockingRequestEvent(this.getCacheController(), access, tag, getSet(), getWay(), hitInCache));
        this.getCacheController().updateStats(this.getCacheController().getCache(), access.getType().isRead(), hitInCache);
    }

    /**
     * Fire a "replacement" event.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     */
    public void fireReplacementEvent(MemoryHierarchyAccess access, int tag) {
        this.getCacheController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerLineReplacementEvent(this.getCacheController(), access, tag, getSet(), getWay()));
    }

    /**
     * Fire a "nonblocking request hit to transient tag" event.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     */
    public void fireNonblockingRequestHitToTransientTagEvent(MemoryHierarchyAccess access, int tag) {
        this.getCacheController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerNonblockingRequestHitToTransientTagEvent(this.getCacheController(), access, tag, getSet(), getWay()));
    }

    /**
     * Get the state of the owning L1 cache controller.
     *
     * @return the state of the owning L1 cache controller
     */
    @Override
    public CacheControllerState get() {
        return this.getState();
    }

    /**
     * Get the initial state of the owning L1 cache controller.
     *
     * @return the initial state of the owning L1 cache controller
     */
    @Override
    public CacheControllerState getInitialValue() {
        return CacheControllerState.I;
    }

    /**
     * Get the line in the owning L1 cache controller.
     *
     * @return the line in the owning L1 cache controller
     */
    public CacheLine<CacheControllerState> getLine() {
        return this.cacheController.getCache().getLine(this.getSet(), this.getWay());
    }

    /**
     * Get the set index.
     *
     * @return the set index
     */
    public int getSet() {
        return set;
    }

    /**
     * Get the way.
     *
     * @return the way
     */
    public int getWay() {
        return way;
    }

    /**
     * Get the owning L1 cache controller.
     *
     * @return the owning L1 cache controller
     */
    public CacheController getCacheController() {
        return cacheController;
    }

    /**
     * Get the list of stalled events.
     *
     * @return the list of stalled events
     */
    public List<Action> getStalledEvents() {
        return stalledEvents;
    }

    /**
     * Get the previous state of the line in the owning L1 cache controller.
     *
     * @return the previous state of the line in the owning L1 cache controller
     */
    public CacheControllerState getPreviousState() {
        return previousState;
    }

    /**
     * Get the callback action performed when the pending event is completed.
     *
     * @return the callback action performed when the pending event is completed
     */
    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    /**
     * Set the callback action performed when the pending event is completed.
     *
     * @param onCompletedCallback the callback action performed when the pending event is completed
     */
    public void setOnCompletedCallback(Action onCompletedCallback) {
        if (this.onCompletedCallback != null && onCompletedCallback != null) {
            throw new IllegalArgumentException();
        }

        this.onCompletedCallback = onCompletedCallback;
    }
}
