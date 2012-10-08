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
package archimulator.service;

import archimulator.model.Architecture;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.service.Service;

import java.util.List;

/**
 *
 * Service for managing architectures.
 *
 * @author Min Cai
 */
public interface ArchitectureService extends Service {
    /**
     *
     * @return
     */
    List<Architecture> getAllArchitectures();

    /**
     *
     * @param first
     * @param count
     * @return
     */
    List<Architecture> getAllArchitectures(long first, long count);

    /**
     *
     * @return
     */
    long getNumAllArchitectures();

    /**
     *
     * @param id
     * @return
     */
    Architecture getArchitectureById(long id);

    /**
     *
     * @param title
     * @return
     */
    Architecture getArchitectureByTitle(String title);

    /**
     *
     * @return
     */
    Architecture getFirstArchitecture();

    /**
     *
     * @param architecture
     */
    void addArchitecture(Architecture architecture);

    /**
     *
     * @param id
     */
    void removeArchitectureById(long id);

    /**
     *
     */
    void clearArchitectures();

    /**
     *
     * @param architecture
     */
    void updateArchitecture(Architecture architecture);

    /**
     *
     * @param htLLCRequestProfilingEnabled
     * @param numCores
     * @param numThreadsPerCore
     * @param l1ISize
     * @param l1IAssoc
     * @param l1DSize
     * @param l1DAssoc
     * @param l2Size
     * @param l2Assoc
     * @param l2ReplacementPolicyType
     * @return
     */
    Architecture getOrAddArchitecture(boolean htLLCRequestProfilingEnabled, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType);
}
