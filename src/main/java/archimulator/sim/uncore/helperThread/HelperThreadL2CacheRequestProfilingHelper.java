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
package archimulator.sim.uncore.helperThread;

import archimulator.sim.common.Simulation;
import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.coherence.event.CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action1;

import java.util.HashMap;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

//on last puts or putm from owner:
//  RemoveLRU
//
//on replacement:
//	1. HT replaces INVALID => insert NULL
//	2. HT replaces MT => insert DATA
//	3. HT replaces HT => No action
//	4. MT replaces HT => Remove LRU
//	5. MT replaces MT, Exists HT => Remove LRU, Insert DATA
//
//on reference:
//	1. MT miss + HT miss + VT Hit => ; Bad HT, VT.setLRU
//	2. MT miss + HT hit + VT miss => MT to HT; Good HT, removeLRU
//	3. MT miss + HT hit + VT hit => MT to HT; VT.setLRU, removeLRU
//	4. MT hit + HT miss + VT hit => ; VT.setLRU
public class HelperThreadL2CacheRequestProfilingHelper {
    private DirectoryController l2CacheController;

    private Map<Integer, Map<Integer, HelperThreadL2CacheRequestState>> helperThreadL2CacheRequestStates;
    private EvictableCache<HelperThreadL2CacheRequestVictimCacheLineState> helperThreadL2CacheRequestVictimCache;

    private long numMainThreadL2CacheHits;
    private long numMainThreadL2CacheMisses;

    private long numTotalHelperThreadL2CacheRequests;

    private long numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    private long numRedundantHitToCacheHelperThreadL2CacheRequests;

    private long numUsefulHelperThreadL2CacheRequests;

    private long numTimelyHelperThreadL2CacheRequests;
    private long numLateHelperThreadL2CacheRequests;

    private long numBadHelperThreadL2CacheRequests;

    private long numUglyHelperThreadL2CacheRequests;

    private class HelperThreadL2CacheRequestState {
        private int inFlightThreadId;
        private int threadId;
        private HelperThreadL2CacheRequestQuality quality;
        public boolean hitToTransientTag;

        private HelperThreadL2CacheRequestState() {
            this.inFlightThreadId = -1;
            this.threadId = -1;
            this.quality = HelperThreadL2CacheRequestQuality.INVALID;
        }

        public int getInFlightThreadId() {
            return inFlightThreadId;
        }

        public int getThreadId() {
            return threadId;
        }

        private void setQuality(HelperThreadL2CacheRequestQuality quality) {
            if (this.quality != HelperThreadL2CacheRequestQuality.INVALID && quality != HelperThreadL2CacheRequestQuality.INVALID && !this.quality.isModifiable()) {
                throw new IllegalArgumentException();
            }

            this.quality = quality;
        }
    }

    public HelperThreadL2CacheRequestProfilingHelper(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2CacheController());
    }

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

        this.helperThreadL2CacheRequestVictimCache = new EvictableCache<HelperThreadL2CacheRequestVictimCacheLineState>(l2CacheController, l2CacheController.getName() + ".helperThreadL2CacheRequestVictimCache", l2CacheController.getCache().getGeometry(), CacheReplacementPolicyType.LRU, cacheLineStateProviderFactory);

        l2CacheController.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();
                    boolean requesterIsHelperThread = BasicThread.isHelperThread(event.getAccess().getThread());

                    boolean lineFoundIsHelperThread = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    handleRequest(event, requesterIsHelperThread, lineFoundIsHelperThread);
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(LastLevelCacheLineInsertEvent.class, new Action1<LastLevelCacheLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheLineInsertEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();
                    boolean requesterIsHelperThread = BasicThread.isHelperThread(event.getAccess().getThread());
                    boolean lineFoundIsHelperThread = HelperThreadL2CacheRequestProfilingHelper.this.helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    handleLineInsert(event, requesterIsHelperThread, lineFoundIsHelperThread);
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent.class, new Action1<CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent>() {
            @Override
            public void apply(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();

                    checkInvariants(set);

                    boolean lineFoundIsHelperThread = HelperThreadL2CacheRequestProfilingHelper.this.helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

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

        l2CacheController.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();

                    int requesterThreadId = event.getAccess().getThread().getId();
                    int lineFoundThreadId = HelperThreadL2CacheRequestProfilingHelper.this.helperThreadL2CacheRequestStates.get(set).get(event.getWay()).inFlightThreadId;

                    if (lineFoundThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    boolean requesterIsHelperThread = BasicThread.isHelperThread(requesterThreadId);
                    boolean lineFoundIsHelperThread = BasicThread.isHelperThread(lineFoundThreadId);

                    if (!requesterIsHelperThread && lineFoundIsHelperThread) {
                        markLate(set, event.getWay(), true);
                    } else if (requesterIsHelperThread && !lineFoundIsHelperThread) {
                        markLate(set, event.getWay(), true);
                    }
                }
            }
        });
    }

    private void sumUpUnstableHTLLCRequests() {
        for (int set = 0; set < l2CacheController.getCache().getNumSets(); set++) {
            for (int way = 0; way < l2CacheController.getCache().getAssociativity(); way++) {
                HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = helperThreadL2CacheRequestStates.get(set).get(way);
                if (helperThreadL2CacheRequestState.quality == HelperThreadL2CacheRequestQuality.BAD) {
                    incrementBadHelperThreadL2CacheRequests();
                } else if (helperThreadL2CacheRequestState.quality == HelperThreadL2CacheRequestQuality.UGLY) {
                    incrementUglyHelperThreadL2CacheRequests();
                }
            }
        }
    }

    private void handleRequest(CoherentCacheServiceNonblockingRequestEvent event, boolean requesterIsHelperThread, boolean lineFoundIsHelperThread) {
        checkInvariants(event.getSet());

        boolean mainThreadHit = event.isHitInCache() && !requesterIsHelperThread && !lineFoundIsHelperThread;
        boolean helperThreadHit = event.isHitInCache() && !requesterIsHelperThread && lineFoundIsHelperThread;

        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine = this.helperThreadL2CacheRequestVictimCache.findLine(event.getTag());

        boolean victimHit = victimLine != null;

        if (!event.isHitInCache()) {
            this.markTransientThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId());
        }

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
            this.numTotalHelperThreadL2CacheRequests++;

            if (event.isHitInCache() && !lineFoundIsHelperThread) {
                if (this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).hitToTransientTag) {
                    this.numRedundantHitToTransientTagHelperThreadL2CacheRequests++;
                } else {
                    this.numRedundantHitToCacheHelperThreadL2CacheRequests++;
                }
            }
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

    private void handleRequestCase1(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine) {
        this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.BAD);

        clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());

//        this.setLRU(event.getSet(), victimLine.getWay());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase2(CoherentCacheServiceNonblockingRequestEvent event) {
        if (this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).hitToTransientTag) {
            this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.LATE);
            this.numLateHelperThreadL2CacheRequests++;
        } else {
            this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.TIMELY);
            this.numTimelyHelperThreadL2CacheRequests++;
        }

        this.markMainThread(event.getSet(), event.getWay());
        this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.INVALID);

        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getTag());

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

//        this.removeLRU(event.getSet());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase3(CoherentCacheServiceNonblockingRequestEvent event) {
        this.markMainThread(event.getSet(), event.getWay());
        this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.INVALID);

        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getTag());

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

//        this.setLRU(event.getSet(), vtLine.getWay());
//        this.removeLRU(event.getSet());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase4(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine) {
        clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());

//        this.setLRU(event.getSet(), victimLine.getWay());

        checkInvariants(event.getSet());
    }

    private void handleLineInsert(LastLevelCacheLineInsertEvent event, boolean requesterIsHelperThread, boolean lineFoundIsHelperThread) {
        checkInvariants(event.getSet());

        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> victimLine = this.helperThreadL2CacheRequestVictimCache.findLine(event.getTag());

        if (victimLine != null) {
            clearVictimInVictimCacheLine(event.getSet(), victimLine.getWay());
        }

        if (lineFoundIsHelperThread) {
            HelperThreadL2CacheRequestQuality quality = helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).quality;

            if (quality == HelperThreadL2CacheRequestQuality.BAD) {
                this.incrementBadHelperThreadL2CacheRequests();
            } else if (quality == HelperThreadL2CacheRequestQuality.UGLY) {
                this.incrementUglyHelperThreadL2CacheRequests();
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (requesterIsHelperThread) {
            markHelperThread(event.getSet(), event.getWay());
            this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.UGLY);
        } else {
            markMainThread(event.getSet(), event.getWay());
            this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HelperThreadL2CacheRequestQuality.INVALID);
        }

        if (requesterIsHelperThread && !event.isEviction()) {
            handleLineInsert1(event);
        } else if (requesterIsHelperThread && event.isEviction() && !lineFoundIsHelperThread) {
            handleLineInsert2(event);
        } else if (requesterIsHelperThread && event.isEviction() && lineFoundIsHelperThread) {
            handleLineInsert3(event);
        } else if (!requesterIsHelperThread && event.isEviction() && lineFoundIsHelperThread) {
            handleLineInsert4(event);
        } else if (!requesterIsHelperThread && event.isEviction() && !lineFoundIsHelperThread) {
            handleLineInsert5(event);
        } else {
            checkInvariants(event.getSet());
        }

        if (this.helperThreadL2CacheRequestVictimCache.findWay(event.getTag()) != -1) {
            throw new IllegalArgumentException();
        }
    }

    private void handleLineInsert1(LastLevelCacheLineInsertEvent event) {
        // case 1
        this.insertNullEntry(event.getSet(), event.getTag());

        checkInvariants(event.getSet());
    }

    private void handleLineInsert2(LastLevelCacheLineInsertEvent event) {
        // case 2
        this.insertDataEntry(event.getSet(), event.getVictimTag(), event.getTag());

        checkInvariants(event.getSet());
    }

    private void handleLineInsert3(LastLevelCacheLineInsertEvent event) {
        // case 3
        int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getVictimTag());

        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(event.getSet(), wayOfVictimCacheLine);
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.helperThreadRequestTag = event.getTag();

        checkInvariants(event.getSet());
    }

    private void handleLineInsert4(LastLevelCacheLineInsertEvent event) {
        // case 4
        int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(event.getSet(), event.getVictimTag());

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

        checkInvariants(event.getSet());
    }

    private void handleLineInsert5(LastLevelCacheLineInsertEvent event) {
//        boolean htLLCRequestFound = false;
//
//        for (int way = 0; way < this.helperThreadL2CacheRequestVictimCache.getAssociativity(); way++) {
//            if (this.helperThreadL2CacheRequestVictimCache.getLine(event.getSet(), way).getState() != HelperThreadL2CacheRequestVictimCacheLineState.INVALID) {
//                htLLCRequestFound = true;
//                break;
//            }
//        }
//
//        if (htLLCRequestFound) {
//            //case 5
//            this.removeLRU(event.getSet());
//            this.insertDataEntry(event.getSet(), victimTag, event.getTag());
//        }

        checkInvariants(event.getSet());
    }

    //    private boolean checkInvariantsEnabled = true;
    private boolean checkInvariantsEnabled = false;

    private void checkInvariants(int set) {
        if (checkInvariantsEnabled) {
            int numHelperThreadLinesInL2 = select(this.helperThreadL2CacheRequestStates.get(set).values(), having(on(HelperThreadL2CacheRequestState.class).getThreadId(), equalTo(BasicThread.getHelperThreadId()))).size();
            int numMainThreadLinesInL2 = select(this.helperThreadL2CacheRequestStates.get(set).values(), having(on(HelperThreadL2CacheRequestState.class).getThreadId(), not(BasicThread.getHelperThreadId()))).size();
            int numVictimEntriesInVictimCache = select(this.helperThreadL2CacheRequestVictimCache.getLines(set), having(on(CacheLine.class).getState(), not(HelperThreadL2CacheRequestVictimCacheLineState.INVALID))).size();

            if (numHelperThreadLinesInL2 != numVictimEntriesInVictimCache || numVictimEntriesInVictimCache + numMainThreadLinesInL2 > this.l2CacheController.getCache().getAssociativity()) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < this.l2CacheController.getCache().getAssociativity(); i++) {
                CacheLine<DirectoryControllerState> line = this.l2CacheController.getCache().getLine(set, i);
                if (line.getState().isStable() && line.isValid() && this.helperThreadL2CacheRequestStates.get(set).get(i).getThreadId() == BasicThread.getHelperThreadId()) {
                    int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHelperThreadL2CacheRequestTag(set, line.getTag());

                    if (wayOfVictimCacheLine == -1) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    private void incrementUglyHelperThreadL2CacheRequests() {
        this.numUglyHelperThreadL2CacheRequests++;
    }

    private void incrementBadHelperThreadL2CacheRequests() {
        this.numBadHelperThreadL2CacheRequests++;
    }

    private void markInvalid(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, -1, false);
        this.markLate(set, way, false);
    }

    private void markHelperThread(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getHelperThreadId(), false);
    }

    private void markMainThread(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getMainThreadId(), false);
    }

    private void markTransientThreadId(int set, int way, int threadId) {
        this.setLLCLineBroughterThreadId(set, way, threadId, true);
    }

    private void setLLCLineBroughterThreadId(int set, int way, int llcLineBroughterThreadId, boolean inFlight) {
        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = this.helperThreadL2CacheRequestStates.get(set).get(way);

        if (inFlight) {
            helperThreadL2CacheRequestState.inFlightThreadId = llcLineBroughterThreadId;
        } else {
            helperThreadL2CacheRequestState.inFlightThreadId = -1;
            helperThreadL2CacheRequestState.threadId = llcLineBroughterThreadId;
        }
    }

    private void markLate(int set, int way, boolean late) {
        HelperThreadL2CacheRequestState helperThreadL2CacheRequestState = this.helperThreadL2CacheRequestStates.get(set).get(way);
        helperThreadL2CacheRequestState.hitToTransientTag = late;
    }

    private void insertDataEntry(int set, int tag, int helperThreadRequestTag) {
        if (tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        CacheAccess<HelperThreadL2CacheRequestVictimCacheLineState> newMiss = this.newMiss(tag, set);
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = newMiss.getLine();
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.DATA;
        stateProvider.helperThreadRequestTag = helperThreadRequestTag;
        line.setTag(tag);
        helperThreadL2CacheRequestVictimCache.getReplacementPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

    private void insertNullEntry(int set, int helperThreadRequestTag) {
        CacheAccess<HelperThreadL2CacheRequestVictimCacheLineState> newMiss = this.newMiss(0, set);
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = newMiss.getLine();
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.NULL;
        stateProvider.helperThreadRequestTag = helperThreadRequestTag;
        line.setTag(CacheLine.INVALID_TAG);
        helperThreadL2CacheRequestVictimCache.getReplacementPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

//    private void setLRU(int set, int way) {
//        this.getLruPolicyForHelperThreadL2RequestVictimCache().setLRU(set, way);
//    }
//
//    private void removeLRU(int set) {
//        LRUPolicy<HelperThreadL2CacheRequestVictimCacheLineState> lru = this.getLruPolicyForHelperThreadL2RequestVictimCache();
//
//        for (int i = this.l2CacheController.getCache().getAssociativity() - 1; i >= 0; i--) {
//            int way = lru.getWayInStackPosition(set, i);
//            CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
//            if (!line.getState().equals(HelperThreadL2CacheRequestVictimCacheLineState.INVALID)) {
//                invalidateVictimCacheLine(set, way);
//                return;
//            }
//        }
//
//        throw new IllegalArgumentException();
//    }

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

    private void invalidateVictimCacheLine(int set, int way) {
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.INVALID;
        stateProvider.helperThreadRequestTag = CacheLine.INVALID_TAG;
        line.setTag(CacheLine.INVALID_TAG);
    }

    private void clearVictimInVictimCacheLine(int set, int way) {
        CacheLine<HelperThreadL2CacheRequestVictimCacheLineState> line = this.helperThreadL2CacheRequestVictimCache.getLine(set, way);
        HelperThreadL2CacheRequestVictimCacheLineStateValueProvider stateProvider = (HelperThreadL2CacheRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HelperThreadL2CacheRequestVictimCacheLineState.NULL;
        line.setTag(CacheLine.INVALID_TAG);
    }

    private LRUPolicy<HelperThreadL2CacheRequestVictimCacheLineState> getLruPolicyForHelperThreadL2RequestVictimCache() {
        return (LRUPolicy<HelperThreadL2CacheRequestVictimCacheLineState>) this.helperThreadL2CacheRequestVictimCache.getReplacementPolicy();
    }

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

    public double getHelperThreadL2CacheRequestCoverage() {
        return (this.numMainThreadL2CacheMisses + this.numUsefulHelperThreadL2CacheRequests) == 0 ? 0 : (double) this.numUsefulHelperThreadL2CacheRequests / (this.numMainThreadL2CacheMisses + this.numUsefulHelperThreadL2CacheRequests);
    }

    public double getHelperThreadL2CacheRequestAccuracy() {
        return this.numTotalHelperThreadL2CacheRequests == 0 ? 0 : (double) this.numUsefulHelperThreadL2CacheRequests / this.numTotalHelperThreadL2CacheRequests;
    }

    public boolean getSummedUpUnstableHelperThreadL2CacheRequests() {
        this.sumUpUnstableHTLLCRequests();
        return true;
    }

    public long getNumMainThreadL2CacheHits() {
        return numMainThreadL2CacheHits;
    }

    public long getNumMainThreadL2CacheMisses() {
        return numMainThreadL2CacheMisses;
    }

    public long getNumTotalHelperThreadL2CacheRequests() {
        return numTotalHelperThreadL2CacheRequests;
    }

    public long getNumRedundantHitToTransientTagHelperThreadL2CacheRequests() {
        return numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    }

    public long getNumRedundantHitToCacheHelperThreadL2CacheRequests() {
        return numRedundantHitToCacheHelperThreadL2CacheRequests;
    }

    public long getNumUsefulHelperThreadL2CacheRequests() {
        return numUsefulHelperThreadL2CacheRequests;
    }

    public long getNumTimelyHelperThreadL2CacheRequests() {
        return numTimelyHelperThreadL2CacheRequests;
    }

    public long getNumLateHelperThreadL2CacheRequests() {
        return numLateHelperThreadL2CacheRequests;
    }

    public long getNumBadHelperThreadL2CacheRequests() {
        return numBadHelperThreadL2CacheRequests;
    }

    public long getNumUglyHelperThreadL2CacheRequests() {
        return numUglyHelperThreadL2CacheRequests;
    }

    public boolean isCheckInvariantsEnabled() {
        return checkInvariantsEnabled;
    }

    public static enum HelperThreadL2CacheRequestVictimCacheLineState {
        INVALID,
        NULL,
        DATA
    }

    private static class HelperThreadL2CacheRequestVictimCacheLineStateValueProvider implements ValueProvider<HelperThreadL2CacheRequestVictimCacheLineState> {
        private HelperThreadL2CacheRequestVictimCacheLineState state;
        private int helperThreadRequestTag;

        public HelperThreadL2CacheRequestVictimCacheLineStateValueProvider() {
            this.state = HelperThreadL2CacheRequestVictimCacheLineState.INVALID;
            this.helperThreadRequestTag = CacheLine.INVALID_TAG;
        }

        @Override
        public HelperThreadL2CacheRequestVictimCacheLineState get() {
            return state;
        }

        @Override
        public HelperThreadL2CacheRequestVictimCacheLineState getInitialValue() {
            return HelperThreadL2CacheRequestVictimCacheLineState.INVALID;
        }
    }
}
