/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core;

import archimulator.core.bpred.BranchPredictorConfig;
import archimulator.core.bpred.PerfectBranchPredictorConfig;
import archimulator.mem.MemoryHierarchyConfig;
import archimulator.mem.tlb.TranslationLookasideBufferConfig;
import archimulator.os.KernelCapability;
import archimulator.os.KernelCapabilityFactory;
import archimulator.sim.capability.ProcessorCapability;
import archimulator.sim.capability.ProcessorCapabilityFactory;

import java.util.Map;

public class ProcessorConfig {
    private int numCores;
    private int numThreadsPerCore;

    private int physicalRegisterFileCapacity;
    private int decodeWidth;
    private int issueWidth;
    private int commitWidth;
    private int decodeBufferCapacity;
    private int reorderBufferCapacity;
    private int loadStoreQueueCapacity;

    private BranchPredictorConfig bpred;
    private TranslationLookasideBufferConfig tlb;

    private MemoryHierarchyConfig memoryHierarchyConfig;

    private Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories;
    private Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories;

    public ProcessorConfig(int numCores, int numThreadsPerCore, MemoryHierarchyConfig memoryHierarchyConfig, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories, Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories) {
        this(numCores, numThreadsPerCore, 128, 8, 8, 8, 96, 96, 48, memoryHierarchyConfig, processorCapabilityFactories, kernelCapabilityFactories);
    }

    public ProcessorConfig(int numCores, int numThreadsPerCore, int physicalRegisterFileCapacity, int decodeWidth, int issueWidth, int commitWidth, int decodeBufferCapacity, int reorderBufferCapacity, int loadStoreQueueCapacity, MemoryHierarchyConfig memoryHierarchyConfig, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories, Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories) {
        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;

        this.physicalRegisterFileCapacity = physicalRegisterFileCapacity;
        this.decodeWidth = decodeWidth;
        this.issueWidth = issueWidth;
        this.commitWidth = commitWidth;
        this.decodeBufferCapacity = decodeBufferCapacity;
        this.reorderBufferCapacity = reorderBufferCapacity;
        this.loadStoreQueueCapacity = loadStoreQueueCapacity;

        this.memoryHierarchyConfig = memoryHierarchyConfig;

        this.processorCapabilityFactories = processorCapabilityFactories;
        this.kernelCapabilityFactories = kernelCapabilityFactories;
    }

    public int getNumCores() {
        return numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public int getPhysicalRegisterFileCapacity() {
        return physicalRegisterFileCapacity;
    }

    public int getDecodeWidth() {
        return decodeWidth;
    }

    public int getIssueWidth() {
        return issueWidth;
    }

    public int getCommitWidth() {
        return commitWidth;
    }

    public int getDecodeBufferCapacity() {
        return decodeBufferCapacity;
    }

    public int getReorderBufferCapacity() {
        return reorderBufferCapacity;
    }

    public int getLoadStoreQueueCapacity() {
        return loadStoreQueueCapacity;
    }

    public BranchPredictorConfig getBpred() {
        return bpred;
    }

    public void setBpred(BranchPredictorConfig bpred) {
        this.bpred = bpred;
    }

    public TranslationLookasideBufferConfig getTlb() {
        return tlb;
    }

    public void setTlb(TranslationLookasideBufferConfig tlb) {
        this.tlb = tlb;
    }

    public MemoryHierarchyConfig getMemoryHierarchyConfig() {
        return memoryHierarchyConfig;
    }

    public Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> getProcessorCapabilityFactories() {
        return processorCapabilityFactories;
    }

    public Map<Class<? extends KernelCapability>, KernelCapabilityFactory> getKernelCapabilityFactories() {
        return kernelCapabilityFactories;
    }

    public static ProcessorConfig createDefaultProcessorConfig(MemoryHierarchyConfig memoryHierarchyConfig, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories, Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories, int numCores, int numThreadsPerCore) {
        ProcessorConfig processorConfig = new ProcessorConfig(numCores, numThreadsPerCore, memoryHierarchyConfig, processorCapabilityFactories, kernelCapabilityFactories);
        processorConfig.setBpred(new PerfectBranchPredictorConfig());
        processorConfig.setTlb(new TranslationLookasideBufferConfig(32768, 4));
        return processorConfig;
    }
}
