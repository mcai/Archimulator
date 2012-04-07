package archimulator.sim.base.experiment.profile;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class ExperimentProfileStat implements Serializable {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true)
    private long experimentProfileId;

    @DatabaseField(index = true)
    private String key;

    @DatabaseField
    private String value;

    public ExperimentProfileStat() {
    }

    public ExperimentProfileStat(long experimentProfileId, String key, String value) {
        this.experimentProfileId = experimentProfileId;
        this.key = key;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public long getExperimentProfileId() {
        return experimentProfileId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
