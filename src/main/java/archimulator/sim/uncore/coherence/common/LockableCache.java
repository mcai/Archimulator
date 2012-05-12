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
package archimulator.sim.uncore.coherence.common;

import archimulator.sim.base.simulation.SimulationObject;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.util.action.Function3;

import java.io.Serializable;

public class LockableCache<StateT extends Serializable, LineT extends LockableCacheLine<StateT>> extends EvictableCache<StateT, LineT> {
    public LockableCache(SimulationObject parent, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, Function3<Cache<?, ?>, Integer, Integer, LineT> createLine) {
        super(parent, name, geometry, evictionPolicyClz, createLine);
    }

    @Override
    public LineT findLine(int address) {
        int tag = this.getTag(address);
        int set = this.getSet(address);

        for (int way = 0; way < this.getAssociativity(); way++) {
            LineT line = this.getLine(set, way);
            if (!line.isLocked() && line.getTag() == tag && line.getState() != line.getInitialState()) {
                return line;
            } else if (line.isLocked() && line.getTransientTag() == tag) {
                return line;
            }
        }

        return null;
    }
}
