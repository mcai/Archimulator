package archimulator.model;

import archimulator.service.ServiceManager;
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

    public ExperimentPack() {
    }

    public ExperimentPack(String title) {
        this.title = title;
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

    public List<Experiment> getExperiments() {
        return ServiceManager.getExperimentService().getExperimentsByParent(this);
    }

    public void dump() {
        System.out.printf("[%s] experiment pack %s\n", DateHelper.toString(getCreateTime()), getTitle());
        System.out.println();

        for (Experiment experiment : getExperiments()) {
            experiment.dump();

            System.out.println();
        }
    }

    //TODO: to be extended and integrated!!!
    public List<Double> getSpeedups(Experiment baselineExperiment) {
        long baselineTotalCycles = Long.parseLong(baselineExperiment.getStats().get("detailed/cycleAccurateEventQueue/currentCycle"));

        List<Double> speedups = new ArrayList<Double>();

        for (Experiment experiment : getExperiments()) {
            long totalCycles = Long.parseLong(experiment.getStats().get("detailed/cycleAccurateEventQueue/currentCycle"));
            speedups.add((double) baselineTotalCycles / totalCycles);
        }

        return speedups;
    }

    @Override
    public String toString() {
        return title;
    }
}
