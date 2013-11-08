/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.dram;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;

/**
 * Memory controller.
 *
 * @author Min Cai
 */
public abstract class MemoryController extends MemoryDevice implements Reportable {
    private long numReads;
    private long numWrites;

    /**
     * Create a memory controller.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    public MemoryController(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy, "memoryController");
    }

    /**
     * Get the net for the destination memory device.
     *
     * @param to the destination memory device
     * @return the net for the destination memory device
     */
    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getMemoryHierarchy().getL2ToMemNet();
    }

    /**
     * Act on receiving a read request.
     *
     * @param source              the source memory device
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the request is completed
     */
    public void memReadRequestReceive(final MemoryDevice source, int tag, final Action onCompletedCallback) {
        this.numReads++;

        this.access(tag, () -> transfer(source, ((DirectoryController) source).getCache().getLineSize() + 8, onCompletedCallback));
    }

    /**
     * Act on receiving a write request.
     *
     * @param source              the source memory device
     * @param tag                 the tag
     * @param onCompletedCallback the callback action performed when the request is completed
     */
    public void memWriteRequestReceive(final MemoryDevice source, int tag, final Action onCompletedCallback) {
        this.numWrites++;

        this.access(tag, () -> transfer(source, 8, onCompletedCallback));
    }

    /**
     * Access the specified address.
     *
     * @param address             the address
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    protected abstract void access(int address, Action onCompletedCallback);

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, getName()) {{
            getChildren().add(new ReportNode(this, "numAccesses", getNumAccesses() + ""));
            getChildren().add(new ReportNode(this, "numReads", getNumReads() + ""));
            getChildren().add(new ReportNode(this, "numWrites", getNumWrites() + ""));
        }});
    }

    /**
     * Get the number of accesses.
     *
     * @return the number of accesses
     */
    public long getNumAccesses() {
        return this.numReads + this.numWrites;
    }

    /**
     * Get the number of reads.
     *
     * @return the number of reads
     */
    public long getNumReads() {
        return numReads;
    }

    /**
     * Get the number of writes.
     *
     * @return the number of writes
     */
    public long getNumWrites() {
        return numWrites;
    }

    /**
     * Get the line size.
     *
     * @return the line size
     */
    public int getLineSize() {
        return getExperiment().getArchitecture().getMemoryControllerLineSize();
    }

    @Override
    public String toString() {
        return getName();
    }
}



















