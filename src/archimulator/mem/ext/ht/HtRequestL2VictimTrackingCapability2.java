/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.mem.ext.ht;

import archimulator.core.BasicThread;
import archimulator.core.Processor;
import archimulator.ext.ProcessorCapability;
import archimulator.ext.ProcessorCapabilityFactory;
import archimulator.mem.MemoryHierarchyAccess;
import archimulator.mem.cache.Cache;
import archimulator.mem.cache.CacheLine;
import archimulator.mem.coherence.CoherentCache;
import archimulator.mem.coherence.MESIState;
import archimulator.mem.coherence.event.CoherentCacheFillLineEvent;
import archimulator.mem.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function2;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;

import java.util.Map;

public class HtRequestL2VictimTrackingCapability2 implements ProcessorCapability {
    private CoherentCache<MESIState>.LockableCache ownerCache;
    private MirrorCache mirrorCache;

    private long totalHtRequests;

    private long goodHtRequests;
    private long badHtRequests;
    private long uglyHtRequests;
    private long uselessHtRequests;

    @SuppressWarnings("unchecked")
    public HtRequestL2VictimTrackingCapability2(Processor processor) {
        CoherentCache<MESIState>.LockableCache ownerCache = processor.getCacheHierarchy().getL2Cache().getCache();

        this.ownerCache = ownerCache;

        this.mirrorCache = new MirrorCache(ownerCache.getName() + ".htRequestEvictTable.mirrorCache");

        processor.getBlockingEventDispatcher().addListener(CoherentCacheFillLineEvent.class, new Action1<CoherentCacheFillLineEvent>() {
            public void apply(CoherentCacheFillLineEvent event) {
                if (event.getCache().getCache() == HtRequestL2VictimTrackingCapability2.this.ownerCache) {
                    fillLine(event.getAddress(), event.getRequesterAccess(), (CoherentCache.LockableCacheLine) event.getLineToReplace());
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == HtRequestL2VictimTrackingCapability2.this.ownerCache) {
                    serviceRequest(event.getAddress(), event.getRequesterAccess(), (CoherentCache.LockableCacheLine) event.getLineFound());
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                HtRequestL2VictimTrackingCapability2.this.totalHtRequests = 0;

                HtRequestL2VictimTrackingCapability2.this.goodHtRequests = 0;
                HtRequestL2VictimTrackingCapability2.this.badHtRequests = 0;
                HtRequestL2VictimTrackingCapability2.this.uglyHtRequests = 0;
                HtRequestL2VictimTrackingCapability2.this.uselessHtRequests = 0;
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
        stats.put("HtRequestL2VictimTrackingCapability2." + HtRequestL2VictimTrackingCapability2.this.ownerCache.getName() + ".totalHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability2.this.totalHtRequests));

        stats.put("HtRequestL2VictimTrackingCapability2." + HtRequestL2VictimTrackingCapability2.this.ownerCache.getName() + ".goodHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability2.this.goodHtRequests));
        stats.put("HtRequestL2VictimTrackingCapability2." + HtRequestL2VictimTrackingCapability2.this.ownerCache.getName() + ".badHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability2.this.badHtRequests));
        stats.put("HtRequestL2VictimTrackingCapability2." + HtRequestL2VictimTrackingCapability2.this.ownerCache.getName() + ".uglyHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability2.this.uglyHtRequests));
        stats.put("HtRequestL2VictimTrackingCapability2." + HtRequestL2VictimTrackingCapability2.this.ownerCache.getName() + ".uselessHtRequests", String.valueOf(HtRequestL2VictimTrackingCapability2.this.uselessHtRequests));
    }

    private void fillLine(int addr, final MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineToReplace) {
        MESIState victimState = lineToReplace.getState();

        boolean requesterIsHt = BasicThread.isHelperThread(requesterAccess.getThread());

        boolean victimIsHt = victimState != MESIState.INVALID && this.mirrorCache.getLine(lineToReplace).ht;

        if (requesterIsHt) {
            if (victimState == MESIState.INVALID) {
                this.mirrorCache.getLine(lineToReplace).ht = true;
            } else if (!victimIsHt) {
                this.mirrorCache.getLine(lineToReplace).ht = true;
                this.mirrorCache.getLine(lineToReplace).victimTag = lineToReplace.getTag();
            } else {
                if (this.mirrorCache.getLine(lineToReplace).victimHit) {
                    this.badHtRequests++;
                    this.mirrorCache.getLine(lineToReplace).victimHit = false;
                } else {
                    this.uselessHtRequests++;
                    this.mirrorCache.getLine(lineToReplace).victimTag = -1;
                }
            }

            this.totalHtRequests++;
        } else {
            if (victimIsHt) {
                if (this.mirrorCache.getLine(lineToReplace).victimHit) {
                    this.badHtRequests++;
                } else {
                    this.uselessHtRequests++;
                }

                this.mirrorCache.getLine(lineToReplace).ht = false;

                this.mirrorCache.getLine(lineToReplace).victimTag = -1;
                this.mirrorCache.getLine(lineToReplace).victimHit = false;
            }
        }
    }

    private void serviceRequest(int addr, final MemoryHierarchyAccess requesterAccess, final CoherentCache<MESIState>.LockableCacheLine lineFound) {
        boolean htHit = lineFound != null && this.mirrorCache.getLine(lineFound).ht;
        boolean mtHit = lineFound != null && !this.mirrorCache.getLine(lineFound).ht;

        MirrorCacheLine lineForVictim = this.mirrorCache.findLineForVictim(addr);
        boolean victimHit = lineForVictim != null;

        if (BasicThread.isMainThread(requesterAccess.getThread())) {
            if (!mtHit && htHit && !victimHit) {
                if (this.mirrorCache.getLine(lineFound).victimHit) {
                    this.uglyHtRequests++;
                } else {
                    this.goodHtRequests++;
                }

                this.mirrorCache.getLine(lineFound).ht = false;

                this.mirrorCache.getLine(lineFound).victimTag = -1;
                this.mirrorCache.getLine(lineFound).victimHit = false;
            } else if (!mtHit && !htHit && victimHit) {
                lineForVictim.victimTag = -1;
                lineForVictim.victimHit = true;
            } else if (!mtHit && htHit && victimHit) {
                //TODO
            } else if (mtHit && !htHit && victimHit) {
                //TODO
            }
        }
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private boolean ht;
        private int victimTag;
        private boolean victimHit;

        private MirrorCacheLine(int set, int way) {
            super(set, way, true);
        }
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache(String name) {
            super(ownerCache, name, ownerCache.getGeometry(), new Function2<Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Integer set, Integer way) {
                    return new MirrorCacheLine(set, way);
                }
            });
        }

        private MirrorCacheLine getLine(CacheLine<?> ownerCacheLine) {
            return mirrorCache.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
        }

        public MirrorCacheLine findLineForVictim(int address) {
            int tag = this.getTag(address);
            int set = this.getSet(address);

            for (MirrorCacheLine line : this.sets.get(set).getLines()) {
                if (line.victimTag == tag) {
                    return line;
                }
            }

            return null;
        }
    }

    public static final ProcessorCapabilityFactory FACTORY = new ProcessorCapabilityFactory() {
        public ProcessorCapability createCapability(Processor processor) {
            return new HtRequestL2VictimTrackingCapability2(processor);
        }
    };
}