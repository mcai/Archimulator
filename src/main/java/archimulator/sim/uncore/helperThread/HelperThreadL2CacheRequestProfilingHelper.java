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
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.action.Action1;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper thread L2 cache request profiling helper.
 *
 * @author Min Cai
 */
public class HelperThreadL2CacheRequestProfilingHelper implements Reportable {
    private DirectoryController l2CacheController;

    private Map<Integer, Map<Integer, HelperThreadL2CacheRequestState>> helperThreadL2CacheRequestStates;

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

    private long numEarlyHelperThreadL2CacheRequests;

    private long numUglyHelperThreadL2CacheRequests;

    private Predictor<Boolean> helperThreadL2CacheRequestQualityPredictor;

    /**
     * Create a helper thread L2 request profiling helper.
     *
     * @param simulation simulation
     */
    public HelperThreadL2CacheRequestProfilingHelper(Simulation simulation) {
        this.l2CacheController = simulation.getProcessor().getMemoryHierarchy().getL2CacheController();

        this.helperThreadL2CacheRequestStates = new HashMap<Integer, Map<Integer, HelperThreadL2CacheRequestState>>();
        for (int set = 0; set < this.l2CacheController.getCache().getNumSets(); set++) {
            HashMap<Integer, HelperThreadL2CacheRequestState> helperThreadL2CacheRequestStatesPerSet = new HashMap<Integer, HelperThreadL2CacheRequestState>();
            this.helperThreadL2CacheRequestStates.put(set, helperThreadL2CacheRequestStatesPerSet);

            for (int way = 0; way < this.l2CacheController.getCache().getAssociativity(); way++) {
                helperThreadL2CacheRequestStatesPerSet.put(way, new HelperThreadL2CacheRequestState());
            }
        }

        this.helperThreadL2CacheRequestQualityPredictor = new CacheBasedPredictor<Boolean>(l2CacheController, l2CacheController.getName() + "/helperThreadL2CacheRequestQualityPredictor", new CacheGeometry(64, 1, 1), 4, 16, false); //TODO: parameters should not be hardcoded

        this.l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();
                    boolean requesterIsHelperThread = HelperThreadingHelper.isHelperThread(event.getAccess().getThread());
                    boolean lineFoundIsHelperThread = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();

                    handleL2CacheRequest(event, requesterIsHelperThread, lineFoundIsHelperThread);
                }
            }
        });

        this.l2CacheController.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, new Action1<LastLevelCacheControllerLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheControllerLineInsertEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();
                    boolean lineFoundIsHelperThread = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();

                    handleL2CacheLineInsert(event, lineFoundIsHelperThread);
                }
            }
        });

        this.l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent.class, new Action1<GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent>() {
            @Override
            public void apply(GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();

                    markInvalid(set, event.getWay());
                }
            }
        });

        this.l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerNonblockingRequestHitToTransientTagEvent.class, new Action1<GeneralCacheControllerNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(GeneralCacheControllerNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCacheController().equals(HelperThreadL2CacheRequestProfilingHelper.this.l2CacheController)) {
                    int set = event.getSet();

                    int requesterThreadId = event.getAccess().getThread().getId();
                    int lineFoundThreadId = helperThreadL2CacheRequestStates.get(set).get(event.getWay()).getInFlightThreadId();

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

        this.l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestEvent event) {
                helperThreadL2CacheRequestQualityPredictor.update(event.getPc(), event.getQuality().isUseful());
            }
        });
    }

    /**
     * Sum up the unstable helper thread L2 cache requests.
     */
    public void sumUpUnstableHelperThreadL2CacheRequests() {
        for (int set = 0; set < this.l2CacheController.getCache().getNumSets(); set++) {
            for (int way = 0; way < this.l2CacheController.getCache().getAssociativity(); way++) {
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
        CacheLine<DirectoryControllerState> llcLine = this.l2CacheController.getCache().getLine(set, way);
        HelperThreadL2CacheRequestState llcLineState = this.helperThreadL2CacheRequestStates.get(set).get(way);

        boolean lineFoundIsHelperThread = this.helperThreadL2CacheRequestStates.get(set).get(way).getThreadId() == HelperThreadingHelper.getHelperThreadId();

        if (lineFoundIsHelperThread) {
            if (llcLineState.getQuality() == HelperThreadL2CacheRequestQuality.UGLY) {
                //Ugly.
                this.numUglyHelperThreadL2CacheRequests++;
                this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                        HelperThreadL2CacheRequestQuality.UGLY,
                        set,
                        llcLineState.getThreadId(),
                        llcLineState.getPc(),
                        llcLine.getTag()
                ));
            } else if (llcLineState.getQuality() == HelperThreadL2CacheRequestQuality.BAD) {
            } else {
                throw new IllegalArgumentException(llcLineState.getQuality() + "");
            }
        }

        llcLineState.setVictimThreadId(-1);
        llcLineState.setVictimPc(-1);
        llcLineState.setVictimTag(CacheLine.INVALID_TAG);
        llcLineState.setQuality(HelperThreadL2CacheRequestQuality.INVALID);
        this.setL2CacheLineBroughterThreadId(set, way, -1, -1, false);
    }

    /**
     * Handle an L2 cache request.
     *
     * @param event                   the event
     * @param requesterIsHelperThread a value indicating whether the requester is the main thread or not
     * @param lineFoundIsHelperThread a value indicating whether the line found is brought by the helper thread or not
     */
    private void handleL2CacheRequest(GeneralCacheControllerServiceNonblockingRequestEvent event, boolean requesterIsHelperThread, boolean lineFoundIsHelperThread) {
        boolean mainThreadHit = event.isHitInCache() && !requesterIsHelperThread && !lineFoundIsHelperThread;
        boolean helperThreadHit = event.isHitInCache() && !requesterIsHelperThread && lineFoundIsHelperThread;

        int victimWay = this.findWayOfL2CacheLineByVictimTag(event.getSet(), event.getTag());
        CacheLine<DirectoryControllerState> victimLine = victimWay != -1 ? this.l2CacheController.getCache().getLine(event.getSet(), victimWay) : null;
        HelperThreadL2CacheRequestState victimLineState = victimWay != -1 ? this.helperThreadL2CacheRequestStates.get(event.getSet()).get(victimWay) : null;

        boolean victimHit = victimLine != null;
        boolean victimEvicterMainThreadHit = victimLine != null && this.helperThreadL2CacheRequestStates.get(event.getSet()).get(victimWay).getThreadId() == HelperThreadingHelper.getMainThreadId();
        boolean victimEvicterHelperThreadHit = victimLine != null && this.helperThreadL2CacheRequestStates.get(event.getSet()).get(victimWay).getThreadId() == HelperThreadingHelper.getHelperThreadId();
        boolean victimMainThreadHit = victimLine != null && this.helperThreadL2CacheRequestStates.get(event.getSet()).get(victimWay).getVictimThreadId() == HelperThreadingHelper.getMainThreadId();
        boolean victimHelperThreadHit = victimLine != null && this.helperThreadL2CacheRequestStates.get(event.getSet()).get(victimWay).getVictimThreadId() == HelperThreadingHelper.getHelperThreadId();

        CacheLine<DirectoryControllerState> llcLine = this.l2CacheController.getCache().getLine(event.getSet(), event.getWay());
        HelperThreadL2CacheRequestState llcLineState = this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay());

        if (!requesterIsHelperThread) {
            if (event.isHitInCache()) {
                this.numMainThreadL2CacheHits++;
                this.l2CacheController.getBlockingEventDispatcher().dispatch(new MainThreadL2CacheHitEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));

                if (lineFoundIsHelperThread) {
                    this.numUsefulHelperThreadL2CacheRequests++;
                }
            } else {
                this.numMainThreadL2CacheMisses++;
                this.l2CacheController.getBlockingEventDispatcher().dispatch(new MainThreadL2CacheMissEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));
            }
        } else {
            if (event.isHitInCache()) {
                this.numHelperThreadL2CacheHits++;
                this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheHitEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));
            } else {
                this.numHelperThreadL2CacheMisses++;
                this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheMissEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));
            }

            if (event.isHitInCache() && !lineFoundIsHelperThread) {
                //Redundant.
                if (llcLineState.isHitToTransientTag()) {
                    this.numRedundantHitToTransientTagHelperThreadL2CacheRequests++;
                    this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                            HelperThreadL2CacheRequestQuality.REDUNDANT_HIT_TO_TRANSIENT_TAG,
                            event.getSet(),
                            event.getAccess().getThread().getId(),
                            event.getAccess().getVirtualPc(),
                            event.getTag()
                    ));
                } else {
                    this.numRedundantHitToCacheHelperThreadL2CacheRequests++;
                    this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                            HelperThreadL2CacheRequestQuality.REDUNDANT_HIT_TO_CACHE,
                            event.getSet(),
                            event.getAccess().getThread().getId(),
                            event.getAccess().getVirtualPc(),
                            event.getTag()
                    ));
                }
            }
        }

        if (!event.isHitInCache()) {
            this.markTransientThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc());
        }

        if (!requesterIsHelperThread) {
            if (!mainThreadHit && !helperThreadHit) {
                if (!victimHit) {
                    //No action.
                } else if (victimEvicterMainThreadHit && victimMainThreadHit) {
                    //No action.
                } else if (victimEvicterHelperThreadHit && victimMainThreadHit) {
                    bad(event, victimLine, victimLineState);
                } else if (victimEvicterMainThreadHit && victimHelperThreadHit) {
                    early(event, victimLine, victimLineState);
                } else if (victimEvicterHelperThreadHit && victimHelperThreadHit) {
                    //Ugly.
                }
            } else if (helperThreadHit) {
                if (!victimHit) {
                    good(event, llcLine, llcLineState);
                } else if (victimEvicterMainThreadHit && victimMainThreadHit) {
                    good(event, llcLine, llcLineState);
                } else if (victimEvicterHelperThreadHit && victimMainThreadHit) {
                    good(event, llcLine, llcLineState);
                    bad(event, victimLine, victimLineState);
                } else if (victimEvicterMainThreadHit && victimHelperThreadHit) {
                    good(event, llcLine, llcLineState);
                    early(event, victimLine, victimLineState);
                } else if (victimEvicterHelperThreadHit && victimHelperThreadHit) {
                    good(event, llcLine, llcLineState);
                }
            } else {
                if (!victimHit) {
                } else if (victimEvicterMainThreadHit && victimMainThreadHit) {
                } else if (victimEvicterHelperThreadHit && victimMainThreadHit) {
                } else if (victimEvicterMainThreadHit && victimHelperThreadHit) {
                } else if (victimEvicterHelperThreadHit && victimHelperThreadHit) {
                }
            }
        }

        if (event.isHitInCache()) {
            llcLineState.setVictimThreadId(llcLineState.getThreadId());
            llcLineState.setVictimPc(llcLineState.getPc());
            llcLineState.setVictimTag(llcLine.getTag());
            llcLineState.setQuality(HelperThreadingHelper.isHelperThread(event.getAccess().getThread()) ? HelperThreadL2CacheRequestQuality.UGLY : HelperThreadL2CacheRequestQuality.INVALID);
            this.setL2CacheLineBroughterThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), false);
        }

        if (victimHit) {
            victimLineState.setVictimThreadId(-1);
            victimLineState.setVictimPc(-1);
            victimLineState.setVictimTag(CacheLine.INVALID_TAG);
        }
    }

    /**
     * A good helper thread L2 request has been identified.
     *
     * @param event        the event
     * @param llcLine      the LLC line
     * @param llcLineState the LLC line state
     */
    private void good(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<DirectoryControllerState> llcLine, HelperThreadL2CacheRequestState llcLineState) {
        //Good.
        if (llcLineState.isHitToTransientTag()) {
            this.numLateHelperThreadL2CacheRequests++;
            this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                    HelperThreadL2CacheRequestQuality.LATE,
                    event.getSet(),
                    llcLineState.getInFlightThreadId(),
                    llcLineState.getPc(),
                    llcLine.getTag()
            ));
        } else {
            this.numTimelyHelperThreadL2CacheRequests++;
            this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                    HelperThreadL2CacheRequestQuality.TIMELY,
                    event.getSet(),
                    llcLineState.getThreadId(),
                    llcLineState.getPc(),
                    llcLine.getTag()
            ));
        }
    }

    /**
     * A bad helper thread L2 request has been identified.
     *
     * @param event           the event
     * @param victimLine      the victim line
     * @param victimLineState the victim line state
     */
    private void bad(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<DirectoryControllerState> victimLine, HelperThreadL2CacheRequestState victimLineState) {
        //Bad.
        this.helperThreadL2CacheRequestStates.get(event.getSet()).get(victimLine.getWay()).setQuality(HelperThreadL2CacheRequestQuality.BAD);
        this.numBadHelperThreadL2CacheRequests++;
        this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                HelperThreadL2CacheRequestQuality.BAD,
                event.getSet(),
                victimLineState.getThreadId(),
                victimLineState.getPc(),
                victimLine.getTag()
        ));
    }

    /**
     * An early helper thread L2 request has been identified.
     *
     * @param event           the event
     * @param victimLine      the victim line
     * @param victimLineState the victim line state
     */
    private void early(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<DirectoryControllerState> victimLine, HelperThreadL2CacheRequestState victimLineState) {
        //Early.
        this.numEarlyHelperThreadL2CacheRequests++;
        this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                HelperThreadL2CacheRequestQuality.EARLY,
                event.getSet(),
                victimLineState.getVictimThreadId(),
                victimLineState.getVictimPc(),
                victimLineState.getVictimTag()
        ));
    }

    /**
     * Handle an L2 cache line insertion.
     *
     * @param event                   the event
     * @param lineFoundIsHelperThread a value indicating whether the LLC line is brought by the helper thread or not
     */
    private void handleL2CacheLineInsert(LastLevelCacheControllerLineInsertEvent event, boolean lineFoundIsHelperThread) {
        CacheLine<DirectoryControllerState> llcLine = this.l2CacheController.getCache().getLine(event.getSet(), event.getWay());
        HelperThreadL2CacheRequestState llcLineState = this.helperThreadL2CacheRequestStates.get(event.getSet()).get(event.getWay());

        if (lineFoundIsHelperThread) {
            if (llcLineState.getQuality() == HelperThreadL2CacheRequestQuality.UGLY) {
                //Ugly.
                this.numUglyHelperThreadL2CacheRequests++;
                this.l2CacheController.getBlockingEventDispatcher().dispatch(new HelperThreadL2CacheRequestEvent(
                        HelperThreadL2CacheRequestQuality.UGLY,
                        event.getSet(),
                        llcLineState.getThreadId(),
                        llcLineState.getPc(),
                        llcLine.getTag()
                ));
            } else if (llcLineState.getQuality() == HelperThreadL2CacheRequestQuality.BAD) {
            } else {
                throw new IllegalArgumentException(llcLineState.getQuality() + "");
            }
        }

        if (!event.isEviction()) {
            llcLineState.setVictimThreadId(-1);
            llcLineState.setVictimPc(-1);
            llcLineState.setVictimTag(CacheLine.INVALID_TAG);
        } else {
            llcLineState.setVictimThreadId(llcLineState.getThreadId());
            llcLineState.setVictimPc(llcLineState.getVictimPc());
            llcLineState.setVictimTag(event.getVictimTag());
        }

        llcLineState.setQuality(HelperThreadingHelper.isHelperThread(event.getAccess().getThread()) ? HelperThreadL2CacheRequestQuality.UGLY : HelperThreadL2CacheRequestQuality.INVALID);
        this.setL2CacheLineBroughterThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), false);
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
            getChildren().add(new ReportNode(this, "numEarlyHelperThreadL2CacheRequests", getNumEarlyHelperThreadL2CacheRequests() + ""));
            getChildren().add(new ReportNode(this, "numUglyHelperThreadL2CacheRequests", getNumUglyHelperThreadL2CacheRequests() + ""));

            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestCoverage", getHelperThreadL2CacheRequestCoverage() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestAccuracy", getHelperThreadL2CacheRequestAccuracy() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestLateness", getHelperThreadL2CacheRequestLateness() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestPollution", getHelperThreadL2CacheRequestPollution() + ""));
            getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestRedundancy", getHelperThreadL2CacheRequestRedundancy() + ""));

            getHelperThreadL2CacheRequestQualityPredictor().dumpStats(this);
        }});
    }

    /**
     * Find the way of the L2 cache line matching the specified victim tag.
     *
     * @param set       the set index
     * @param victimTag the victim tag
     * @return the way of the L2 cache line matching the specified victim tag
     */
    private int findWayOfL2CacheLineByVictimTag(int set, int victimTag) {
        for (int way = 0; way < this.l2CacheController.getCache().getAssociativity(); way++) {
            HelperThreadL2CacheRequestState state = this.helperThreadL2CacheRequestStates.get(set).get(way);
            if (state.getVictimTag() == victimTag) {
                return way;
            }
        }

        return -1;
    }

    /**
     * Mark as invalid for the specified set index and way.
     *
     * @param set the set index
     * @param way the way
     */
    private void markInvalid(int set, int way) {
        HelperThreadL2CacheRequestState llcLineState = this.helperThreadL2CacheRequestStates.get(set).get(way);

        this.setL2CacheLineBroughterThreadId(set, way, -1, -1, false);
        llcLineState.setPc(-1);
        llcLineState.setVictimThreadId(-1);
        llcLineState.setVictimPc(-1);
        llcLineState.setVictimTag(CacheLine.INVALID_TAG);
        this.markLate(set, way, false);
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

        this.helperThreadL2CacheRequestQualityPredictor.update(pc, helperThreadL2CacheRequestQuality.isUseful());
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
     * Get the helper thread L2 cache request quality predictor.
     *
     * @return the helper thread L2 cache request quality predictor
     */
    public Predictor<Boolean> getHelperThreadL2CacheRequestQualityPredictor() {
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
     * Get the number of early helper thread L2 cache requests.
     *
     * @return the number of early helper thread L2 cache requests
     */
    public long getNumEarlyHelperThreadL2CacheRequests() {
        return numEarlyHelperThreadL2CacheRequests;
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
     * L2 cache request event.
     */
    public abstract class L2CacheRequestEvent extends SimulationEvent {
        private int set;
        private int threadId;
        private int pc;
        private int tag;

        /**
         * Create a L2 cache request event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public L2CacheRequestEvent(int set, int threadId, int pc, int tag) {
            super(l2CacheController);
            this.set = set;
            this.threadId = threadId;
            this.pc = pc;
            this.tag = tag;
        }

        /**
         * Get the set.
         *
         * @return the set
         */
        public int getSet() {
            return set;
        }

        /**
         * Get the thread ID.
         *
         * @return the thread ID
         */
        public int getThreadId() {
            return threadId;
        }

        /**
         * Get the program counter value.
         *
         * @return the program counter value
         */
        public int getPc() {
            return pc;
        }

        /**
         * Get the tag.
         *
         * @return the tag
         */
        public int getTag() {
            return tag;
        }
    }

    /**
     * Helper thread L2 cache request event.
     */
    public class HelperThreadL2CacheRequestEvent extends L2CacheRequestEvent {
        private HelperThreadL2CacheRequestQuality quality;

        /**
         * Create a helper thread L2 cache request event.
         *
         * @param quality  the quality
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public HelperThreadL2CacheRequestEvent(HelperThreadL2CacheRequestQuality quality, int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
            this.quality = quality;
        }

        /**
         * Get the quality.
         *
         * @return the quality
         */
        public HelperThreadL2CacheRequestQuality getQuality() {
            return quality;
        }
    }

    /**
     * Main thread L2 cache hit event.
     */
    public class MainThreadL2CacheHitEvent extends L2CacheRequestEvent {
        /**
         * Create a main thread L2 cache hit event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public MainThreadL2CacheHitEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }

    /**
     * Main thread L2 cache miss event.
     */
    public class MainThreadL2CacheMissEvent extends L2CacheRequestEvent {
        /**
         * Create a main thread L2 cache miss event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public MainThreadL2CacheMissEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }

    /**
     * Helper thread L2 cache hit event.
     */
    public class HelperThreadL2CacheHitEvent extends L2CacheRequestEvent {
        /**
         * Create a helper thread L2 cache hit event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public HelperThreadL2CacheHitEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }

    /**
     * Main thread L2 cache miss event.
     */
    public class HelperThreadL2CacheMissEvent extends L2CacheRequestEvent {
        /**
         * Create a main thread L2 cache miss event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public HelperThreadL2CacheMissEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }
}
