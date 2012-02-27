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

import archimulator.model.simulation.BasicSimulationObject;
import archimulator.model.simulation.SimulationObject;
import archimulator.sim.uncore.coherence.message.MemoryDeviceMessage;
import archimulator.sim.uncore.net.Net;
import archimulator.util.action.Action;

import java.io.Serializable;

public abstract class MemoryDevice extends BasicSimulationObject implements SimulationObject, Serializable {
    private CacheHierarchy cacheHierarchy;
    private String name;

    public MemoryDevice(CacheHierarchy cacheHierarchy, String name) {
        super(cacheHierarchy);

        this.cacheHierarchy = cacheHierarchy;
        this.name = name;
    }

    public void sendRequest(final MemoryDevice target, final MemoryDeviceMessage message, final int size) {
        this.getNet(target).transfer(this, target, size, new Action() {
            public void apply() {
                target.receiveRequest(MemoryDevice.this, message);
            }
        });
    }

    protected abstract Net getNet(MemoryDevice to);

    public void sendReply(final MemoryDevice source, final MemoryDeviceMessage message, final int size) {
        this.getNet(source).transfer(this, source, size, new Action() {
            public void apply() {
                message.reply();
            }
        });
    }

    public abstract void receiveRequest(MemoryDevice source, MemoryDeviceMessage message);

    public CacheHierarchy getCacheHierarchy() {
        return cacheHierarchy;
    }

    public String getName() {
        return name;
    }
}