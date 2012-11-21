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
     *
     */
    public ExperimentSpec() {
    }

    /**
     *
     * @param benchmarkTitle
     * @param benchmarkArguments
     * @param numMaxInstructions
     * @param helperThreadLookahead
     * @param helperThreadStride
     * @param numCores
     * @param numThreadsPerCore
     * @param l1ISize
     * @param l1IAssociativity
     * @param l1DSize
     * @param l1DAssociativity
     * @param l2Size
     * @param l2Associativity
     * @param l2ReplacementPolicyType
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
     *
     * @return
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public long getParentId() {
        return parentId;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @param parent
     */
    public void setParent(ExperimentPack parent) {
        this.parentId = parent != null ? parent.getId() : -1;
    }

    /**
     *
     * @return
     */
    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    /**
     *
     * @return
     */
    public String getBenchmarkArguments() {
        return benchmarkArguments;
    }

    /**
     *
     * @return
     */
    public long getNumMaxInstructions() {
        if(numMaxInstructions == 0L) {
            numMaxInstructions = -1L;
        }

        return numMaxInstructions;
    }

    /**
     *
     * @return
     */
    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    /**
     *
     * @return
     */
    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    /**
     *
     * @return
     */
    public int getNumCores() {
        return numCores;
    }

    /**
     *
     * @return
     */
    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    /**
     *
     * @return
     */
    public int getL1ISizeAsInt() {
        return l1ISize == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l1ISize);
    }

    /**
     *
     * @return
     */
    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    /**
     *
     * @return
     */
    public int getL1DSizeAsInt() {
        return l1DSize == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l1DSize);
    }

    /**
     *
     * @return
     */
    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    /**
     *
     * @return
     */
    public int getL2SizeAsInt() {
        return l2Size == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l2Size);
    }

    /**
     *
     * @return
     */
    public int getL2Associativity() {
        return l2Associativity;
    }

    /**
     *
     * @return
     */
    public String getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    /**
     *
     * @return
     */
    public String getL1ISize() {
        return l1ISize;
    }

    /**
     *
     * @return
     */
    public String getL1DSize() {
        return l1DSize;
    }

    /**
     *
     * @return
     */
    public String getL2Size() {
        return l2Size;
    }

    /**
     *
     * @param benchmarkTitle
     */
    public void setBenchmarkTitle(String benchmarkTitle) {
        this.benchmarkTitle = benchmarkTitle;
    }

    /**
     *
     * @param benchmarkArguments
     */
    public void setBenchmarkArguments(String benchmarkArguments) {
        this.benchmarkArguments = benchmarkArguments;
    }

    /**
     *
     * @param numMaxInstructions
     */
    public void setNumMaxInstructions(long numMaxInstructions) {
        this.numMaxInstructions = numMaxInstructions;
    }

    /**
     *
     * @param helperThreadLookahead
     */
    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
    }

    /**
     *
     * @param helperThreadStride
     */
    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    /**
     *
     * @param numCores
     */
    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    /**
     *
     * @param numThreadsPerCore
     */
    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    /**
     *
     * @param l1ISize
     */
    public void setL1ISize(String l1ISize) {
        this.l1ISize = l1ISize;
    }

    /**
     *
     * @param l1IAssociativity
     */
    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    /**
     *
     * @param l1DSize
     */
    public void setL1DSize(String l1DSize) {
        this.l1DSize = l1DSize;
    }

    /**
     *
     * @param l1DAssociativity
     */
    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    /**
     *
     * @param l2Size
     */
    public void setL2Size(String l2Size) {
        this.l2Size = l2Size;
    }

    /**
     *
     * @param l2Associativity
     */
    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    /**
     *
     * @param l2ReplacementPolicyType
     */
    public void setL2ReplacementPolicyType(String l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    /**
     *
     * @return
     */
    public Architecture getArchitecture() {
        if(architecture == null) {
            architecture = ServiceManager.getArchitectureService().getOrAddArchitecture(true, getNumCores(), getNumThreadsPerCore(), getL1ISizeAsInt(), getL1IAssociativity(), getL1DSizeAsInt(), getL1DAssociativity(), getL2SizeAsInt(), getL2Associativity(), Enum.valueOf(CacheReplacementPolicyType.class, getL2ReplacementPolicyType()));
        }

        return architecture;
    }

    /**
     *
     * @return
     */
    public Benchmark getBenchmark() {
        if(benchmark == null) {
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmarkTitle);
        }

        return benchmark;
    }

    /**
     *
     * @return
     */
    public String getArguments() {
        if(arguments == null) {
            String benchmarkArguments = getBenchmarkArguments();
            arguments = benchmarkArguments == null ? getBenchmark().getDefaultArguments() : benchmarkArguments;
        }

        return arguments;
    }
}
