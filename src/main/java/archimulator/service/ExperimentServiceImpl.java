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
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.sql.SQLException;
import java.util.*;

public class ExperimentServiceImpl extends AbstractService implements ExperimentService {
    private Dao<Experiment, Long> experiments;

    @SuppressWarnings("unchecked")
    public ExperimentServiceImpl(){
        super(ServiceManager.DATABASE_DIRECTORY, ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(Experiment.class));

        this.experiments = createDao(Experiment.class);

        new ExperimentWorker();
    }

    @Override
    public List<Experiment> getAllExperiments() throws SQLException {
        return this.getAllItems(this.experiments);
    }

    @Override
    public Experiment getExperimentById(long id) throws SQLException {
        return this.getItemById(this.experiments, id);
    }

    @Override
    public List<Experiment> getExperimentsByTitle(String title) throws SQLException {
        return this.getItemsByTitle(this.experiments, title);
    }

    @Override
    public Experiment getLatestExperimentByTitle(String title) throws SQLException {
        return this.getLatestItemByTitle(this.experiments, title);
    }

    @Override
    public List<Experiment> getExperimentsBySimulatedProgram(SimulatedProgram simulatedProgram) throws SQLException {
        List<Experiment> result = new ArrayList<Experiment>();

        for(Experiment experiment : getAllExperiments()) {
            for(ContextMapping contextMapping : experiment.getContextMappings()) {
                if(contextMapping.getSimulatedProgramId() == simulatedProgram.getId()) {
                    result.add(experiment);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public List<Experiment> getExperimentsByArchitecture(Architecture architecture) throws SQLException {
        List<Experiment> result = new ArrayList<Experiment>();

        for(Experiment experiment : getAllExperiments()) {
            if(experiment.getArchitectureId() == architecture.getId()) {
                result.add(experiment);
            }
        }

        return result;
    }

    @Override
    public void addExperiment(Experiment experiment) throws SQLException {
        this.addItem(this.experiments, Experiment.class, experiment);
    }

    @Override
    public void removeExperimentById(long id) throws SQLException {
        this.removeItemById(this.experiments, Experiment.class, id);
    }

    @Override
    public void updateExperiment(Experiment experiment) throws SQLException {
        this.updateItem(this.experiments, Experiment.class, experiment);
    }

    @Override
    public void dumpExperiment(Experiment experiment) throws SQLException {
        System.out.printf("[%s] %s\n", DateHelper.toString(experiment.getCreateTime()), experiment);

        Map<String, String> configs = new LinkedHashMap<String, String>();

        JaxenHelper.dumpValueFromXPath(configs, experiment, "title");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "createTimeAsString");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "type");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "numMaxInsts");
        JaxenHelper.dumpValuesFromXPath(configs, experiment, "contextMappings");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/title");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/createTimeAsString");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/htPthreadSpawnIndex");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/htLLCRequestProfilingEnabled");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/numCores");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/numThreadsPerCore");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/physicalRegisterFileCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/decodeWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/issueWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/commitWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/decodeBufferCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/reorderBufferCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/loadStoreQueueCapacity");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/bpredType");
        switch (experiment.getArchitecture().getBpredType()) {
            case PERFECT:
                break;
            case TAKEN:
                break;
            case NOT_TAKEN:
                break;
            case TWO_BIT:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBpredBimodSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBpredBtbSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBpredBtbAssoc");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBpredRetStackSize");
                break;
            case TWO_LEVEL:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredL1Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredL2Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredShiftWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredXor");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredBtbSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredBtbAssoc");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBpredRetStackSize");
                break;
            case COMBINED:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredBimodSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredL1Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredL2Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredMetaSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredShiftWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredXor");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredBtbSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredBtbAssoc");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBpredBtbRetStackSize");
                break;
        }

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbAssoc");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbLineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbMissLatency");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1ISize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IAssoc");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1ILineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1INumReadPorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1INumWritePorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DAssoc");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DLineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DNumReadPorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DNumWritePorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2Size");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2Assoc");
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
    }

    @Override
    public Experiment getFirstExperimentToRun() throws SQLException {
        for(Experiment experiment : getAllExperiments()) {
            if(experiment.getState() == ExperimentState.PENDING) {
                return experiment;
            }
        }

        return null;
    }

    @Override
    public void waitForExperimentStopped(Experiment experiment) throws SQLException {
        long id = experiment.getId();

        try {
            for(;;) {
                if(getExperimentById(id).isStopped()) {
                    break;
                }

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
