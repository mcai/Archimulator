package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.coherence.msi.fsm.DirectoryControllerFiniteStateMachine;
import archimulator.sim.uncore.coherence.msi.message.*;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.sim.uncore.dram.MemoryController;
import archimulator.sim.uncore.net.Net;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action;
import net.pickapack.action.Action2;

import java.util.ArrayList;
import java.util.List;

public class DirectoryController extends Controller {
    private EvictableCache<DirectoryControllerState> cache;
    private List<CacheController> cacheControllers;

    public DirectoryController(CacheHierarchy cacheHierarchy, final String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config);

        ValueProviderFactory<DirectoryControllerState, ValueProvider<DirectoryControllerState>> cacheLineStateProviderFactory = new ValueProviderFactory<DirectoryControllerState, ValueProvider<DirectoryControllerState>>() {
            @Override
            public ValueProvider<DirectoryControllerState> createValueProvider(Object... args) {
                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new DirectoryControllerFiniteStateMachine(name, set, way, DirectoryController.this);
            }
        };

        this.cache = new EvictableCache<DirectoryControllerState>(name, config.getGeometry(), config.getEvictionPolicyClz(), cacheLineStateProviderFactory);
        this.cacheControllers = new ArrayList<CacheController>();
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return to instanceof MemoryController ? this.getCacheHierarchy().getL2ToMemNetwork() : this.getCacheHierarchy().getL1sToL2Network();
    }

    public void setNext(MemoryController next) {
        super.setNext(next);
    }

    public MemoryController getNext() {
        return (MemoryController) super.getNext();
    }

    @Override
    public void receive(CoherenceMessage message) {
        switch (message.getType()) {
            case GETS:
                onGetS((GetSMessage) message);
                break;
            case GETM:
                onGetM((GetMMessage) message);
                break;
            case RECALL_ACK:
                onRecallAck((RecallAckMessage) message);
                break;
            case PUTS:
                onPutS((PutSMessage) message);
                break;
            case PUTM_AND_DATA:
                onPutMAndData((PutMAndDataMessage) message);
                break;
            case DATA:
                onData((DataMessage) message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void access(CacheCoherenceFlow producerFlow, final int tag, final Action2<Integer, Integer> onReplacementCompletedCallback, final Action onReplacementStalledCallback) {
        final int set = this.cache.getSet(tag);
        int way = this.getCache().findWay(tag);
        if (way == -1) {
            for (int wayFound = 0; wayFound < this.getCache().getAssociativity(); wayFound++) {
                CacheLine<DirectoryControllerState> lineFound = this.getCache().getLine(set, wayFound);
                if (lineFound.getState() == lineFound.getInitialState()) {
                    way = wayFound;
                    break;
                }
            }

            if (way != -1) {
                onReplacementCompletedCallback.apply(set, way);
            } else {
                way = this.getCache().findVictim(set);
                final CacheLine<DirectoryControllerState> line = this.getCache().getLine(set, way);
                final DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
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
                                getCycleAccurateEventQueue().schedule(DirectoryController.this, onReplacementStalledCallback, 1);
                            }
                        }
                );
            }
        } else {
            onReplacementCompletedCallback.apply(set, way);
        }
    }

    private void onGetS(final GetSMessage message) {
        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                onGetS(message);
            }
        };

        this.access(message, message.getTag(), new Action2<Integer, Integer>() {
            @Override
            public void apply(Integer set, Integer way) {
                CacheLine<DirectoryControllerState> line = getCache().getLine(set, way);
                final DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventGetS(message, message.getReq(), message.getTag(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    private void onGetM(final GetMMessage message) {
        final Action onStalledCallback = new Action() {
            @Override
            public void apply() {
                onGetM(message);
            }
        };

        this.access(message, message.getTag(), new Action2<Integer, Integer>() {
            @Override
            public void apply(Integer set, Integer way) {
                CacheLine<DirectoryControllerState> line = getCache().getLine(set, way);
                final DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
                fsm.onEventGetM(message, message.getReq(), message.getTag(), onStalledCallback);
            }
        }, onStalledCallback);
    }

    private void onRecallAck(RecallAckMessage message) {
        CacheController sender = message.getSender();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);
        CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
        DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventRecallAck(message, sender, tag);
    }

    private void onPutS(PutSMessage message) {
        CacheController req = message.getReq();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);

        if (way == -1) {
            DirectoryControllerFiniteStateMachine.sendPutAckToReq(message, this, req, tag);
        } else {
            CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            fsm.onEventPutS(message, req, tag);
        }
    }

    private void onPutMAndData(PutMAndDataMessage message) {
        CacheController req = message.getReq();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);

        if (tag == -1) {
            DirectoryControllerFiniteStateMachine.sendPutAckToReq(message, this, req, tag);
        } else {
            CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
            DirectoryControllerFiniteStateMachine fsm = (DirectoryControllerFiniteStateMachine) line.getStateProvider();
            fsm.onEventPutMAndData(message, req, tag);
        }
    }

    private void onData(DataMessage message) {
        CacheController sender = (CacheController) message.getSender();
        int tag = message.getTag();

        int way = this.cache.findWay(tag);
        CacheLine<DirectoryControllerState> line = this.cache.getLine(this.cache.getSet(tag), way);
        DirectoryControllerFiniteStateMachine fsm =(DirectoryControllerFiniteStateMachine) line.getStateProvider();
        fsm.onEventData(message, sender, tag);
    }

    public EvictableCache<DirectoryControllerState> getCache() {
        return cache;
    }

    public List<CacheController> getCacheControllers() {
        return cacheControllers;
    }

    @Override
    public String toString() {
        return this.getCache().getName();
    }
}
