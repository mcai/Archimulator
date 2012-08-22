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
package archimulator.sim.uncore.dram;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;

public abstract class MemoryController extends MemoryDevice {
    private long numReads;
    private long numWrites;

    public MemoryController(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy, "mainMemory");
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getCacheHierarchy().getL2ToMemNetwork();
    }

    public void memReadRequestReceive(final MemoryDevice source, int tag, final Action onSuccessCallback) {
        this.numReads++;

        this.access(tag, new Action() {
            @Override
            public void apply() {
                transfer(source, ((DirectoryController) source).getCache().getLineSize() + 8, onSuccessCallback);
            }
        });
    }

    public void memWriteRequestReceive(final MemoryDevice source, int tag, final Action onSuccessCallback) {
        this.numWrites++;

        this.access(tag, new Action() {
            @Override
            public void apply() {
                transfer(source, 8, onSuccessCallback);
            }
        });
    }

    protected abstract void access(int address, Action onCompletedCallback);

    public long getNumAccesses() {
        return this.numReads + this.numWrites;
    }

    public long getNumReads() {
        return numReads;
    }

    public long getNumWrites() {
        return numWrites;
    }

    public int getLineSize() {
        return getExperiment().getArchitecture().getMainMemoryLineSize();
    }

    @Override
    public String toString() {
        return getName();
    }
}



















