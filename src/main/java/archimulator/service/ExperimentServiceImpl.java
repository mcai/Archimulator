/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.service;

import archimulator.model.*;
import archimulator.util.JaxenHelper;
import com.j256.ormlite.dao.Dao;
import net.pickapack.Pair;
import net.pickapack.action.Function1;
import net.pickapack.action.Function2;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.util.*;

import static archimulator.util.CollectionHelper.toMap;
import static archimulator.util.CollectionHelper.transform;

public class ExperimentServiceImpl extends AbstractService implements ExperimentService {
    private Dao<Experiment, Long> experiments;
    private Dao<ExperimentPack, Long> experimentPacks;

    @SuppressWarnings("unchecked")
    public ExperimentServiceImpl() {
        super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(Experiment.class, ExperimentPack.class));

        this.experiments = createDao(Experiment.class);
        this.experimentPacks = createDao(ExperimentPack.class);
    }

    @Override
    public List<Experiment> getAllExperiments() {
        return this.getAllItems(this.experiments);
    }

    @Override
    public Experiment getExperimentById(long id) {
        return this.getItemById(this.experiments, id);
    }

    @Override
    public List<Experiment> getExperimentsByTitle(String title) {
        return this.getItemsByTitle(this.experiments, title);
    }

    @Override
    public Experiment getLatestExperimentByTitle(String title) {
        return this.getLatestItemByTitle(this.experiments, title);
    }

    @Override
    public List<Experiment> getExperimentsBySimulatedProgram(SimulatedProgram simulatedProgram) {
        List<Experiment> result = new ArrayList<Experiment>();

        for (Experiment experiment : getAllExperiments()) {
            for (ContextMapping contextMapping : experiment.getContextMappings()) {
                if (contextMapping.getSimulatedProgramId() == simulatedProgram.getId()) {
                    result.add(experiment);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public List<Experiment> getExperimentsByArchitecture(Architecture architecture) {
        List<Experiment> result = new ArrayList<Experiment>();

        for (Experiment experiment : getAllExperiments()) {
            if (experiment.getArchitectureId() == architecture.getId()) {
                result.add(experiment);
            }
        }

        return result;
    }

    @Override
    public List<Experiment> getExperimentsByParent(ExperimentPack parent) {
        return this.getItemsByParent(this.experiments, parent);
    }

    @Override
    public void addExperiment(Experiment experiment) {
        this.addItem(this.experiments, Experiment.class, experiment);
    }

    @Override
    public void removeExperimentById(long id) {
        this.removeItemById(this.experiments, Experiment.class, id);
    }

    @Override
    public void updateExperiment(Experiment experiment) {
        this.updateItem(this.experiments, Experiment.class, experiment);
    }

    @Override
    public void dumpExperiment(Experiment experiment) {
        System.out.printf("[%s] experiment %s\n", DateHelper.toString(experiment.getCreateTime()), experiment);

        Map<String, String> configs = new LinkedHashMap<String, String>();

        JaxenHelper.dumpValueFromXPath(configs, experiment, "title");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "createTimeAsString");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "type");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "state");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "numMaxInstructions");
        JaxenHelper.dumpValuesFromXPath(configs, experiment, "contextMappings");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/title");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/createTimeAsString");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/helperThreadPthreadSpawnIndex");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/helperThreadL2CacheRequestProfilingEnabled");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/numCores");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/numThreadsPerCore");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/physicalRegisterFileCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/decodeWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/issueWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/commitWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/decodeBufferCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/reorderBufferCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/loadStoreQueueCapacity");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/branchPredictorType");
        switch (experiment.getArchitecture().getBranchPredictorType()) {
            case PERFECT:
                break;
            case TAKEN:
                break;
            case NOT_TAKEN:
                break;
            case TWO_BIT:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorBimodSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorBranchTargetBufferNumSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorBranchTargetBufferAssociativity");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorReturnAddressStackSize");
                break;
            case TWO_LEVEL:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorL1Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorL2Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorShiftWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorXor");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorBranchTargetBufferNumSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorBranchTargetBufferAssociativity");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorReturnAddressStackSize");
                break;
            case COMBINED:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorBimodSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorL1Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorL2Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorMetaSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorShiftWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorXor");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorBranchTargetBufferNumSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorBranchTargetBufferAssociativity");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorReturnAddressStackSize");
                break;
        }

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbAssociativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbLineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbMissLatency");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1ISize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IAssociativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1ILineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1INumReadPorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1INumWritePorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DAssociativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DLineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DNumReadPorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DNumWritePorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2Size");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2Associativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2LineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2HitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2ReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/mainMemoryType");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/mainMemoryLineSize");
        switch (experiment.getArchitecture().getMainMemoryType()) {
            case FIXED_LATENCY:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/fixedLatencyMainMemoryLatency");
                break;
            case SIMPLE:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/simpleMainMemoryMemoryLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/simpleMainMemoryMemoryTrunkLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/simpleMainMemoryBusWidth");
                break;
            case BASIC:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryToDramLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryFromDramLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryPrechargeLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryClosedLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryConflictLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryBusWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryNumBanks");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMainMemoryRowSize");
                break;
        }

        System.out.println("  configs:");
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            System.out.printf("\t%s: %s\n", entry.getKey(), entry.getValue());
        }

        System.out.println();

        System.out.println("  stats:");
        for (Map.Entry<String, String> entry : experiment.getStats().entrySet()) {
            System.out.printf("\t%s: %s\n", entry.getKey(), entry.getValue());
        }

        System.out.println();
    }

    @Override
    public Experiment getFirstExperimentToRun() {
        for (Experiment experiment : getAllExperiments()) {
            if (experiment.getState() == ExperimentState.PENDING) {
                return experiment;
            }
        }

        return null;
    }

    @Override
    public List<ExperimentPack> getAllExperimentPacks() {
        return this.getAllItems(this.experimentPacks);
    }

    @Override
    public ExperimentPack getExperimentPackById(long id) {
        return this.getItemById(this.experimentPacks, id);
    }

    @Override
    public ExperimentPack getExperimentPackByTitle(String title) {
        return this.getFirstItemByTitle(this.experimentPacks, title);
    }

    @Override
    public void addExperimentPack(ExperimentPack experimentPack) {
        this.addItem(this.experimentPacks, ExperimentPack.class, experimentPack);
    }

    @Override
    public void removeExperimentPack(long id) {
        this.removeItemById(this.experimentPacks, ExperimentPack.class, id);
    }

    @Override
    public void updateExperimentPack(ExperimentPack experimentPack) {
        this.updateItem(this.experimentPacks, ExperimentPack.class, experimentPack);
    }

    @Override
    public void runExperiments() {
        new ExperimentWorker().run();
    }

    @Override
    public List<Double> getNormalizedStats(List<Experiment> experiments, Experiment baselineExperiment, Function1<Experiment, Double> function) {
        double baseline = function.apply(baselineExperiment);

        List<Double> result = new ArrayList<Double>();

        for (Experiment experiment : experiments) {
            double value = function.apply(experiment);
            result.add(value / baseline);
        }

        return result;
    }

    @Override
    public List<Map<String, Double>> getBreakdowns(List<Experiment> experiments, final Function1<Experiment, List<String>> keysFunction) {
        return transform(experiments, new Function1<Experiment, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Experiment experiment) {
                return getBreakdown(experiment, keysFunction);
            }
        });
    }

    @Override
    public Map<String, Double> getBreakdown(Experiment experiment, Function1<Experiment, List<String>> keysFunction) {
        final List<String> keys = keysFunction.apply(experiment);
        return toMap(transform(experiment.getStatValues(keys), new Function2<Integer, String, Pair<String, Double>>() {
            @Override
            public Pair<String, Double> apply(Integer index, String param) {
                return new Pair<String, Double>(keys.get(index), Double.parseDouble(param));
            }
        }));
    }

    @Override
    public List<Map<String, Double>> getNormalizedBreakdowns(List<Experiment> experiments, Experiment baselineExperiment, final Function1<Experiment, List<String>> keysFunction) {
        final Map<String, Double> baselineBreakdown = getBreakdown(baselineExperiment, keysFunction);
        List<Map<String, Double>> breakdowns = getBreakdowns(experiments, keysFunction);

        return transform(breakdowns, new Function1<Map<String, Double>, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Map<String, Double> breakdown) {
                return transform(breakdown, new Function2<String, Double, Double>() {
                    @Override
                    public Double apply(String key, Double value) {
                        return value / baselineBreakdown.get(key);
                    }
                });
            }
        });
    }

    @Override
    public List<Map<String, Double>> getBreakdownRatios(List<Experiment> experiments, Function1<Experiment, List<String>> keysFunction, final Function1<Experiment, String> totalKeyFunction) {
        final List<Double> totalValues = transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(totalKeyFunction.apply(experiment)));
            }
        });

        List<Map<String, Double>> breakdowns = getBreakdowns(experiments, keysFunction);
        return transform(breakdowns, new Function2<Integer, Map<String, Double>, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(final Integer index, Map<String, Double> breakdown) {
                final Double totalValue = totalValues.get(index);
                return transform(breakdown, new Function2<String, Double, Double>() {
                    @Override
                    public Double apply(String key, Double value) {
                        return value / totalValue;
                    }
                });
            }
        });
    }

    @Override
    public List<Double> getSpeedups(Experiment baselineExperiment, List<Experiment> experiments) {
        long baselineTotalCycles = Long.parseLong(baselineExperiment.getStatValue(baselineExperiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));

        List<Double> speedups = new ArrayList<Double>();

        for (Experiment experiment : experiments) {
            long totalCycles = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
            speedups.add((double) baselineTotalCycles / totalCycles);
        }

        return speedups;
    }

    @Override
    public List<Double> getNormalizedTotalCycles(Experiment baselineExperiment, List<Experiment> experiments) {
        return getNormalizedStats(experiments, baselineExperiment, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
            }
        });
    }

    @Override
    public List<Long> getNumL2DownwardReadMisses(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Long>() {
            @Override
            public Long apply(Experiment experiment) {
                return Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "processor/cacheHierarchy/cacheControllers[name=l2]/numDownwardReadMisses"));
            }
        });
    }

    @Override
    public List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                long numL2DownwardReadMisses = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "processor/cacheHierarchy/cacheControllers[name=l2]/numDownwardReadMisses"));
                long totalInstructions = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "totalInstructions"));
                return (double) numL2DownwardReadMisses / totalInstructions;
            }
        });
    }

    @Override
    public List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments) {
        return getBreakdowns(experiments, getHelperThreadL2CacheRequestBreakdownKeysFunction());
    }

    @Override
    public List<Map<String, Double>> getHelperThreadL2CacheRequestNormalizedBreakdowns(Experiment baselineExperiment, List<Experiment> experiments) {
        return getNormalizedBreakdowns(experiments, baselineExperiment, getHelperThreadL2CacheRequestBreakdownKeysFunction());
    }

    @Override
    public List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdownRatios(List<Experiment> experiments) {
        return getBreakdownRatios(experiments, getHelperThreadL2CacheRequestBreakdownKeysFunction(), new Function1<Experiment, String>() {
            @Override
            public String apply(Experiment experiment) {
                return experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numTotalHelperThreadL2CacheRequests";
            }
        });
    }

    private Function1<Experiment, List<String>> getHelperThreadL2CacheRequestBreakdownKeysFunction() {
        return new Function1<Experiment, List<String>>() {
            @Override
            public List<String> apply(Experiment experiment) {
                return Arrays.asList(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests",

                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numUsefulHelperThreadL2CacheRequests",

                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests",

                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests");
            }
        };
    }
}
