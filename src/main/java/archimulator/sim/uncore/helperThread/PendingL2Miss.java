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
package archimulator.sim.uncore.helperThread;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheMissType;

public class PendingL2Miss {
    private MemoryHierarchyAccess access;
    private long beginCycle;
    private long endCycle;
    private CacheMissType missType;
    private double mlpCost;

    private transient int numMlpSamples;
    private transient int mlpSum;

    public PendingL2Miss(MemoryHierarchyAccess access, long beginCycle, CacheMissType missType) {
        this.access = access;
        this.beginCycle = beginCycle;
        this.missType = missType;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    public CacheMissType getMissType() {
        return missType;
    }

    public long getEndCycle() {
        return endCycle;
    }

    public void setEndCycle(long endCycle) {
        this.endCycle = endCycle;
    }

    public double getMlpCost() {
        return mlpCost;
    }

    public void setMlpCost(double mlpCost) {
        this.mlpCost = mlpCost;
    }

    public int getNumMlpSamples() {
        return numMlpSamples;
    }

    public void setNumMlpSamples(int numMlpSamples) {
        this.numMlpSamples = numMlpSamples;
    }

    public int getMlpSum() {
        return mlpSum;
    }

    public void setMlpSum(int mlpSum) {
        this.mlpSum = mlpSum;
    }

    public int getNumCycles() {
        return (int) (this.endCycle - this.beginCycle);
    }

    public double getAverageMlp() {
        return (double) mlpSum / (double) numMlpSamples;
    }
}
