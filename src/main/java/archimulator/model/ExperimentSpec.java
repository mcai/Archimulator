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
package archimulator.model;

import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.util.StorageUnitHelper;

import java.util.Date;

/**
 * Experiment specification.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentSpec")
public class ExperimentSpec implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private long parentId;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String benchmarkTitle;

    @DatabaseField
    private String benchmarkArguments;

    @DatabaseField
    private long numMaxInstructions;

    @DatabaseField
    private int helperThreadLookahead;

    @DatabaseField
    private int helperThreadStride;

    @DatabaseField
    private int numCores;

    @DatabaseField
    private int numThreadsPerCore;

    @DatabaseField
    private String l1ISize;

    @DatabaseField
    private int l1IAssociativity;

    private String l1DSize;

    @DatabaseField
    private int l1DAssociativity;

    @DatabaseField
    private String l2Size;

    @DatabaseField
    private int l2Associativity;

    @DatabaseField
    private String l2ReplacementPolicyType;

    private transient Architecture architecture;

    private transient Benchmark benchmark;

    private transient String arguments;

    /**
     * Create an experiment specification. Reserved for ORM only.
     */
    public ExperimentSpec() {
    }

    /**
     * Create an experiment specification.
     *
     * @param benchmarkTitle          the benchmark title
     * @param benchmarkArguments      the benchmark arguments
     * @param numMaxInstructions      the upper limit of the number of instructions executed on the first hardware thread
     * @param helperThreadLookahead   the helper thread lookahead
     * @param helperThreadStride      the helper thread stride
     * @param numCores                the number of cores
     * @param numThreadsPerCore       the number of threads per core
     * @param l1ISize                 the size of the L1I caches
     * @param l1IAssociativity        the associativity of the L1I caches
     * @param l1DSize                 the size of the L1D caches
     * @param l1DAssociativity        the associativity of the L1D caches
     * @param l2Size                  the size of the L2 cache
     * @param l2Associativity         the associativity of the L2 cache
     * @param l2ReplacementPolicyType the replacement policy type of the L2 cache
     */
    public ExperimentSpec(String benchmarkTitle, String benchmarkArguments, long numMaxInstructions, int helperThreadLookahead, int helperThreadStride, int numCores, int numThreadsPerCore, String l1ISize, int l1IAssociativity, String l1DSize, int l1DAssociativity, String l2Size, int l2Associativity, String l2ReplacementPolicyType) {
        this.title = "";
        this.createTime = DateHelper.toTick(new Date());

        this.benchmarkTitle = benchmarkTitle;
        this.benchmarkArguments = benchmarkArguments;
        this.numMaxInstructions = numMaxInstructions;

        this.helperThreadLookahead = helperThreadLookahead;
        this.helperThreadStride = helperThreadStride;

        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;

        this.l1ISize = l1ISize;
        this.l1IAssociativity = l1IAssociativity;

        this.l1DSize = l1DSize;
        this.l1DAssociativity = l1DAssociativity;

        this.l2Size = l2Size;
        this.l2Associativity = l2Associativity;
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    /**
     * Get the experiment specification's ID.
     *
     * @return the experiment specification's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the parent experiment pack's ID.
     *
     * @return the parent experiment pack's ID
     */
    @Override
    public long getParentId() {
        return parentId;
    }

    /**
     * Get the title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Get the time in ticks when the experiment specification is created.
     *
     * @return the time in ticks when the experiment specification is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Set the parent experiment pack.
     *
     * @param parent the parent experiment pack
     */
    public void setParent(ExperimentPack parent) {
        this.parentId = parent != null ? parent.getId() : -1;
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
     * Get the benchmark arguments.
     *
     * @return the benchmark arguments
     */
    public String getBenchmarkArguments() {
        return benchmarkArguments;
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
     * Set the benchmark title.
     *
     * @param benchmarkTitle the benchmark title
     */
    public void setBenchmarkTitle(String benchmarkTitle) {
        this.benchmarkTitle = benchmarkTitle;
    }

    /**
     * Set the benchmark arguments.
     *
     * @param benchmarkArguments the benchmark arguments
     */
    public void setBenchmarkArguments(String benchmarkArguments) {
        this.benchmarkArguments = benchmarkArguments;
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
     * Get the architecture object.
     *
     * @return the architecture object
     */
    public Architecture getArchitecture() {
        if (architecture == null) {
            architecture = ServiceManager.getArchitectureService().getOrAddArchitecture(true, getNumCores(), getNumThreadsPerCore(), getL1ISizeAsInt(), getL1IAssociativity(), getL1DSizeAsInt(), getL1DAssociativity(), getL2SizeAsInt(), getL2Associativity(), Enum.valueOf(CacheReplacementPolicyType.class, getL2ReplacementPolicyType()));
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

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public String getArguments() {
        if (arguments == null) {
            String benchmarkArguments = getBenchmarkArguments();
            arguments = benchmarkArguments == null ? getBenchmark().getDefaultArguments() : benchmarkArguments;
        }

        return arguments;
    }
}
