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
package archimulator.model;

import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.util.StorageUnitHelper;

import java.io.Serializable;

/**
 * Experiment specification.
 *
 * @author Min Cai
 */
public class ExperimentSpec implements Serializable {
    private String benchmarkTitle;

    private long numMaxInstructions;

    private int helperThreadLookahead;

    private int helperThreadStride;

    private int numCores;

    private int numThreadsPerCore;

    private String l1ISize;

    private int l1IAssociativity;

    private String l1DSize;

    private int l1DAssociativity;

    private String l2Size;

    private int l2Associativity;

    private String l2ReplacementPolicyType;

    private boolean dynamicSpeculativePrecomputationEnabled;

    private transient Architecture architecture;

    private transient Benchmark benchmark;

    /**
     * Create an experiment specification.
     */
    public ExperimentSpec() {
    }

    /**
     * Get the benchmark title.
     *
     * @return the benchmark title
     */
    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    /**
     * Get the upper limit of the number of instructions executed on the first hardware thread.
     *
     * @return the upper limit of the number of instructions executed on the first hardware thread.
     */
    public long getNumMaxInstructions() {
        if (numMaxInstructions == 0L) {
            numMaxInstructions = -1L;
        }

        return numMaxInstructions;
    }

    /**
     * Get the helper thread lookahead.
     *
     * @return the helper thread lookahead
     */
    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    /**
     * Get the helper thread stride.
     *
     * @return the helper thread stride
     */
    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    /**
     * Get the number of cores.
     *
     * @return the number of cores
     */
    public int getNumCores() {
        return numCores;
    }

    /**
     * Get the number of threads per core.
     *
     * @return the number of threads per core
     */
    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    /**
     * Get the size in bytes of the L1I cache.
     *
     * @return the size in bytes of the L1I cache
     */
    public int getL1ISizeAsInt() {
        return l1ISize == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l1ISize);
    }

    /**
     * Get the associativity of the L1I cache.
     *
     * @return the associativity of the L1I cache
     */
    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    /**
     * Get the size in bytes of the L1D cache.
     *
     * @return the size in bytes of the L1D cache
     */
    public int getL1DSizeAsInt() {
        return l1DSize == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l1DSize);
    }

    /**
     * Get the associativity of the L1D cache.
     *
     * @return the associativity of the L1D cache
     */
    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    /**
     * Get the size in bytes of the L2 cache.
     *
     * @return the size in bytes of the L2 cache
     */
    public int getL2SizeAsInt() {
        return l2Size == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l2Size);
    }

    /**
     * Get the associativity of the L2 cache.
     *
     * @return the associativity of the L2 cache
     */
    public int getL2Associativity() {
        return l2Associativity;
    }

    /**
     * Get the replacement policy type of the L2 cache.
     *
     * @return the replacement policy type of the L2 cache
     */
    public String getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    /**
     * Get the size of the L1I cache.
     *
     * @return the size of the L1I cache
     */
    public String getL1ISize() {
        return l1ISize;
    }

    /**
     * Get the size of the L1D cache.
     *
     * @return the size of the L1D cache
     */
    public String getL1DSize() {
        return l1DSize;
    }

    /**
     * Get the size of the L2 cache.
     *
     * @return the size of the L2 cache
     */
    public String getL2Size() {
        return l2Size;
    }

    /**
     * Get a value indicating whether dynamic speculative precomputation is enabled or not.
     *
     * @return a value indicating whether dynamic speculative precomputation is enabled or not
     */
    public boolean isDynamicSpeculativePrecomputationEnabled() {
        return dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Set the benchmark title.
     *
     * @param benchmarkTitle the benchmark title
     */
    public void setBenchmarkTitle(String benchmarkTitle) {
        this.benchmarkTitle = benchmarkTitle;
    }

    /**
     * Set the upper limit of the number of instructions executed on the first hardware thread.
     *
     * @param numMaxInstructions the upper limit of the number of instructions executed on the first hardware thread.
     */
    public void setNumMaxInstructions(long numMaxInstructions) {
        this.numMaxInstructions = numMaxInstructions;
    }

    /**
     * Set the helper thread lookahead.
     *
     * @param helperThreadLookahead the helper thread lookahead
     */
    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
    }

    /**
     * Set the helper thread stride.
     *
     * @param helperThreadStride the helper thread stride
     */
    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    /**
     * Set the number of cores.
     *
     * @param numCores the number of cores
     */
    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    /**
     * Set the number of threads per core.
     *
     * @param numThreadsPerCore the number of threads per core
     */
    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    /**
     * Set the size of the L1I caches.
     *
     * @param l1ISize the size of the L1I caches
     */
    public void setL1ISize(String l1ISize) {
        this.l1ISize = l1ISize;
    }

    /**
     * Set the associativity of the L1I caches.
     *
     * @param l1IAssociativity the associativity of the L1I caches
     */
    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    /**
     * Set the size of the L1D caches.
     *
     * @param l1DSize the size of the L1D caches
     */
    public void setL1DSize(String l1DSize) {
        this.l1DSize = l1DSize;
    }

    /**
     * Set the associativity of the L1D caches.
     *
     * @param l1DAssociativity the associativity of the L1D caches
     */
    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    /**
     * Set the size of the L2 cache.
     *
     * @param l2Size the size of the L2 cache
     */
    public void setL2Size(String l2Size) {
        this.l2Size = l2Size;
    }

    /**
     * Set the associativity of the L2 cache.
     *
     * @param l2Associativity the associativity of the L2 cache
     */
    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    /**
     * Set the replacement policy type of the L2 cache.
     *
     * @param l2ReplacementPolicyType the replacement policy type of the L2 cache
     */
    public void setL2ReplacementPolicyType(String l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    /**
     * Set a value indicating whether dynamic speculative precomputation is enabled or not.
     *
     * @param dynamicSpeculativePrecomputationEnabled
     *         a value indicating whether dynamic speculative precomputation is enabled or not
     */
    public void setDynamicSpeculativePrecomputationEnabled(boolean dynamicSpeculativePrecomputationEnabled) {
        this.dynamicSpeculativePrecomputationEnabled = dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Get the architecture object.
     *
     * @return the architecture object
     */
    public Architecture getArchitecture() {
        if (architecture == null) {
            architecture = ServiceManager.getArchitectureService().getOrAddArchitecture(true, true, dynamicSpeculativePrecomputationEnabled, numCores, numThreadsPerCore, getL1ISizeAsInt(), l1IAssociativity, getL1DSizeAsInt(), l1DAssociativity, getL2SizeAsInt(), l2Associativity, Enum.valueOf(CacheReplacementPolicyType.class, getL2ReplacementPolicyType()));
        }

        return architecture;
    }

    /**
     * Get the benchmark.
     *
     * @return the benchmark
     */
    public Benchmark getBenchmark() {
        if (benchmark == null) {
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmarkTitle);
        }

        return benchmark;
    }
}
