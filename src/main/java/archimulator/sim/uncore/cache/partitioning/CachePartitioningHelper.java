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
import archimulator.sim.core.Thread;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.action.Action;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache partitioning helper.
 *
 * @author Min Cai
 */
public abstract class CachePartitioningHelper {
    private Simulation simulation;

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
        this.simulation = simulation;

//        this.numThreads = simulation.getExperiment().getArchitecture().getNumThreadsPerCore() * this.l2CacheController.getExperiment().getArchitecture().getNumCores();
        this.numThreads = simulation.getExperiment().getArchitecture().getNumCores();

        this.partition = new ArrayList<Integer>();

        int l2Associativity = simulation.getExperiment().getArchitecture().getL2Associativity();

        if (l2Associativity < this.numThreads) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < this.numThreads; i++) {
            this.partition.add(l2Associativity / this.numThreads);
        }

        this.numCyclesElapsedPerInterval = 5000000;

        simulation.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
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
     * Get the simulation.
     *
     * @return the simulation
     */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Get the L2 cache controller.
     *
     * @return the L2 cache controller
     */
    public DirectoryController getL2CacheController() {
        return getSimulation().getProcessor().getMemoryHierarchy().getL2CacheController();
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

    /**
     * Get the identifier for the specified thread.
     *
     * @param thread the thread
     * @return the identifier for the specified thread
     */
    public static int getThreadIdentifier(Thread thread) {
        return thread.getCore().getNum();
    }

    /**
     * Divide the integer n into k partitions.
     *
     * @param n the integer n to be partitioned
     * @param k the number of partitions
     * @return the partition list of the specified integer n
     */
    public static List<List<Integer>> partition(int n, int k) {
        List<List<Integer>> result = new ArrayList<List<Integer>>();

        Generator<Integer> generator = Factory.createCompositionGenerator(n);

        for (ICombinatoricsVector<Integer> vector : generator) {
            if (vector.getSize() == k) {
                result.add(vector.getVector());
            }
        }

        return result;
    }
}
