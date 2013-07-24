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
package archimulator.service;

import archimulator.model.Architecture;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.service.Service;

import java.util.List;

/**
 * Service for managing architectures.
 *
 * @author Min Cai
 */
public interface ArchitectureService extends Service {
    /**
     * Get all architectures.
     *
     * @return all architectures
     */
    List<Architecture> getAllArchitectures();

    /**
     * Get all architectures by offset and count.
     *
     * @param first offset
     * @param count count
     * @return all architectures by offset and count
     */
    List<Architecture> getAllArchitectures(long first, long count);

    /**
     * Get the number of all architectures.
     *
     * @return the number of all architectures
     */
    long getNumAllArchitectures();

    /**
     * Get the architecture by id.
     *
     * @param id the architecture's id
     * @return the architecture matching the id if any exists, otherwise null
     */
    Architecture getArchitectureById(long id);

    /**
     * Get the architecture by title.
     *
     * @param title the architecture's title
     * @return the architecture matching the title if any exists, otherwise null
     */
    Architecture getArchitectureByTitle(String title);

    /**
     * Get the first architecture.
     *
     * @return the first architecture if any exists, otherwise null
     */
    Architecture getFirstArchitecture();

    /**
     * Add an architecture.
     *
     * @param architecture the architecture to be added
     */
    void addArchitecture(Architecture architecture);

    /**
     * Remove the architecture by id.
     *
     * @param id the architecture's id
     */
    void removeArchitectureById(long id);

    /**
     * Clear all architectures.
     */
    void clearArchitectures();

    /**
     * Update the specified architecture.
     *
     * @param architecture the architecture to be updated
     */
    void updateArchitecture(Architecture architecture);

    /**
     * Get or update an architecture matching the given parameters.
     *
     * @param dynamicSpeculativePrecomputationEnabled
     *                                a value indicating whether dynamic speculative precomputation is enabled or not
     * @param numMainThreadWaysInStaticPartitionedLRUPolicy the number of main thread ways used in the static partitioned LRU policy
     * @param numCores                the number of cores
     * @param numThreadsPerCore       number of threads per core
     * @param l1ISize                 L1 instruction cache size
     * @param l1IAssoc                L1 instruction cache associativity
     * @param l1DSize                 L1 data cache size
     * @param l1DAssoc                L1 data cache associativity
     * @param l2Size                  L2 cache size
     * @param l2Assoc                 L2 cache associativity
     * @param l2ReplacementPolicyType L2 cache replacement policy type
     * @return the existing or newly added architecture matching the given parameters
     */
    Architecture getOrAddArchitecture(boolean dynamicSpeculativePrecomputationEnabled, int numMainThreadWaysInStaticPartitionedLRUPolicy, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType);

    /**
     * Initialize the service.
     */
    void initialize();
}
