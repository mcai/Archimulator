/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.coherence.msi.fsm;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.CacheAccess;
import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.coherence.event.*;
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.uncore.coherence.msi.controller.DirectoryController;
import archimulator.uncore.coherence.msi.controller.DirectoryEntry;
import archimulator.uncore.coherence.msi.event.directory.*;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.uncore.coherence.msi.message.*;
import archimulator.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.ValueProvider;
import archimulator.util.fsm.BasicFiniteStateMachine;
import archimulator.util.fsm.event.ExitStateEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Directory controller finite state machine.
 *
 * @author Min Cai
 */
public class DirectoryControllerFiniteStateMachine extends BasicFiniteStateMachine<DirectoryControllerState, DirectoryControllerEventType> implements ValueProvider<DirectoryControllerState> {
    private DirectoryController directoryController;
    private DirectoryEntry directoryEntry;
    private DirectoryControllerState previousState;
    private int set;
    private int way;

    private int numRecallAcks;

    private List<Runnable> stalledEvents;

    private Runnable onCompletedCallback;

    private int evicterTag;

    private int victimTag;

    /**
     * Create a directory controller finite state machine.
     *
     * @param name                the name
     * @param set                 the set index
     * @param way                 the way
     * @param directoryController the directory controller
     */
    public DirectoryControllerFiniteStateMachine(String name, int set, int way, final DirectoryController directoryController) {
        super(name, DirectoryControllerState.I);
        this.set = set;
        this.way = way;
        this.directoryController = directoryController;
        this.directoryEntry = new DirectoryEntry();
        this.stalledEvents = new ArrayList<>();
        this.evicterTag = CacheLine.INVALID_TAG;
        this.victimTag = CacheLine.INVALID_TAG;

        this.addListener(ExitStateEvent.class, exitStateEvent -> previousState = getState());
    }

    /**
     * Act on a "GetS" event.
     *
     * @param producerFlow      the producer cache coherence flow
     * @param requester         the requester L1 cache controller
     * @param tag               the tag
     * @param onStalledCallback the callback action performed when the event is stalled
     */
    public void onEventGetS(CacheCoherenceFlow producerFlow, CacheController requester, int tag, Runnable onStalledCallback) {
        GetSEvent getSEvent = new GetSEvent(this.directoryController, producerFlow, requester, tag, set, way, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), getSEvent);
    }

    /**
     * Act on a "GetM" event.
     *
     * @param producerFlow      the producer cache coherence flow
     * @param requester         the requester L1 cache controller
     * @param tag               the tag
     * @param onStalledCallback the callback action performed when the event is stalled
     */
    public void onEventGetM(CacheCoherenceFlow producerFlow, CacheController requester, int tag, Runnable onStalledCallback) {
        GetMEvent getMEvent = new GetMEvent(this.directoryController, producerFlow, requester, tag, set, way, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), getMEvent);
    }

    /**
     * Act on a "replacement" event.
     *
     * @param producerFlow        the producer cache coherence flow
     * @param requester           the requester L1 cache controller
     * @param tag                 the tag
     * @param cacheAccess         the cache access
     * @param onCompletedCallback the callback action performed when the replacement is completed
     * @param onStalledCallback   the callback action performed when the replacement is stalled
     */
    public void onEventReplacement(CacheCoherenceFlow producerFlow, CacheController requester, int tag, CacheAccess<DirectoryControllerState> cacheAccess, Runnable onCompletedCallback, Runnable onStalledCallback) {
        ReplacementEvent replacementEvent = new ReplacementEvent(this.directoryController, producerFlow, tag, cacheAccess, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), replacementEvent);
    }

    /**
     * Act on a "recall acknowledgement" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender L1 cache controller
     * @param tag          the tag
     */
    public void onEventRecallAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        RecallAckEvent recallAckEvent = new RecallAckEvent(this.directoryController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), recallAckEvent);

        if (this.numRecallAcks == 0) {
            LastRecallAckEvent lastRecallAckEvent = new LastRecallAckEvent(this.directoryController, producerFlow, tag, producerFlow.getAccess());
            this.fireTransition(sender + "." + String.format("0x%08x", tag), lastRecallAckEvent);
        }
    }

    /**
     * Act on a "PutS" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void onEventPutS(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        if (this.getDirectoryEntry().getSharers().size() > 1) {
            PutSNotLastEvent putSNotLastEvent = new PutSNotLastEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putSNotLastEvent);
        } else {
            PutSLastEvent putSLastEvent = new PutSLastEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putSLastEvent);
        }
    }

    /**
     * Act on a "PutM and data" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void onEventPutMAndData(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        if (requester == this.getDirectoryEntry().getOwner()) {
            PutMAndDataFromOwnerEvent putMAndDataFromOwnerEvent = new PutMAndDataFromOwnerEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putMAndDataFromOwnerEvent);
        } else {
            PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = new PutMAndDataFromNonOwnerEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putMAndDataFromNonOwnerEvent);
        }
    }

    /**
     * Act on a "data" event.
     *
     * @param producerFlow the producer cache coherence flow
     * @param sender       the sender L1 cache controller
     * @param tag          the tag
     */
    public void onEventData(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        DataEvent dataEvent = new DataEvent(this.directoryController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), dataEvent);
    }

    /**
     * Fire the predefined action based on the specified directory controller event.
     *
     * @param sender the sender object
     * @param event  the directory controller event
     */
    public void fireTransition(Object sender, DirectoryControllerEvent event) {
        event.onCompleted();
        this.directoryController.getFsmFactory().fireTransition(this, sender, event.getType(), event);
    }

    /**
     * Act on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     * @param set    the set
     * @param way    the way
     */
    public void hit(MemoryHierarchyAccess access, int tag, int set, int way) {
        this.fireServiceNonblockingRequestEvent(access, tag, true);
        this.directoryController.getCache().getReplacementPolicy().handlePromotionOnHit(access, set, way);
        this.getLine().setAccess(access);
    }

    /**
     * Act on a stall.
     *
     * @param sender the sender object
     * @param event  the directory controller event
     */
    public void stall(final Object sender, final DirectoryControllerEvent event) {
        stall(() -> fireTransition(sender, event));
    }

    /**
     * Act on a stall.
     *
     * @param action the callback action performed when the stall is awaken
     */
    public void stall(Runnable action) {
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
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerServiceNonblockingRequestEvent(this.getDirectoryController(), access, tag, getSet(), getWay(), hitInCache));
        this.getDirectoryController().updateStats(access.getType().isRead(), hitInCache);
    }

    /**
     * Fire a "cache line inserted" event.
     *
     * @param access    the memory hierarchy access
     * @param tag       the tag
     * @param victimTag the victim tag
     */
    public void fireCacheLineInsertEvent(MemoryHierarchyAccess access, int tag, int victimTag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new LastLevelCacheControllerLineInsertEvent(this.getDirectoryController(), access, tag, getSet(), getWay(), victimTag));
    }

    /**
     * Fire a "replacement" event.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     */
    public void fireReplacementEvent(MemoryHierarchyAccess access, int tag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerLineReplacementEvent(this.getDirectoryController(), access, tag, getSet(), getWay()));
    }

    /**
     * Fire a "PutS or PutM and data from the owner" event.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     */
    public void firePutSOrPutMAndDataFromOwnerEvent(MemoryHierarchyAccess access, int tag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent(this.getDirectoryController(), access, tag, getSet(), getWay()));
    }

    /**
     * Fire a "non-blocking request hit to transient tag" event.
     *
     * @param access the memory hierarchy access
     * @param tag    the tag
     */
    public void fireNonblockingRequestHitToTransientTagEvent(MemoryHierarchyAccess access, int tag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new GeneralCacheControllerNonblockingRequestHitToTransientTagEvent(this.getDirectoryController(), access, tag, getSet(), getWay()));
    }

    /**
     * Send a "data" message to the requester L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     * @param numInvalidationAcknowledgements
     *                     the number of pending invalidation acknowledgements expected
     */
    public void sendDataToRequester(CacheCoherenceFlow producerFlow, CacheController requester, int tag, int numInvalidationAcknowledgements) {
        this.directoryController.transfer(requester, this.directoryController.getCache().getLineSize() + 8, new DataMessage(this.directoryController, producerFlow, this.directoryController, tag, numInvalidationAcknowledgements, producerFlow.getAccess()));
    }

    /**
     * Send a "Put acknowledgement" message to the requester L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendPutAckToReq(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        sendPutAckToReq(producerFlow, this.directoryController, requester, tag);
    }

    /**
     * Send a "Put acknowledgement" message to the requester L1 cache controller.
     *
     * @param producerFlow        the producer cache coherence flow
     * @param directoryController the directory controller
     * @param requester           the requester L1 cache controller
     * @param tag                 the tag
     */
    public static void sendPutAckToReq(CacheCoherenceFlow producerFlow, DirectoryController directoryController, CacheController requester, int tag) {
        directoryController.transfer(requester, 8, new PutAckMessage(directoryController, producerFlow, tag, producerFlow.getAccess()));
    }

    /**
     * Send the data to the memory controller.
     *
     * @param tag the tag
     */
    public void copyDataToMem(int tag) {
        this.directoryController.getNext().memWriteRequestReceive(this.directoryController, tag, () -> {
        });
    }

    /**
     * Send a "forwarded GetS" message to the owner L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendFwdGetSToOwner(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        this.directoryController.transfer(getDirectoryEntry().getOwner(), 8, new FwdGetSMessage(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess()));
    }

    /**
     * Send a "forwarded GetM" message to the owner L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendFwdGetMToOwner(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        this.directoryController.transfer(getDirectoryEntry().getOwner(), 8, new FwdGetMMessage(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess()));
    }

    /**
     * Send "invalidation" messages to the L1 sharers.
     *
     * @param producerFlow the producer cache coherence flow
     * @param requester    the requester L1 cache controller
     * @param tag          the tag
     */
    public void sendInvToSharers(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        this.getDirectoryEntry().getSharers().stream().filter(sharer -> requester != sharer).forEach(sharer -> this.directoryController.transfer(sharer, 8, new InvMessage(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess())));
    }

    /**
     * Send a "recall" message to the owner L1 cache controller.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void sendRecallToOwner(CacheCoherenceFlow producerFlow, int tag) {
        CacheController owner = getDirectoryEntry().getOwner();
        if (owner.getCache().findWay(tag) == -1) {
            throw new IllegalArgumentException();
        }

        this.directoryController.transfer(owner, 8, new RecallMessage(this.directoryController, producerFlow, tag, producerFlow.getAccess()));
    }

    /**
     * Send "recall" messages to the L1 sharers.
     *
     * @param producerFlow the producer cache coherence flow
     * @param tag          the tag
     */
    public void sendRecallToSharers(CacheCoherenceFlow producerFlow, int tag) {
        for (CacheController sharer : this.getDirectoryEntry().getSharers()) {
            if (sharer.getCache().findWay(tag) == -1) {
                throw new IllegalArgumentException();
            }

            this.directoryController.transfer(sharer, 8, new RecallMessage(this.directoryController, producerFlow, tag, producerFlow.getAccess()));
        }
    }

    /**
     * Set the number of recall acknowledgements.
     *
     * @param numRecallAcks the number of recall acknowledgements
     */
    public void setNumRecallAcks(int numRecallAcks) {
        this.numRecallAcks = numRecallAcks;
    }

    /**
     * Decrement the number of recall acknowledgements.
     */
    public void decrementRecallAcks() {
        this.numRecallAcks--;
    }

    /**
     * Add the requester L1 cache controller and the owner L1 cache controller to the list of sharers.
     *
     * @param requester the requester L1 cache controller
     */
    public void addRequesterAndOwnerToSharers(CacheController requester) {
        if (this.getDirectoryEntry().getSharers().contains(requester) || this.getDirectoryEntry().getSharers().contains(this.getDirectoryEntry().getOwner())) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().add(requester);
        this.getDirectoryEntry().getSharers().add(this.getDirectoryEntry().getOwner());
    }

    /**
     * Add the requester L1 cache controller to the list of sharers.
     *
     * @param requester the requester L1 cache controller
     */
    public void addRequesterToSharers(CacheController requester) {
        if (this.getDirectoryEntry().getSharers().contains(requester)) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().add(requester);
    }

    /**
     * Remove the requester L1 cache controller from the list of sharers.
     *
     * @param requester the requester L1 cache controller
     */
    public void removeRequesterFromSharers(CacheController requester) {
        if (!this.getDirectoryEntry().getSharers().contains(requester)) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().remove(requester);
    }

    /**
     * Set the owner to the requester L1 cache controller.
     *
     * @param requester the requester L1 cache controller
     */
    public void setOwnerToRequester(CacheController requester) {
        this.getDirectoryEntry().setOwner(requester);
    }

    /**
     * Clear the list of sharers.
     */
    public void clearSharers() {
        this.getDirectoryEntry().getSharers().clear();
    }

    /**
     * Clear the owner L1 cache controller.
     */
    public void clearOwner() {
        this.getDirectoryEntry().setOwner(null);
    }

    /**
     * Get the state of the owning directory controller.
     *
     * @return the state of the owning directory controller
     */
    @Override
    public DirectoryControllerState get() {
        return this.getState();
    }

    /**
     * Get the initial state of the owning directory controller.
     *
     * @return the initial state of the owning directory controller
     */
    @Override
    public DirectoryControllerState getInitialValue() {
        return DirectoryControllerState.I;
    }

    /**
     * Get the directory entry.
     *
     * @return the directory entry
     */
    public DirectoryEntry getDirectoryEntry() {
        return directoryEntry;
    }

    /**
     * Get the line in the owning directory controller.
     *
     * @return the line in the owning directory controller
     */
    public CacheLine<DirectoryControllerState> getLine() {
        return this.directoryController.getCache().getLine(this.getSet(), this.getWay());
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
     * Get the owning directory controller.
     *
     * @return the owning directory controller
     */
    public DirectoryController getDirectoryController() {
        return directoryController;
    }

    /**
     * Get the evicter tag.
     *
     * @return the evicter tag
     */
    public int getEvicterTag() {
        return evicterTag;
    }

    /**
     * Set the evicter tag.
     *
     * @param evicterTag the evicter tag
     */
    public void setEvicterTag(int evicterTag) {
        this.evicterTag = evicterTag;
    }

    /**
     * Set the victim tag.
     *
     * @return the victim tag
     */
    public int getVictimTag() {
        return victimTag;
    }

    /**
     * Set the victim tag.
     *
     * @param victimTag the victim tag
     */
    public void setVictimTag(int victimTag) {
        this.victimTag = victimTag;
    }

    /**
     * Get the previous state of the line in the owning directory controller.
     *
     * @return the previous state of the line in the owning directory controller
     */
    public DirectoryControllerState getPreviousState() {
        return previousState;
    }

    /**
     * Get the list of stalled events.
     *
     * @return the list of stalled events
     */
    public List<Runnable> getStalledEvents() {
        return stalledEvents;
    }

    /**
     * Get the callback action performed when the pending event is completed.
     *
     * @return the callback action performed when the pending event is completed
     */
    public Runnable getOnCompletedCallback() {
        return onCompletedCallback;
    }

    /**
     * Set the callback action performed when the pending event is completed.
     *
     * @param onCompletedCallback the callback action performed when the pending event is completed
     */
    public void setOnCompletedCallback(Runnable onCompletedCallback) {
        if (this.onCompletedCallback != null && onCompletedCallback != null) {
            throw new IllegalArgumentException();
        }

        this.onCompletedCallback = onCompletedCallback;
    }
}