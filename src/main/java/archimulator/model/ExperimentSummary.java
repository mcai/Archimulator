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
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithParentId;
import net.pickapack.model.WithTitle;

import java.util.Date;

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
    private String benchmarkTitle;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long parentId;

    @DatabaseField
    private ExperimentType type;

    @DatabaseField
    private ExperimentState state;

    @DatabaseField
    private String beginTimeAsString;

    @DatabaseField
    private String endTimeAsString;

    @DatabaseField
    private String duration;

    @DatabaseField
    private long durationInSeconds;

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
    private int numMainThreadWaysInStaticPartitionedLRUPolicy;

    @DatabaseField
    private long numInstructions;

    @DatabaseField
    private long c0t0NumInstructions;

    @DatabaseField
    private long c1t0NumInstructions;

    @DatabaseField
    private long numCycles;

    @DatabaseField
    private double ipc;

    @DatabaseField
    private double c0t0Ipc;

    @DatabaseField
    private double c1t0Ipc;

    @DatabaseField
    private double cpi;

    @DatabaseField
    private double c0t0Cpi;

    @DatabaseField
    private double c1t0Cpi;

    @DatabaseField
    private double l2Mpki;

    @DatabaseField
    private double c0t0L2Mpki;

    @DatabaseField
    private double c1t0L2Mpki;

    @DatabaseField
    private long numMainThreadL2Hits;

    @DatabaseField
    private long numMainThreadL2Misses;

    @DatabaseField
    private long numHelperThreadL2Hits;

    @DatabaseField
    private long numHelperThreadL2Misses;

    @DatabaseField
    private long numL2Evictions;

    @DatabaseField
    private double l2HitRatio;

    @DatabaseField
    private double l2OccupancyRatio;

    @DatabaseField
    private double helperThreadL2RequestCoverage;

    @DatabaseField
    private double helperThreadL2RequestAccuracy;

    @DatabaseField
    private double helperThreadL2RequestRedundancy;

    @DatabaseField
    private double helperThreadL2RequestEarliness;

    @DatabaseField
    private double helperThreadL2RequestLateness;

    @DatabaseField
    private double helperThreadL2RequestPollution;

    @DatabaseField
    private long numLateHelperThreadL2Requests;

    @DatabaseField
    private long numTimelyHelperThreadL2Requests;

    @DatabaseField
    private long numBadHelperThreadL2Requests;

    @DatabaseField
    private long numEarlyHelperThreadL2Requests;

    @DatabaseField
    private long numUglyHelperThreadL2Requests;

    @DatabaseField
    private long numRedundantHitToTransientTagHelperThreadL2Requests;

    @DatabaseField
    private long numRedundantHitToCacheHelperThreadL2Requests;

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
     * Get the benchmark title.
     *
     * @return the benchmark title
     */
    public String getBenchmarkTitle() {
        return benchmarkTitle;
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
     * Get the string representation of the begin time.
     *
     * @return the string representation of the begin time
     */
    public String getBeginTimeAsString() {
        return beginTimeAsString;
    }

    /**
     * Set the string representation of the begin time.
     *
     * @param beginTimeAsString the string representation of the begin time
     */
    public void setBeginTimeAsString(String beginTimeAsString) {
        this.beginTimeAsString = beginTimeAsString;
    }

    /**
     * Get the string representation of the end time.
     *
     * @return the string representation of the end time
     */
    public String getEndTimeAsString() {
        return endTimeAsString;
    }

    /**
     * Set the string representation of the end time.
     *
     * @param endTimeAsString the string representation of the end time
     */
    public void setEndTimeAsString(String endTimeAsString) {
        this.endTimeAsString = endTimeAsString;
    }

    /**
     * Get the duration.
     *
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Set the duration.
     *
     * @param duration the duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Get the duration in seconds.
     *
     * @return the duration in seconds
     */
    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    /**
     * Set the duration in seconds.
     *
     * @param durationInSeconds the duration in seconds
     */
    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
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
     * Get the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache.
     *
     * @return the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache
     */
    public int getNumMainThreadWaysInStaticPartitionedLRUPolicy() {
        return numMainThreadWaysInStaticPartitionedLRUPolicy;
    }

    /**
     * Set the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache.
     *
     * @param numMainThreadWaysInStaticPartitionedLRUPolicy
     *         the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache
     */
    public void setNumMainThreadWaysInStaticPartitionedLRUPolicy(int numMainThreadWaysInStaticPartitionedLRUPolicy) {
        this.numMainThreadWaysInStaticPartitionedLRUPolicy = numMainThreadWaysInStaticPartitionedLRUPolicy;
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
     * Get the number of instructions executed on the thread C0T0.
     *
     * @return the number of instructions executed on the thread C0T0
     */
    public long getC0t0NumInstructions() {
        return c0t0NumInstructions;
    }

    /**
     * Set the number of instructions executed on the thread C0T0.
     *
     * @param c0t0NumInstructions the number of instructions executed on the thread C0T0
     */
    public void setC0t0NumInstructions(long c0t0NumInstructions) {
        this.c0t0NumInstructions = c0t0NumInstructions;
    }

    /**
     * Get the number of instructions executed on the thread C1T0.
     *
     * @return the number of instructions executed on the thread C1T0
     */
    public long getC1t0NumInstructions() {
        return c1t0NumInstructions;
    }

    /**
     * Set the number of instructions executed on the thread C1T0.
     *
     * @param c1t0NumInstructions the number of instructions executed on the thread C1T0
     */
    public void setC1t0NumInstructions(long c1t0NumInstructions) {
        this.c1t0NumInstructions = c1t0NumInstructions;
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
     * Get the IPC value for the thread C0T0.
     *
     * @return the IPC value for the thread C0T0
     */
    public double getC0t0Ipc() {
        return c0t0Ipc;
    }

    /**
     * Set the IPC value for the thread C0T0.
     *
     * @param c0t0Ipc the IPC value for the thread C0T0
     */
    public void setC0t0Ipc(double c0t0Ipc) {
        this.c0t0Ipc = c0t0Ipc;
    }

    /**
     * Get the IPC value for the thread C1T0.
     *
     * @return the IPC value for the thread C1T0
     */
    public double getC1t0Ipc() {
        return c1t0Ipc;
    }

    /**
     * Set the IPC value for the thread C1T0.
     *
     * @param c1t0Ipc the IPC value for the thread C1T0
     */
    public void setC1t0Ipc(double c1t0Ipc) {
        this.c1t0Ipc = c1t0Ipc;
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
     * Get the CPI value for the thread C0T0.
     *
     * @return the CPI value for the thread C0T0
     */
    public double getC0t0Cpi() {
        return c0t0Cpi;
    }

    /**
     * Set the CPI value for the thread C0T0.
     *
     * @param c0t0Cpi the CPI value for the thread C0T0.
     */
    public void setC0t0Cpi(double c0t0Cpi) {
        this.c0t0Cpi = c0t0Cpi;
    }

    /**
     * Get the CPI value for the thread C1T0.
     *
     * @return the CPI value for the thread C1T0
     */
    public double getC1t0Cpi() {
        return c1t0Cpi;
    }

    /**
     * Set the CPI value for the thread C1T0.
     *
     * @param c1t0Cpi the CPI value for the thread C1T0
     */
    public void setC1t0Cpi(double c1t0Cpi) {
        this.c1t0Cpi = c1t0Cpi;
    }

    /**
     * Get the L2 MPKI value.
     *
     * @return the MPKI value
     */
    public double getL2Mpki() {
        return l2Mpki;
    }

    /**
     * Set the L2 MPKI value.
     *
     * @param l2Mpki the MPKI value
     */
    public void setL2Mpki(double l2Mpki) {
        this.l2Mpki = l2Mpki;
    }

    /**
     * Get the L2 MPKI value for the thread C0T0.
     *
     * @return the L2 MPKI value for the thread C0T0
     */
    public double getC0t0L2Mpki() {
        return c0t0L2Mpki;
    }

    /**
     * Set the L2 MPKI value for the thread C0T0.
     *
     * @param c0t0L2Mpki the L2 MPKI value for the thread C0T0
     */
    public void setC0t0L2Mpki(double c0t0L2Mpki) {
        this.c0t0L2Mpki = c0t0L2Mpki;
    }

    /**
     * Get the L2 MPKI value for the thread C1T0.
     *
     * @return the L2 MPKI value for the thread C1T0
     */
    public double getC1t0L2Mpki() {
        return c1t0L2Mpki;
    }

    /**
     * Set the L2 MPKI value for the thread C1T0.
     *
     * @param c1t0L2Mpki the L2 MPKI value for the thread C1T0
     */
    public void setC1t0L2Mpki(double c1t0L2Mpki) {
        this.c1t0L2Mpki = c1t0L2Mpki;
    }

    /**
     * Get the number of the main thread L2 cache hits.
     *
     * @return the number of the main thread L2 cache hits
     */
    public long getNumMainThreadL2Hits() {
        return numMainThreadL2Hits;
    }

    /**
     * Set the number of the main thread L2 cache hits.
     *
     * @param numMainThreadL2Hits the number of the main thread L2 cache hits
     */
    public void setNumMainThreadL2Hits(long numMainThreadL2Hits) {
        this.numMainThreadL2Hits = numMainThreadL2Hits;
    }

    /**
     * Get the number of the main thread L2 cache misses.
     *
     * @return the main thread L2 cache misses
     */
    public long getNumMainThreadL2Misses() {
        return numMainThreadL2Misses;
    }

    /**
     * Set the number of the main thread L2 cache misses.
     *
     * @param numMainThreadL2Misses the number of the main thread L2 cache misses
     */
    public void setNumMainThreadL2Misses(long numMainThreadL2Misses) {
        this.numMainThreadL2Misses = numMainThreadL2Misses;
    }

    /**
     * Get the number of the helper thread L2 cache hits.
     *
     * @return the number of the helper thread L2 cache hits
     */
    public long getNumHelperThreadL2Hits() {
        return numHelperThreadL2Hits;
    }

    /**
     * Set the number of the helper thread L2 cache hits.
     *
     * @param numHelperThreadL2Hits the number of the helper thread L2 cache hits
     */
    public void setNumHelperThreadL2Hits(long numHelperThreadL2Hits) {
        this.numHelperThreadL2Hits = numHelperThreadL2Hits;
    }

    /**
     * Get the number of the helper thread L2 cache misses.
     *
     * @return the number of the helper thread L2 cache misses
     */
    public long getNumHelperThreadL2Misses() {
        return numHelperThreadL2Misses;
    }

    /**
     * Set the number of the helper thread L2 cache misses.
     *
     * @param numHelperThreadL2Misses the number of the helper thread L2 cache misses
     */
    public void setNumHelperThreadL2Misses(long numHelperThreadL2Misses) {
        this.numHelperThreadL2Misses = numHelperThreadL2Misses;
    }

    /**
     * Get the number of evictions in the L2 cache.
     *
     * @return the number of evictions in the L2 cache
     */
    public long getNumL2Evictions() {
        return numL2Evictions;
    }

    /**
     * Set the number of evictions in the L2 cache.
     *
     * @param numL2Evictions the number of evictions in the L2 cache
     */
    public void setNumL2Evictions(long numL2Evictions) {
        this.numL2Evictions = numL2Evictions;
    }

    /**
     * Get the hit ratio of the L2 cache.
     *
     * @return the hit ratio of the L2 cache
     */
    public double getL2HitRatio() {
        return l2HitRatio;
    }

    /**
     * Set the hit ratio of the L2 cache.
     *
     * @param l2HitRatio the hit ratio of the L2 cache
     */
    public void setL2HitRatio(double l2HitRatio) {
        this.l2HitRatio = l2HitRatio;
    }

    /**
     * Get the occupancy ratio of the L2 cache.
     *
     * @return the occupancy ratio of the L2 cache
     */
    public double getL2OccupancyRatio() {
        return l2OccupancyRatio;
    }

    /**
     * Set the occupancy ratio of the L2 cache.
     *
     * @param l2OccupancyRatio the occupancy ratio of the L2 cache
     */
    public void setL2OccupancyRatio(double l2OccupancyRatio) {
        this.l2OccupancyRatio = l2OccupancyRatio;
    }

    /**
     * Get the coverage of the helper thread L2 cache requests.
     *
     * @return the coverage of the helper thread L2 cache requests.
     */
    public double getHelperThreadL2RequestCoverage() {
        return helperThreadL2RequestCoverage;
    }

    /**
     * Set the coverage of the helper thread L2 cache requests.
     *
     * @param helperThreadL2RequestCoverage
     *         the coverage of the helper thread L2 cache requests
     */
    public void setHelperThreadL2RequestCoverage(double helperThreadL2RequestCoverage) {
        this.helperThreadL2RequestCoverage = helperThreadL2RequestCoverage;
    }

    /**
     * Get the accuracy of the helper thread L2 cache requests.
     *
     * @return the accuracy of the helper thread L2 cache requests
     */
    public double getHelperThreadL2RequestAccuracy() {
        return helperThreadL2RequestAccuracy;
    }

    /**
     * Set the accuracy of the helper thread L2 cache requests.
     *
     * @param helperThreadL2RequestAccuracy
     *         the accuracy of the helper thread L2 cache requests
     */
    public void setHelperThreadL2RequestAccuracy(double helperThreadL2RequestAccuracy) {
        this.helperThreadL2RequestAccuracy = helperThreadL2RequestAccuracy;
    }

    /**
     * Get the redundancy of the helper thread L2 cache requests.
     *
     * @return the redundancy of the helper thread L2 cache requests
     */
    public double getHelperThreadL2RequestRedundancy() {
        return helperThreadL2RequestRedundancy;
    }

    /**
     * Set the redundancy of the helper thread L2 cache requests.
     *
     * @param helperThreadL2RequestRedundancy
     *         the redundancy of the helper thread L2 cache requests
     */
    public void setHelperThreadL2RequestRedundancy(double helperThreadL2RequestRedundancy) {
        this.helperThreadL2RequestRedundancy = helperThreadL2RequestRedundancy;
    }

    /**
     * Get the earliness of the helper thread L2 cache requests.
     *
     * @return the earliness of the helper thread L2 cache requests
     */
    public double getHelperThreadL2RequestEarliness() {
        return helperThreadL2RequestEarliness;
    }

    /**
     * Set the earliness of the helper thread L2 cache requests.
     *
     * @param helperThreadL2RequestEarliness the earliness of the helper thread L2 requests
     */
    public void setHelperThreadL2RequestEarliness(double helperThreadL2RequestEarliness) {
        this.helperThreadL2RequestEarliness = helperThreadL2RequestEarliness;
    }

    /**
     * Get the lateness of the helper thread L2 cache requests.
     *
     * @return the lateness of the helper thread L2 cache requests
     */
    public double getHelperThreadL2RequestLateness() {
        return helperThreadL2RequestLateness;
    }

    /**
     * Set the lateness of the helper thread L2 cache requests.
     *
     * @param helperThreadL2RequestLateness
     *         the lateness of the helper thread L2 cache requests
     */
    public void setHelperThreadL2RequestLateness(double helperThreadL2RequestLateness) {
        this.helperThreadL2RequestLateness = helperThreadL2RequestLateness;
    }

    /**
     * Get the pollution of the helper thread L2 cache requests.
     *
     * @return the pollution of the helper thread L2 cache requests
     */
    public double getHelperThreadL2RequestPollution() {
        return helperThreadL2RequestPollution;
    }

    /**
     * Set the pollution of the helper thread L2 cache requests.
     *
     * @param helperThreadL2RequestPollution
     *         the pollution of the helper thread L2  cache requests
     */
    public void setHelperThreadL2RequestPollution(double helperThreadL2RequestPollution) {
        this.helperThreadL2RequestPollution = helperThreadL2RequestPollution;
    }

    /**
     * Get the number of the late helper thread L2 cache requests.
     *
     * @return the number of the late helper thread L2 cache requests
     */
    public long getNumLateHelperThreadL2Requests() {
        return numLateHelperThreadL2Requests;
    }

    /**
     * Set the number of the late helper thread L2 cache requests.
     *
     * @param numLateHelperThreadL2Requests
     *         the number of the late helper thread L2 cache requests
     */
    public void setNumLateHelperThreadL2Requests(long numLateHelperThreadL2Requests) {
        this.numLateHelperThreadL2Requests = numLateHelperThreadL2Requests;
    }

    /**
     * Get the number of the timely helper thread L2 cache requests.
     *
     * @return the number of the timely helper thread L2 cache requests
     */
    public long getNumTimelyHelperThreadL2Requests() {
        return numTimelyHelperThreadL2Requests;
    }

    /**
     * Set the number of the timely helper thread L2 cache requests.
     *
     * @param numTimelyHelperThreadL2Requests
     *         the number of the timely helper thread L2 cache requests
     */
    public void setNumTimelyHelperThreadL2Requests(long numTimelyHelperThreadL2Requests) {
        this.numTimelyHelperThreadL2Requests = numTimelyHelperThreadL2Requests;
    }

    /**
     * Get the number of the bad helper thread L2 cache requests.
     *
     * @return the number of the bad helper thread L2 cache requests
     */
    public long getNumBadHelperThreadL2Requests() {
        return numBadHelperThreadL2Requests;
    }

    /**
     * Set the number of the bad helper thread L2 cache requests.
     *
     * @param numBadHelperThreadL2Requests
     *         the number of the bad helper thread L2 cache requests
     */
    public void setNumBadHelperThreadL2Requests(long numBadHelperThreadL2Requests) {
        this.numBadHelperThreadL2Requests = numBadHelperThreadL2Requests;
    }

    /**
     * Get the number of the early helper thread L2 cache requests.
     *
     * @return the number of the early helper thread L2 cache requests
     */
    public long getNumEarlyHelperThreadL2Requests() {
        return numEarlyHelperThreadL2Requests;
    }

    /**
     * Set the number of the early helper thread L2 cache requests.
     *
     * @param numEarlyHelperThreadL2Requests
     *         the number of the early helper thread L2 cache requests
     */
    public void setNumEarlyHelperThreadL2Requests(long numEarlyHelperThreadL2Requests) {
        this.numEarlyHelperThreadL2Requests = numEarlyHelperThreadL2Requests;
    }

    /**
     * Get the number of the ugly helper thread L2 cache requests.
     *
     * @return the number of the ugly helper thread L2 cache requests
     */
    public long getNumUglyHelperThreadL2Requests() {
        return numUglyHelperThreadL2Requests;
    }

    /**
     * Set the number of the ugly helper thread L2 cache requests.
     *
     * @param numUglyHelperThreadL2Requests
     *         the number of the ugly helper thread L2 cache requests
     */
    public void setNumUglyHelperThreadL2Requests(long numUglyHelperThreadL2Requests) {
        this.numUglyHelperThreadL2Requests = numUglyHelperThreadL2Requests;
    }

    /**
     * Get the number of the redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @return the number of the redundant "hit to transient tag" helper thread L2 cache requests
     */
    public long getNumRedundantHitToTransientTagHelperThreadL2Requests() {
        return numRedundantHitToTransientTagHelperThreadL2Requests;
    }

    /**
     * Set the number of the redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @param numRedundantHitToTransientTagHelperThreadL2Requests
     *         the number of the redundant "hit to transient tag" helper thread L2 cache requests
     */
    public void setNumRedundantHitToTransientTagHelperThreadL2Requests(long numRedundantHitToTransientTagHelperThreadL2Requests) {
        this.numRedundantHitToTransientTagHelperThreadL2Requests = numRedundantHitToTransientTagHelperThreadL2Requests;
    }

    /**
     * Get the number of the redundant "hit to cache" helper thread L2 cache requests.
     *
     * @return the number of the redundant "hit to cache" helper thread L2 cache requests
     */
    public long getNumRedundantHitToCacheHelperThreadL2Requests() {
        return numRedundantHitToCacheHelperThreadL2Requests;
    }

    /**
     * Set the number of the redundant "hit to cache" helper thread L2 cache requests.
     *
     * @param numRedundantHitToCacheHelperThreadL2Requests
     *         the number of the redundant "hit to cache" helper thread L2 cache requests
     */
    public void setNumRedundantHitToCacheHelperThreadL2Requests(long numRedundantHitToCacheHelperThreadL2Requests) {
        this.numRedundantHitToCacheHelperThreadL2Requests = numRedundantHitToCacheHelperThreadL2Requests;
    }

    /**
     * Get the parent experiment object.
     *
     * @return the parent experiment object
     */
    public Experiment getParent() {
        return ServiceManager.getExperimentService().getExperimentById(parentId);
    }
}
