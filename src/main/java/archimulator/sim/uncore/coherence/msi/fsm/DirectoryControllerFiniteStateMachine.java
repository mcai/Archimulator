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
import archimulator.sim.uncore.coherence.event.*;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryEntry;
import archimulator.sim.uncore.coherence.msi.event.directory.*;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.util.ValueProvider;
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

    private int numRecallAcknowledgements;

    private List<Action> stalledEvents;

    private Action onCompletedCallback;

    private int evicterTag;

    private int victimTag;

    public DirectoryControllerFiniteStateMachine(String name, int set, int way, final DirectoryController directoryController) {
        super(name, DirectoryControllerState.I);
        this.set = set;
        this.way = way;
        this.directoryController = directoryController;
        this.directoryEntry = new DirectoryEntry();
        this.stalledEvents = new ArrayList<Action>();
        this.evicterTag = CacheLine.INVALID_TAG;
        this.victimTag = CacheLine.INVALID_TAG;

        this.addListener(ExitStateEvent.class, new Action1<ExitStateEvent>() {
            @Override
            public void apply(ExitStateEvent exitStateEvent) {
                previousState = getState();
            }
        });
    }

    public DirectoryControllerState getPreviousState() {
        return previousState;
    }

    public List<Action> getStalledEvents() {
        return stalledEvents;
    }

    public void onEventGetS(CacheCoherenceFlow producerFlow, CacheController requester, int tag, Action onStalledCallback) {
        GetSEvent getSEvent = new GetSEvent(this.directoryController, producerFlow, requester, tag, set, way, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), getSEvent);
    }

    public void onEventGetM(CacheCoherenceFlow producerFlow, CacheController requester, int tag, Action onStalledCallback) {
        GetMEvent getMEvent = new GetMEvent(this.directoryController, producerFlow, requester, tag, set, way, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), getMEvent);
    }

    public void onEventReplacement(CacheCoherenceFlow producerFlow, CacheController requester, int tag, CacheAccess<DirectoryControllerState> cacheAccess, Action onCompletedCallback, Action onStalledCallback) {
        ReplacementEvent replacementEvent = new ReplacementEvent(this.directoryController, producerFlow, tag, cacheAccess, set, way, onCompletedCallback, onStalledCallback, producerFlow.getAccess());
        this.fireTransition(requester + "." + String.format("0x%08x", tag), replacementEvent);
    }

    public void onEventRecallAck(CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        RecallAcknowledgementEvent recallAcknowledgementEvent = new RecallAcknowledgementEvent(this.directoryController, producerFlow, sender, tag, producerFlow.getAccess());
        this.fireTransition(sender + "." + String.format("0x%08x", tag), recallAcknowledgementEvent);

        if (this.numRecallAcknowledgements == 0) {
            LastRecallAcknowledgementEvent lastRecallAcknowledgementEvent = new LastRecallAcknowledgementEvent(this.directoryController, producerFlow, tag, producerFlow.getAccess());
            this.fireTransition(sender + "." + String.format("0x%08x", tag), lastRecallAcknowledgementEvent);
        }
    }

    public void onEventPutS(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        if (this.getDirectoryEntry().getSharers().size() > 1) {
            PutSNotLastEvent putSNotLastEvent = new PutSNotLastEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putSNotLastEvent);
        } else {
            PutSLastEvent putSLastEvent = new PutSLastEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putSLastEvent);
        }
    }

    public void onEventPutMAndData(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        if (requester == this.getDirectoryEntry().getOwner()) {
            PutMAndDataFromOwnerEvent putMAndDataFromOwnerEvent = new PutMAndDataFromOwnerEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putMAndDataFromOwnerEvent);
        } else {
            PutMAndDataFromNonOwnerEvent putMAndDataFromNonOwnerEvent = new PutMAndDataFromNonOwnerEvent(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess());
            this.fireTransition(requester + "." + String.format("0x%08x", tag), putMAndDataFromNonOwnerEvent);
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
        this.directoryController.getCache().getReplacementPolicy().handlePromotionOnHit(access, set, way);
        this.fireServiceNonblockingRequestEvent(access, tag, true);
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

    public void fireServiceNonblockingRequestEvent(MemoryHierarchyAccess access, int tag, boolean hitInCache) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getDirectoryController(), access, tag, getSet(), getWay(), hitInCache));
        this.getDirectoryController().updateStats(this.getDirectoryController().getCache(), access.getType().isRead(), hitInCache);
    }

    public void fireCacheLineInsertEvent(MemoryHierarchyAccess access, int tag, int victimTag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineInsertEvent(this.getDirectoryController(), access, tag, getSet(), getWay(), victimTag));
    }

    public void fireReplacementEvent(MemoryHierarchyAccess access, int tag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheLineReplacementEvent(this.getDirectoryController(), access, tag, getSet(), getWay()));
    }

    public void firePutSOrPutMAndDataFromOwnerEvent(MemoryHierarchyAccess access, int tag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent(this.getDirectoryController(), access, tag, getSet(), getWay()));
    }

    public void fireNonblockingRequestHitToTransientTagEvent(MemoryHierarchyAccess access, int tag) {
        this.getDirectoryController().getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(this.getDirectoryController(), access, tag, getSet(), getWay()));
    }

    public void sendDataToRequester(CacheCoherenceFlow producerFlow, CacheController requester, int tag, int numInvalidationAcknowledgements) {
        this.directoryController.transfer(requester, this.directoryController.getCache().getLineSize() + 8, new DataMessage(this.directoryController, producerFlow, this.directoryController, tag, numInvalidationAcknowledgements, producerFlow.getAccess()));
    }

    public void sendPutAckToReq(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        sendPutAckToReq(producerFlow, this.directoryController, requester, tag);
    }

    public static void sendPutAckToReq(CacheCoherenceFlow producerFlow, DirectoryController directoryController, CacheController requester, int tag) {
        directoryController.transfer(requester, 8, new PutAcknowledgementMessage(directoryController, producerFlow, tag, producerFlow.getAccess()));
    }

    public void copyDataToMemory(int tag) {
        this.directoryController.getNext().memWriteRequestReceive(this.directoryController, tag, new Action() {
            @Override
            public void apply() {
            }
        });
    }

    public void sendForwardGetSToOwner(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        this.directoryController.transfer(getDirectoryEntry().getOwner(), 8, new ForwardGetSMessage(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess()));
    }

    public void sendForwardGetMToOwner(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        this.directoryController.transfer(getDirectoryEntry().getOwner(), 8, new ForwardGetMMessage(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess()));
    }

    public void sendInvalidationToSharers(CacheCoherenceFlow producerFlow, CacheController requester, int tag) {
        for (CacheController sharer : this.getDirectoryEntry().getSharers()) {
            if (requester != sharer) {
                this.directoryController.transfer(sharer, 8, new InvalidationMessage(this.directoryController, producerFlow, requester, tag, producerFlow.getAccess()));
            }
        }
    }

    public void sendRecallToOwner(CacheCoherenceFlow producerFlow, int tag) {
        CacheController owner = getDirectoryEntry().getOwner();
        if (owner.getCache().findWay(tag) == -1) {
            throw new IllegalArgumentException();
        }

        this.directoryController.transfer(owner, 8, new RecallMessage(this.directoryController, producerFlow, tag, producerFlow.getAccess()));
    }

    public void sendRecallToSharers(CacheCoherenceFlow producerFlow, int tag) {
        for (CacheController sharer : this.getDirectoryEntry().getSharers()) {
            if (sharer.getCache().findWay(tag) == -1) {
                throw new IllegalArgumentException();
            }

            this.directoryController.transfer(sharer, 8, new RecallMessage(this.directoryController, producerFlow, tag, producerFlow.getAccess()));
        }
    }

    public void setNumRecallAcknowledgements(int numRecallAcknowledgements) {
        this.numRecallAcknowledgements = numRecallAcknowledgements;
    }

    public void decrementRecallAcknowledgements() {
        this.numRecallAcknowledgements--;
    }

    public void addRequesterAndOwnerToSharers(CacheController requester) {
        if (this.getDirectoryEntry().getSharers().contains(requester) || this.getDirectoryEntry().getSharers().contains(this.getDirectoryEntry().getOwner())) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().add(requester);
        this.getDirectoryEntry().getSharers().add(this.getDirectoryEntry().getOwner());
    }

    public void addRequesterToSharers(CacheController requester) {
        if (this.getDirectoryEntry().getSharers().contains(requester)) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().add(requester);
    }

    public void removeRequesterFromSharers(CacheController requester) {
        if (!this.getDirectoryEntry().getSharers().contains(requester)) {
            throw new IllegalArgumentException();
        }

        this.getDirectoryEntry().getSharers().remove(requester);
    }

    public void setOwnerToRequester(CacheController requester) {
        this.getDirectoryEntry().setOwner(requester);
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

    public int getEvicterTag() {
        return evicterTag;
    }

    public void setEvicterTag(int evicterTag) {
        this.evicterTag = evicterTag;
    }

    public int getVictimTag() {
        return victimTag;
    }

    public void setVictimTag(int victimTag) {
        this.victimTag = victimTag;
    }
}