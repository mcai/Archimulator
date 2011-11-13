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
package archimulator.ext.uncore.ht;

import archimulator.core.BasicThread;
import archimulator.core.Processor;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.Cache;
import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.uncore.coherence.CoherentCache;
import archimulator.uncore.coherence.MESIState;
import archimulator.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.capability.ProcessorCapability;
import archimulator.sim.capability.ProcessorCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;
import archimulator.util.action.Predicate;

import java.util.Map;

public class HtRequestL2VictimTrackingCapability implements ProcessorCapability {
    private EvictableCache<HtRequestEvictTableEntryState, HtRequestEvictTableLine> cache;

    private CoherentCache<MESIState>.LockableCache ownerCache;
    private MirrorCache mirrorCache;

    private LeastRecentlyUsedEvictionPolicy evictionPolicy;

    private long totalHtRequests;
    private long goodHtRequests;
    private long badHtRequests;

    @SuppressWarnings("unchecked")
    public HtRequestL2VictimTrackingCapability(Processor processor) {
        CoherentCache<MESIState>.LockableCache ownerCache = processor.getCacheHierarchy().getL2Cache().getCache();

        this.cache = new EvictableCache(processor, ownerCache.getName() + ".htRequestEvictTable", ownerCache.getGeometry(), LeastRecentlyUsedEvictionPolicy.FACTORY, new Function3<Cache<?, ?>, Integer, Integer, HtRequestEvictTableLine>() {
            public HtRequestEvictTableLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new HtRequestEvictTableLine(cache, set, way);
            }
        });

        this.ownerCache = ownerCache;

        this.mirrorCache = new MirrorCache(ownerCache.getName() + ".htRequestEvictTable.mirrorCache");

        this.evictionPolicy = new LeastRecentlyUsedEvictionPolicy(cache);

        processor.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == HtRequestL2VictimTrackingCapability.this.ownerCache) {
                    serviceRequest(event.getAddress(), event.getRequesterAccess(), (CoherentCache.LockableCacheLine) event.getLineFound());

                    if (!event.isHitInCache()) {
                        fillLine(event.getAddress(), event.getRequesterAccess(), (CoherentCache.LockableCacheLine) event.getLineFound());
                    }
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                HtRequestL2VictimTrackingCapability.this.totalHtRequests = 0;
                HtRequestL2VictimTrackingCapability.this.goodHtRequests = 0;
                HtRequestL2VictimTrackingCapability.this.badHtRequests = 0;
            }
        });

        processor.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        processor.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put(HtRequestL2VictimTrackingCapability.this.cache.getName() + ".totalHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability.this.totalHtRequests));
        stats.put(HtRequestL2VictimTrackingCapability.this.cache.getName() + ".goodHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability.this.goodHtRequests));
        stats.put(HtRequestL2VictimTrackingCapability.this.cache.getName() + ".badHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability.this.badHtRequests));
    }

    private void fillLine(int addr, final MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineToReplace) {
        final int set = this.cache.getSet(addr);

        MESIState victimState = lineToReplace.getState();

        boolean requesterIsHt = BasicThread.isHelperThread(requesterAccess.getThread());

        boolean victimIsHt = victimState != MESIState.INVALID && this.mirrorCache.getLine(lineToReplace).htRequest;

        this.checkInvariants(set);

        if (requesterIsHt) {
            this.totalHtRequests++;

            if (victimState == MESIState.INVALID) {
                this.setHtRequestForVictim(requesterAccess, lineToReplace, set);
                this.insertNullEntry(requesterAccess, set);
            } else if (!victimIsHt) {
                this.setHtRequestForVictim(requesterAccess, lineToReplace, set);
                this.insertDataEntryForVictim(requesterAccess, lineToReplace, set);
            } else {
                MemoryHierarchyAccess broughterHtAccess = this.mirrorCache.getLine(lineToReplace).broughterHtAccess;
                this.updateBroughterHtAccessForVictim(requesterAccess, lineToReplace);
                this.updateEvicterHtAccessForEntryOfVictim(requesterAccess, broughterHtAccess, set);
            }
        } else {
            if (victimIsHt) {
                MemoryHierarchyAccess victimBroughterHtAccess = this.mirrorCache.getLine(lineToReplace).broughterHtAccess;
                this.clearHtRequestForVictim(lineToReplace);
                this.removeEntryForVictimBroughterHtAccess(victimBroughterHtAccess, set);
            } else if (victimState != MESIState.INVALID && !victimIsHt) {
                this.ownerCache.forAny(set,
                        new Predicate<CoherentCache<MESIState>.LockableCacheLine>() {
                            public boolean apply(CoherentCache<MESIState>.LockableCacheLine lineInOwnerCache) {
                                return lineInOwnerCache.getState() != MESIState.INVALID && mirrorCache.getLine(lineInOwnerCache).htRequest;
                            }
                        }, new Action1<CoherentCache<MESIState>.LockableCacheLine>() {
                    public void apply(final CoherentCache<MESIState>.LockableCacheLine lineInCache) {
                        cache.forExact(set,
                                new Predicate<HtRequestEvictTableLine>() {
                                    public boolean apply(HtRequestEvictTableLine lineInEt) {
                                        return lineInEt.getState() != HtRequestEvictTableEntryState.INVALID && lineInEt.htRequest.getHtAccess() == mirrorCache.getLine(lineInCache).broughterHtAccess;
                                    }
                                }, new Action1<HtRequestEvictTableLine>() {
                            public void apply(HtRequestEvictTableLine lineInEt) {
                                lineInEt.setStateAndTag(HtRequestEvictTableEntryState.DATA, lineToReplace.getTag());
                                accessLine(set, lineInEt.getWay());
                            }
                        }
                        );
                    }
                }
                );
            }
        }

        this.checkInvariants(set);
    }

    private void serviceRequest(int addr, final MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineFound) {
        final int set = this.cache.getSet(addr);

        boolean htHit = lineFound != null && mirrorCache.getLine(lineFound).htRequest;
        boolean mtHit = lineFound != null && !mirrorCache.getLine(lineFound).htRequest;

        final HtRequestEvictTableLine lineFoundInEt = this.cache.findLine(addr);
        boolean etHit = lineFoundInEt != null;

        this.checkInvariants(set);

        if (BasicThread.isMainThread(requesterAccess.getThread())) {
            if (!mtHit && !htHit && etHit) {
                lineFoundInEt.htRequest.setQuality(HtRequestQuality.BAD);
                lineFoundInEt.htRequest.setVictimAccess(requesterAccess);
                this.clearHtRequestForEntry(set, lineFoundInEt);
                this.setLRUAndRemoveEntry(set, lineFoundInEt);
                this.badHtRequests++;
            } else if (!mtHit && htHit && !etHit) {
                this.mirrorCache.getLine(lineFound).htRequest = false;
                this.cache.forExact(set,
                        new Predicate<HtRequestEvictTableLine>() {
                            public boolean apply(HtRequestEvictTableLine line) {
                                return line.getState() != HtRequestEvictTableEntryState.INVALID && line.htRequest.getHtAccess() == mirrorCache.getLine(lineFound).broughterHtAccess;
                            }
                        }, new Action1<HtRequestEvictTableLine>() {
                    public void apply(HtRequestEvictTableLine line) {
                        line.htRequest.setQuality(HtRequestQuality.GOOD);
                        line.htRequest.setHitByAccess(requesterAccess);
                        accessLine(set, line.getWay());

                        line.invalidate();
                        line.htRequest = null;
                    }
                }
                );
                this.goodHtRequests++;
            } else if (!mtHit && htHit && etHit) {
                //assert (this.getTag(lineFoundInEt.getLabel().getHtAccess().getTag()) != lineFoundInEt.getTag());

                this.mirrorCache.getLine(lineFound).htRequest = false;
                this.cache.forExact(set,
                        new Predicate<HtRequestEvictTableLine>() {
                            public boolean apply(HtRequestEvictTableLine line) {
                                return line.getState() != HtRequestEvictTableEntryState.INVALID && line.htRequest.getHtAccess() == mirrorCache.getLine(lineFound).broughterHtAccess;
                            }
                        },
                        new Action1<HtRequestEvictTableLine>() {
                            public void apply(HtRequestEvictTableLine line) {
                                line.htRequest.setHitByAccess(requesterAccess);
                                accessLine(set, line.getWay());

//                                assert (line != lineFoundInEt);

                                line.invalidate();
                                line.htRequest = null;
                            }
                        }
                );

                if (lineFoundInEt.getState() != HtRequestEvictTableEntryState.INVALID) {
                    this.clearHtRequestForEntry(set, lineFoundInEt);
                    this.setLRUAndRemoveEntry(set, lineFoundInEt);
                }
            } else if (mtHit && !htHit && etHit) {
                this.clearHtRequestForEntry(set, lineFoundInEt);
                this.setLRUAndRemoveEntry(set, lineFoundInEt);
            }
        }

        this.checkInvariants(set);
    }

    private int miss(int set) {
        assert (set >= 0 && set < this.cache.getNumSets());

        for (int stackPosition = this.cache.getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
            CacheLine<?> line = this.evictionPolicy.getCacheLineInStackPosition(set, stackPosition);
            if (line.getState() == HtRequestEvictTableEntryState.INVALID) {
                return line.getWay();
            }
        }

        throw new IllegalArgumentException();
    }

    private void accessLine(int set, int way) {
        this.evictionPolicy.setMRU(set, way);
    }

    private void checkInvariants(final int set) {
        this.ownerCache.forAll(set,
                new Predicate<CoherentCache<MESIState>.LockableCacheLine>() {
                    public boolean apply(CoherentCache<MESIState>.LockableCacheLine line) {
                        return mirrorCache.getLine(line).htRequest;
                    }
                },
                new Action1<CoherentCache<MESIState>.LockableCacheLine>() {
                    public void apply(final CoherentCache<MESIState>.LockableCacheLine line) {
                        assert (cache.containsAny(set, new Predicate<HtRequestEvictTableLine>() {
                            public boolean apply(HtRequestEvictTableLine lineInEt) {
                                return lineInEt.getState() != HtRequestEvictTableEntryState.INVALID && lineInEt.htRequest.getHtAccess() == mirrorCache.getLine(line).broughterHtAccess;
                            }
                        }));
                    }
                }
        );

        this.cache.forAll(set,
                new Predicate<HtRequestEvictTableLine>() {
                    public boolean apply(HtRequestEvictTableLine lineInEt) {
                        return lineInEt.getState() != HtRequestEvictTableEntryState.INVALID;
                    }
                },
                new Action1<HtRequestEvictTableLine>() {
                    public void apply(final HtRequestEvictTableLine lineInEt) {
                        assert (ownerCache.containsAny(set, new Predicate<CoherentCache<MESIState>.LockableCacheLine>() {
                            public boolean apply(CoherentCache<MESIState>.LockableCacheLine line) {
                                return mirrorCache.getLine(line).htRequest && mirrorCache.getLine(line).broughterHtAccess == lineInEt.htRequest.getHtAccess();
                            }
                        }));
                    }
                }
        );
    }

    private void setHtRequestForVictim(MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineToReplace, int set) {
        assert (requesterAccess != null);

        if (this.mirrorCache.getLine(lineToReplace).htRequest) {
            if (this.cache.containsAny(set, new Predicate<HtRequestEvictTableLine>() {
                public boolean apply(HtRequestEvictTableLine line) {
                    return line.getState() != HtRequestEvictTableEntryState.INVALID && line.htRequest.getHtAccess() == mirrorCache.getLine(lineToReplace).broughterHtAccess;
                }
            })) {
                this.removeEntryForVictimBroughterHtAccess(mirrorCache.getLine(lineToReplace).broughterHtAccess, set);
            }
        }

        this.mirrorCache.getLine(lineToReplace).htRequest = true;
        this.mirrorCache.getLine(lineToReplace).broughterHtAccess = requesterAccess;
    }

    private void updateBroughterHtAccessForVictim(MemoryHierarchyAccess requesterAccess, CoherentCache<MESIState>.LockableCacheLine lineToReplace) {
        assert (requesterAccess != null);

        assert (this.mirrorCache.getLine(lineToReplace).htRequest);
        assert (this.mirrorCache.getLine(lineToReplace).broughterHtAccess != null);

        this.mirrorCache.getLine(lineToReplace).broughterHtAccess = requesterAccess;
    }

    private void clearHtRequestForVictim(CoherentCache<MESIState>.LockableCacheLine lineToReplace) {
        assert (this.mirrorCache.getLine(lineToReplace).htRequest);

        this.mirrorCache.getLine(lineToReplace).htRequest = false;
        this.mirrorCache.getLine(lineToReplace).broughterHtAccess = null;
    }

    private void insertNullEntry(MemoryHierarchyAccess requesterAccess, int set) {
        assert (requesterAccess != null);

        HtRequestEvictTableLine line = this.cache.getLine(set, this.miss(set));

        assert (line.getState() == HtRequestEvictTableEntryState.INVALID);

        line.setStateAndTag(HtRequestEvictTableEntryState.NULL, 0);
        line.htRequest = new HtRequest(requesterAccess);
        this.accessLine(set, line.getWay());
    }

    private void insertDataEntryForVictim(MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineToReplace, int set) {
        assert (requesterAccess != null);

        HtRequestEvictTableLine line = this.cache.getLine(set, this.miss(set));

        assert (line.getState() == HtRequestEvictTableEntryState.INVALID);

        line.setStateAndTag(HtRequestEvictTableEntryState.DATA, lineToReplace.getTag());
        line.htRequest = new HtRequest(requesterAccess);
        this.accessLine(set, line.getWay());
    }

    private void removeEntryForVictimBroughterHtAccess(final MemoryHierarchyAccess victimBroughterHtAccess, int set) {
        assert (victimBroughterHtAccess != null);

        this.cache.forExact(set,
                new Predicate<HtRequestEvictTableLine>() {
                    public boolean apply(HtRequestEvictTableLine line) {
                        return line.getState() != HtRequestEvictTableEntryState.INVALID && line.htRequest.getHtAccess() == victimBroughterHtAccess;
                    }
                }, new Action1<HtRequestEvictTableLine>() {
            public void apply(HtRequestEvictTableLine line) {
                line.invalidate();
                line.htRequest = null;
            }
        }
        );
    }

    private void updateEvicterHtAccessForEntryOfVictim(final MemoryHierarchyAccess requesterAccess, final MemoryHierarchyAccess broughterHtAccess, int set) {
        assert (requesterAccess != null);
        assert (broughterHtAccess != null);

        this.cache.forExact(set,
                new Predicate<HtRequestEvictTableLine>() {
                    public boolean apply(HtRequestEvictTableLine line) {
                        return line.getState() != HtRequestEvictTableEntryState.INVALID && line.htRequest.getHtAccess() == broughterHtAccess;
                    }
                }, new Action1<HtRequestEvictTableLine>() {
            public void apply(HtRequestEvictTableLine line) {
                line.htRequest.setHtAccess(requesterAccess);
            }
        }
        );
    }

    private void clearHtRequestForEntry(int set, final HtRequestEvictTableLine lineFoundInEt) {
        this.ownerCache.forExact(set,
                new Predicate<CoherentCache<MESIState>.LockableCacheLine>() {
                    public boolean apply(CoherentCache<MESIState>.LockableCacheLine line) {
                        return mirrorCache.getLine(line).broughterHtAccess == lineFoundInEt.htRequest.getHtAccess();
                    }
                }, new Action1<CoherentCache<MESIState>.LockableCacheLine>() {
            public void apply(CoherentCache<MESIState>.LockableCacheLine line) {
                mirrorCache.getLine(line).htRequest = false;
                mirrorCache.getLine(line).broughterHtAccess = null;
            }
        }
        );
    }

    private void setLRUAndRemoveEntry(int set, HtRequestEvictTableLine lineFoundInEt) {
        this.evictionPolicy.setLRU(set, lineFoundInEt.getWay());
        lineFoundInEt.invalidate();
        lineFoundInEt.htRequest = null;
    }

    private class HtRequestEvictTableLine extends CacheLine<HtRequestEvictTableEntryState> {
        private HtRequest htRequest;

        private HtRequestEvictTableLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, HtRequestEvictTableEntryState.INVALID);
        }

        private void setStateAndTag(HtRequestEvictTableEntryState state, int tag) {
            this.setNonInitialState(state);
            this.setTag(tag);
        }
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private transient boolean htRequest;
        private transient MemoryHierarchyAccess broughterHtAccess;

        private MirrorCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, true);
        }
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache(String name) {
            super(cache, name, ownerCache.getGeometry(), new Function3<Cache<?, ?>, Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                    return new MirrorCacheLine(cache, set, way);
                }
            });
        }

        private MirrorCacheLine getLine(CacheLine<?> ownerCacheLine) {
            return mirrorCache.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
        }
    }

    public static final ProcessorCapabilityFactory FACTORY = new ProcessorCapabilityFactory() {
        public ProcessorCapability createCapability(Processor processor) {
            return new HtRequestL2VictimTrackingCapability(processor);
        }
    };
}