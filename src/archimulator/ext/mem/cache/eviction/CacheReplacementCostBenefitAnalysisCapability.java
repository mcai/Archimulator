///*******************************************************************************
// * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
// *
// * This file is part of the Archimulator multicore architectural simulator.
// *
// * Archimulator is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Archimulator is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
//package archimulator.mem.cache.eviction;
//
//import archimulator.core.BasicThread;
//import archimulator.core.Processor;
//import archimulator.sim.capability.ProcessorCapability;
//import archimulator.sim.capability.ProcessorCapabilityFactory;
//import archimulator.mem.MemoryHierarchyAccess;
//import archimulator.mem.cache.Cache;
//import archimulator.mem.cache.CacheLine;
//import archimulator.mem.coherence.base.CoherentCache;
//import archimulator.mem.coherence.MESIState;
//import archimulator.mem.coherence.events.CoherentCacheFillLineEvent;
//import archimulator.mem.coherence.events.CoherentCacheServiceNonblockingRequestEvent;
//import archimulator.cloud.event.DumpStatEvent;
//import archimulator.cloud.event.PollStatsEvent;
//import archimulator.cloud.event.ResetStatEvent;
//import archimulator.util.action.Action1;
//import archimulator.util.event.BlockingEventDispatcher;
//import archimulator.util.action.Function2;
//
//import java.util.Map;
//
//public class CacheReplacementCostBenefitAnalysisCapability implements ProcessorCapability {
//    private CoherentCache<MESIState>.LockableCache ownerCache;
//    private MirrorCache mirrorCache;
//
//    private long totalReplacements;
//
//    @SuppressWarnings("unchecked")
//    public CacheReplacementCostBenefitAnalysisCapability(Processor processor) {
//        CoherentCache<MESIState>.LockableCache ownerCache = processor.getCacheHierarchy().getL2Cache().getCache();
//
//        this.ownerCache = ownerCache;
//
//        this.mirrorCache = new MirrorCache(ownerCache.getName() + ".CacheReplacementCostBenefitAnalysisCapability.mirrorCache");
//
//        BlockingEventDispatcher.addListener(CoherentCacheFillLineEvent.class, new Action1<CoherentCacheFillLineEvent>() {
//            @Override
//            public void apply(CoherentCacheFillLineEvent event) {
//                if (event.getCache().getCache() == CacheReplacementCostBenefitAnalysisCapability.this.ownerCache) {
//                    fillLine(event.getAddress(), event.getRequesterAccess(), (CoherentCache<MESIState>.LockableCacheLine) event.getLineToReplace());
//                }
//            }
//        });
//
//        BlockingEventDispatcher.addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
//            @Override
//            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
//                if (event.getCache().getCache() == CacheReplacementCostBenefitAnalysisCapability.this.ownerCache) {
//                    serviceRequest(event.getAddress(), event.getRequesterAccess(), (CoherentCache<MESIState>.LockableCacheLine) event.getLineFound());
//                }
//            }
//        });
//
//        BlockingEventDispatcher.addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
//            @Override
//            public void apply(ResetStatEvent event) {
//                CacheReplacementCostBenefitAnalysisCapability.this.totalReplacements = 0;
//            }
//        });
//
//        BlockingEventDispatcher.addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
//            @Override
//            public void apply(PollStatsEvent event) {
//                dumpStats(event.getStats());
//            }
//        });
//
//        BlockingEventDispatcher.addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
//            @Override
//            public void apply(DumpStatEvent event) {
//                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
//                    dumpStats(event.getStats());
//                }
//            }
//        });
//    }
//
//    private void dumpStats(Map<String, Object> stats) {
//        stats.put("CacheReplacementCostBenefitAnalysisCapability." + CacheReplacementCostBenefitAnalysisCapability.this.ownerCache.getName() + ".totalReplacements", String.valueOf(CacheReplacementCostBenefitAnalysisCapability.this.totalReplacements));
//    }
//
//    private void fillLine(int addr, final MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineToReplace) {
//        MESIState victimState = lineToReplace.getState();
//
//        if (requesterIsHt) {
//            if (victimState == MESIState.INVALID) {
//                this.mirrorCache.getLine(lineToReplace).ht = true;
//            } else if (!victimIsHt) {
//                this.mirrorCache.getLine(lineToReplace).ht = true;
//                this.mirrorCache.getLine(lineToReplace).victimTag = lineToReplace.getTag();
//            } else {
//                if (this.mirrorCache.getLine(lineToReplace).victimHit) {
//                    this.badHtRequests++;
//                    this.mirrorCache.getLine(lineToReplace).victimHit = false;
//                } else {
//                    this.uselessHtRequests++;
//                    this.mirrorCache.getLine(lineToReplace).victimTag = -1;
//                }
//            }
//
//            this.totalHtRequests++;
//        } else {
//            if (victimIsHt) {
//                if (this.mirrorCache.getLine(lineToReplace).victimHit) {
//                    this.badHtRequests++;
//                } else {
//                    this.uselessHtRequests++;
//                }
//
//                this.mirrorCache.getLine(lineToReplace).victimTag = -1;
//                this.mirrorCache.getLine(lineToReplace).victimHit = false;
//            }
//        }
//    }
//
//    private void serviceRequest(int addr, final MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineFound) {
//        boolean htHit = lineFound != null && this.mirrorCache.getLine(lineFound).ht;
//        boolean mtHit = lineFound != null && !this.mirrorCache.getLine(lineFound).ht;
//
//        MirrorCacheLine lineForVictim = this.mirrorCache.findLineForVictim(addr);
//        boolean victimHit = lineForVictim != null;
//
//        if (BasicThread.isMainThread(requesterAccess.getThread())) {
//            if (!mtHit && htHit && !victimHit) {
//                if (this.mirrorCache.getLine(lineFound).victimHit) {
//                    this.uglyHtRequests++;
//                } else {
//                    this.goodHtRequests++;
//                }
//
//                this.mirrorCache.getLine(lineFound).ht = false;
//
//                this.mirrorCache.getLine(lineFound).victimTag = -1;
//                this.mirrorCache.getLine(lineFound).victimHit = false;
//            } else if (!mtHit && !htHit && victimHit) {
//                lineForVictim.victimTag = -1;
//                lineForVictim.victimHit = true;
//            } else if (!mtHit && htHit && victimHit) {
//                //TODO
//            } else if (mtHit && !htHit && victimHit) {
//                //TODO
//            }
//        }
//    }
//
//    private class MirrorCacheLine extends CacheLine<Boolean> {
//        private int victimTag;
//        private boolean victimHit;
//
//        private MirrorCacheLine(int set, int way) {
//            super(set, way, true);
//        }
//    }
//
//    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
//        private MirrorCache(String name) {
//            super(name, ownerCache.getGeometry(), new Function2<Integer, Integer, MirrorCacheLine>() {
//                @Override
//                public MirrorCacheLine apply(Integer set, Integer way) {
//                    return new MirrorCacheLine(set, way);
//                }
//            });
//        }
//
//        private MirrorCacheLine getLine(CacheLine<?> ownerCacheLine) {
//            return mirrorCache.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
//        }
//
//        public MirrorCacheLine findLineForVictim(int address) {
//            int tag = this.getTag(address);
//            int set = this.getSet(address);
//
//            for (MirrorCacheLine line : this.sets.get(set).getLines()) {
//                if (line.victimTag == tag) {
//                    return line;
//                }
//            }
//
//            return null;
//        }
//    }
//
//    public static final ProcessorCapabilityFactory FACTORY = new ProcessorCapabilityFactory() {
//        @Override
//        public ProcessorCapability createCapability(Processor processor) {
//            return new CacheReplacementCostBenefitAnalysisCapability(processor);
//        }
//    };
//}