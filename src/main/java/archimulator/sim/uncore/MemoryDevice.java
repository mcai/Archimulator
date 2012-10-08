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
package archimulator.sim.uncore;

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;

import java.io.Serializable;

/**
 *
 * @author Min Cai
 */
public abstract class MemoryDevice extends BasicSimulationObject implements SimulationObject, Serializable {
    private CacheHierarchy cacheHierarchy;
    private String name;

    /**
     *
     * @param cacheHierarchy
     * @param name
     */
    public MemoryDevice(CacheHierarchy cacheHierarchy, String name) {
        super(cacheHierarchy);

        this.cacheHierarchy = cacheHierarchy;
        this.name = name;
    }

    /**
     *
     * @param to
     * @param size
     * @param action
     */
    public void transfer(MemoryDevice to, int size, Action action) {
        this.getNet(to).transfer(this, to, size, action);
    }

    /**
     *
     * @param to
     * @return
     */
    protected abstract Net getNet(MemoryDevice to);

    /**
     *
     * @return
     */
    public CacheHierarchy getCacheHierarchy() {
        return cacheHierarchy;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }
}
