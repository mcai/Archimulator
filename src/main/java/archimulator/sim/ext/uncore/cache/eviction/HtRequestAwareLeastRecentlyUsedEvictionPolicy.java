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
package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.model.event.DumpStatEvent;
import archimulator.model.event.PollStatsEvent;
import archimulator.model.event.ResetStatEvent;
import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.CacheMiss;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.uncore.coherence.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;
import archimulator.util.math.MathHelper;
import archimulator.util.math.SaturatingCounter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class HtRequestAwareLeastRecentlyUsedEvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends LeastRecentlyUsedEvictionPolicy<StateT, LineT> {
    private MirrorCache mirrorCache;

    private int evictedMtLinesPerInterval;

    private long intervals;
    private int evictedMtLines;

    private IntervalStat totalHtRequests;
    private IntervalStat usedHtRequests;
    private IntervalStat lateHtRequests;
    private IntervalStat htRequestInducedMtMisses;
    private IntervalStat totalMtMisses;

    private QuntizedHtRequestCachePollutionForEvictionPolicy quntizedHtRequestCachePollutionForEvictionPolicy;

    public HtRequestAwareLeastRecentlyUsedEvictionPolicy(EvictableCache<StateT, LineT> cache) {
        super(cache);

        this.mirrorCache = new MirrorCache();

        this.evictedMtLinesPerInterval = cache.getNumSets() * cache.getAssociativity() / 2;

        this.totalHtRequests = new IntervalStat();
        this.usedHtRequests = new IntervalStat();
        this.lateHtRequests = new IntervalStat();
        this.htRequestInducedMtMisses = new IntervalStat();
        this.totalMtMisses = new IntervalStat();

        this.quntizedHtRequestCachePollutionForEvictionPolicy = QuntizedHtRequestCachePollutionForEvictionPolicy.MEDIUM;

        cache.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(getCache()) && !event.isHitInCache()) {
                    if (event.getLineFound().getState() != MESIState.INVALID && !mirrorCache.getLine(event.getLineFound()).htRequest) {
                        evictedMtLines++;

                        if (evictedMtLines == evictedMtLinesPerInterval) {
                            newInterval();

                            evictedMtLines = 0;
                            intervals++;
                        }
                    }

                    if (BasicThread.isHelperThread(event.getRequesterAccess().getThread())) {
                        if (event.getLineFound().getState() != MESIState.INVALID && !mirrorCache.getLine(event.getLineFound()).htRequest) {
                            newMtCacheLineEvictedByHtRequest(event.getLineFound().getTag(), event.getRequesterAccess().getPhysicalAddress());
                        }

                        mirrorCache.getLine(event.getLineFound()).htRequest = true;
                        totalHtRequests.inc();
                    }
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(getCache()) && event.getLineFound() != null && BasicThread.isMainThread(event.getRequesterAccess().getThread()) && mirrorCache.getLine(event.getLineFound()).htRequest) {
                    mirrorCache.getLine(event.getLineFound()).htRequest = false;
                    usedHtRequests.inc();
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCache().getCache().equals(getCache()) && event.getLineFound() != null && BasicThread.isMainThread(event.getRequesterAccess().getThread()) && mirrorCache.getLine(event.getLineFound()).htRequest) {
                    mirrorCache.getLine(event.getLineFound()).htRequest = false;
                    lateHtRequests.inc();
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(CoherentCacheBeginCacheAccessEvent.class, new Action1<CoherentCacheBeginCacheAccessEvent>() {
            public void apply(CoherentCacheBeginCacheAccessEvent event) {
                if (!event.getCacheAccess().isHitInCache() && event.getCache().getCache().equals(getCache()) && BasicThread.isMainThread(event.getAccess().getThread())) {
                    if (isNewMtCacheMissCausedByHtRequest(event.getAccess().getPhysicalAddress())) {
                        htRequestInducedMtMisses.inc();
                    }

                    totalMtMisses.inc();
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                HtRequestAwareLeastRecentlyUsedEvictionPolicy.this.totalHtRequests.reset();
                HtRequestAwareLeastRecentlyUsedEvictionPolicy.this.usedHtRequests.reset();
                HtRequestAwareLeastRecentlyUsedEvictionPolicy.this.lateHtRequests.reset();
            }
        });

        cache.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        if (BasicThread.isHelperThread(miss.getReference().getAccess().getThread().getId())) {
            int newStackPosition;

            switch (this.quntizedHtRequestCachePollutionForEvictionPolicy) {
                case HIGH:
                    newStackPosition = this.getCache().getAssociativity() - 1;
                    break;
                case MEDIUM:
                    newStackPosition = (int) ((double) (this.getCache().getAssociativity() - 1) * 3 / 4);
                    break;
                case LOW:
                    newStackPosition = (int) ((double) (this.getCache().getAssociativity() - 1) / 2) - 1;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            this.setStackPosition(miss.getReference().getSet(), miss.getWay(), newStackPosition);
        } else {
            super.handleInsertionOnMiss(miss);
        }
    }

    private void newInterval() {
        long totalHtRequests = this.totalHtRequests.newInterval();
        long usedHtRequests = this.usedHtRequests.newInterval();
        long lateHtRequests = this.lateHtRequests.newInterval();
        long htRequestsInducedMtMisses = this.htRequestInducedMtMisses.newInterval();
        long totalMtMisses = this.totalMtMisses.newInterval();

        double htRequestAccuracy = this.getHtRequestAccuracy(usedHtRequests, totalHtRequests);
        double htRequestLateness = this.getHtRequestLateness(lateHtRequests, usedHtRequests);
        double htRequestPollution = this.getHtRequestPollution(htRequestsInducedMtMisses, totalMtMisses);

        //TODO: adjust the HT program's aggressiveness

        if (htRequestPollution > htRequestCachePollutionHighThresholdForEvictionPolicy) {
            this.quntizedHtRequestCachePollutionForEvictionPolicy = QuntizedHtRequestCachePollutionForEvictionPolicy.HIGH;
        } else if (htRequestPollution < htRequestCachePollutionLowThresholdForEvictionPolicy) {
            this.quntizedHtRequestCachePollutionForEvictionPolicy = QuntizedHtRequestCachePollutionForEvictionPolicy.LOW;
        } else {
            this.quntizedHtRequestCachePollutionForEvictionPolicy = QuntizedHtRequestCachePollutionForEvictionPolicy.MEDIUM;
        }
    }

    private void newMtCacheLineEvictedByHtRequest(int victimAddr, int requesterAddr) {
        this.getCachePollutionFilter().add(victimAddr);
        this.getCachePollutionFilter().remove(requesterAddr);
    }

    private boolean isNewMtCacheMissCausedByHtRequest(int addr) {
        return this.getCachePollutionFilter().contains(addr);
    }

    public abstract AddressSetFilter getCachePollutionFilter();

    public interface AddressSetFilter {
        public boolean contains(int addr);

        public void add(int addr);

        public void remove(int addr);
    }

    private static class TreeSetBasedAddressSetFilter implements AddressSetFilter {
        private EvictableCache<?, ?> cache;
        private Set<Integer> proxy;

        private TreeSetBasedAddressSetFilter(EvictableCache<?, ?> cache) {
            this.cache = cache;
            this.proxy = new TreeSet<Integer>();
        }

        public boolean contains(int addr) {
            return this.proxy.contains(hashAddress(addr));
        }

        public void add(int addr) {
            this.proxy.add(hashAddress(addr));
        }

        public void remove(int addr) {
            this.proxy.remove(hashAddress(addr));
        }

        private int hashAddress(int addr) {
            return this.cache.getLineId(addr);
        }

        @Override
        public String toString() {
            return "<TreeSetBasedAddressSetFilter>";
        }
    }

    private static class XorBasedAddressSetFilter implements AddressSetFilter {
        private EvictableCache<?, ?> cache;
        private int proxy;

        private XorBasedAddressSetFilter(EvictableCache<?, ?> cache) {
            this.cache = cache;
        }

        public boolean contains(int addr) {
            addr = hashAddress(addr);
            return (addr != 0) && (this.proxy & addr) == addr;
        }

        public void add(int addr) {
            this.proxy |= hashAddress(addr);
        }

        public void remove(int addr) {
            this.proxy &= ~(hashAddress(addr));
        }

        private int hashAddress(int addr) {
            addr = this.cache.getLineId(addr);
            return (MathHelper.bits(addr, 11, 0) ^ MathHelper.bits(addr, 23, 12));
        }

        @Override
        public String toString() {
            return Integer.toBinaryString(this.proxy);
        }
    }

    private double getHtRequestAccuracy(long usedHtRequests, long totalHtRequests) {
        return (double) usedHtRequests / totalHtRequests;
    }

    private double getHtRequestLateness(long lateHtRequests, long usedHtRequests) {
        return (double) lateHtRequests / usedHtRequests;
    }

    private double getHtRequestPollution(long htRequestsIncurredMtMisses, long totalMtMisses) {
        return (double) htRequestsIncurredMtMisses / totalMtMisses;
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.constants.evictedMtLinesPerInterval", String.valueOf(this.evictedMtLinesPerInterval));

        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.cachePollutionFilter", this.getCachePollutionFilter().toString());

        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.intervals", String.valueOf(this.intervals));
        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.evictedMtLines", String.valueOf(this.evictedMtLines));

        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.totalHtRequests", String.valueOf(this.totalHtRequests));
        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.usedHtRequests", String.valueOf(this.usedHtRequests));
        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.lateHtRequests", String.valueOf(this.lateHtRequests));
        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.htRequestInducedMtMisses", String.valueOf(this.htRequestInducedMtMisses));
        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.totalMtMisses", String.valueOf(this.totalMtMisses));

        stats.put(this.getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.currentInterval.quntizedHtRequestCachePollutionForEvictionPolicy", String.valueOf(this.quntizedHtRequestCachePollutionForEvictionPolicy));
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private transient boolean htRequest;

        private MirrorCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, true);
        }
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache() {
            super(getCache(), getCache().getName() + ".htRequestAwareLeastRecentlyUsedEvictionPolicy.mirrorCache", getCache().getGeometry(), new Function3<Cache<?, ?>, Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                    return new MirrorCacheLine(cache, set, way);
                }
            });
        }

        private MirrorCacheLine getLine(CacheLine<?> ownerCacheLine) {
            return this.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
        }
    }

    private class IntervalStat {
        private long valueInPreviousInterval;
        private long value;

        public void inc() {
            this.value++;
        }

        public void reset() {
            this.value = this.valueInPreviousInterval = 0;
        }

        public long newInterval() {
            this.valueInPreviousInterval = (this.valueInPreviousInterval + this.value) / 2;
            this.value = 0;
            return this.valueInPreviousInterval;
        }

        @Override
        public String toString() {
            return Long.toString(this.value);
        }
    }

    private enum QuntizedHtRequestAccuracyForHtAggressiveness {
        HIGH,
        MEDIUM,
        LOW
    }

    private enum QuantizedHtRequestLatenessForHtAggressiveness {
        LATE,
        NOT_LATE
    }

    private enum QuantizedHtRequestCachePollutionForHtAggressiveness {
        POLLUTING,
        NOT_POLLUTING
    }

    private enum QuntizedHtRequestCachePollutionForEvictionPolicy {
        HIGH,
        MEDIUM,
        LOW
    }

    private class HtConfig {
        private int lookahead;
        private int stride;

        private HtConfig(int lookahead, int stride) {
            this.lookahead = lookahead;
            this.stride = stride;
        }
    }

    //TODO: for demonstration only, to be adpated for the HT scheme
    private class HtAggressivenessCounter {
        private SaturatingCounter aggressiveness;

        private Map<Integer, HtConfig> configs;

        private HtAggressivenessCounter() {
            this.aggressiveness = new SaturatingCounter(1, 3, 5, 3);

            this.configs = new TreeMap<Integer, HtConfig>();
            this.configs.put(1, new HtConfig(4, 1));
            this.configs.put(2, new HtConfig(8, 1));
            this.configs.put(3, new HtConfig(16, 2));
            this.configs.put(4, new HtConfig(32, 4));
            this.configs.put(5, new HtConfig(64, 4));
        }

        private HtConfig getConfig() {
            return this.configs.get(this.aggressiveness.getValue());
        }
    }

    private static final double htRequestCachePollutionHighThresholdForEvictionPolicy = 0.25;
    private static final double htRequestCachePollutionLowThresholdForEvictionPolicy = 0.005;

    public static final EvictionPolicyFactory TREE_SET_BASED_VICTIM_TRACKING_FACTORY = new EvictionPolicyFactory() {
        public String getName() {
            return "HT_REQUEST_AWARE_LEAST_RECENTLY_USED";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new HtRequestAwareLeastRecentlyUsedEvictionPolicy<StateT, LineT>(cache) {
                private TreeSetBasedAddressSetFilter filter;

                @Override
                public AddressSetFilter getCachePollutionFilter() {
                    if (filter == null) {
                        filter = new TreeSetBasedAddressSetFilter(this.getCache());
                    }
                    return filter;
                }
            };
        }
    };

    public static final EvictionPolicyFactory XOR_BASED_VICTIM_TRACKING_FACTORY = new EvictionPolicyFactory() {
        public String getName() {
            return "HT_REQUEST_AWARE_LEAST_RECENTLY_USED";
        }

        public <StateT extends Serializable, LineT extends CacheLine<StateT>> EvictionPolicy<StateT, LineT> create(EvictableCache<StateT, LineT> cache) {
            return new HtRequestAwareLeastRecentlyUsedEvictionPolicy<StateT, LineT>(cache) {
                private XorBasedAddressSetFilter filter;

                @Override
                public AddressSetFilter getCachePollutionFilter() {
                    if (filter == null) {
                        filter = new XorBasedAddressSetFilter(this.getCache());
                    }
                    return filter;
                }
            };
        }
    };
}
