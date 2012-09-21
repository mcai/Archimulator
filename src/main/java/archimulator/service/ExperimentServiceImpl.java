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

import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentSpec;
import archimulator.model.Architecture;
import archimulator.model.Description;
import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentType;
import archimulator.model.SimulatedProgram;
import com.Ostermiller.util.CSVPrinter;
import com.j256.ormlite.dao.Dao;
import net.pickapack.JsonSerializationHelper;
import net.pickapack.Pair;
import net.pickapack.StorageUnit;
import net.pickapack.action.Function1;
import net.pickapack.action.Function2;
import net.pickapack.action.Predicate;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.io.cmd.CommandLineHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;
import net.pickapack.util.CollectionHelper;
import net.pickapack.util.IndentedPrintWriter;
import net.pickapack.util.JaxenHelper;
import net.pickapack.util.TableHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.jsoup.helper.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static net.pickapack.util.CollectionHelper.toMap;
import static net.pickapack.util.CollectionHelper.transform;

public class ExperimentServiceImpl extends AbstractService implements ExperimentService {
    private Dao<Experiment, Long> experiments;
    private Dao<ExperimentPack, Long> experimentPacks;

    @SuppressWarnings("unchecked")
    public ExperimentServiceImpl() {
        super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(Experiment.class, ExperimentPack.class));

        this.experiments = createDao(Experiment.class);
        this.experimentPacks = createDao(ExperimentPack.class);

        for(Experiment experiment : getAllExperiments()) {
            if(experiment.getState() == ExperimentState.READY_TO_RUN || experiment.getState() == ExperimentState.RUNNING) {
                experiment.reset();
                updateExperiment(experiment);
            }
        }

        //TODO: to be exposed as import/upload experiment pack via web UI
        try {
            for (File file : FileUtils.listFiles(new File("experiment_inputs"), null, true)) {
                ExperimentPack experimentPack = JsonSerializationHelper.deserialize(ExperimentPack.class, FileUtils.readFileToString(file));
                if (experimentPack != null && getExperimentPackByTitle(experimentPack.getTitle()) == null) {
                    addExperimentPack(experimentPack);

                    for (ExperimentSpec experimentSpec : experimentPack.getExperimentSpecs()) {
                        Experiment experiment = getFirstExperimentByTitle(experimentSpec.getTitle());
                        if (experiment == null) {
                            ExperimentType experimentType = experimentPack.getExperimentType();
                            SimulatedProgram simulatedProgram = experimentSpec.getSimulatedProgram();
                            Architecture architecture = experimentSpec.getArchitecture();
                            String arguments = experimentSpec.getArguments();

                            List<ContextMapping> contextMappings = new ArrayList<ContextMapping>();

                            ContextMapping contextMapping = new ContextMapping(0, simulatedProgram, arguments);
                            contextMapping.setHelperThreadLookahead(experimentSpec.getHelperThreadLookahead());
                            contextMapping.setHelperThreadStride(experimentSpec.getHelperThreadStride());
                            contextMapping.setDynamicHelperThreadParams(false);
                            contextMappings.add(contextMapping);

                            experiment = new Experiment(experimentSpec.getTitle(), experimentType, architecture, -1, contextMappings);
                            addExperiment(experiment);
                        }

                        experimentPack.getExperimentTitles().add(experiment.getTitle());
                    }

                    updateExperimentPack(experimentPack);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        new Thread(new ExperimentWorker()){{
            setDaemon(true);
        }}.start();
    }

    @Override
    public List<Experiment> getAllExperiments() {
        return this.getAllItems(this.experiments);
    }

    @Override
    public List<Experiment> getAllExperiments(long first, long count) {
        return this.getAllItems(this.experiments, first, count);
    }

    @Override
    public long getNumAllExperiments() {
        return this.getNumAllItems(this.experiments);
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
    public List<Experiment> getExperimentsByTitlePrefix(String titlePrefix, boolean stoppedExperimentsOnly) {
        List<Experiment> result = new ArrayList<Experiment>();

        for(Experiment experiment : this.getAllExperiments()) {
            if(experiment.getTitle().startsWith(titlePrefix) && getLatestExperimentByTitle(experiment.getTitle()).getId() == experiment.getId() && (!stoppedExperimentsOnly || experiment.isStopped())) {
                result.add(experiment);
            }
        }

        return result;
    }

    @Override
    public Experiment getFirstExperimentByTitle(String title) {
        return this.getFirstItemByTitle(this.experiments, title);
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
    public List<Experiment> getExperimentsByExperimentPack(ExperimentPack experimentPack) {
        List<Experiment> result = new ArrayList<Experiment>();

        for (String experimentTitle : experimentPack.getExperimentTitles()) {
            Experiment experiment = getLatestExperimentByTitle(experimentTitle);
            if(experiment != null) {
                result.add(experiment);
            }
        }

        return result;
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
        dumpExperiment(experiment, new IndentedPrintWriter(new PrintWriter(System.out), true));
    }

    @Override
    public void dumpExperiment(Experiment experiment, IndentedPrintWriter writer) {
        writer.printf("[%s] experiment %s\n", DateHelper.toString(experiment.getCreateTime()), experiment);

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

        writer.incrementIndentation();
        writer.println("  configs:");
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            writer.incrementIndentation();
            writer.printf("%s: %s\n", entry.getKey(), entry.getValue());
            writer.decrementIndentation();
        }

        writer.println();
        writer.decrementIndentation();

        writer.incrementIndentation();
        writer.println("  stats:");
        for (Map.Entry<String, String> entry : experiment.getStats().entrySet()) {
            writer.incrementIndentation();
            writer.printf("%s: %s\n", entry.getKey(), entry.getValue());
            writer.decrementIndentation();
        }

        writer.println();
        writer.decrementIndentation();
    }

    @Override
    public Experiment getFirstExperimentToRun() {
        for (Experiment experiment : getAllExperiments()) {
            if (experiment.getState() == ExperimentState.READY_TO_RUN) {
                return experiment;
            }
        }

        return null;
    }

    @Override
    public List<Experiment> getStoppedExperimentsByExperimentPack(ExperimentPack experimentPack) {
        return CollectionHelper.filter(ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack), new Predicate<Experiment>() {
            @Override
            public boolean apply(Experiment experiment) {
                return experiment.isStopped();
            }
        });
    }

    @Override
    public Experiment getFirstStoppedExperimentByExperimentPack(ExperimentPack experimentPack) {
        List<Experiment> stoppedExperimentsByExperimentPack = getStoppedExperimentsByExperimentPack(experimentPack);
        return stoppedExperimentsByExperimentPack.isEmpty() ? null : stoppedExperimentsByExperimentPack.get(0);
    }

    @Override
    public List<ExperimentPack> getAllExperimentPacks() {
        return this.getAllItems(this.experimentPacks);
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
    public void removeExperimentPackById(long id) {
        this.removeItemById(this.experimentPacks, ExperimentPack.class, id);
    }

    @Override
    public void updateExperimentPack(ExperimentPack experimentPack) {
        this.updateItem(this.experimentPacks, ExperimentPack.class, experimentPack);
    }

    @Override
    public void runExperimentPackByTitle(String experimentPackTitle) {
        ExperimentPack experimentPack = getExperimentPackByTitle(experimentPackTitle);
        if(experimentPack == null) {
            return;
        }

        for(Experiment experiment : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack)) {
            if(experiment.getState() == ExperimentState.PENDING) {
                experiment.setState(ExperimentState.READY_TO_RUN);
                updateExperiment(experiment);
            }
        }
    }

    @Override
    public void runExperimentByTitle(String experimentTitle) {
        Experiment experiment = getLatestExperimentByTitle(experimentTitle);
        if(experiment == null) {
            return;
        }

        if(experiment.getState() == ExperimentState.PENDING) {
            experiment.setState(ExperimentState.READY_TO_RUN);
            updateExperiment(experiment);
        }
    }

    @Override
    public void tableSummary(String title, Experiment baselineExperiment, List<Experiment> experiments) {
        boolean helperThreadEnabled = baselineExperiment.getContextMappings().get(0).getSimulatedProgram().getHelperThreadEnabled();

        List<String> columns = helperThreadEnabled ? Arrays.asList(
                "L2 Size", "L2 Assoc", "L2 Repl",
                "Lookahead", "Stride",
                "Total Cycles", "Speedup", "IPC", "CPI",
                "Main Thread Hit", "Main Thread Miss", "L2 Hit Ratio", "L2 Evictions", "L2 Occupancy Ratio", "Helper Thread Hit", "Helper Thread Miss", "Helper Thread Coverage", "Helper Thread Accuracy", "Redundant MSHR", "Redundant Cache", "Timely", "Late", "Bad", "Ugly"
        ) : Arrays.asList(
                "L2 Size", "L2 Assoc", "L2 Repl",
                "Total Cycles", "Speedup", "IPC", "CPI",
                "Main Thread Hit", "Main Thread Miss", "L2 Hit Ratio", "L2 Evictions", "L2 Occupancy Ratio"
        );

        List<List<String>> rows = new ArrayList<List<String>>();

        for (Experiment experiment : experiments) {
            List<String> row = new ArrayList<String>();

            row.add(StorageUnit.toString(experiment.getArchitecture().getL2Size()));
//            row.add(experiment.getArchitecture().getL2Size() + "");

            row.add(experiment.getArchitecture().getL2Associativity() + "");
            row.add(experiment.getArchitecture().getL2ReplacementPolicyType() + "");

            if(helperThreadEnabled) {
                row.add(experiment.getContextMappings().get(0).getHelperThreadLookahead() + "");
                row.add(experiment.getContextMappings().get(0).getHelperThreadStride() + "");
            }

            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));

            row.add(String.format("%.4f", getSpeedup(baselineExperiment, experiment)));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "instructionsPerCycle"))));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "cyclesPerInstruction"))));

            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "processor/cacheHierarchy/cacheControllers[name=l2]/hitRatio"))));
            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "processor/cacheHierarchy/cacheControllers[name=l2]/numEvictions"));
            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "processor/cacheHierarchy/cacheControllers[name=l2]/occupancyRatio"))));

            if(helperThreadEnabled) {
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));

                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));

                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests"));
            }

            rows.add(row);
        }

        table(title, "summary", columns, rows);
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
    public double getSpeedup(Experiment baselineExperiment, Experiment experiment) {
        long baselineTotalCycles = Long.parseLong(baselineExperiment.getStatValue(baselineExperiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
        long totalCycles = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
        return (double) baselineTotalCycles / totalCycles;
    }

    @Override
    public void plotSpeedups(ExperimentPack experimentPack, Experiment baselineExperiment, List<Experiment> experiments) {
        plot(experimentPack, "speedups", "Speedups", getSpeedups(baselineExperiment, experiments));
    }

    @Override
    public List<Double> getTotalInstructions(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "totalInstructions"));
            }
        });
    }

    @Override
    public void plotTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalInstructions", "Total Instructions", getTotalCycles(experiments));
    }

    @Override
    public List<Double> getNormalizedTotalInstructions(List<Experiment> experiments) {
        return normalize(getTotalInstructions(experiments));
    }

    @Override
    public void plotNormalizedTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalInstructions_normalized", "Normalized Total Instructions", getNormalizedTotalCycles(experiments));
    }

    @Override
    public List<Double> getTotalCycles(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "cycleAccurateEventQueue/currentCycle"));
            }
        });
    }

    @Override
    public void plotTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalCycles", "Total Cycles", getTotalCycles(experiments));
    }

    @Override
    public List<Double> getNormalizedTotalCycles(List<Experiment> experiments) {
        return normalize(getTotalCycles(experiments));
    }

    @Override
    public void plotNormalizedTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalCycles_normalized", "Normalized Total Cycles", getNormalizedTotalCycles(experiments));
    }

    @Override
    public List<Double> getNumL2DownwardReadMisses(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "processor/cacheHierarchy/cacheControllers[name=l2]/numDownwardReadMisses"));
            }
        });
    }

    @Override
    public void plotNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numL2DownwardReadMisses", "# L2 Downward Read Misses", getNumL2DownwardReadMisses(experiments));
    }

    @Override
    public List<Double> getNormalizedNumL2DownwardReadMisses(List<Experiment> experiments) {
        return normalize(getNumL2DownwardReadMisses(experiments));
    }

    @Override
    public void plotNormalizedNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numL2DownwardReadMisses_normalized", "# Normalized L2 Downward Read Misses", getNormalizedNumL2DownwardReadMisses(experiments));
    }

    @Override
    public List<Double> getNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            }
        });
    }

    @Override
    public List<Double> getNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));
            }
        });
    }

    @Override
    public List<Double> getNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
            }
        });
    }

    @Override
    public List<Double> getNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));
            }
        });
    }

    @Override
    public List<Double> getNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    @Override
    public List<Double> getNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    @Override
    public List<Double> getNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    @Override
    public List<Double> getNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    @Override
    public void plotNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheHits", "# Main Thread L2 Cache Hits", getNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    @Override
    public void plotNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheMisses", "# Main Thread L2 Cache Misses", getNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    @Override
    public void plotNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheHits", "# Helper Thread L2 Cache Hits", getNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    @Override
    public void plotNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheMisses", "# Helper Thread L2 Cache Misses", getNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    @Override
    public void plotNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheHits_normalized", "# Normalized Main Thread L2 Cache Hits", getNormalizedNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    @Override
    public void plotNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheMisses_normalized", "# Normalized Main Thread L2 Cache Misses", getNormalizedNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    @Override
    public void plotNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheHits_normalized", "# Normalized Helper Thread L2 Cache Hits", getNormalizedNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    @Override
    public void plotNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheMisses_normalized", "# Normalized Helper Thread L2 Cache Misses", getNormalizedNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    @Override
    public List<Double> getHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
            }
        });
    }

    @Override
    public List<Double> getHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));
            }
        });
    }

    @Override
    public List<Double> getNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    @Override
    public List<Double> getNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
    }

    @Override
    public void plotHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestCoverage", "Helper Thread L2 Cache Request Coverage", getHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    @Override
    public void plotHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestAccuracy", "HelperT hread L2 Cache Request Accuracy", getHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
    }

    @Override
    public void plotNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestCoverage_normalized", "Normalized Helper Thread L2 Cache Request Coverage", getNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    @Override
    public void plotNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestAccuracy_normalized", "Normalized Helper Thread L2 Cache Request Accuracy", getNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
    }

    @Override
    public List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                long numL2DownwardReadMisses = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "processor/cacheHierarchy/cacheControllers[name=l2]/numDownwardReadMisses"));
                long totalInstructions = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "totalInstructions"));
                return (double) numL2DownwardReadMisses / (totalInstructions / FileUtils.ONE_KB);
            }
        });
    }

    @Override
    public void plotL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "l2DownwardReadMPKIs", "# L2 Downward Read MPKIs", getL2DownwardReadMPKIs(experiments));
    }

    @Override
    public List<Double> getNormalizedL2DownwardReadMPKIs(List<Experiment> experiments) {
        return normalize(getL2DownwardReadMPKIs(experiments));
    }

    @Override
    public void plotNormalizedL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "l2DownwardReadMPKIs_normalized", "# Normalized L2 Downward Read MPKIs", getNormalizedL2DownwardReadMPKIs(experiments));
    }

    @Override
    public List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments) {
        return getBreakdowns(experiments, new Function1<Experiment, List<String>>() {
            @Override
            public List<String> apply(Experiment experiment) {
                return Arrays.asList(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests");
            }
        });
    }

    @Override
    public void plotHelperThreadL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments) {
        List<Map<String, Double>> breakdowns = getHelperThreadL2CacheRequestBreakdowns(experiments);
        List<Map<String, Double>> transformedBreakdowns = transform(breakdowns, new Function1<Map<String, Double>, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Map<String, Double> input) {
                Map<String, Double> output = new LinkedHashMap<String, Double>();

                int i = 0;
                for (String key : input.keySet()) {
                    switch (i++) {
                        case 0:
                            output.put("Redundant MSHR", input.get(key));
                            break;
                        case 1:
                            output.put("Redundant Cache", input.get(key));
                            break;
                        case 2:
                            output.put("Timely", input.get(key));
                            break;
                        case 3:
                            output.put("Late", input.get(key));
                            break;
                        case 4:
                            output.put("Bad", input.get(key));
                            break;
                        case 5:
                            output.put("Ugly", input.get(key));
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }

                return output;
            }
        });
        plot(experimentPack, "helperThreadL2CacheRequestBreakdowns", "# Helper Thread L2 Request Breakdowns", transformedBreakdowns);
    }

    @Override
    public List<Map<String, Double>> getL2CacheRequestBreakdowns(List<Experiment> experiments) {
        return getBreakdowns(experiments, new Function1<Experiment, List<String>>() {
            @Override
            public List<String> apply(Experiment experiment) {
                return Arrays.asList(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses");
            }
        });
    }

    @Override
    public void plotL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments) {
        List<Map<String, Double>> breakdowns = getL2CacheRequestBreakdowns(experiments);
        List<Map<String, Double>> transformedBreakdowns = transform(breakdowns, new Function1<Map<String, Double>, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Map<String, Double> input) {
                Map<String, Double> output = new LinkedHashMap<String, Double>();

                int i = 0;
                for (String key : input.keySet()) {
                    switch (i++) {
                        case 0:
                            output.put("Main Thread L2 Hits", input.get(key));
                            break;
                        case 1:
                            output.put("Main Thread L2 Misses", input.get(key));
                            break;
                        case 2:
                            output.put("Helper Thread L2 Hits", input.get(key));
                            break;
                        case 3:
                            output.put("Helper Thread L2 Misses", input.get(key));
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }

                return output;
            }
        });
        plot(experimentPack, "l2CacheRequestBreakdowns", "# L2 Request Breakdowns", transformedBreakdowns);
    }

    @Override
    public void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, boolean stoppedExperimentsOnly) {
        dumpExperimentPack(experimentPack, detailed, new IndentedPrintWriter(new PrintWriter(System.out), true), stoppedExperimentsOnly);
    }

    @Override
    public void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, IndentedPrintWriter writer, boolean stoppedExperimentsOnly) {
        writer.printf("experiment pack %s\n", experimentPack.getTitle());
        writer.println();

        writer.incrementIndentation();

        List<Experiment> experimentsByExperimentPack = stoppedExperimentsOnly ? getStoppedExperimentsByExperimentPack(experimentPack) : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack);
        Experiment firstExperimentByExperimentPack = stoppedExperimentsOnly ? getFirstStoppedExperimentByExperimentPack(experimentPack) : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack).get(0);

        if (firstExperimentByExperimentPack != null) {
            writer.println("experiment titles: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(transform(experimentsByExperimentPack, new Function1<Experiment, String>() {
                @Override
                public String apply(Experiment experiment) {
                    return experiment.getTitle();
                }
            }), true));
            writer.println();
            writer.decrementIndentation();

            ServiceManager.getExperimentService().tableSummary(experimentPack.getTitle(), experimentsByExperimentPack.get(0), experimentsByExperimentPack);

            writer.println("simulation times in seconds: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(transform(experimentsByExperimentPack, new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "durationInSeconds"));
                }
            }), true));
            writer.println();
            writer.decrementIndentation();

            writer.println("speedups: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getSpeedups(firstExperimentByExperimentPack, experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            ServiceManager.getExperimentService().plotSpeedups(experimentPack, firstExperimentByExperimentPack, experimentsByExperimentPack);

            writer.println("total instructions: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getTotalInstructions(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            ServiceManager.getExperimentService().plotTotalInstructions(experimentPack, experimentsByExperimentPack);

            writer.println("normalized total instructions: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNormalizedTotalInstructions(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            ServiceManager.getExperimentService().plotNormalizedTotalInstructions(experimentPack, experimentsByExperimentPack);

            writer.println("total cycles: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getTotalCycles(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            ServiceManager.getExperimentService().plotTotalCycles(experimentPack, experimentsByExperimentPack);

            writer.println("normalized total cycles: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNormalizedTotalCycles(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            ServiceManager.getExperimentService().plotNormalizedTotalCycles(experimentPack, experimentsByExperimentPack);

            if (firstExperimentByExperimentPack.getType() != ExperimentType.FUNCTIONAL) {
                writer.println("l2 request breakdowns: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getL2CacheRequestBreakdowns(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotL2CacheRequestBreakdowns(experimentPack, experimentsByExperimentPack);

                writer.println("# l2 downward read MPKIs: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getL2DownwardReadMPKIs(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotL2DownwardReadMPKIs(experimentPack, experimentsByExperimentPack);

                writer.println("# normalized l2 downward read MPKIs: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNormalizedL2DownwardReadMPKIs(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotNormalizedL2DownwardReadMPKIs(experimentPack, experimentsByExperimentPack);

                writer.println("helper thread L2 request breakdowns: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getHelperThreadL2CacheRequestBreakdowns(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotHelperThreadL2CacheRequestBreakdowns(experimentPack, experimentsByExperimentPack);

                writer.println("helper thread L2 request coverage: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack), true));
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack);

                writer.println("helper thread L2 request accuracy: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack);

                writer.println("normalized helper thread L2 request coverage: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack), true));
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack);

                writer.println("normalized helper thread L2 request accuracy: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(ServiceManager.getExperimentService().getNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                ServiceManager.getExperimentService().plotNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack);

                //TODO: dump and plot 3C misses/cache request/miss latencies and mlp based costs (and breakdowns)!!!
            }
        }

        writer.decrementIndentation();

        if (detailed) {
            writer.incrementIndentation();
            for (Experiment experiment : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack)) {
                ServiceManager.getExperimentService().dumpExperiment(experiment, writer);
            }
            writer.decrementIndentation();
        }
    }

    private void table(String title, String tableFileNameSuffix, List<String> columns, List<List<String>> rows) {
        String fileNamePrefix = "experiment_tables" + File.separator + title + "_" + tableFileNameSuffix;
        new File(fileNamePrefix).getParentFile().mkdirs();

        TableHelper.generateTable(fileNamePrefix + ".pdf", columns, rows);

        try {
            CSVPrinter csvPrinter = new CSVPrinter(new FileOutputStream(fileNamePrefix + ".csv"));

            csvPrinter.writeln(columns.toArray(new String[columns.size()]));

            for(List<String> row : rows) {
                csvPrinter.writeln(row.toArray(new String[row.size()]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void plot(final ExperimentPack experimentPack, String plotFileNameSuffix, String yLabel, List<?> rows) {
        try {
            String fileNamePdf = "experiment_plots" + File.separator + experimentPack.getTitle() + "_" + plotFileNameSuffix + ".pdf";
            new File(fileNamePdf).getParentFile().mkdirs();

            File fileInput = File.createTempFile("archimulator", "bargraph_input.txt");

            PrintWriter pw = new PrintWriter(fileInput);

            String variablePropertyDescription = CollectionUtils.isEmpty(experimentPack.getVariablePropertyNames()) ? "" : StringUtil.join(transform(experimentPack.getVariablePropertyNames(), new Function1<String, Object>() {
                @Override
                public Object apply(String variablePropertyName) {
                    try {
                        return ExperimentSpec.class.getDeclaredField(variablePropertyName).getAnnotation(Description.class).value();
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }), "_");

            pw.println("yformat=%g");
            pw.println("=nogridy");
            pw.println("=norotate");
//            pw.println("=patterns");

            pw.println("xscale=1.2");

//            pw.println("legendx=right");
//            pw.println("legendy=top");
//            pw.println("=nolegoutline");
//            pw.println("legendfill=");
//            pw.println("legendfontsz=13");

            if (rows.get(0) instanceof Map) {
                pw.println("=stacked;" + StringUtils.join(((Map) (rows.get(0))).keySet(), ';'));
                pw.println("=table");
            }

            pw.println("xlabel=" + variablePropertyDescription);
            pw.println("ylabel=" + yLabel);

            int i = 0;
            for (Object row : rows) {
                String title = CollectionUtils.isEmpty(experimentPack.getVariablePropertyValues()) ? "" : experimentPack.getVariablePropertyValues().get(i++);
                pw.println(title.replaceAll(" ", "_") + "\t" + ((row instanceof Map) ? StringUtils.join(((Map) row).values(), '\t') : row));
            }

            pw.close();

            System.err.println(StringUtils.join(CommandLineHelper.invokeShellCommandAndGetResult("tools/bargraph.pl" + " -pdf " + fileInput + " > " + fileNamePdf), IOUtils.LINE_SEPARATOR));

            if (!fileInput.delete()) {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Double> normalize(List<Double> input) {
        double[] resultArray = StatUtils.normalize(ArrayUtils.toPrimitive(input.toArray(new Double[input.size()])));
        List<Double> result = new ArrayList<Double>();
        for (double d : resultArray) {
            result.add(d);
        }

        return result;
    }
}
