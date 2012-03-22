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
package archimulator.sim.base.experiment.profile;

import archimulator.sim.base.experiment.CheckpointedExperiment;
import archimulator.sim.base.experiment.DetailedExperiment;
import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.FunctionalExperiment;
import archimulator.sim.base.experiment.capability.KernelCapability;
import archimulator.sim.base.experiment.capability.ProcessorCapability;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.SimulatedProgram;
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

    @DatabaseField(index = true)
    private String title;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ProcessorProfile processorProfile;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<ContextConfig> contextConfigs = new ArrayList<ContextConfig>();

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<Class<? extends SimulationCapability>> simulationCapabilityClasses;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<Class<? extends ProcessorCapability>> processorCapabilityClasses;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<Class<? extends KernelCapability>> kernelCapabilityClasses;

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

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private LinkedHashMap<String, Object> stats = new LinkedHashMap<String, Object>();

    public ExperimentProfile() {
    }

    public ExperimentProfile(ProcessorProfile processorProfile) {
        this(UUID.randomUUID().toString(), processorProfile);
    }

    public ExperimentProfile(String title, ProcessorProfile processorProfile) {
        this.title = title;
        this.processorProfile = processorProfile;
        this.state = ExperimentProfileState.SUBMITTED;

        this.createdTime = DateHelper.toTick(new Date());

        this.simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>>();
        this.processorCapabilityClasses = new ArrayList<Class<? extends ProcessorCapability>>();
        this.kernelCapabilityClasses = new ArrayList<Class<? extends KernelCapability>>();
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

    public ExperimentProfile cacheWarmupToPseudoCallAndInDetailForMaxInsts(int pthreadSpawnedIndex, int maxInsts) {
        this.type = ExperimentProfileType.CHECKPOINTED_EXPERIMENT;
        this.pthreadSpawnedIndex = pthreadSpawnedIndex;
        this.maxInsts = maxInsts;
        return this;
    }

    public ExperimentProfile addSimulationCapabilityClass(Class<? extends SimulationCapability> clz) {
        this.simulationCapabilityClasses.add(clz);
        return this;
    }

    public Experiment createExperiment() {
        switch (this.type) {
            case FUNCTIONAL_EXPERIMENT:
                return new FunctionalExperiment(this.title, this.processorProfile.getNumCores(), this.processorProfile.getNumThreadsPerCore(), this.contextConfigs,
                        this.simulationCapabilityClasses, this.processorCapabilityClasses, this.kernelCapabilityClasses);
            case DETAILED_EXPERIMENT:
                return new DetailedExperiment(this.title, this.processorProfile.getNumCores(), this.processorProfile.getNumThreadsPerCore(), this.contextConfigs, this.processorProfile.getL2Size(), this.processorProfile.getL2Associativity(), this.processorProfile.getL2EvictionPolicyClz(),
                        this.simulationCapabilityClasses, this.processorCapabilityClasses, this.kernelCapabilityClasses);
            case CHECKPOINTED_EXPERIMENT:
                return new CheckpointedExperiment(this.title, this.processorProfile.getNumCores(), this.processorProfile.getNumThreadsPerCore(), this.contextConfigs, this.processorProfile.getL2Size(), this.processorProfile.getL2Associativity(), this.processorProfile.getL2EvictionPolicyClz(), this.maxInsts, this.pthreadSpawnedIndex,
                        this.simulationCapabilityClasses, this.processorCapabilityClasses, this.kernelCapabilityClasses);
            default:
                throw new IllegalArgumentException();
        }
    }

    public ArrayList<Class<? extends SimulationCapability>> getSimulationCapabilityClasses() {
        return simulationCapabilityClasses;
    }

    public ArrayList<Class<? extends ProcessorCapability>> getProcessorCapabilityClasses() {
        return processorCapabilityClasses;
    }

    public ArrayList<Class<? extends KernelCapability>> getKernelCapabilityClasses() {
        return kernelCapabilityClasses;
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

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public void setTitle(String title) {
        this.title = title;
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

    public Map<String, Object> getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return String.format("ExperimentProfile{id=%d, title='%s', processorProfile.title='%s', type='%s', pthreadSpawnedIndex=%d, maxInsts=%d, state='%s', createdTime='%s'}", id, title, processorProfile.getTitle(), type, pthreadSpawnedIndex, maxInsts, state, DateHelper.toString(createdTime));
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }
}
