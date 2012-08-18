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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;

public class L1DCacheController extends CacheController {
    public L1DCacheController(CacheHierarchy cacheHierarchy, String name) {
        super(cacheHierarchy, name);
    }

    @Override
    public int getNumReadPorts() {
        return getExperiment().getArchitecture().getL1DNumReadPorts();
    }

    @Override
    public int getNumWritePorts() {
        return getExperiment().getArchitecture().getL1DNumWritePorts();
    }

    @Override
    public CacheGeometry getGeometry() {
        return new CacheGeometry(getExperiment().getArchitecture().getL1DSize(), getExperiment().getArchitecture().getL1DAssoc(), getExperiment().getArchitecture().getL1DLineSize());
    }

    @Override
    public int getHitLatency() {
        return getExperiment().getArchitecture().getL1DHitLatency();
    }

    @Override
    public CacheReplacementPolicyType getReplacementPolicyType() {
        return getExperiment().getArchitecture().getL1DReplacementPolicyType();
    }
}
