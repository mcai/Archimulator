/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;

/**
 * L1I cache controller.
 *
 * @author Min Cai
 */
public class L1IController extends CacheController {
    /**
     * Create an L1I cache controller.
     *
     * @param memoryHierarchy the parent memory hierarchy
     * @param name            the name
     */
    public L1IController(MemoryHierarchy memoryHierarchy, String name) {
        super(memoryHierarchy, name);
    }

    @Override
    public int getNumReadPorts() {
        return getExperiment().getL1INumReadPorts();
    }

    @Override
    public int getNumWritePorts() {
        return getExperiment().getL1INumWritePorts();
    }

    @Override
    public CacheGeometry getGeometry() {
        return new CacheGeometry(getExperiment().getL1ISize(), getExperiment().getL1IAssociativity(), getExperiment().getL1ILineSize());
    }

    @Override
    public int getHitLatency() {
        return getExperiment().getL1IHitLatency();
    }

    @Override
    public CacheReplacementPolicyType getReplacementPolicyType() {
        return getExperiment().getL1IReplacementPolicyType();
    }
}
