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

    public ExperimentSpec() {
    }

    public ExperimentSpec(String benchmarkTitle, String benchmarkArguments, int helperThreadLookahead, int helperThreadStride, int numCores, int numThreadsPerCore, String l1ISize, int l1IAssociativity, String l1DSize, int l1DAssociativity, String l2Size, int l2Associativity, String l2ReplacementPolicyType) {
        this.title = "";
        this.createTime = DateHelper.toTick(new Date());

        this.benchmarkTitle = benchmarkTitle;
        this.benchmarkArguments = benchmarkArguments;

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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return parentId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    public void setParent(ExperimentPack parent) {
        this.parentId = parent != null ? parent.getId() : -1;
    }

    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    public String getBenchmarkArguments() {
        return benchmarkArguments;
    }

    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    public int getNumCores() {
        return numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public int getL1ISizeAsInt() {
        return l1ISize == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l1ISize);
    }

    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    public int getL1DSizeAsInt() {
        return l1DSize == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l1DSize);
    }

    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    public int getL2SizeAsInt() {
        return l2Size == null ? 0 : (int) StorageUnitHelper.displaySizeToByteCount(l2Size);
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public String getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    public String getL1ISize() {
        return l1ISize;
    }

    public String getL1DSize() {
        return l1DSize;
    }

    public String getL2Size() {
        return l2Size;
    }

    public void setBenchmarkTitle(String benchmarkTitle) {
        this.benchmarkTitle = benchmarkTitle;
    }

    public void setBenchmarkArguments(String benchmarkArguments) {
        this.benchmarkArguments = benchmarkArguments;
    }

    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
    }

    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    public void setL1ISize(String l1ISize) {
        this.l1ISize = l1ISize;
    }

    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    public void setL1DSize(String l1DSize) {
        this.l1DSize = l1DSize;
    }

    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    public void setL2Size(String l2Size) {
        this.l2Size = l2Size;
    }

    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    public void setL2ReplacementPolicyType(String l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public Architecture getArchitecture() {
        if(architecture == null) {
            architecture = ServiceManager.getArchitectureService().getOrAddArchitecture(true, getNumCores(), getNumThreadsPerCore(), getL1ISizeAsInt(), getL1IAssociativity(), getL1DSizeAsInt(), getL1DAssociativity(), getL2SizeAsInt(), getL2Associativity(), Enum.valueOf(CacheReplacementPolicyType.class, getL2ReplacementPolicyType()));
        }

        return architecture;
    }

    public Benchmark getBenchmark() {
        if(benchmark == null) {
            benchmark = ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmarkTitle);
        }

        return benchmark;
    }

    public String getArguments() {
        if(arguments == null) {
            String benchmarkArguments = getBenchmarkArguments();
            arguments = benchmarkArguments == null ? getBenchmark().getDefaultArguments() : benchmarkArguments;
        }

        return arguments;
    }
}
