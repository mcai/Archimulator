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
import archimulator.sim.core.Thread;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.cache.prediction.Predictor;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper thread L2 cache request profiling helper.
 *
 * Redundant: HT->HT (USED=true), REDUNDANT++
 * Good: HT->MT, GOOD++
 * Bad: HT->HT (victim->INVALID, USED=true), BAD++
 * Early: MT->MT (victim->INVALID), EARLY++,UGLY--
 *
 * @author Min Cai
 */
public class HelperThreadL2RequestProfilingHelper implements Reportable {
    private DirectoryController l2Controller;

    private Map<Integer, Map<Integer, HelperThreadL2RequestState>> helperThreadL2RequestStates;

    private long numMainThreadL2Hits;
    private long numMainThreadL2Misses;

    private long numHelperThreadL2Hits;
    private long numHelperThreadL2Misses;

    private long numRedundantHitToTransientTagHelperThreadL2Requests;
    private long numRedundantHitToCacheHelperThreadL2Requests;

    private long numUsefulHelperThreadL2Requests;

    private long numTimelyHelperThreadL2Requests;
    private long numLateHelperThreadL2Requests;

    private long numBadHelperThreadL2Requests;

    private long numEarlyHelperThreadL2Requests;

    private Predictor<HelperThreadL2RequestQuality> helperThreadL2RequestQualityPredictor;
    private Predictor<Boolean> helperThreadL2RequestUsefulnessPredictor;
    private Predictor<Boolean> helperThreadL2RequestPollutionPredictor;

    /**
     * Create a helper thread L2 request profiling helper.
     *
     * @param simulation simulation
     */
    public HelperThreadL2RequestProfilingHelper(Simulation simulation) {
        this.l2Controller = simulation.getProcessor().getMemoryHierarchy().getL2Controller();

        this.helperThreadL2RequestStates = new HashMap<>();
        for (int set = 0; set < this.l2Controller.getCache().getNumSets(); set++) {
            HashMap<Integer, HelperThreadL2RequestState> helperThreadL2RequestStatesPerSet = new HashMap<>();
            this.helperThreadL2RequestStates.put(set, helperThreadL2RequestStatesPerSet);

            for (int way = 0; way < this.l2Controller.getCache().getAssociativity(); way++) {
                helperThreadL2RequestStatesPerSet.put(way, new HelperThreadL2RequestState());
            }
        }

        this.helperThreadL2RequestQualityPredictor = new CacheBasedPredictor<>(
                this.l2Controller,
                this.l2Controller.getName() + "/helperThreadL2RequestQualityPredictor",
                64,
                4,
                16,
                HelperThreadL2RequestQuality.UGLY
        );

        this.helperThreadL2RequestUsefulnessPredictor = new CacheBasedPredictor<>(
                this.l2Controller,
                this.l2Controller.getName() + "/helperThreadL2RequestUsefulnessPredictor",
                64,
                4,
                16,
                false
        );

        this.helperThreadL2RequestPollutionPredictor = new CacheBasedPredictor<>(
                this.l2Controller,
                this.l2Controller.getName() + "/helperThreadL2RequestPollutionPredictor",
                64,
                4,
                16,
                false
        );

        this.l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController().equals(HelperThreadL2RequestProfilingHelper.this.l2Controller)) {
                boolean requesterIsHelperThread = HelperThreadingHelper.isHelperThread(event.getAccess().getThread());
                boolean lineFoundIsHelperThread = helperThreadL2RequestStates.get(event.getSet()).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();
                handleL2Request(event, requesterIsHelperThread, lineFoundIsHelperThread);
            }
        });

        this.l2Controller.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, event -> {
            if (event.getCacheController().equals(HelperThreadL2RequestProfilingHelper.this.l2Controller)) {
                boolean lineFoundIsHelperThread = helperThreadL2RequestStates.get(event.getSet()).get(event.getWay()).getThreadId() == HelperThreadingHelper.getHelperThreadId();
                handleL2LineInsert(event, lineFoundIsHelperThread);
            }
        });

        this.l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerLastPutSOrPutMAndDataFromOwnerEvent.class, event -> {
            if (event.getCacheController().equals(HelperThreadL2RequestProfilingHelper.this.l2Controller)) {
                markInvalid(event.getSet(), event.getWay());
            }
        });

        this.l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerNonblockingRequestHitToTransientTagEvent.class, event -> {
            if (event.getCacheController().equals(HelperThreadL2RequestProfilingHelper.this.l2Controller)) {
                int set = event.getSet();

                int requesterThreadId = event.getAccess().getThread().getId();
                int lineFoundThreadId = helperThreadL2RequestStates.get(set).get(event.getWay()).getInFlightThreadId();

                boolean requesterIsHelperThread = HelperThreadingHelper.isHelperThread(requesterThreadId);
                boolean lineFoundIsHelperThread = HelperThreadingHelper.isHelperThread(lineFoundThreadId);

                if (!requesterIsHelperThread && lineFoundIsHelperThread) {
                    markLate(set, event.getWay(), true);
                } else if (requesterIsHelperThread && !lineFoundIsHelperThread) {
                    markLate(set, event.getWay(), true);
                }
            }
        });

        this.l2Controller.getBlockingEventDispatcher().addListener(
                HelperThreadL2RequestEvent.class,
                event -> {
                    helperThreadL2RequestQualityPredictor.update(event.getPc(), event.getQuality());
                    helperThreadL2RequestUsefulnessPredictor.update(event.getPc(), event.getQuality().isUseful());
                    helperThreadL2RequestPollutionPredictor.update(event.getPc(), event.getQuality().isPolluting());
                }
        );
    }

    /**
     * Handle an L2 cache request.
     *
     * @param event                   the event
     * @param requesterIsHelperThread a value indicating whether the requester is the main thread or not
     * @param lineFoundIsHelperThread a value indicating whether the line found is brought by the helper thread or not
     */
    private void handleL2Request(GeneralCacheControllerServiceNonblockingRequestEvent event, boolean requesterIsHelperThread, boolean lineFoundIsHelperThread) {
        int victimWay = this.findWayOfL2LineByVictimTag(event.getSet(), event.getTag());
        CacheLine<DirectoryControllerState> victimLine = victimWay != -1 ? this.l2Controller.getCache().getLine(event.getSet(), victimWay) : null;
        HelperThreadL2RequestState victimLineState = victimWay != -1 ? this.helperThreadL2RequestStates.get(event.getSet()).get(victimWay) : null;

        boolean victimHit = victimLine != null;
        boolean victimEvicterMainThreadHit = victimHit && this.helperThreadL2RequestStates.get(event.getSet()).get(victimWay).getThreadId() == HelperThreadingHelper.getMainThreadId();
        boolean victimEvicterHelperThreadHit = victimHit && !victimLineState.isUsed() && this.helperThreadL2RequestStates.get(event.getSet()).get(victimWay).getThreadId() == HelperThreadingHelper.getHelperThreadId();
        boolean victimMainThreadHit = victimHit && this.helperThreadL2RequestStates.get(event.getSet()).get(victimWay).getVictimThreadId() == HelperThreadingHelper.getMainThreadId();
        boolean victimHelperThreadHit = victimHit && this.helperThreadL2RequestStates.get(event.getSet()).get(victimWay).getVictimThreadId() == HelperThreadingHelper.getHelperThreadId();

        CacheLine<DirectoryControllerState> llcLine = this.l2Controller.getCache().getLine(event.getSet(), event.getWay());
        HelperThreadL2RequestState llcLineState = this.helperThreadL2RequestStates.get(event.getSet()).get(event.getWay());

        boolean mainThreadHit = event.isHitInCache() && !requesterIsHelperThread && !lineFoundIsHelperThread;
        boolean helperThreadHit = event.isHitInCache() && !llcLineState.isUsed() && !requesterIsHelperThread && lineFoundIsHelperThread;

        if (!requesterIsHelperThread) {
            if (event.isHitInCache()) {
                this.numMainThreadL2Hits++;
                this.l2Controller.getBlockingEventDispatcher().dispatch(new MainThreadL2HitEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));

                if (lineFoundIsHelperThread && !llcLineState.isUsed()) {
                    this.numUsefulHelperThreadL2Requests++;
                }
            } else {
                this.numMainThreadL2Misses++;
                this.l2Controller.getBlockingEventDispatcher().dispatch(new MainThreadL2MissEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));
            }
        } else {
            if (event.isHitInCache()) {
                this.numHelperThreadL2Hits++;
                this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2HitEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));
            } else {
                this.numHelperThreadL2Misses++;
                this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2MissEvent(
                        event.getSet(),
                        event.getAccess().getThread().getId(),
                        event.getAccess().getVirtualPc(),
                        event.getTag()
                ));
            }

            if (event.isHitInCache() && !lineFoundIsHelperThread) {
                redundant(event, llcLineState);
            }
        }

        if (!event.isHitInCache()) {
            this.setL2LineBroughterThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), true);
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
                    early(event, victimLineState);
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
                    early(event, victimLineState);
                } else if (victimEvicterHelperThreadHit && victimHelperThreadHit) {
                    good(event, llcLine, llcLineState);
                }
            } else {
                if (!victimHit) {
                    //No action.
                } else if (victimEvicterMainThreadHit && victimMainThreadHit) {
                    //No action.
                } else if (victimEvicterHelperThreadHit && victimMainThreadHit) {
                    //Bandwidth waste.
                } else if (victimEvicterMainThreadHit && victimHelperThreadHit) {
                    //Bandwidth waste.
                } else if (victimEvicterHelperThreadHit && victimHelperThreadHit) {
                    //Bandwidth waste.
                }
            }
        }

        if (event.isHitInCache()) {
            llcLineState.setVictimThreadId(llcLineState.getThreadId());
            llcLineState.setVictimPc(llcLineState.getPc());
            llcLineState.setVictimTag(llcLine.getTag());
            this.setL2LineBroughterThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), false);

            if (requesterIsHelperThread && !lineFoundIsHelperThread) {
                llcLineState.setUsed(true);
            } else {
                llcLineState.setUsed(false);
            }
        }

        if (victimHit) {
            victimLineState.setVictimThreadId(-1);
            victimLineState.setVictimPc(-1);
            victimLineState.setVictimTag(CacheLine.INVALID_TAG);
        }
    }

    /**
     * A redundant helper thread L2 request has been identified.
     *
     * @param event        the event
     * @param llcLineState the LLC line state
     */
    private void redundant(GeneralCacheControllerServiceNonblockingRequestEvent event, HelperThreadL2RequestState llcLineState) {
        //Redundant.
        if (llcLineState.isHitToTransientTag()) {
            this.numRedundantHitToTransientTagHelperThreadL2Requests++;
            this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestEvent(
                    HelperThreadL2RequestQuality.REDUNDANT_HIT_TO_TRANSIENT_TAG,
                    event.getSet(),
                    event.getAccess().getThread().getId(),
                    event.getAccess().getVirtualPc(),
                    event.getTag()
            ));
        } else {
            this.numRedundantHitToCacheHelperThreadL2Requests++;
            this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestEvent(
                    HelperThreadL2RequestQuality.REDUNDANT_HIT_TO_CACHE,
                    event.getSet(),
                    event.getAccess().getThread().getId(),
                    event.getAccess().getVirtualPc(),
                    event.getTag()
            ));
        }
    }

    /**
     * A good helper thread L2 request has been identified.
     *
     * @param event        the event
     * @param llcLine      the LLC line
     * @param llcLineState the LLC line state
     */
    private void good(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<DirectoryControllerState> llcLine, HelperThreadL2RequestState llcLineState) {
        //Good.
        if (llcLineState.isHitToTransientTag()) {
            this.numLateHelperThreadL2Requests++;
            this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestEvent(
                    HelperThreadL2RequestQuality.LATE,
                    event.getSet(),
                    llcLineState.getThreadId(),
                    llcLineState.getPc(),
                    llcLine.getTag()
            ));
        } else {
            this.numTimelyHelperThreadL2Requests++;
            this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestEvent(
                    HelperThreadL2RequestQuality.TIMELY,
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
    private void bad(GeneralCacheControllerServiceNonblockingRequestEvent event, CacheLine<DirectoryControllerState> victimLine, HelperThreadL2RequestState victimLineState) {
        //Bad.
        this.numBadHelperThreadL2Requests++;
        this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestEvent(
                HelperThreadL2RequestQuality.BAD,
                event.getSet(),
                victimLineState.getThreadId(),
                victimLineState.getPc(),
                victimLine.getTag()
        ));
        victimLineState.setUsed(true);
    }

    /**
     * An early helper thread L2 request has been identified.
     *
     * @param event           the event
     * @param victimLineState the victim line state
     */
    private void early(GeneralCacheControllerServiceNonblockingRequestEvent event, HelperThreadL2RequestState victimLineState) {
        //Early.
        this.numEarlyHelperThreadL2Requests++;
        this.l2Controller.getBlockingEventDispatcher().dispatch(new HelperThreadL2RequestEvent(
                HelperThreadL2RequestQuality.EARLY,
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
    private void handleL2LineInsert(LastLevelCacheControllerLineInsertEvent event, boolean lineFoundIsHelperThread) {
        HelperThreadL2RequestState llcLineState = this.helperThreadL2RequestStates.get(event.getSet()).get(event.getWay());

        if (!lineFoundIsHelperThread && llcLineState.isUsed()) {
            throw new IllegalArgumentException();
        }

        if (!event.isEviction()) {
            llcLineState.setVictimThreadId(-1);
            llcLineState.setVictimPc(-1);
            llcLineState.setVictimTag(CacheLine.INVALID_TAG);
        } else {
            llcLineState.setVictimThreadId(llcLineState.getThreadId());
            llcLineState.setVictimPc(llcLineState.getPc());
            llcLineState.setVictimTag(event.getVictimTag());
        }

        this.setL2LineBroughterThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId(), event.getAccess().getVirtualPc(), false);
        llcLineState.setUsed(false);
    }

    /**
     * Find the way of the L2 cache line matching the specified victim tag.
     *
     * @param set       the set index
     * @param victimTag the victim tag
     * @return the way of the L2 cache line matching the specified victim tag
     */
    private int findWayOfL2LineByVictimTag(int set, int victimTag) {
        for (int way = 0; way < this.l2Controller.getCache().getAssociativity(); way++) {
            HelperThreadL2RequestState state = this.helperThreadL2RequestStates.get(set).get(way);
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
        HelperThreadL2RequestState llcLineState = this.helperThreadL2RequestStates.get(set).get(way);

        this.setL2LineBroughterThreadId(set, way, -1, -1, false);
        llcLineState.setPc(-1);
        llcLineState.setVictimThreadId(-1);
        llcLineState.setVictimPc(-1);
        llcLineState.setVictimTag(CacheLine.INVALID_TAG);
        llcLineState.setUsed(false);
        this.markLate(set, way, false);
    }

    /**
     * Set the L2 cache line's broughter thread ID.
     *
     * @param set                          the set index
     * @param way                          the way
     * @param l2LineBroughterThreadId the L2 cache line's broughter thread ID
     * @param pc                           the virtual address of the program counter (PC)
     * @param inFlight                     a value indicating whether it is in-flight or not
     */
    private void setL2LineBroughterThreadId(int set, int way, int l2LineBroughterThreadId, int pc, boolean inFlight) {
        HelperThreadL2RequestState llcLineState = this.helperThreadL2RequestStates.get(set).get(way);

        if (inFlight) {
            llcLineState.setInFlightThreadId(l2LineBroughterThreadId);
            llcLineState.setPc(pc);
        } else {
            llcLineState.setInFlightThreadId(-1);
            llcLineState.setThreadId(l2LineBroughterThreadId);
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
        HelperThreadL2RequestState llcLineState = this.helperThreadL2RequestStates.get(set).get(way);
        llcLineState.setHitToTransientTag(late);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "helperThreadL2RequestProfilingHelper") {{
            getChildren().add(new ReportNode(this, "numMainThreadL2Hits", getNumMainThreadL2Hits() + ""));
            getChildren().add(new ReportNode(this, "numMainThreadL2Misses", getNumMainThreadL2Misses() + ""));

            getChildren().add(new ReportNode(this, "numHelperThreadL2Hits", getNumHelperThreadL2Hits() + ""));
            getChildren().add(new ReportNode(this, "numHelperThreadL2Misses", getNumHelperThreadL2Misses() + ""));

            getChildren().add(new ReportNode(this, "numTotalHelperThreadL2Requests", getNumTotalHelperThreadL2Requests() + ""));

            getChildren().add(new ReportNode(this, "numRedundantHitToTransientTagHelperThreadL2Requests", getNumRedundantHitToTransientTagHelperThreadL2Requests() + ""));
            getChildren().add(new ReportNode(this, "numRedundantHitToCacheHelperThreadL2Requests", getNumRedundantHitToCacheHelperThreadL2Requests() + ""));

            getChildren().add(new ReportNode(this, "numUsefulHelperThreadL2Requests", getNumUsefulHelperThreadL2Requests() + ""));

            getChildren().add(new ReportNode(this, "numTimelyHelperThreadL2Requests", getNumTimelyHelperThreadL2Requests() + ""));
            getChildren().add(new ReportNode(this, "numLateHelperThreadL2Requests", getNumLateHelperThreadL2Requests() + ""));

            getChildren().add(new ReportNode(this, "numBadHelperThreadL2Requests", getNumBadHelperThreadL2Requests() + ""));
            getChildren().add(new ReportNode(this, "numEarlyHelperThreadL2Requests", getNumEarlyHelperThreadL2Requests() + ""));
            getChildren().add(new ReportNode(this, "numUglyHelperThreadL2Requests",
                    (getNumTotalHelperThreadL2Requests()
                            - getNumRedundantHitToCacheHelperThreadL2Requests()
                            - getNumRedundantHitToTransientTagHelperThreadL2Requests()
                            - getNumTimelyHelperThreadL2Requests()
                            - getNumLateHelperThreadL2Requests()
                            - getNumBadHelperThreadL2Requests()
                            - getNumEarlyHelperThreadL2Requests()
                    ) +
                            ""));

            getHelperThreadL2RequestQualityPredictor().dumpStats(this);
        }});
    }

    /**
     * Get the helper thread L2 cache request states.
     *
     * @return the helper thread L2 cache request states
     */
    public Map<Integer, Map<Integer, HelperThreadL2RequestState>> getHelperThreadL2RequestStates() {
        return helperThreadL2RequestStates;
    }

    /**
     * Get the helper thread L2 cache request quality predictor.
     *
     * @return the helper thread L2 cache request quality predictor
     */
    public Predictor<HelperThreadL2RequestQuality> getHelperThreadL2RequestQualityPredictor() {
        return helperThreadL2RequestQualityPredictor;
    }

    /**
     * Get the helper thread L2 cache request usefulness predictor.
     *
     * @return the helper thread L2 cache request usefulness predictor
     */
    public Predictor<Boolean> getHelperThreadL2RequestUsefulnessPredictor() {
        return helperThreadL2RequestUsefulnessPredictor;
    }

    /**
     * Get the helper thread L2 cache request pollution predictor.
     *
     * @return the helper thread L2 cache request pollution predictor
     */
    public Predictor<Boolean> getHelperThreadL2RequestPollutionPredictor() {
        return helperThreadL2RequestPollutionPredictor;
    }

    /**
     * Get the number of main thread L2 cache hits.
     *
     * @return the number of main thread L2 cache hits
     */
    public long getNumMainThreadL2Hits() {
        return numMainThreadL2Hits;
    }

    /**
     * Get the number of main thread L2 cache misses.
     *
     * @return the number of main thread L2 cache misses
     */
    public long getNumMainThreadL2Misses() {
        return numMainThreadL2Misses;
    }

    /**
     * Get the number of helper thread L2 cache hits.
     *
     * @return the number of helper thread L2 cache hits
     */
    public long getNumHelperThreadL2Hits() {
        return numHelperThreadL2Hits;
    }

    /**
     * Get the number of helper thread L2 cache misses.
     *
     * @return the number of helper thread L2 cache misses
     */
    public long getNumHelperThreadL2Misses() {
        return numHelperThreadL2Misses;
    }

    /**
     * Get the number of total helper thread L2 cache requests.
     *
     * @return the number of total helper thread L2 cache requests
     */
    public long getNumTotalHelperThreadL2Requests() {
        return numHelperThreadL2Hits + numHelperThreadL2Misses;
    }

    /**
     * Get the number of redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @return the number of redundant "hit to transient tag" helper thread L2 cache requests
     */
    public long getNumRedundantHitToTransientTagHelperThreadL2Requests() {
        return numRedundantHitToTransientTagHelperThreadL2Requests;
    }

    /**
     * Get the number of redundant "hit to cache" helper thread L2 cache requests.
     *
     * @return the number of redundant "hit to cache" helper thread L2 cache requests
     */
    public long getNumRedundantHitToCacheHelperThreadL2Requests() {
        return numRedundantHitToCacheHelperThreadL2Requests;
    }

    /**
     * Get the number of useful helper thread L2 cache requests.
     *
     * @return the number of useful helper thread L2 cache requests
     */
    public long getNumUsefulHelperThreadL2Requests() {
        return numUsefulHelperThreadL2Requests;
    }

    /**
     * Get the number of timely helper thread L2 cache requests.
     *
     * @return the number of timely helper thread L2 cache requests
     */
    public long getNumTimelyHelperThreadL2Requests() {
        return numTimelyHelperThreadL2Requests;
    }

    /**
     * Get the number of late helper thread L2 cache requests.
     *
     * @return the number of late helper thread L2 cache requests
     */
    public long getNumLateHelperThreadL2Requests() {
        return numLateHelperThreadL2Requests;
    }

    /**
     * Get the number of bad helper thread L2 cache requests.
     *
     * @return the number of late helper thread L2 cache requests
     */
    public long getNumBadHelperThreadL2Requests() {
        return numBadHelperThreadL2Requests;
    }

    /**
     * Get the number of early helper thread L2 cache requests.
     *
     * @return the number of early helper thread L2 cache requests
     */
    public long getNumEarlyHelperThreadL2Requests() {
        return numEarlyHelperThreadL2Requests;
    }

    /**
     * L2 cache request event.
     */
    public abstract class L2RequestEvent extends SimulationEvent {
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
        public L2RequestEvent(int set, int threadId, int pc, int tag) {
            super(l2Controller);
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
         * Get the core ID.
         *
         * @return the core ID
         */
        public int getCoreId() {
            Thread thread = getSender().getSimulation().getProcessor().getThreads().get(threadId);
            return thread.getCore().getNum();
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
    public class HelperThreadL2RequestEvent extends L2RequestEvent {
        private HelperThreadL2RequestQuality quality;

        /**
         * Create a helper thread L2 cache request event.
         *
         * @param quality  the quality
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public HelperThreadL2RequestEvent(HelperThreadL2RequestQuality quality, int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
            this.quality = quality;
        }

        /**
         * Get the quality.
         *
         * @return the quality
         */
        public HelperThreadL2RequestQuality getQuality() {
            return quality;
        }
    }

    /**
     * Main thread L2 cache hit event.
     */
    public class MainThreadL2HitEvent extends L2RequestEvent {
        /**
         * Create a main thread L2 cache hit event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public MainThreadL2HitEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }

    /**
     * Main thread L2 cache miss event.
     */
    public class MainThreadL2MissEvent extends L2RequestEvent {
        /**
         * Create a main thread L2 cache miss event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public MainThreadL2MissEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }

    /**
     * Helper thread L2 cache hit event.
     */
    public class HelperThreadL2HitEvent extends L2RequestEvent {
        /**
         * Create a helper thread L2 cache hit event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public HelperThreadL2HitEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }

    /**
     * Main thread L2 cache miss event.
     */
    public class HelperThreadL2MissEvent extends L2RequestEvent {
        /**
         * Create a main thread L2 cache miss event.
         *
         * @param set      the set
         * @param threadId the thread ID
         * @param pc       the program counter value
         * @param tag      the tag
         */
        public HelperThreadL2MissEvent(int set, int threadId, int pc, int tag) {
            super(set, threadId, pc, tag);
        }
    }
}
