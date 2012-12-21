package archimulator.model.metric.meter;

import archimulator.model.Experiment;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithParentId;

import java.util.Date;

/**
 * Experiment meter mark.
 */
@DatabaseTable(tableName = "ExperimentMeterMark")
public class ExperimentMeterMark implements WithId, WithParentId, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private long parentId;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long experimentId;

    @DatabaseField
    private int pc;

    @DatabaseField
    private int address;

    @DatabaseField
    private int threadId;

    @DatabaseField
    private String functionName;

    @DatabaseField
    private long currentCycle;

    @DatabaseField
    private double value;

    /**
     * Create an experiment meter mark. Reserved for ORM only.
     */
    public ExperimentMeterMark() {
    }

    /**
     * Create an experiment meter mark.
     *
     * @param parent the parent experiment meter
     * @param experiment the experiment
     * @param pc the value of the program counter (PC)
     * @param address the data access address
     * @param threadId the thread ID
     * @param functionName the function symbol name
     * @param currentCycle the current time in cycles
     * @param value the value
     */
    public ExperimentMeterMark(ExperimentMeter parent, Experiment experiment, int pc, int address, int threadId, String functionName, long currentCycle, double value) {
        this.parentId = parent == null ? -1 : parent.getId();
        this.experimentId = experiment == null ? -1 : experiment.getId();
        this.pc = pc;
        this.address = address;
        this.threadId = threadId;
        this.functionName = functionName;
        this.currentCycle = currentCycle;
        this.value = value;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the experiment meter mark's ID.
     *
     * @return the experiment meter mark's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the parent experiment meter's ID.
     *
     * @return the parent experiment meter's ID
     */
    @Override
    public long getParentId() {
        return parentId;
    }

    /**
     * Get the time in ticks when the experiment meter mark is created.
     *
     * @return the time in ticks when the experiment meter mark is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the experiment ID.
     *
     * @return the experiment ID
     */
    public long getExperimentId() {
        return experimentId;
    }

    /**
     * Get the value of the program counter (PC).
     *
     * @return the value of the program counter (PC)
     */
    public int getPc() {
        return pc;
    }

    /**
     * Get the data access address.
     *
     * @return the data access address
     */
    public int getAddress() {
        return address;
    }

    /**
     * Get the thread ID.
     *
     * @return the thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Get the function symbol name.
     *
     * @return the function symbol name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Get the current time in cycles.
     *
     * @return the current time in cycles
     */
    public long getCurrentCycle() {
        return currentCycle;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public double getValue() {
        return value;
    }
}
