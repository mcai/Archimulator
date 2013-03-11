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
package archimulator.sim.uncore.cache.partitioning;

import archimulator.sim.common.Simulation;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache partitioning helper.
 *
 * @author Min Cai
 */
public abstract class CachePartitioningHelper {
    private DirectoryController l2CacheController;

    private int numCyclesElapsedPerInterval;

    private long numIntervals;
    private int numCyclesElapsed;

    private int numThreads;

    private List<Integer> partition;

    /**
     * Create a cache partitioning helper.
     *
     * @param simulation the simulation
     */
    public CachePartitioningHelper(Simulation simulation) {
        this(simulation.getProcessor().getMemoryHierarchy().getL2CacheController());
    }

    /**
     * Create a cache partitioning helper.
     *
     * @param l2CacheController the L2 cache controller
     */
    public CachePartitioningHelper(DirectoryController l2CacheController) {
        this.l2CacheController = l2CacheController;

//        this.numThreads = this.l2CacheController.getExperiment().getArchitecture().getNumThreadsPerCore() * this.l2CacheController.getExperiment().getArchitecture().getNumCores();
        this.numThreads = this.l2CacheController.getExperiment().getArchitecture().getNumCores();

        this.partition = new ArrayList<Integer>();

        int l2Associativity = this.l2CacheController.getCache().getAssociativity();

        if (l2Associativity < this.numThreads) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < this.numThreads; i++) {
            this.partition.add(l2Associativity / this.numThreads);
        }

        this.numCyclesElapsedPerInterval = 5000000;

        this.l2CacheController.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                numCyclesElapsed++;

                if (numCyclesElapsed == numCyclesElapsedPerInterval) {
                    newInterval();

                    numCyclesElapsed = 0;
                    numIntervals++;
                }

            }
        });
    }

    /**
     * New interval.
     */
    protected abstract void newInterval();

    /**
     * Get the L2 cache controller.
     *
     * @return the L2 cache controller
     */
    public DirectoryController getL2CacheController() {
        return l2CacheController;
    }

    /**
     * Get the measuring interval in cycles.
     *
     * @return the measuring interval in cycles
     */
    public int getNumCyclesElapsedPerInterval() {
        return numCyclesElapsedPerInterval;
    }

    /**
     * Get the number of intervals.
     *
     * @return the number of intervals
     */
    public long getNumIntervals() {
        return numIntervals;
    }

    /**
     * Get the number of cycles elapsed.
     *
     * @return the number of cycles elapsed
     */
    public int getNumCyclesElapsed() {
        return numCyclesElapsed;
    }

    /**
     * Get the number of threads.
     *
     * @return the number of threads
     */
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * Get the partition.
     *
     * @return the partition
     */
    public List<Integer> getPartition() {
        return partition;
    }

    /**
     * Set the partition.
     *
     * @param partition the partition
     */
    public void setPartition(List<Integer> partition) {
        this.partition = partition;
    }
}
