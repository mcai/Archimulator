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
package archimulator.model.experiment.profile;

import archimulator.model.capability.SimulationCapability;
import archimulator.model.experiment.CheckpointedExperiment;
import archimulator.model.experiment.DetailedExperiment;
import archimulator.model.experiment.Experiment;
import archimulator.model.experiment.FunctionalExperiment;
import archimulator.model.simulation.ContextConfig;
import archimulator.model.simulation.SimulatedProgram;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.util.DateHelper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.*;

@DatabaseTable
public class ExperimentProfile implements Serializable {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ProcessorProfile processorProfile;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<ContextConfig> contextConfigs = new ArrayList<ContextConfig>();

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<Class<? extends SimulationCapability>> simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>>();

    @DatabaseField
    private ExperimentProfileType type = ExperimentProfileType.FUNCTIONAL_EXPERIMENT;

    @DatabaseField
    private int pthreadSpawnedIndex;

    @DatabaseField
    private int maxInsts;

    @DatabaseField
    private ExperimentProfileState state;

    @DatabaseField
    private long createdTime;

    @DatabaseField
    private String simulatorUserId;

    public ExperimentProfile() {
    }

    public ExperimentProfile(ProcessorProfile processorProfile) {
        this.processorProfile = processorProfile;
        this.state = ExperimentProfileState.SUBMITTED;

        this.createdTime = DateHelper.toTick(new Date());
    }

    public ExperimentProfile addWorkload(SimulatedProgram simulatedProgram) {
        return this.addWorkload(simulatedProgram, 0);
    }

    public ExperimentProfile addWorkload(SimulatedProgram simulatedProgram, int threadId) {
        this.contextConfigs.add(new ContextConfig(simulatedProgram, threadId));
        return this;
    }

    public List<ContextConfig> getContextConfigs() {
        return contextConfigs;
    }

    public void setType(ExperimentProfileType type) {
        this.type = type;
    }

    public ExperimentProfile functionallyToEnd() {
        this.type = ExperimentProfileType.FUNCTIONAL_EXPERIMENT;
        return this;
    }

    public ExperimentProfile inDetailToEnd() {
        this.type = ExperimentProfileType.DETAILED_EXPERIMENT;
        return this;
    }

    public ExperimentProfile functionallyToPseudoCallAndInDetailForMaxInsts(int pthreadSpawnedIndex, int maxInsts) {
        this.type = ExperimentProfileType.CHECKPOINTED_EXPERIMENT;
        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
        this.maxInsts = maxInsts;
        return this;
    }

    public ExperimentProfile addSimulationCapabilityClass(Class<? extends SimulationCapability> clz) {
        this.simulationCapabilityClasses.add(clz);
        return this;
    }

    public ArrayList<Class<? extends SimulationCapability>> getSimulationCapabilityClasses() {
        return simulationCapabilityClasses;
    }

    public ExperimentProfileType getType() {
        return type;
    }

    public int getPthreadSpawnedIndex() {
        return pthreadSpawnedIndex;
    }

    public int getMaxInsts() {
        return maxInsts;
    }

    public Experiment createExperiment() {
        switch (this.type) {
            case FUNCTIONAL_EXPERIMENT:
                return new FunctionalExperiment(UUID.randomUUID().toString(), this.getProcessorProfile().getNumCores(), this.getProcessorProfile().getNumThreadsPerCore(), this.getContextConfigs(),
                        this.simulationCapabilityClasses, this.getProcessorProfile().getProcessorCapabilityClasses(), this.getProcessorProfile().getKernelCapabilityClasses());
            case DETAILED_EXPERIMENT:
                return new DetailedExperiment(UUID.randomUUID().toString(), this.getProcessorProfile().getNumCores(), this.getProcessorProfile().getNumThreadsPerCore(), this.getContextConfigs(), LeastRecentlyUsedEvictionPolicy.FACTORY, this.getProcessorProfile().getL2Size(), this.getProcessorProfile().getL2Associativity(),
                        this.simulationCapabilityClasses, this.getProcessorProfile().getProcessorCapabilityClasses(), this.getProcessorProfile().getKernelCapabilityClasses());
            case CHECKPOINTED_EXPERIMENT:
                return new CheckpointedExperiment(UUID.randomUUID().toString(), this.getProcessorProfile().getNumCores(), this.getProcessorProfile().getNumThreadsPerCore(), this.getContextConfigs(), this.getMaxInsts(), this.getProcessorProfile().getL2Size(), this.getProcessorProfile().getL2Associativity(), LeastRecentlyUsedEvictionPolicy.FACTORY, this.getPthreadSpawnedIndex(),
                        this.simulationCapabilityClasses, this.getProcessorProfile().getProcessorCapabilityClasses(), this.getProcessorProfile().getKernelCapabilityClasses());
                default:
                    throw new IllegalArgumentException();
        }
    }

    public long getId() {
        return id;
    }

    public ProcessorProfile getProcessorProfile() {
        return processorProfile;
    }

    public ExperimentProfileState getState() {
        return state;
    }

    public void setState(ExperimentProfileState state) {
        this.state = state;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getCreatedTimeAsString() {
        return DateHelper.toString(DateHelper.fromTick(this.createdTime));
    }

    public void setProcessorProfile(ProcessorProfile processorProfile) {
        this.processorProfile = processorProfile;
    }

    public void setPthreadSpawnedIndex(int pthreadSpawnedIndex) {
        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
    }

    public void setMaxInsts(int maxInsts) {
        this.maxInsts = maxInsts;
    }

    public String getSimulatorUserId() {
        return simulatorUserId;
    }

    public void setSimulatorUserId(String simulatorUserId) {
        this.simulatorUserId = simulatorUserId;
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }
}
