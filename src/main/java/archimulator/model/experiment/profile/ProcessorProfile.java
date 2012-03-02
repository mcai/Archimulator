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

import archimulator.model.capability.KernelCapability;
import archimulator.model.capability.ProcessorCapability;
import archimulator.util.DateHelper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable
public class ProcessorProfile implements Serializable {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private int numCores;

    @DatabaseField
    private int numThreadsPerCore;

    @DatabaseField
    private int l2Size;

    @DatabaseField
    private int l2Associativity;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<Class<? extends ProcessorCapability>> processorCapabilityClasses;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<Class<? extends KernelCapability>> kernelCapabilityClasses;

    @DatabaseField
    private long createdTime;

    public ProcessorProfile() {
    }

    public ProcessorProfile(int numCores, int numThreadsPerCore, int l2Size, int l2Associativity) {
        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;
        this.l2Size = l2Size;
        this.l2Associativity = l2Associativity;

        this.processorCapabilityClasses = new ArrayList<Class<? extends ProcessorCapability>>();
        this.kernelCapabilityClasses = new ArrayList<Class<? extends KernelCapability>>();

        this.createdTime = DateHelper.toTick(new Date());
    }

    public long getId() {
        return id;
    }

    public int getNumCores() {
        return numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public int getL2Size() {
        return l2Size;
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public List<Class<? extends ProcessorCapability>> getProcessorCapabilityClasses() {
        return processorCapabilityClasses;
    }

    public List<Class<? extends KernelCapability>> getKernelCapabilityClasses() {
        return kernelCapabilityClasses;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getCreatedTimeAsString() {
        return DateHelper.toString(DateHelper.fromTick(this.createdTime));
    }

    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    public void setL2Size(int l2Size) {
        this.l2Size = l2Size;
    }

    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    @Override
    public String toString() {
        return String.format("ProcessorProfile{id=%d, numCores=%d, numThreadsPerCore=%d, l2Size=%d, l2Associativity=%d}", id, numCores, numThreadsPerCore, l2Size, l2Associativity);
    }
}
