/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.helperThread;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.action.Action1;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.util.HashMap;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

/**
 * Helper thread L2 cache request profiling helper.
 *
 * @author Min Cai
 */
public class HelperThreadL2CacheRequestProfilingHelper implements Reportable {
    private DirectoryController l2CacheController;

    private Map<Integer, Map<Integer, HelperThreadL2CacheRequestState>> helperThreadL2CacheRequestStates;
    private EvictableCache<HelperThreadL2CacheRequestVictimCacheLineState> helperThreadL2CacheRequestVictimCache;

    private long numMainThreadL2CacheHits;
    private long numMainThreadL2CacheMisses;

    private long numHelperThreadL2CacheHits;
    private long numHelperThreadL2CacheMisses;

    private long numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    private long numRedundantHitToCacheHelperThreadL2CacheRequests;

    private long numUsefulHelperThreadL2CacheRequests;

    private long numTimelyHelperThreadL2CacheRequests;
    private long numLateHelperThreadL2CacheRequests;

    private long numBadHelperThreadL2CacheRequests;

    private long numUglyHelperThreadL2CacheRequests;

    private Predictor<HelperThreadL2CacheRequestQuality> helperThreadL2CacheRequestQualityPredictor;

    /**
     * Create a helper thread L2 request profiling helper.
     *
     * @param simulation simulation
     */
    public HelperThreadL2CacheRequestProfilingHelper(Simulation simulation) {
        this(simulation.getProcessor().getMemoryHierarchy().getL2CacheController());
    }

    /**
     * Create a helper thread L2 request profiling helper.
     *
     * @param l2CacheController L2 cache (directory) controller
     */
    public HelperThreadL2CacheRequestProfilingHelper(final DirectoryController l2CacheController) {
        this.l2CacheController = l2CacheController;

        this.helperThreadL2CacheRequestStates = new HashMap<Integer, Map<Integer, HelperThreadL2CacheRequestState>>();
        for (int set = 0; set < this.l2CacheController.getCache().getNumSets(); set++) {
            HashMap<Integer, HelperThreadL2CacheRequestState> helperThreadL2CacheRequestStatesPerSet = new HashMap<Integer, HelperThreadL2CacheRequestState>();
            this.helperThreadL2CacheRequestStates.put(set, helperThreadL2CacheRequestStatesPerSet);

            for (int way = 0; way < this.l2CacheController.getCache().getAssociativity(); way++) {
                helperThreadL2CacheRequestStatesPerSet.put(way, new HelperThreadL2CacheRequestState());
            }
        }

        ValueProviderFactory<HelperThreadL2CacheRequestVictimCacheLineState, ValueProvider<HelperThreadL2CacheRequestVictimCacheLineState>> cacheLineStateProviderFactory = new ValueProviderFactory<HelperThreadL2CacheRequestVictimCacheLineState, ValueProvider<HelperThreadL2CacheRequestVictimCacheLineState>>() {
            @Override
            public ValueProvider<HelperThreadL2CacheRequestVictimCacheLineState> createValueProvider(Object... args) {
                return new HelperThreadL2CacheRequestVictimCacheLineStateValueProvider();
            }
        };

        this.helperThreadL2CacheRequestVictimCache = new EvictableCache<HelperThreadL2CacheRequestVictimCacheLineState>(l2CacheController, l2CacheController.getName() + "/helperThreadL2CacheRequestVictimCache", l2CacheController.getCache().getGeometry(), CacheReplacementPolicyType.LRU, cacheLineStateProviderFactory);

        this.helperThreadL2CacheRequestQualityPredictor = new CacheBasedPredictor<HelperThreadL2CacheRequestQuality>(l2CacheController, l2CacheController.getName() + "/helperThreadL2CacheRequestQualityPredictor", new CacheGeometry(64, 1, 1), 4, 16, HelperThreadL2CacheRequestQuality.UGLY); //TODO: parameters should not be hardcoded

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();
                    boolean requesterIsHelperThread = HelperThreadingHelper.isHelperThread(event.getAccess().getThread());
                    boolean lineFoundIsHelperThread = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();

                    handleL2CacheRequest(event, requesterIsHelperThread, lineFoundIsHelperThread);
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, new Action1<LastLevelCacheControllerLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheControllerLineInsertEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();
                    boolean requesterIsHelperThread = HelperThreadingHelper.isHelperThread(event.getAccess().getThread());
                    boolean lineFoundIsHelperThread = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();

                    handleL2CacheLineInsert(event, requesterIsHelperThread, lineFoundIsHelperThread);
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent.class, new Action1<GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent>() {
            @Override
            public void apply(GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();

                    checkInvariants(set);

                    boolean lineFoundIsHelperThread = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();

                    markInvalid(set, event.getWay());

                    if (lineFoundIsHelperThread) {
                        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getTag());

                        if (wayOfVictimCacheLine == -1) {
                            throw new IllegalArgumentException();
                        }

                        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);
                    }

                    checkInvariants(set);
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerNonblockingRequestHitToTransientTagEvent.class, new Action1<GeneralCacheControllerNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(GeneralCacheControllerNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();

                    int requesterThreadId = event.getAccess().getThread().getId();
                    int lineFoundThreadId = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getInFlightThreadId();

                    if (lineFoundThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    boolean requesterIsHelperThread = HelperThreadingHelper.isHelperThread(requesterThreadId);
                    boolean lineFoundIsHelperThread = HelperThreadingHelper.isHelperThread(lineFoundThreadId);

                    if (!requesterIsHelperThread && lineFoundIsHelperThread) {
                        markLate(set, event.getWay(), true);
                    } else if (requesterIsHelperThread && !lineFoundIsHelperThread) {
                        markLate(set, event.getWay(), true);
                    }
                }
            }
        });
    }

    /**
     * Sum up the unstable helper thread L2 cache requests.
     */
    public void sumUpUnstableHelperThreadL2CacheRequests() {
        for (int set = 0; set < l2CacheController.getCache().getNumSets(); set++) {
            for (int way = 0; way < l2CacheController.getCache().getAssociativity(); way++) {
                this.sumUpUnstableHelperThreadL2CacheRequest(set, way);
            }
        }
    }

    /**
     * Sum up unstable helper thread L2 cache request for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     */
    private void sumUpUnstableHelperThreadL2CacheRequest(int set, int way) {
        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = helperThreadL2CacheRequestStates.get(set).get(way);

        if (helperThreadL2CacheRequestState.getQuality() != HelperThreadL2CacheRequestQuality.INVALID) {
            if (helperThreadL2CacheRequestState.getQuality() == HelperThreadL2CacheRequestQuality.BAD) {
                this.incrementBadHelperThreadL2CacheRequests(set, helperThreadL2CacheRequestState.getThreadId(), helperThreadL2CacheRequestState.getPc());
            } else if (helperThreadL2CacheRequestState.getQuality() == HelperThreadL2CacheRequestQuality.UGLY) {
                this.incrementUglyHelperThreadL2CacheRequests(set, helperThreadL2CacheRequestState.getThreadId(), helperThreadL2CacheRequestState.getPc());
            } else {
                throw new IllegalArgumentException(helperThreadL2CacheRequestState.getQuality() + "");
            }

            helperThreadL2CacheRequestState.setQuality(HelperThreadL2CacheRequestQuality.INVALID);

            this.setL2CacheLineBroughterThreadId(set, way, -1, -1, false);
            helperThreadL2CacheRequestState.setPc(-1);
            this.markLate(set, way, false);
        }
    }

    /**
     * Handle an L2 cache request.
     *
     * @param event                   the event
     * @param requesterIsHelperThread a value indicating whether the requester is the main thread or not
     * @param lineFoundIsHelperThread a value indicating whether the line found is brought by the helper thread or not
     */
    private void handleL2CacheRequest(GeneralCacheControllerServiceNonblockingRequestEvent event, boolean requesterIsHelperThread, boolean lineFoundIsHelperThread) {
        checkInvariants(event.getSet());

        boolean mainThreadHit = event.isHitInCache() && !requesterIsHelperThread && !lineFoundIsHelperThread;
        boolean helperThreadHit = event.isHitInCache() && !requesterIsHelperThread && lineFoundIsHelperThread;

        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine = this.helperThreadL2CacheRequestVictimCache.findLine(event.getTag());

        boolean victimHit = victimLine != null;

        if (!requesterIsHelperThread) {
            if (!event.isHitInCache()) {
                this.numMainThreadL2CacheMisses++;
            } else {
                this.numMainThreadL2CacheHits++;

                if (lineFoundIsHelperThread) {
                    this.numUsefulHelperThreadL2CacheRequests++;
                }
            }
        } else {
            if (!event.isHitInCache()) {
                this.numHelperThreadL2CacheMisses++;
            } else {
                this.numHelperThreadL2CacheHits++;
            }

            if (event.isHitInCache() && !lineFoundIsHelperThread) {
                if (this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).isHitToTransientTag()) {
                    this.numRedundantHitToTransientTagHelperThreadL2CacheRequests++;
                    this.l2CacheController.getBlockingEventDispatcher().dispatch(new RedundantHitToTransientTagHelperThreadL2CacheRequestEvent(event.getSet()));
                    this.updateHelperThreadL2CacheRequestQualityPredictor(event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), HelperThreadL2CacheRequestQuality.REDUNDANT_HIT_TO_TRANSIENT_TAG);
                } else {
                    this.numRedundantHitToCacheHelperThreadL2CacheRequests++;
                    this.l2CacheController.getBlockingEventDispatcher().dispatch(new RedundantHitToCacheHelperThreadL2CacheRequestEvent(event.getSet()));
                    this.updateHelperThreadL2CacheRequestQualityPredictor(event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), HelperThreadL2CacheRequestQuality.REDUNDANT_HIT_TO_CACHE);
                }
            }
        }

        if (!event.isHitInCache()) {
            this.markTransientThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc());
        }

        if (!requesterIsHelperThread && !mainThreadHit && !helperThreadHit && victimHit) {
            handleRequestCase1(event, victimLine);
        } else if (!requesterIsHelperThread && !mainThreadHit && helperThreadHit && !victimHit) {
            handleRequestCase2(event);
        } else if (!requesterIsHelperThread && !mainThreadHit && helperThreadHit && victimHit) {
            handleRequestCase3(event);
        } else if (!requesterIsHelperThread && mainThreadHit && victimHit) {
            handleRequestCase4(event, victimLine);
        } else if (requesterIsHelperThread && victimHit) {
            clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());
            checkInvariants(event.getSet());
        }

        if (this.helperThreadL2CacheRequestVictimCache.findWay(event.getTag()) != -1) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Handle a cache request in the case 1.
     *
     * @param event      the event
     * @param victimLine the victim cache line
     */
    private void handleRequestCase1(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine) {
        this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.BAD);

        clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache request in the case 2.
     *
     * @param event the event
     */
    private void handleRequestCase2(GeneralCacheControllerServiceNonblockingRequestEvent event) {
        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay());

        if (helperThreadL2CacheRequestState.isHitToTransientTag()) {
            helperThreadL2CacheRequestState.setQuality(HelperThreadL2CacheRequestQuality.LATE);
            this.numLateHelperThreadL2CacheRequests++;
            this.l2CacheController.getBlockingEventDispatcher().dispatch(new LateHelperThreadL2CacheRequestEvent(event.getSet()));
            this.updateHelperThreadL2CacheRequestQualityPredictor(helperThreadL2CacheRequestState.getThreadId(), helperThreadL2CacheRequestState.getPc(), HelperThreadL2CacheRequestQuality.LATE);
        } else {
            helperThreadL2CacheRequestState.setQuality(HelperThreadL2CacheRequestQuality.TIMELY);
            this.numTimelyHelperThreadL2CacheRequests++;
            this.l2CacheController.getBlockingEventDispatcher().dispatch(new TimelyHelperThreadL2CacheRequestEvent(event.getSet()));
            this.updateHelperThreadL2CacheRequestQualityPredictor(helperThreadL2CacheRequestState.getThreadId(), helperThreadL2CacheRequestState.getPc(), HelperThreadL2CacheRequestQuality.TIMELY);
        }

        this.markMainThread(event.getSet(), event.getWay(), event.getAccess().getVirtualPc());
        helperThreadL2CacheRequestState.setQuality(HelperThreadL2CacheRequestQuality.INVALID);

        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getTag());

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache request in the case 3.
     *
     * @param event the event
     */
    private void handleRequestCase3(GeneralCacheControllerServiceNonblockingRequestEvent event) {
        this.markMainThread(event.getSet(), event.getWay(), event.getAccess().getVirtualPc());
        this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.INVALID);

        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getTag());

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache request in the case 4.
     *
     * @param event      the event
     * @param victimLine the victim cache line
     */
    private void handleRequestCase4(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine) {
        clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());

        checkInvariants(event.getSet());
    }

    /**
     * Handle an L2 cache line insertion.
     *
     * @param event                   the event
     * @param requesterIsHelperThread a value indicating whether the requester is the helper thread or not
     * @param lineFoundIsHelperThread a value indicating whether the line found is brought by the helper thread or not
     */
    private void handleL2CacheLineInsert(LastLevelCacheControllerLineInsertEvent event, boolean requesterIsHelperThread, boolean lineFoundIsHelperThread) {
        checkInvariants(event.getSet());

        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine = this.helperThreadL2CacheRequestVictimCache.findLine(event.getTag());

        if (victimLine != null) {
            clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());
        }

        if (lineFoundIsHelperThread) {
            HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay());
            HelperThreadL2CacheRequestQuality quality = helperThreadL2CacheRequestState.getQuality();

            if (quality == HelperThreadL2CacheRequestQuality.BAD) {
                this.incrementBadHelperThreadL2CacheRequests(event.getSet(), helperThreadL2CacheRequestState.getThreadId(), helperThreadL2CacheRequestState.getPc());
            } else if (quality == HelperThreadL2CacheRequestQuality.UGLY) {
                this.incrementUglyHelperThreadL2CacheRequests(event.getSet(), helperThreadL2CacheRequestState.getThreadId(), helperThreadL2CacheRequestState.getPc());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (requesterIsHelperThread) {
            markHelperThread(event.getSet(), event.getWay(), event.getAccess().getVirtualPc());
            this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.UGLY);
        } else {
            markMainThread(event.getSet(), event.getWay(), event.getAccess().getVirtualPc());
            this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.INVALID);
        }

        if (requesterIsHelperThread && !event.isEviction()) {
            handleLineInsertCase1(event);
        } else if (requesterIsHelperThread && event.isEviction() && !lineFoundIsHelperThread) {
            handleLineInsertCase2(event);
        } else if (requesterIsHelperThread && event.isEviction() && lineFoundIsHelperThread) {
            handleLineInsertCase3(event);
        } else if (!requesterIsHelperThread && event.isEviction() && lineFoundIsHelperThread) {
            handleLineInsertCase4(event);
        } else if (!requesterIsHelperThread && event.isEviction() && !lineFoundIsHelperThread) {
            handleLineInsertCase5(event);
        } else {
            checkInvariants(event.getSet());
        }

        if (this.helperThreadL2CacheRequestVictimCache.findWay(event.getTag()) != -1) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Handle a cache line insertion in the case 1.
     *
     * @param event the event
     */
    private void handleLineInsertCase1(LastLevelCacheControllerLineInsertEvent event) {
        this.insertNullEntry(event.getAccess(), event.getSet(), event.getTag());

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache line insertion in the case 2.
     *
     * @param event the event
     */
    private void handleLineInsertCase2(LastLevelCacheControllerLineInsertEvent event) {
        this.insertDataEntry(event.getAccess(), event.getSet(), event.getVictimTag(), event.getTag());

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache line insertion in the case 3.
     *
     * @param event the event
     */
    private void handleLineInsertCase3(LastLevelCacheControllerLineInsertEvent event) {
        int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getVictimTag());

        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(event.getSet(), wayOfVictimCacheLine);
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.helperThreadRequestTag = event.getTag();

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache line insertion in the case 4.
     *
     * @param event the event
     */
    private void handleLineInsertCase4(LastLevelCacheControllerLineInsertEvent event) {
        int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getVictimTag());

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

        checkInvariants(event.getSet());
    }

    /**
     * Handle a cache line insertion in the case 5.
     *
     * @param event the event
     */
    private void handleLineInsertCase5(LastLevelCacheControllerLineInsertEvent event) {
//        for (int way = 0; way < this.helperThreadL2CacheRequestVictimCache.getAssociativity(); way++) {
//            if (this.helperThreadL2CacheRequestVictimCache.getLine(event.getSet(), way).getState() != HelperThreadL2CacheRequestVictimCacheLineState.INVALID) {
//                this.removeLRU(event.getSet());
//                this.insertDataEntry(event.getSet(), victimTag, event.getTag());
//                break;
//            }
//        }

        checkInvariants(event.getSet());
    }

    //    private boolean checkInvariantsEnabled = true;
    private boolean checkInvariantsEnabled = false;

    /**
     * Check for invariants for the specified set index.
     *
     * @param set the set index
     */
    private void checkInvariants(int set) {
        if (checkInvariantsEnabled) {
            int numHelperThreadLinesInL2 = select(this.helperThreadL2CacheRequestStates.get(set).values(), having(on(HelperThreadL2CacheRequestState.class).getThreadId(), equalTo(HelperThreadingHelper.getHelperThreadId()))).size();
            int numMainThreadLinesInL2 = select(this.helperThreadL2CacheRequestStates.get(set).values(), having(on(HelperThreadL2CacheRequestState.class).getThreadId(), not(HelperThreadingHelper.getHelperThreadId()))).size();
            int numVictimEntriesInVictimCache = select(this.helperThreadL2CacheRequestVictimCache.getLines(set), having(on(CacheLine.class).getState(), not(HelperThreadL2CacheRequestVictimCacheLineState.INVALID))).size();

            if (numHelperThreadLinesInL2 != numVictimEntriesInVictimCache || numVictimEntriesInVictimCache + numMainThreadLinesInL2 > this.l2CacheController.getCache().getAssociativity()) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < this.l2CacheController.getCache().getAssociativity(); i++) {
                CacheLine<DirectoryControllerState> line = this.l2CacheController.getCache().getLine(set, i);
                if (line.getState().isStable() && line.isValid() && this.helperThreadL2CacheRequestStates.get(set).get(i).getThreadId() == HelperThreadingHelper.getHelperThreadId()) {
                    int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(set, line.getTag());

                    if (wayOfVictimCacheLine == -1) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    /**
     * Increment the number of ugly helper thread L2 cache requests.
     *
     * @param set      the set
     * @param threadId the thread ID
     * @param pc       the virtual address of the program counter (PC)
     */
    private void incrementUglyHelperThreadL2CacheRequests(int set, int threadId, int pc) {
        this.numUglyHelperThreadL2CacheRequests++;
        this.l2CacheController.getBlockingEventDispatcher().dispatch(new UglyHelperThreadL2CacheRequestEvent(set));
        updateHelperThreadL2CacheRequestQualityPredictor(threadId, pc, HelperThreadL2CacheRequestQuality.UGLY);
    }

    /**
     * Increment the number of bad helper thread L2 cache requests.
     *
     * @param set      the set
     * @param threadId the thread ID
     * @param pc       the virtual address of the program counter (PC)
     */
    private void incrementBadHelperThreadL2CacheRequests(int set, int threadId, int pc) {
        this.numBadHelperThreadL2CacheRequests++;
        this.l2CacheController.getBlockingEventDispatcher().dispatch(new BadHelperThreadL2CacheRequestEvent(set));
        updateHelperThreadL2CacheRequestQualityPredictor(threadId, pc, HelperThreadL2CacheRequestQuality.BAD);
    }

    /**
     * Update the helper thread L2 cache request quality predictor.
     *
     * @param threadId the thread ID
     * @param pc       the virtual address of the program counter (PC)
     * @param helperThreadL2CacheRequestQuality
     *                 the quality of the helper thread L2 cache request
     */
    private void updateHelperThreadL2CacheRequestQualityPredictor(int threadId, int pc, HelperThreadL2CacheRequestQuality helperThreadL2CacheRequestQuality) {
        if (threadId != HelperThreadingHelper.getHelperThreadId()) {
            throw new IllegalArgumentException(String.format("ctx: %d: pc: 0x%08x, quality: %s", threadId, pc, helperThreadL2CacheRequestQuality));
        }

        this.helperThreadL2CacheRequestQualityPredictor.update(pc, helperThreadL2CacheRequestQuality);
    }

    /**
     * Mark as invalid for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     */
    private void markInvalid(int set, int way) {
        this.sumUpUnstableHelperThreadL2CacheRequest(set, way);
    }

    /**
     * Mark the broughter as the helper thread for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     * @param pc  the virtual address of the program counter (PC)
     */
    private void markHelperThread(int set, int way, int pc) {
        this.setL2CacheLineBroughterThreadId(set, way, HelperThreadingHelper.getHelperThreadId(), pc, false);
    }

    /**
     * Mark the broughter as the main thread for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     * @param pc  the virtual address of the program counter (PC)
     */
    private void markMainThread(int set, int way, int pc) {
        this.setL2CacheLineBroughterThreadId(set, way, HelperThreadingHelper.getMainThreadId(), pc, false);
    }

    /**
     * Mark the transient thread ID for the specified set index and way.
     *
     * @param set      the set index
     * @param way      the way
     * @param threadId the thread ID
     * @param pc       the virtual address of the program counter (PC)
     */
    private void markTransientThreadId(int set, int way, int threadId, int pc) {
        this.setL2CacheLineBroughterThreadId(set, way, threadId, pc, true);
    }

    /**
     * Set the L2 cache line's broughter thread ID.
     *
     * @param set                          the set index
     * @param way                          the way
     * @param l2CacheLineBroughterThreadId the L2 cache line's broughter thread ID
     * @param pc                           the virtual address of the program counter (PC)
     * @param inFlight                     a value indicating whether it is in-flight or not
     */
    private void setL2CacheLineBroughterThreadId(int set, int way, int l2CacheLineBroughterThreadId, int pc, boolean inFlight) {
        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = this.helperThreadL2CacheRequestStates.get(set).get(way);

        if (inFlight) {
            helperThreadL2CacheRequestState.setInFlightThreadId(l2CacheLineBroughterThreadId);
            helperThreadL2CacheRequestState.setPc(pc);
        } else {
            helperThreadL2CacheRequestState.setInFlightThreadId(-1);

            if (helperThreadL2CacheRequestState.getPc() == -1) {
                throw new IllegalArgumentException();
            }

            helperThreadL2CacheRequestState.setThreadId(l2CacheLineBroughterThreadId);
        }
    }

    /**
     * Mark as late for the specified set index and way.
     *
     * @param set  the set index
     * @param way  the way
     * @param late whether its is late or not
     */
    private void markLate(int set, int way, boolean late) {
        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = this.helperThreadL2CacheRequestStates.get(set).get(way);
        helperThreadL2CacheRequestState.setHitToTransientTag(late);
    }

    /**
     * Insert a data entry in the victim cache.
     *
     * @param access                 the memory hierarchy access
     * @param set                    the set index
     * @param tag                    the tag
     * @param helperThreadRequestTag the helper thread request tag
     */
    private void insertDataEntry(MemoryHierarchyAccess access, int set, int tag, int helperThreadRequestTag) {
        if (tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        CacheAccess<HelperThreadL2CacheRequestVictimCacheLineState> newMiss = this.newMiss(tag, set);
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = newMiss.getLine();
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.DATA;
        stateProvider.helperThreadRequestTag = helperThreadRequestTag;
        line.setTag(tag);
        helperThreadL2CacheRequestVictimCache.getReplacementPolicy().handleInsertionOnMiss(access, set, newMiss.getWay());
    }

    /**
     * Insert a null entry in the victim cache.
     *
     * @param access                 the memory hierarchy access
     * @param set                    the set index
     * @param helperThreadRequestTag the helper thread request tag
     */
    private void insertNullEntry(MemoryHierarchyAccess access, int set, int helperThreadRequestTag) {
        CacheAccess<HelperThreadL2CacheRequestVictimCacheLineState> newMiss = this.newMiss(0, set);
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = newMiss.getLine();
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.NULL;
        stateProvider.helperThreadRequestTag = helperThreadRequestTag;
        line.setTag(CacheLine.INVALID_TAG);
        helperThreadL2CacheRequestVictimCache.getReplacementPolicy().handleInsertionOnMiss(access, set, newMiss.getWay());
    }

    /**
     * Find the way of the victim cache line matching the specified helper thread L2 cache request tag.
     *
     * @param set                    the set index
     * @param helperThreadRequestTag the helper thread request tag
     * @return the way of the victim cache line matching the specified helper thread L2 cache request tag
     */
    private int findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(int set, int helperThreadRequestTag) {
        for (int way = 0; way < this.l2CacheController.getCache().getAssociativity(); way++) {
            CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
            HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
            if (stateProvider.helperThreadRequestTag == helperThreadRequestTag) {
                return way;
            }
        }

        return -1;
    }

    /**
     * Invalidate the victim cache line for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     */
    private void invalidateVictimCacheLine(int set, int way) {
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.INVALID;
        stateProvider.helperThreadRequestTag = CacheLine.INVALID_TAG;
        line.setTag(CacheLine.INVALID_TAG);
    }

    /**
     * Clear the victim tag and set the state to NULL in the victim cache line for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     */
    private void clearVictimInVictimCacheLine(int set, int way) {
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.NULL;
        line.setTag(CacheLine.INVALID_TAG);
    }

    /**
     * Get the LRU policy in the victim cache.
     *
     * @return the LRU policy in the victim cache
     */
    private LRUPolicy<HelperThreadL2CacheRequestVictimCacheLineState> getLruPolicyForHelperThreadL2RequestVictimCache() {
        return (LRUPolicy<HelperThreadL2CacheRequestVictimCacheLineState>) this.helperThreadL2CacheRequestVictimCache.getReplacementPolicy();
    }

    /**
     * Create a new cache miss in the victim cache for the specified address.
     *
     * @param address the address
     * @param set     the set index
     * @return the newly created cache miss in the victim cache for the specified address
     */
    private CacheAccess<HelperThreadL2CacheRequestVictimCacheLineState> newMiss(int address, int set) {
        int tag = this.helperThreadL2CacheRequestVictimCache.getTag(address);

        for (int i = 0; i < this.helperThreadL2CacheRequestVictimCache.getAssociativity(); i++) {
            int way = this.getLruPolicyForHelperThreadL2RequestVictimCache().getWayInStackPosition(set, i);
            CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<HelperThreadL2CacheRequestVictimCacheLineState>(this.helperThreadL2CacheRequestVictimCache, null, set, way, tag);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "helperThreadL2CacheRequestProfilingHelper") {{
            getChildren().add(new ReportNode(this, "numMainThreadL2CacheHits", getNumMainThreadL2CacheHits() + ""));
            getChildren().add(new ReportNode(this, "numMainThreadL2CacheMisses", getNumMainThreadL2CacheMisses() + ""));

            getChildren().add(new ReportNode(this, "numHelperThreadL2CacheHits", getNumHelperThreadL2CacheHits() + ""));
            getChildren().add(new ReportNode(this, "numHelperThreadL2CacheMisses", getNumHelperThreadL2CacheMisses() + ""));

            getChildren().add(new ReportNode(this, "numTotalHelperThreadL2CacheRequests", getNumTotalHelperThreadL2CacheRequests() + ""));

            getChildren().add(new ReportNode(this, "numRedundantHitToTransientTagHelperThreadL2CacheRequests", getNumRedundantHitToTransientTagHelperThreadL2CacheRequests() + ""));
            getChildren().add(new ReportNode(this, "numRedundantHitToCacheHelperThreadL2CacheRequests", getNumRedundantHitToCacheHelperThreadL2CacheRequests() + ""));

            getChildren().add(new ReportNode(this, "numUsefulHelperThreadL2CacheRequests", getNumUsefulHelperThreadL2CacheRequests() + ""));

            getChildren().add(new ReportNode(this, "numTimelyHelperThreadL2CacheRequests", getNumTimelyHelperThreadL2CacheRequests() + ""));
            getChildren().add(new ReportNode(this, "numLateHelperThreadL2CacheRequests", getNumLateHelperThreadL2CacheRequests() + ""));

            getChildren().add(new ReportNode(this, "numBadHelperThreadL2CacheRequests", getNumBadHelperThreadL2CacheRequests() + ""));
            getChildren().add(new ReportNode(this, "numUglyHelperThreadL2CacheRequests", getNumUglyHelperThreadL2CacheRequests() + ""));

            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestCoverage", getHelperThreadL2CacheRequestCoverage() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestAccuracy", getHelperThreadL2CacheRequestAccuracy() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestLateness", getHelperThreadL2CacheRequestLateness() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestPollution", getHelperThreadL2CacheRequestPollution() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestRedundancy", getHelperThreadL2CacheRequestRedundancy() + ""));

            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestQualityPredictor/numHits", getHelperThreadL2CacheRequestQualityPredictor().getNumHits() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestQualityPredictor/numMisses", getHelperThreadL2CacheRequestQualityPredictor().getNumMisses() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestQualityPredictor/hitRatio", getHelperThreadL2CacheRequestQualityPredictor().getHitRatio() + ""));
        }});
    }

    /**
     * Get the coverage of the helper thread L2 cache requests.
     *
     * @return the coverage of the helper thread L2 cache requests
     */
    public double getHelperThreadL2CacheRequestCoverage() {
        return (this.numMainThreadL2CacheMisses + this.numUsefulHelperThreadL2CacheRequests) == 0 ? 0 : (double) this.numUsefulHelperThreadL2CacheRequests / (this.numMainThreadL2CacheMisses + this.numUsefulHelperThreadL2CacheRequests);
    }

    /**
     * Get the accuracy of the helper thread L2 cache requests.
     *
     * @return the accuracy of the helper thread L2 cache requests
     */
    public double getHelperThreadL2CacheRequestAccuracy() {
        return this.getNumTotalHelperThreadL2CacheRequests() == 0 ? 0 : (double) this.numUsefulHelperThreadL2CacheRequests / this.getNumTotalHelperThreadL2CacheRequests();
    }

    /**
     * Get the lateness of the helper thread L2 cache requests.
     *
     * @return the lateness of the helper thread L2 cache requests
     */
    public double getHelperThreadL2CacheRequestLateness() {
        return this.getNumTotalHelperThreadL2CacheRequests() == 0 ? 0 : (double) this.numLateHelperThreadL2CacheRequests / this.getNumTotalHelperThreadL2CacheRequests();
    }

    /**
     * Get the pollution of the helper thread L2 cache requests.
     *
     * @return the pollution of the helper thread L2 cache requests
     */
    public double getHelperThreadL2CacheRequestPollution() {
        return this.getNumTotalHelperThreadL2CacheRequests() == 0 ? 0 : (double) this.numBadHelperThreadL2CacheRequests / this.getNumTotalHelperThreadL2CacheRequests();
    }

    /**
     * Get the redundancy of the helper thread L2 cache requests.
     *
     * @return the redundancy of the helper thread L2 cache requests
     */
    public double getHelperThreadL2CacheRequestRedundancy() {
        return this.getNumTotalHelperThreadL2CacheRequests() == 0 ? 0 : (double) (this.numRedundantHitToTransientTagHelperThreadL2CacheRequests + numRedundantHitToCacheHelperThreadL2CacheRequests) / this.getNumTotalHelperThreadL2CacheRequests();
    }

    /**
     * Get the helper thread L2 cache request states.
     *
     * @return the helper thread L2 cache request states
     */
    public Map<Integer, Map<Integer, HelperThreadL2CacheRequestState>> getHelperThreadL2CacheRequestStates() {
        return helperThreadL2CacheRequestStates;
    }

    /**
     * Get the helper thread L2 cache request  victim cache.
     *
     * @return victim cache
     */
    public EvictableCache<HelperThreadL2CacheRequestVictimCacheLineState> getHelperThreadL2CacheRequestVictimCache() {
        return helperThreadL2CacheRequestVictimCache;
    }

    /**
     * Get the helper thread L2 cache request quality predictor.
     *
     * @return the helper thread L2 cache request quality predictor
     */
    public Predictor<HelperThreadL2CacheRequestQuality> getHelperThreadL2CacheRequestQualityPredictor() {
        return helperThreadL2CacheRequestQualityPredictor;
    }

    /**
     * Get the number of main thread L2 cache hits.
     *
     * @return the number of main thread L2 cache hits
     */
    public long getNumMainThreadL2CacheHits() {
        return numMainThreadL2CacheHits;
    }

    /**
     * Get the number of main thread L2 cache misses.
     *
     * @return the number of main thread L2 cache misses
     */
    public long getNumMainThreadL2CacheMisses() {
        return numMainThreadL2CacheMisses;
    }

    /**
     * Get the number of helper thread L2 cache hits.
     *
     * @return the number of helper thread L2 cache hits
     */
    public long getNumHelperThreadL2CacheHits() {
        return numHelperThreadL2CacheHits;
    }

    /**
     * Get the number of helper thread L2 cache misses.
     *
     * @return the number of helper thread L2 cache misses
     */
    public long getNumHelperThreadL2CacheMisses() {
        return numHelperThreadL2CacheMisses;
    }

    /**
     * Get the number of total helper thread L2 cache requests.
     *
     * @return the number of total helper thread L2 cache requests
     */
    public long getNumTotalHelperThreadL2CacheRequests() {
        return numHelperThreadL2CacheHits + numHelperThreadL2CacheMisses;
    }

    /**
     * Get the number of redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @return the number of redundant "hit to transient tag" helper thread L2 cache requests
     */
    public long getNumRedundantHitToTransientTagHelperThreadL2CacheRequests() {
        return numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of redundant "hit to cache" helper thread L2 cache requests.
     *
     * @return the number of redundant "hit to cache" helper thread L2 cache requests
     */
    public long getNumRedundantHitToCacheHelperThreadL2CacheRequests() {
        return numRedundantHitToCacheHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of useful helper thread L2 cache requests.
     *
     * @return the number of useful helper thread L2 cache requests
     */
    public long getNumUsefulHelperThreadL2CacheRequests() {
        return numUsefulHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of timely helper thread L2 cache requests.
     *
     * @return the number of timely helper thread L2 cache requests
     */
    public long getNumTimelyHelperThreadL2CacheRequests() {
        return numTimelyHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of late helper thread L2 cache requests.
     *
     * @return the number of late helper thread L2 cache requests
     */
    public long getNumLateHelperThreadL2CacheRequests() {
        return numLateHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of bad helper thread L2 cache requests.
     *
     * @return the number of late helper thread L2 cache requests
     */
    public long getNumBadHelperThreadL2CacheRequests() {
        return numBadHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of ugly helper thread L2 cache requests.
     *
     * @return the number of ugly helper thread L2 cache requests
     */
    public long getNumUglyHelperThreadL2CacheRequests() {
        return numUglyHelperThreadL2CacheRequests;
    }

    /**
     * Get a value indicating whether the checking of invariants is enabled or not.
     *
     * @return a value indicating whether the checking of invariants is enabled or not
     */
    public boolean isCheckInvariantsEnabled() {
        return checkInvariantsEnabled;
    }

    /**
     * Helper thread L2 cache request victim cache line state.
     */
    public static enum HelperThreadL2CacheRequestVictimCacheLineState {
        /**
         * Invalid.
         */
        INVALID,

        /**
         * Null.
         */
        NULL,

        /**
         * Data.
         */
        DATA
    }

    /**
     * Helper thread L2 cache request victim cache line state value provider.
     */
    private static class HelperThreadL2CacheRequestVictimCacheLineStateValueProvider implements ValueProvider<HelperThreadL2CacheRequestVictimCacheLineState> {
        private HelperThreadL2CacheRequestVictimCacheLineState state;
        private int helperThreadRequestTag;

        /**
         * Create a helper thread L2 cache request victim cache line state value provider.
         */
        public HelperThreadL2CacheRequestVictimCacheLineStateValueProvider() {
            this.state = HelperThreadL2CacheRequestVictimCacheLineState.INVALID;
            this.helperThreadRequestTag = CacheLine.INVALID_TAG;
        }

        /**
         * Get the value.
         *
         * @return the value
         */
        @Override
        public HelperThreadL2CacheRequestVictimCacheLineState get() {
            return state;
        }

        /**
         * Get the initial value.
         *
         * @return the initial value
         */
        @Override
        public HelperThreadL2CacheRequestVictimCacheLineState getInitialValue() {
            return HelperThreadL2CacheRequestVictimCacheLineState.INVALID;
        }
    }

    /**
     * Stable helper thread L2 cache request event.
     */
    public abstract class StableHelperThreadL2CacheRequestEvent extends SimulationEvent {
        private int set;

        /**
         * Create a stable helper thread L2 cache request event.
         */
        public StableHelperThreadL2CacheRequestEvent(int set) {
            super(l2CacheController);
            this.set = set;
        }

        /**
         * Get the set.
         *
         * @return the set
         */
        public int getSet() {
            return set;
        }
    }

    /**
     * Redundant "hit to transient tag" helper thread L2 cache request event.
     */
    public class RedundantHitToTransientTagHelperThreadL2CacheRequestEvent extends StableHelperThreadL2CacheRequestEvent {
        public RedundantHitToTransientTagHelperThreadL2CacheRequestEvent(int set) {
            super(set);
        }
    }

    /**
     * Redundant "hit to cache" helper thread L2 cache request event.
     */
    public class RedundantHitToCacheHelperThreadL2CacheRequestEvent extends StableHelperThreadL2CacheRequestEvent {
        public RedundantHitToCacheHelperThreadL2CacheRequestEvent(int set) {
            super(set);
        }
    }

    /**
     * Timely helper thread L2 cache request event.
     */
    public class TimelyHelperThreadL2CacheRequestEvent extends StableHelperThreadL2CacheRequestEvent {
        public TimelyHelperThreadL2CacheRequestEvent(int set) {
            super(set);
        }
    }

    /**
     * Late helper thread L2 cache request event.
     */
    public class LateHelperThreadL2CacheRequestEvent extends StableHelperThreadL2CacheRequestEvent {
        public LateHelperThreadL2CacheRequestEvent(int set) {
            super(set);
        }
    }

    /**
     * Bad helper thread L2 cache request event.
     */
    public class BadHelperThreadL2CacheRequestEvent extends StableHelperThreadL2CacheRequestEvent {
        public BadHelperThreadL2CacheRequestEvent(int set) {
            super(set);
        }
    }

    /**
     * Ugly helper thread L2 cache request event.
     */
    public class UglyHelperThreadL2CacheRequestEvent extends StableHelperThreadL2CacheRequestEvent {
        public UglyHelperThreadL2CacheRequestEvent(int set) {
            super(set);
        }
    }
}
