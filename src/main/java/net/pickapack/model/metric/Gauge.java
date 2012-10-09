package net.pickapack.model.metric;

import com.j256.ormlite.field.DatabaseField;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

/**
 *
 * @author Min Cai
 */
public abstract class Gauge implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String valueExpression;

    @DatabaseField
    private String description;

    /**
     *
     */
    public Gauge() {
    }

    /**
     *
     * @param title
     * @param valueExpression
     */
    public Gauge(String title, String valueExpression) {
        this.title = title;
        this.valueExpression = valueExpression;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public long getParentId() {
        return -1;
    }

    /**
     *
     * @return
     */
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
     * @return
     */
    public String getValueExpression() {
        return valueExpression;
    }

    /**
     *
     * @param valueExpression
     */
    public void setValueExpression(String valueExpression) {
        this.valueExpression = valueExpression;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
