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
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.StorageUnit;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithParentId;
import net.pickapack.model.WithTitle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Experiment summary.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentSummary")
public class ExperimentSummary implements WithId, WithParentId, WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long parentId;

    @DatabaseField
    private ExperimentType type;

    @DatabaseField
    private ExperimentState state;

    @DatabaseField
    private int l2Size;

    @DatabaseField
    private int l2Associativity;

    @DatabaseField
    private CacheReplacementPolicyType l2ReplacementPolicyType;

    @DatabaseField
    private int helperThreadLookahead;

    @DatabaseField
    private int helperThreadStride;

    @DatabaseField
    private long numInstructions;

    @DatabaseField
    private long numCycles;

    @DatabaseField
    private double ipc;

    @DatabaseField
    private double cpi;

    @DatabaseField
    private long numMainThreadL2CacheHits;

    @DatabaseField
    private long numMainThreadL2CacheMisses;

    @DatabaseField
    private long numHelperThreadL2CacheHits;

    @DatabaseField
    private long numHelperThreadL2CacheMisses;

    @DatabaseField
    private long numL2CacheEvictions;

    @DatabaseField
    private double l2CacheHitRatio;

    @DatabaseField
    private double l2CacheOccupancyRatio;

    @DatabaseField
    private double helperThreadL2CacheRequestCoverage;

    @DatabaseField
    private double helperThreadL2CacheRequestAccuracy;

    @DatabaseField
    private long numLateHelperThreadL2CacheRequests;

    @DatabaseField
    private long numTimelyHelperThreadL2CacheRequests;

    @DatabaseField
    private long numBadHelperThreadL2CacheRequests;

    @DatabaseField
    private long numUglyHelperThreadL2CacheRequests;

    @DatabaseField
    private long numRedundantHitToTransientTagHelperThreadL2CacheRequests;

    @DatabaseField
    private long numRedundantHitToCacheHelperThreadL2CacheRequests;

    /**
     * Create an experiment summary. Reserved for ORM only.
     */
    public ExperimentSummary() {
    }

    /**
     * Create an experiment summary.
     *
     * @param parent the parent experiment object
     */
    public ExperimentSummary(Experiment parent) {
        this.parentId = parent.getId();
        this.title = parent.getTitle();
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the experiment summary's ID.
     *
     * @return the experiment summary's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the parent experiment's ID.
     *
     * @return the parent experiment's ID
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
     * Get the time in ticks when the experiment summary is created.
     *
     * @return the time in ticks when the experiment summary is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the experiment type.
     *
     * @return the experiment type
     */
    public ExperimentType getType() {
        return type;
    }

    /**
     * Set the experiment type.
     *
     * @param type the experiment type
     */
    public void setType(ExperimentType type) {
        this.type = type;
    }

    /**
     * Get the experiment state.
     *
     * @return the experiment state
     */
    public ExperimentState getState() {
        return state;
    }

    /**
     * Set the experiment state.
     *
     * @param state the experiment state
     */
    public void setState(ExperimentState state) {
        this.state = state;
    }

    /**
     * Get the size in bytes of the L2 cache.
     *
     * @return the size in bytes of the L2 cache
     */
    public int getL2Size() {
        return l2Size;
    }

    /**
     * Set the size in bytes of the L2 cache.
     *
     * @param l2Size the size in bytes of the L2 cache
     */
    public void setL2Size(int l2Size) {
        this.l2Size = l2Size;
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
     * Set the associativity of the L2 cache.
     *
     * @param l2Associativity the associativity of the L2 cache
     */
    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    /**
     * Get the replacement policy type of the L2 cache.
     *
     * @return the replacement policy type of the L2 cache
     */
    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    /**
     * Set the replacement policy type of the L2 cache.
     *
     * @param l2ReplacementPolicyType the replacement policy type of the L2 cache
     */
    public void setL2ReplacementPolicyType(CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
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
     * Set the helper thread lookahead.
     *
     * @param helperThreadLookahead the helper thread lookahead
     */
    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
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
     * Set the helper thread stride.
     *
     * @param helperThreadStride the helper thread stride
     */
    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    /**
     * Get the number of instructions executed on all the hardware threads.
     *
     * @return the number of instructions executed on all the hardware threads
     */
    public long getNumInstructions() {
        return numInstructions;
    }

    /**
     * Set the number of instructions executed on all the hardware threads.
     *
     * @param numInstructions the number of instructions executed on all the hardware threads
     */
    public void setNumInstructions(long numInstructions) {
        this.numInstructions = numInstructions;
    }

    /**
     * Get the number of cycles elapsed.
     *
     * @return the number of cycles elapsed
     */
    public long getNumCycles() {
        return numCycles;
    }

    /**
     * Set the number of cycles elapsed.
     *
     * @param numCycles the number of cycles elapsed
     */
    public void setNumCycles(long numCycles) {
        this.numCycles = numCycles;
    }

    /**
     * Get the IPC value.
     *
     * @return the IPC value
     */
    public double getIpc() {
        return ipc;
    }

    /**
     * Set the IPC value.
     *
     * @param ipc the IPC value
     */
    public void setIpc(double ipc) {
        this.ipc = ipc;
    }

    /**
     * Get the CPI value.
     *
     * @return the CPI value
     */
    public double getCpi() {
        return cpi;
    }

    /**
     * Set the CPI value.
     *
     * @param cpi the CPI value
     */
    public void setCpi(double cpi) {
        this.cpi = cpi;
    }

    /**
     * Get the number of the main thread L2 cache hits.
     *
     * @return the number of the main thread L2 cache hits
     */
    public long getNumMainThreadL2CacheHits() {
        return numMainThreadL2CacheHits;
    }

    /**
     * Set the number of the main thread L2 cache hits.
     *
     * @param numMainThreadL2CacheHits the number of the main thread L2 cache hits
     */
    public void setNumMainThreadL2CacheHits(long numMainThreadL2CacheHits) {
        this.numMainThreadL2CacheHits = numMainThreadL2CacheHits;
    }

    /**
     * Get the number of the main thread L2 cache misses.
     *
     * @return the main thread L2 cache misses
     */
    public long getNumMainThreadL2CacheMisses() {
        return numMainThreadL2CacheMisses;
    }

    /**
     * Set the number of the main thread L2 cache misses.
     *
     * @param numMainThreadL2CacheMisses the number of the main thread L2 cache misses
     */
    public void setNumMainThreadL2CacheMisses(long numMainThreadL2CacheMisses) {
        this.numMainThreadL2CacheMisses = numMainThreadL2CacheMisses;
    }

    /**
     * Get the number of the helper thread L2 cache hits.
     *
     * @return the number of the helper thread L2 cache hits
     */
    public long getNumHelperThreadL2CacheHits() {
        return numHelperThreadL2CacheHits;
    }

    /**
     * Set the number of the helper thread L2 cache hits.
     *
     * @param numHelperThreadL2CacheHits the number of the helper thread L2 cache hits
     */
    public void setNumHelperThreadL2CacheHits(long numHelperThreadL2CacheHits) {
        this.numHelperThreadL2CacheHits = numHelperThreadL2CacheHits;
    }

    /**
     * Get the number of the helper thread L2 cache misses.
     *
     * @return the number of the helper thread L2 cache misses
     */
    public long getNumHelperThreadL2CacheMisses() {
        return numHelperThreadL2CacheMisses;
    }

    /**
     * Set the number of the helper thread L2 cache misses.
     *
     * @param numHelperThreadL2CacheMisses the number of the helper thread L2 cache misses
     */
    public void setNumHelperThreadL2CacheMisses(long numHelperThreadL2CacheMisses) {
        this.numHelperThreadL2CacheMisses = numHelperThreadL2CacheMisses;
    }

    /**
     * Get the number of evictions in the L2 cache.
     *
     * @return the number of evictions in the L2 cache
     */
    public long getNumL2CacheEvictions() {
        return numL2CacheEvictions;
    }

    /**
     * Set the number of evictions in the L2 cache.
     *
     * @param numL2CacheEvictions the number of evictions in the L2 cache
     */
    public void setNumL2CacheEvictions(long numL2CacheEvictions) {
        this.numL2CacheEvictions = numL2CacheEvictions;
    }

    /**
     * Get the hit ratio of the L2 cache.
     *
     * @return the hit ratio of the L2 cache
     */
    public double getL2CacheHitRatio() {
        return l2CacheHitRatio;
    }

    /**
     * Set the hit ratio of the L2 cache.
     *
     * @param l2CacheHitRatio the hit ratio of the L2 cache
     */
    public void setL2CacheHitRatio(double l2CacheHitRatio) {
        this.l2CacheHitRatio = l2CacheHitRatio;
    }

    /**
     * Get the occupancy ratio of the L2 cache.
     *
     * @return the occupancy ratio of the L2 cache
     */
    public double getL2CacheOccupancyRatio() {
        return l2CacheOccupancyRatio;
    }

    /**
     * Set the occupancy ratio of the L2 cache.
     *
     * @param l2CacheOccupancyRatio the occupancy ratio of the L2 cache
     */
    public void setL2CacheOccupancyRatio(double l2CacheOccupancyRatio) {
        this.l2CacheOccupancyRatio = l2CacheOccupancyRatio;
    }

    /**
     * Get the coverage of the helper thread L2 cache requests.
     *
     * @return the coverage of the helper thread L2 cache requests.
     */
    public double getHelperThreadL2CacheRequestCoverage() {
        return helperThreadL2CacheRequestCoverage;
    }

    /**
     * Set the coverage of the helper thread L2 cache requests.
     *
     * @param helperThreadL2CacheRequestCoverage the coverage of the helper thread L2 cache requests
     */
    public void setHelperThreadL2CacheRequestCoverage(double helperThreadL2CacheRequestCoverage) {
        this.helperThreadL2CacheRequestCoverage = helperThreadL2CacheRequestCoverage;
    }

    /**
     * Get the accuracy of the helper thread L2 cache requests.
     *
     * @return the accuracy of the helper thread L2 cache requests
     */
    public double getHelperThreadL2CacheRequestAccuracy() {
        return helperThreadL2CacheRequestAccuracy;
    }

    /**
     * Set the accuracy of the helper thread L2 cache requests.
     *
     * @param helperThreadL2CacheRequestAccuracy the accuracy of the helper thread L2 cache requests
     */
    public void setHelperThreadL2CacheRequestAccuracy(double helperThreadL2CacheRequestAccuracy) {
        this.helperThreadL2CacheRequestAccuracy = helperThreadL2CacheRequestAccuracy;
    }

    /**
     * Get the number of the late helper thread L2 cache requests.
     *
     * @return the number of the late helper thread L2 cache requests
     */
    public long getNumLateHelperThreadL2CacheRequests() {
        return numLateHelperThreadL2CacheRequests;
    }

    /**
     * Set the number of the late helper thread L2 cache requests.
     *
     * @param numLateHelperThreadL2CacheRequests the number of the late helper thread L2 cache requests
     */
    public void setNumLateHelperThreadL2CacheRequests(long numLateHelperThreadL2CacheRequests) {
        this.numLateHelperThreadL2CacheRequests = numLateHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of the timely helper thread L2 cache requests.
     *
     * @return the number of the timely helper thread L2 cache requests
     */
    public long getNumTimelyHelperThreadL2CacheRequests() {
        return numTimelyHelperThreadL2CacheRequests;
    }

    /**
     * Set the number of the timely helper thread L2 cache requests.
     *
     * @param numTimelyHelperThreadL2CacheRequests the number of the timely helper thread L2 cache requests
     */
    public void setNumTimelyHelperThreadL2CacheRequests(long numTimelyHelperThreadL2CacheRequests) {
        this.numTimelyHelperThreadL2CacheRequests = numTimelyHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of the bad helper thread L2 cache requests.
     *
     * @return the number of the bad helper thread L2 cache requests
     */
    public long getNumBadHelperThreadL2CacheRequests() {
        return numBadHelperThreadL2CacheRequests;
    }

    /**
     * Set the number of the bad helper thread L2 cache requests.
     *
     * @param numBadHelperThreadL2CacheRequests the number of the bad helper thread L2 cache requests
     */
    public void setNumBadHelperThreadL2CacheRequests(long numBadHelperThreadL2CacheRequests) {
        this.numBadHelperThreadL2CacheRequests = numBadHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of the ugly helper thread L2 cache requests.
     *
     * @return the number of the ugly helper thread L2 cache requests
     */
    public long getNumUglyHelperThreadL2CacheRequests() {
        return numUglyHelperThreadL2CacheRequests;
    }

    /**
     * Set the number of the ugly helper thread L2 cache requests.
     *
     * @param numUglyHelperThreadL2CacheRequests the number of the ugly helper thread L2 cache requests
     */
    public void setNumUglyHelperThreadL2CacheRequests(long numUglyHelperThreadL2CacheRequests) {
        this.numUglyHelperThreadL2CacheRequests = numUglyHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of the redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @return the number of the redundant "hit to transient tag" helper thread L2 cache requests
     */
    public long getNumRedundantHitToTransientTagHelperThreadL2CacheRequests() {
        return numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    }

    /**
     * Set the number of the redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @param numRedundantHitToTransientTagHelperThreadL2CacheRequests the number of the redundant "hit to transient tag" helper thread L2 cache requests
     */
    public void setNumRedundantHitToTransientTagHelperThreadL2CacheRequests(long numRedundantHitToTransientTagHelperThreadL2CacheRequests) {
        this.numRedundantHitToTransientTagHelperThreadL2CacheRequests = numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    }

    /**
     * Get the number of the redundant "hit to cache" helper thread L2 cache requests.
     *
     * @return the number of the redundant "hit to cache" helper thread L2 cache requests
     */
    public long getNumRedundantHitToCacheHelperThreadL2CacheRequests() {
        return numRedundantHitToCacheHelperThreadL2CacheRequests;
    }

    /**
     * Set the number of the redundant "hit to cache" helper thread L2 cache requests.
     *
     * @param numRedundantHitToCacheHelperThreadL2CacheRequests the number of the redundant "hit to cache" helper thread L2 cache requests
     */
    public void setNumRedundantHitToCacheHelperThreadL2CacheRequests(long numRedundantHitToCacheHelperThreadL2CacheRequests) {
        this.numRedundantHitToCacheHelperThreadL2CacheRequests = numRedundantHitToCacheHelperThreadL2CacheRequests;
    }

    /**
     * Get the parent experiment object.
     *
     * @return the parent experiment object
     */
    public Experiment getParent() {
        return ServiceManager.getExperimentService().getExperimentById(parentId);
    }

    /**
     * Generate the table summary row.
     *
     * @return the table summary row
     */
    public List<String> tableSummary2Row() {
        return new ArrayList<String>() {{
            boolean helperThreadEnabled = helperThreadLookahead != -1;

            add(parentId + "");

            add(type + "");
            add(state + "");

            add(StorageUnit.KILOBYTE.getValue(l2Size) + "KB");
            add(l2Associativity + "way");
            add(l2ReplacementPolicyType + "");

            add(helperThreadEnabled ? "L=" + helperThreadLookahead + "" : "");
            add(helperThreadEnabled ? "S=" + helperThreadStride + "" : "");

            add(numInstructions + "");
            add(numCycles + "");

            add(ipc + "");
            add(cpi + "");

            add(numMainThreadL2CacheHits + "");
            add(numMainThreadL2CacheMisses + "");

            add(numHelperThreadL2CacheHits + "");
            add(numHelperThreadL2CacheMisses + "");

            add(numL2CacheEvictions + "");
            add(l2CacheHitRatio + "");
            add(l2CacheOccupancyRatio + "");

            add(helperThreadL2CacheRequestCoverage + "");
            add(helperThreadL2CacheRequestAccuracy + "");

            add(numLateHelperThreadL2CacheRequests + "");
            add(numTimelyHelperThreadL2CacheRequests + "");
            add(numBadHelperThreadL2CacheRequests + "");
            add(numUglyHelperThreadL2CacheRequests + "");
            add(numRedundantHitToTransientTagHelperThreadL2CacheRequests + "");
            add(numRedundantHitToCacheHelperThreadL2CacheRequests + "");
        }};
    }
}
