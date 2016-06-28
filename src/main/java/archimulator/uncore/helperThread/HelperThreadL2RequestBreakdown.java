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
 * Helper thread L2 cache request breakdown.
 *
 * @author Min Cai
 */
public interface HelperThreadL2RequestBreakdown {
    /**
     * Get the number of main thread L2 hits.
     *
     * @return the number of main thread L2 hits
     */
    long getNumMainThreadL2Hits();

    /**
     * Get the number of main thread L2 misses.
     *
     * @return the number of main thread l2 misses
     */
    long getNumMainThreadL2Misses();

    /**
     * Get the number of helper thread L2 hits.
     *
     * @return the number of helper thread l2 hits
     */
    long getNumHelperThreadL2Hits();

    /**
     * Get the number of helper thread L2 misses.
     *
     * @return the number of helper thread L2 misses
     */
    long getNumHelperThreadL2Misses();

    /**
     * Get the number of redundant hit-to-transient-tag helper thread L2 requests.
     *
     * @return the number of redundant hit-to-transient-tag helper thread L2 requests
     */
    long getNumRedundantHitToTransientTagHelperThreadL2Requests();

    /**
     * Get the number of redundant hit-to-cache helper thread L2 requests.
     *
     * @return the number of redundant hit-to-cache helper thread L2 requests
     */
    long getNumRedundantHitToCacheHelperThreadL2Requests();

    /**
     * Get the number of timely helper thread L2 requests.
     *
     * @return the number of timely helper thread l2 requests
     */
    long getNumTimelyHelperThreadL2Requests();

    /**
     * Get the number of late helper thread L2 requests.
     *
     * @return the number of late helper thread L2 requests
     */
    long getNumLateHelperThreadL2Requests();

    /**
     * Get the number of bad helper thread L2 requests.
     *
     * @return the number of bad helper thread L2 requests
     */
    long getNumBadHelperThreadL2Requests();

    /**
     * Get the number of early helper thread L2 requests.
     *
     * @return the number of early helper thread L2 requests
     */
    long getNumEarlyHelperThreadL2Requests();

    /**
     * Get the number of ugly helper thread L2 requests.
     *
     * @return the number of ugly helper thread L2 requests
     */
    long getNumUglyHelperThreadL2Requests();

    /**
     * Get the total number of helper thread L2 cache requests.
     *
     * @return the total number of helper thread L2 cache requests
     */
    default long getNumTotalHelperThreadL2Requests() {
        return getNumLateHelperThreadL2Requests() + getNumTimelyHelperThreadL2Requests() + getNumBadHelperThreadL2Requests()
                + getNumEarlyHelperThreadL2Requests() + getNumUglyHelperThreadL2Requests()
                + getNumRedundantHitToTransientTagHelperThreadL2Requests() + getNumRedundantHitToCacheHelperThreadL2Requests();
    }

    /**
     * Get the number of useful helper thread L2 cache requests.
     *
     * @return the number of useful helper thread L2 requests
     */
    default long getNumUsefulHelperThreadL2Requests() {
        return getNumLateHelperThreadL2Requests() + getNumTimelyHelperThreadL2Requests();
    }

    /**
     * Get the number of useless helper thread L2 cache requests.
     *
     * @return the number of useless helper thread L2 cache requests
     */
    default long getNumUselessHelperThreadL2Requests(
    ) {
        return getNumBadHelperThreadL2Requests()
                + getNumEarlyHelperThreadL2Requests() + getNumUglyHelperThreadL2Requests()
                + getNumRedundantHitToTransientTagHelperThreadL2Requests() + getNumRedundantHitToCacheHelperThreadL2Requests();
    }

    /**
     * Get the coverage of helper thread L2 cache requests.
     *
     * @return the coverage of helper thread L2 cache requests
     * @param numMainThreadL2MissesInBaselineExperiment the number of main thread L2 misses in the baseline experiment
     */
    default double getHelperThreadL2RequestCoverage(
            long numMainThreadL2MissesInBaselineExperiment) {
        return numMainThreadL2MissesInBaselineExperiment == 0 ? 0 : (double) getNumUsefulHelperThreadL2Requests() / numMainThreadL2MissesInBaselineExperiment;
    }

    /**
     * Get the coverage of helper thread L2 cache requests.
     *
     * @return the coverage of helper thread L2 cache requests
     */
    default double getHelperThreadL2RequestCoverage(
    ) {
        return getNumMainThreadL2Misses() == 0 ? 0 : (double) getNumUsefulHelperThreadL2Requests() / (getNumMainThreadL2Misses());
    }

    /**
     * Get the accuracy of helper thread L2 cache requests.
     *
     * @return the accuracy of helper thread L2 cache requests
     */
    default double getHelperThreadL2RequestAccuracy() {
        return getNumTotalHelperThreadL2Requests() == 0 ? 0 : (double) getNumUsefulHelperThreadL2Requests() / getNumTotalHelperThreadL2Requests();
    }

    /**
     * Get the lateness of helper thread L2 cache requests.
     *
     * @return the lateness of helper thread L2 cache requests
     */
    default double getHelperThreadL2RequestLateness() {
        return getNumTotalHelperThreadL2Requests() == 0 ? 0 : (double) getNumLateHelperThreadL2Requests() / getNumUsefulHelperThreadL2Requests();
    }

    /**
     * Get the pollution of helper thread L2 cache requests.
     *
     * @return the pollution of helper thread L2 cache requests
     */
    default double getHelperThreadL2RequestPollution() {
        return getNumTotalHelperThreadL2Requests() == 0 ? 0 : (double) getNumBadHelperThreadL2Requests() / getNumUselessHelperThreadL2Requests();
    }
}
