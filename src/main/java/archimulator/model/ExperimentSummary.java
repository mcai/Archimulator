package archimulator.model;

import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.StorageUnit;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Experiment summary.
 */
@DatabaseTable(tableName = "ExperimentSummary")
public class ExperimentSummary implements ModelElement {
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
    private long totalInstructions;

    @DatabaseField
    private long totalCycles;

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

    public ExperimentSummary() {
    }

    public ExperimentSummary(Experiment parent) {
        this.parentId = parent.getId();
        this.title = parent.getTitle();
        this.createTime = DateHelper.toTick(new Date());
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

    public ExperimentType getType() {
        return type;
    }

    public void setType(ExperimentType type) {
        this.type = type;
    }

    public ExperimentState getState() {
        return state;
    }

    public void setState(ExperimentState state) {
        this.state = state;
    }

    public int getL2Size() {
        return l2Size;
    }

    public void setL2Size(int l2Size) {
        this.l2Size = l2Size;
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    public void setL2ReplacementPolicyType(CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    public void setHelperThreadLookahead(int helperThreadLookahead) {
        this.helperThreadLookahead = helperThreadLookahead;
    }

    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    public void setHelperThreadStride(int helperThreadStride) {
        this.helperThreadStride = helperThreadStride;
    }

    public long getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(long totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public long getTotalCycles() {
        return totalCycles;
    }

    public void setTotalCycles(long totalCycles) {
        this.totalCycles = totalCycles;
    }

    public double getIpc() {
        return ipc;
    }

    public void setIpc(double ipc) {
        this.ipc = ipc;
    }

    public double getCpi() {
        return cpi;
    }

    public void setCpi(double cpi) {
        this.cpi = cpi;
    }

    public long getNumMainThreadL2CacheHits() {
        return numMainThreadL2CacheHits;
    }

    public void setNumMainThreadL2CacheHits(long numMainThreadL2CacheHits) {
        this.numMainThreadL2CacheHits = numMainThreadL2CacheHits;
    }

    public long getNumMainThreadL2CacheMisses() {
        return numMainThreadL2CacheMisses;
    }

    public void setNumMainThreadL2CacheMisses(long numMainThreadL2CacheMisses) {
        this.numMainThreadL2CacheMisses = numMainThreadL2CacheMisses;
    }

    public long getNumHelperThreadL2CacheHits() {
        return numHelperThreadL2CacheHits;
    }

    public void setNumHelperThreadL2CacheHits(long numHelperThreadL2CacheHits) {
        this.numHelperThreadL2CacheHits = numHelperThreadL2CacheHits;
    }

    public long getNumHelperThreadL2CacheMisses() {
        return numHelperThreadL2CacheMisses;
    }

    public void setNumHelperThreadL2CacheMisses(long numHelperThreadL2CacheMisses) {
        this.numHelperThreadL2CacheMisses = numHelperThreadL2CacheMisses;
    }

    public long getNumL2CacheEvictions() {
        return numL2CacheEvictions;
    }

    public void setNumL2CacheEvictions(long numL2CacheEvictions) {
        this.numL2CacheEvictions = numL2CacheEvictions;
    }

    public double getL2CacheHitRatio() {
        return l2CacheHitRatio;
    }

    public void setL2CacheHitRatio(double l2CacheHitRatio) {
        this.l2CacheHitRatio = l2CacheHitRatio;
    }

    public double getL2CacheOccupancyRatio() {
        return l2CacheOccupancyRatio;
    }

    public void setL2CacheOccupancyRatio(double l2CacheOccupancyRatio) {
        this.l2CacheOccupancyRatio = l2CacheOccupancyRatio;
    }

    public double getHelperThreadL2CacheRequestCoverage() {
        return helperThreadL2CacheRequestCoverage;
    }

    public void setHelperThreadL2CacheRequestCoverage(double helperThreadL2CacheRequestCoverage) {
        this.helperThreadL2CacheRequestCoverage = helperThreadL2CacheRequestCoverage;
    }

    public double getHelperThreadL2CacheRequestAccuracy() {
        return helperThreadL2CacheRequestAccuracy;
    }

    public void setHelperThreadL2CacheRequestAccuracy(double helperThreadL2CacheRequestAccuracy) {
        this.helperThreadL2CacheRequestAccuracy = helperThreadL2CacheRequestAccuracy;
    }

    public long getNumLateHelperThreadL2CacheRequests() {
        return numLateHelperThreadL2CacheRequests;
    }

    public void setNumLateHelperThreadL2CacheRequests(long numLateHelperThreadL2CacheRequests) {
        this.numLateHelperThreadL2CacheRequests = numLateHelperThreadL2CacheRequests;
    }

    public long getNumTimelyHelperThreadL2CacheRequests() {
        return numTimelyHelperThreadL2CacheRequests;
    }

    public void setNumTimelyHelperThreadL2CacheRequests(long numTimelyHelperThreadL2CacheRequests) {
        this.numTimelyHelperThreadL2CacheRequests = numTimelyHelperThreadL2CacheRequests;
    }

    public long getNumBadHelperThreadL2CacheRequests() {
        return numBadHelperThreadL2CacheRequests;
    }

    public void setNumBadHelperThreadL2CacheRequests(long numBadHelperThreadL2CacheRequests) {
        this.numBadHelperThreadL2CacheRequests = numBadHelperThreadL2CacheRequests;
    }

    public long getNumUglyHelperThreadL2CacheRequests() {
        return numUglyHelperThreadL2CacheRequests;
    }

    public void setNumUglyHelperThreadL2CacheRequests(long numUglyHelperThreadL2CacheRequests) {
        this.numUglyHelperThreadL2CacheRequests = numUglyHelperThreadL2CacheRequests;
    }

    public long getNumRedundantHitToTransientTagHelperThreadL2CacheRequests() {
        return numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    }

    public void setNumRedundantHitToTransientTagHelperThreadL2CacheRequests(long numRedundantHitToTransientTagHelperThreadL2CacheRequests) {
        this.numRedundantHitToTransientTagHelperThreadL2CacheRequests = numRedundantHitToTransientTagHelperThreadL2CacheRequests;
    }

    public long getNumRedundantHitToCacheHelperThreadL2CacheRequests() {
        return numRedundantHitToCacheHelperThreadL2CacheRequests;
    }

    public void setNumRedundantHitToCacheHelperThreadL2CacheRequests(long numRedundantHitToCacheHelperThreadL2CacheRequests) {
        this.numRedundantHitToCacheHelperThreadL2CacheRequests = numRedundantHitToCacheHelperThreadL2CacheRequests;
    }

    public Experiment getParent() {
        return ServiceManager.getExperimentService().getExperimentById(parentId);
    }

    public List<String> tableSummary2Row() {
        boolean helperThreadEnabled = helperThreadLookahead != -1;

        List<String> row = new ArrayList<String>();

        row.add(parentId + "");

        row.add(type + "");
        row.add(state + "");

        row.add(StorageUnit.KILOBYTE.getValue(l2Size) + "KB");
        row.add(l2Associativity + "way");
        row.add(l2ReplacementPolicyType + "");

        row.add(helperThreadEnabled ? "L=" + helperThreadLookahead + "" : "");
        row.add(helperThreadEnabled ? "S=" + helperThreadStride + "" : "");

        row.add(totalInstructions + "");
        row.add(totalCycles + "");

        row.add(ipc + "");
        row.add(cpi + "");

        row.add(numMainThreadL2CacheHits + "");
        row.add(numMainThreadL2CacheMisses + "");

        row.add(numHelperThreadL2CacheHits + "");
        row.add(numHelperThreadL2CacheMisses + "");

        row.add(numL2CacheEvictions + "");
        row.add(l2CacheHitRatio + "");
        row.add(l2CacheOccupancyRatio + "");

        row.add(helperThreadL2CacheRequestCoverage + "");
        row.add(helperThreadL2CacheRequestAccuracy + "");

        row.add(numLateHelperThreadL2CacheRequests + "");
        row.add(numTimelyHelperThreadL2CacheRequests + "");
        row.add(numBadHelperThreadL2CacheRequests + "");
        row.add(numUglyHelperThreadL2CacheRequests + "");
        row.add(numRedundantHitToTransientTagHelperThreadL2CacheRequests + "");
        row.add(numRedundantHitToCacheHelperThreadL2CacheRequests + "");

        return row;
    }
}
