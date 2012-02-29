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
package archimulator.sim.core;

import archimulator.model.capability.*;
import archimulator.sim.core.bpred.BranchPredictorConfig;
import archimulator.sim.core.bpred.PerfectBranchPredictorConfig;
import archimulator.model.capability.KernelCapability;
import archimulator.sim.uncore.MemoryHierarchyConfig;
import archimulator.sim.uncore.tlb.TranslationLookasideBufferConfig;

import java.io.Serializable;
import java.util.List;

public class ProcessorConfig implements Serializable {
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

    private List<Class<? extends ProcessorCapability>> processorCapabilityClasses;
    private List<Class<? extends KernelCapability>> kernelCapabilityClasses;

    public ProcessorConfig(int numCores, int numThreadsPerCore, MemoryHierarchyConfig memoryHierarchyConfig, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses) {
        this(numCores, numThreadsPerCore, 128, 4, 4, 4, 96, 96, 48, memoryHierarchyConfig, processorCapabilityClasses, kernelCapabilityClasses);
    }

    public ProcessorConfig(int numCores, int numThreadsPerCore, int physicalRegisterFileCapacity, int decodeWidth, int issueWidth, int commitWidth, int decodeBufferCapacity, int reorderBufferCapacity, int loadStoreQueueCapacity, MemoryHierarchyConfig memoryHierarchyConfig, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses) {
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

        this.processorCapabilityClasses = processorCapabilityClasses;
        this.kernelCapabilityClasses = kernelCapabilityClasses;
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

    public List<Class<? extends ProcessorCapability>> getProcessorCapabilityClasses() {
        return processorCapabilityClasses;
    }

    public List<Class<? extends KernelCapability>> getKernelCapabilityClasses() {
        return kernelCapabilityClasses;
    }

    public static ProcessorConfig createDefaultProcessorConfig(MemoryHierarchyConfig memoryHierarchyConfig, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses, int numCores, int numThreadsPerCore) {
        ProcessorConfig processorConfig = new ProcessorConfig(numCores, numThreadsPerCore, memoryHierarchyConfig, processorCapabilityClasses, kernelCapabilityClasses);
        processorConfig.setBpred(new PerfectBranchPredictorConfig());
        processorConfig.setTlb(new TranslationLookasideBufferConfig(32768, 4));
        return processorConfig;
    }
}
