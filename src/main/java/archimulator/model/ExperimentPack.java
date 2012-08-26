package archimulator.model;

import archimulator.service.ServiceManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "ExperimentPack")
public class ExperimentPack implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String variablePropertyName;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<String> variablePropertyValues;

    public ExperimentPack() {
    }

    public ExperimentPack(String title, String variablePropertyName, List<String> variablePropertyValues) {
        this.title = title;
        this.variablePropertyName = variablePropertyName;
        this.variablePropertyValues = variablePropertyName == null ? null : new ArrayList<String>(variablePropertyValues);
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

    public long getCreateTime() {
        return createTime;
    }

    public String getVariablePropertyName() {
        return variablePropertyName;
    }

    public List<String> getVariablePropertyValues() {
        return variablePropertyValues;
    }

    public List<Experiment> getExperiments() {
        return ServiceManager.getExperimentService().getExperimentsByParent(this);
    }

    @Override
    public String toString() {
        return title;
    }
}
