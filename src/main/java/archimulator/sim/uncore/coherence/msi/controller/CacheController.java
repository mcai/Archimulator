package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.uncore.*;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.config.FirstLevelCacheConfig;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.flow.LoadFlow;
import archimulator.sim.uncore.coherence.msi.flow.StoreFlow;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.fsm.CacheControllerFiniteStateMachineFactory;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.CacheControllerState;
import archimulator.sim.uncore.net.Net;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action2;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CacheController extends Controller {
    private EvictableCache<CacheControllerState> cache;
    private List<MemoryHierarchyAccess> pendingAccesses;
    private EnumMap<MemoryHierarchyAccessType, Integer> pendingAccessesPerType;
    private CacheControllerFiniteStateMachineFactory fsmFactory;

    public CacheController(CacheHierarchy cacheHierarchy, final String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config);

        ValueProviderFactory<CacheControllerState, ValueProvider<CacheControllerState>> cacheLineStateProviderFactory = new ValueProviderFactory<CacheControllerState, ValueProvider<CacheControllerState>>() {
            @Override
            public ValueProvider<CacheControllerState> createValueProvider(Object... args) {
                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new CacheControllerFiniteStateMachine(name, set, way, CacheController.this);
            }
        };

        this.cache = new EvictableCache<CacheControllerState>(name, config.getGeometry(), config.getEvictionPolicyClz(), cacheLineStateProviderFactory);

        this.pendingAccesses = new ArrayList<MemoryHierarchyAccess>();

        this.pendingAccessesPerType = new EnumMap<MemoryHierarchyAccessType, Integer>(MemoryHierarchyAccessType.class);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.IFETCH, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.LOAD, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.STORE, 0);

        this.fsmFactory = new CacheControllerFiniteStateMachineFactory(new Action1<CacheControllerFiniteStateMachine>() {
                @Override
                public void apply(CacheControllerFiniteStateMachine fsm) {
                    if (fsm.getPreviousState() != fsm.getState()) {
                        if (fsm.getState().isStable()) {
                            final Action onCompletedCallback = fsm.getOnCompletedCallback();
                            if (onCompletedCallback != null) {
                                fsm.setOnCompletedCallback(null);
                                onCompletedCallback.apply();
                            }
                        }

                        List<Action> stalledEventsToProcess = new ArrayList<Action>();
                        for (Action stalledEvent : fsm.getStalledEvents()) {
                            stalledEventsToProcess.add(stalledEvent);
                        }

                        fsm.getStalledEvents().clear();

                        for (Action stalledEvent : stalledEventsToProcess) {
                            stalledEvent.apply();
                        }
                    }
                }
            });
    }

    public boolean canAccess(MemoryHierarchyAccessType type, int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);
        return access == null ?
                this.pendingAccessesPerType.get(type) < (type == MemoryHierarchyAccessType.STORE ? this.getWritePorts() : this.getReadPorts()) :
                type != MemoryHierarchyAccessType.STORE && access.getType() != MemoryHierarchyAccessType.STORE;
    }

    public MemoryHierarchyAccess findAccess(int physicalTag) {
        for (MemoryHierarchyAccess access : this.pendingAccesses) {
            if (access.getPhysicalTag() == physicalTag) {
                return access;
            }
        }

        return null;
    }

    public MemoryHierarchyAccess beginAccess(DynamicInstruction dynamicInst, MemoryHierarchyThread thread, MemoryHierarchyAccessType type, int virtualPc, int physicalAddress, int physicalTag, Action onCompletedCallback) {
        MemoryHierarchyAccess newAccess = new MemoryHierarchyAccess(dynamicInst, thread, type, virtualPc, physicalAddress, physicalTag, onCompletedCallback, this.getCycleAccurateEventQueue().getCurrentCycle());

        MemoryHierarchyAccess access = this.findAccess(physicalTag);

        if (access != null) {
            access.getAliases().add(0, newAccess);
        } else {
            this.pendingAccesses.add(newAccess);
            this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) + 1);
        }

        return newAccess;
    }

    public void endAccess(int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);
        assert (access != null);

        access.complete(this.getCycleAccurateEventQueue().getCurrentCycle());

        for (MemoryHierarchyAccess alias : access.getAliases()) {
            alias.complete(this.getCycleAccurateEventQueue().getCurrentCycle());
        }

        MemoryHierarchyAccessType type = access.getType();
        this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) - 1);

        this.pendingAccesses.remove(access);
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getCacheHierarchy().getL1sToL2Network();
    }

    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                load(access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    public void receiveLoad(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                load(access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    public void receiveStore(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.getCycleAccurateEventQueue().schedule(this, new Action() {
            @Override
            public void apply() {
                store(access.getPhysicalTag(), onCompletedCallback);
            }
        }, this.getHitLatency());
    }

    public void setNext(DirectoryController next) {
        next.getCacheControllers().add(this);
        super.setNext(next);
    }

    public DirectoryController getNext() {
        return (DirectoryController) super.getNext();
    }

    private int getReadPorts() {
        return ((FirstLevelCacheConfig) getConfig()).getNumReadPorts();
    }

    private int getWritePorts() {
        return ((FirstLevelCacheConfig) getConfig()).getNumWritePorts();
    }

    @Override
    public void receive(CoherenceMessage message) {
        switch (message.getType()) {
            case FWD_GETS:
                onFwdGetS((FwdGetSMessage) message);
                break;
            case FWD_GETM:
                onFwdGetM((FwdGetMMessage) message);
                break;
            case INV:
                onInv((InvMessage) message);
                break;
            case RECALL:
                onRecall((RecallMessage) message);
                break;
            case PUT_ACK:
                onPutAck((PutAckMessage) message);
                break;
            case DATA:
                onData((DataMessage) message);
                break;
            case INV_ACK:
                onInvAck((InvAckMessage) message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void onFwdGetS(FwdGetSMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventFwdGetS(message, message.getReq(), message.getTag());
    }

    private void onFwdGetM(FwdGetMMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventFwdGetM(message, message.getReq(), message.getTag());
    }

    private void onInv(InvMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventInv(message, message.getReq(), message.getTag());
    }

    private void onRecall(RecallMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventRecall(message, message.getTag());
    }

    private void onPutAck(PutAckMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventPutAck(message, message.getTag());
    }

    private void onData(DataMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventData(message, message.getSender(), message.getTag(), message.getNumAcks());
    }

    private void onInvAck(InvAckMessage message) {
        int way = this.cache.findWay(message.getTag());
        CacheLine<CacheControllerState> line = this.getCache().getLine(this.getCache().getSet(message.getTag()), way);
        CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventInvAck(message, message.getSender(), message.getTag());
    }

    public EvictableCache<CacheControllerState> getCache() {
        return cache;
    }

    public void access(CacheCoherenceFlow producerFlow, final int tag, final Action2<Integer, Integer> onReplacementCompletedCallback, final Action onReplacementStalledCallback) {
        final int set = this.cache.getSet(tag);
        int way = this.getCache().findWay(tag);
        if (way == -1) {
            for (int wayFound = 0; wayFound < this.getCache().getAssociativity(); wayFound++) {
                CacheLine<CacheControllerState> lineFound = this.getCache().getLine(set, wayFound);
                if (lineFound.getState() == lineFound.getInitialState()) {
                    way = wayFound;
                    break;
                }
            }

            if (way != -1) {
                onReplacementCompletedCallback.apply(set, way);
            } else {
                way = this.getCache().findVictim(set);
                final CacheLine<CacheControllerState> line = this.getCache().getLine(set, way);
                final CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
                final int finalWay = way;
                fsm.onEventReplacement(producerFlow, tag,
                        new Action() {
                            @Override
                            public void apply() {
                                onReplacementCompletedCallback.apply(set, finalWay);
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                getCycleAccurateEventQueue().schedule(CacheController.this, onReplacementStalledCallback, 1);
                            }
                        }
                );
            }
        } else {
            onReplacementCompletedCallback.apply(set, way);
        }
    }

    public void load(final int tag, final Action onCompletedCallback) {
        final LoadFlow loadFlow = new LoadFlow(this, tag, onCompletedCallback);

        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                load(tag, loadFlow.getOnCompletedCallback2());
            }
        };

        this.access(loadFlow, tag,
                new Action2<Integer, Integer>() {
                    @Override
                    public void apply(Integer set, Integer way) {
                        CacheLine<CacheControllerState> line = getCache().getLine(set, way);
                        final CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
                        fsm.onEventLoad(loadFlow, tag, loadFlow.getOnCompletedCallback2(), onStalledCallback);
                    }
                }, onStalledCallback
        );
    }

    public void store(final int tag, final Action onCompletedCallback) {
        final StoreFlow storeFlow = new StoreFlow(this, tag, onCompletedCallback);

        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                store(tag, storeFlow.getOnCompletedCallback2());
            }
        };

        this.access(storeFlow, tag,
                new Action2<Integer, Integer>() {
                    @Override
                    public void apply(Integer set, Integer way) {
                        CacheLine<CacheControllerState> line = getCache().getLine(set, way);
                        final CacheControllerFiniteStateMachine fsm = (CacheControllerFiniteStateMachine) line.getStateProvider();
                        fsm.onEventStore(storeFlow, tag, storeFlow.getOnCompletedCallback2(), onStalledCallback);
                    }
                }, onStalledCallback
        );
    }

    public DirectoryController getDirectoryController() {
        return this.getNext();
    }

    public CacheControllerFiniteStateMachineFactory getFsmFactory() {
        return fsmFactory;
    }

    @Override
    public String toString() {
        return this.getCache().getName();
    }
}
