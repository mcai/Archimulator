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

/**
 *
 * @author Min Cai
 */
public class PendingL2Miss {
    private MemoryHierarchyAccess access;
    private long beginCycle;
    private long endCycle;
    private double mlpCost;

    private transient int numMlpSamples;
    private transient int mlpSum;

    /**
     *
     * @param access
     * @param beginCycle
     */
    public PendingL2Miss(MemoryHierarchyAccess access, long beginCycle) {
        this.access = access;
        this.beginCycle = beginCycle;
    }

    /**
     *
     * @return
     */
    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    /**
     *
     * @return
     */
    public long getBeginCycle() {
        return beginCycle;
    }

    /**
     *
     * @return
     */
    public long getEndCycle() {
        return endCycle;
    }

    /**
     *
     * @param endCycle
     */
    public void setEndCycle(long endCycle) {
        this.endCycle = endCycle;
    }

    /**
     *
     * @return
     */
    public double getMlpCost() {
        return mlpCost;
    }

    /**
     *
     * @param mlpCost
     */
    public void setMlpCost(double mlpCost) {
        this.mlpCost = mlpCost;
    }

    /**
     *
     * @return
     */
    public int getNumMlpSamples() {
        return numMlpSamples;
    }

    /**
     *
     * @param numMlpSamples
     */
    public void setNumMlpSamples(int numMlpSamples) {
        this.numMlpSamples = numMlpSamples;
    }

    /**
     *
     * @return
     */
    public int getMlpSum() {
        return mlpSum;
    }

    /**
     *
     * @param mlpSum
     */
    public void setMlpSum(int mlpSum) {
        this.mlpSum = mlpSum;
    }

    /**
     *
     * @return
     */
    public int getNumCycles() {
        return (int) (this.endCycle - this.beginCycle);
    }

    /**
     *
     * @return
     */
    public double getAverageMlp() {
        return (double) mlpSum / (double) numMlpSamples;
    }
}
