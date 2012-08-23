package archimulator.model;

import archimulator.service.ServiceManager;
import archimulator.util.CollectionHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.JsonSerializationHelper;
import net.pickapack.action.Function1;
import net.pickapack.action.Predicate;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;
import java.util.List;

import static archimulator.util.CollectionHelper.transform;

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

    public List<Experiment> getStoppedExperiments() {
        return CollectionHelper.filter(getExperiments(), new Predicate<Experiment>() {
            @Override
            public boolean apply(Experiment experiment) {
                return experiment.isStopped();
            }
        });
    }

    public Experiment getFirstStoppedExperiment() {
        return getStoppedExperiments().isEmpty() ? null : getStoppedExperiments().get(0);
    }

    //TODO: to be moved into ExperimentService(Impl)
    public void dump(boolean detailed) {
        System.out.printf("[%s] experiment pack %s\n", DateHelper.toString(getCreateTime()), getTitle());
        System.out.println();

        if(getFirstStoppedExperiment() != null) {
            System.out.println("  experiment titles: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, String>() {
                @Override
                public String apply(Experiment experiment) {
                    return experiment.getTitle();
                }
            }), true));
            System.out.println();

            System.out.println("  simulation times in seconds: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "durationInSeconds"));
                }
            }), true));
            System.out.println();

            System.out.println("  speedups: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getSpeedups(getFirstStoppedExperiment(), getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  total cycles: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNormalizedTotalCycles(getFirstStoppedExperiment(), getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  # l2 downward read misses: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNumL2DownwardReadMisses(getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  # l2 downward read MPKIs: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getL2DownwardReadMPKIs(getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  helper thread L2 cache Request breakdowns: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getHelperThreadL2CacheRequestBreakdowns(getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  helper thread L2 cache Request normalized breakdowns: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getHelperThreadL2CacheRequestNormalizedBreakdowns(getFirstStoppedExperiment(), getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  helper thread L2 cache Request breakdown ratios: ");
            System.out.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getHelperThreadL2CacheRequestBreakdownRatios(getStoppedExperiments()), true));
            System.out.println();

            System.out.println("  # total main thread L2 cache hits: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
                }
            }), true));
            System.out.println();

            System.out.println("  # total main thread L2 cache misses: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));
                }
            }), true));
            System.out.println();

            //TODO:  # total main thread L2 cache normalized hits and normalized misses

            //TODO:  # total helper thread L2 cache hits and misses
            //TODO:  # total helper thread L2 cache normalized hits and normalized misses

            //TODO: to be removed!!!
            System.out.println("  # total helper thread L2 cache requests: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numTotalHelperThreadL2CacheRequests"));
                }
            }), true));
            System.out.println();

            System.out.println("  helper thread L2 cache request coverage: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
                }
            }), true));
            System.out.println();

            System.out.println("  helper thread L2 cache request accuracy: ");
            System.out.println(JsonSerializationHelper.toJson(transform(getStoppedExperiments(), new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));
                }
            }), true));
            System.out.println();
        }

        if(detailed) {
            for (Experiment experiment : getExperiments()) {
                experiment.dump();
            }
        }
    }

    @Override
    public String toString() {
        return title;
    }
}
