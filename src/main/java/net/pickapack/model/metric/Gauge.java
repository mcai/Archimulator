package net.pickapack.model.metric;

import com.j256.ormlite.field.DatabaseField;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

public abstract class Gauge implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String expression;

    @DatabaseField
    private String description;

    public Gauge() {
    }

    public Gauge(String title, String expression) {
        this.title = title;
        this.expression = expression;
        this.createTime = DateHelper.toTick(new Date());
    }

    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    public String getExpression() {
        return expression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
