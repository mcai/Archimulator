/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.common;

import archimulator.util.event.BlockingEvent;

/**
 * Simulation event.
 *
 * @author Min Cai
 */
public abstract class SimulationEvent implements BlockingEvent {
    private SimulationObject sender;

    /**
     * Create a simulation event.
     *
     * @param sender the sender simulation object
     */
    public SimulationEvent(SimulationObject sender) {
        this.sender = sender;
    }

    /**
     * Get the sender simulation object.
     *
     * @return the sender simulation object
     */
    public SimulationObject getSender() {
        return sender;
    }
}
