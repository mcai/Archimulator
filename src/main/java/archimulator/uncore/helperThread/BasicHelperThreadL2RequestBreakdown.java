/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.helperThread;

/**
 * Basic helper thread L2 cache request breakdown.
 *
 * @author Min Cai
 */
public class BasicHelperThreadL2RequestBreakdown implements HelperThreadL2RequestBreakdown {
    private long numMainThreadL2Hits;
    private long numMainThreadL2Misses;

    private long numHelperThreadL2Hits;
    private long numHelperThreadL2Misses;

    private long numRedundantHitToTransientTagHelperThreadL2Requests;
    private long numRedundantHitToCacheHelperThreadL2Requests;

    private long numTimelyHelperThreadL2Requests;
    private long numLateHelperThreadL2Requests;

    private long numBadHelperThreadL2Requests;

    private long numEarlyHelperThreadL2Requests;

    private long numUglyHelperThreadL2Requests;

    /**
     * Create a basic helper thread L2 cache request breakdown.
     *
     * @param numMainThreadL2Hits                                 the number of main thread L2 hits
     * @param numMainThreadL2Misses                               the number of main thread L2 misses
     * @param numHelperThreadL2Hits                               the number of helper thread L2 hits
     * @param numHelperThreadL2Misses                             the number of helper thread L2 misses
     * @param numRedundantHitToTransientTagHelperThreadL2Requests the number of redundant hit-to-transient-tag helper thread L2 requests
     * @param numRedundantHitToCacheHelperThreadL2Requests        the number of redundant hit-to-cache helper thread l2 requests
     * @param numTimelyHelperThreadL2Requests                     the number of timely helper thread L2 requests
     * @param numLateHelperThreadL2Requests                       the number of late helper thread L2 requests
     * @param numBadHelperThreadL2Requests                        the number of bad helper thread L2 requests
     * @param numEarlyHelperThreadL2Requests                      the number of early helper thread L2 requests
     * @param numUglyHelperThreadL2Requests                       the number of ugly helper thread L2 requests
     */
    public BasicHelperThreadL2RequestBreakdown(
            long numMainThreadL2Hits,
            long numMainThreadL2Misses,
            long numHelperThreadL2Hits,
            long numHelperThreadL2Misses,
            long numRedundantHitToTransientTagHelperThreadL2Requests,
            long numRedundantHitToCacheHelperThreadL2Requests,
            long numTimelyHelperThreadL2Requests,
            long numLateHelperThreadL2Requests,
            long numBadHelperThreadL2Requests,
            long numEarlyHelperThreadL2Requests,
            long numUglyHelperThreadL2Requests
    ) {
        this.numMainThreadL2Hits = numMainThreadL2Hits;
        this.numMainThreadL2Misses = numMainThreadL2Misses;
        this.numHelperThreadL2Hits = numHelperThreadL2Hits;
        this.numHelperThreadL2Misses = numHelperThreadL2Misses;
        this.numRedundantHitToTransientTagHelperThreadL2Requests = numRedundantHitToTransientTagHelperThreadL2Requests;
        this.numRedundantHitToCacheHelperThreadL2Requests = numRedundantHitToCacheHelperThreadL2Requests;
        this.numTimelyHelperThreadL2Requests = numTimelyHelperThreadL2Requests;
        this.numLateHelperThreadL2Requests = numLateHelperThreadL2Requests;
        this.numBadHelperThreadL2Requests = numBadHelperThreadL2Requests;
        this.numEarlyHelperThreadL2Requests = numEarlyHelperThreadL2Requests;
        this.numUglyHelperThreadL2Requests = numUglyHelperThreadL2Requests;
    }

    /**
     * Get the number of main thread L2 hits.
     *
     * @return the number of main thread L2 hits
     */
    public long getNumMainThreadL2Hits() {
        return numMainThreadL2Hits;
    }

    /**
     * Get the number of main thread L2 misses.
     *
     * @return the number of main thread l2 misses
     */
    public long getNumMainThreadL2Misses() {
        return numMainThreadL2Misses;
    }

    /**
     * Get the number of helper thread L2 hits.
     *
     * @return the number of helper thread l2 hits
     */
    public long getNumHelperThreadL2Hits() {
        return numHelperThreadL2Hits;
    }

    /**
     * Get the number of helper thread L2 misses.
     *
     * @return the number of helper thread L2 misses
     */
    public long getNumHelperThreadL2Misses() {
        return numHelperThreadL2Misses;
    }

    /**
     * Get the number of redundant hit-to-transient-tag helper thread L2 requests.
     *
     * @return the number of redundant hit-to-transient-tag helper thread L2 requests
     */
    public long getNumRedundantHitToTransientTagHelperThreadL2Requests() {
        return numRedundantHitToTransientTagHelperThreadL2Requests;
    }

    /**
     * Get the number of redundant hit-to-cache helper thread L2 requests.
     *
     * @return the number of redundant hit-to-cache helper thread L2 requests
     */
    public long getNumRedundantHitToCacheHelperThreadL2Requests() {
        return numRedundantHitToCacheHelperThreadL2Requests;
    }

    /**
     * Get the number of timely helper thread L2 requests.
     *
     * @return the number of timely helper thread l2 requests
     */
    public long getNumTimelyHelperThreadL2Requests() {
        return numTimelyHelperThreadL2Requests;
    }

    /**
     * Get the number of late helper thread L2 requests.
     *
     * @return the number of late helper thread L2 requests
     */
    public long getNumLateHelperThreadL2Requests() {
        return numLateHelperThreadL2Requests;
    }

    /**
     * Get the number of bad helper thread L2 requests.
     *
     * @return the number of bad helper thread L2 requests
     */
    public long getNumBadHelperThreadL2Requests() {
        return numBadHelperThreadL2Requests;
    }

    /**
     * Get the number of early helper thread L2 requests.
     *
     * @return the number of early helper thread L2 requests
     */
    public long getNumEarlyHelperThreadL2Requests() {
        return numEarlyHelperThreadL2Requests;
    }

    /**
     * Get the number of ugly helper thread L2 requests.
     *
     * @return the number of ugly helper thread L2 requests
     */
    public long getNumUglyHelperThreadL2Requests() {
        return numUglyHelperThreadL2Requests;
    }
}
