package archimulator.model.metric.meter;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithTitle;

import java.util.Date;

//TODO: add service support for managing, searching and using experiment meters
/**
 * Experiment meter.
 */
@DatabaseTable(tableName = "ExperimentMeter")
public class ExperimentMeter implements WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    /**
     * Create an experiment meter. Reserved for ORM only.
     */
    public ExperimentMeter() {
    }

    /**
     * Create an experiment meter.
     *
     * @param title the title of the experiment meter
     */
    public ExperimentMeter(String title) {
        this.title = title;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the experiment meter's ID.
     *
     * @return the experiment meter's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the title of the experiment meter.
     *
     * @return the title of the experiment meter.
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Get the time in ticks when the experiment meter is created.
     *
     * @return the time in ticks when the experiment meter is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }
}
