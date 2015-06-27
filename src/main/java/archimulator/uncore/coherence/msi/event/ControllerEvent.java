/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.coherence.msi.event;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.msi.controller.Controller;
import archimulator.uncore.coherence.msi.flow.CacheCoherenceFlow;

/**
 * Controller event.
 *
 * @author Min Cai
 */
public abstract class ControllerEvent extends CacheCoherenceFlow {
    /**
     * Create a controller event.
     *
     * @param generator    the generator controller
     * @param producerFlow the producer flow
     * @param access       the memory hierarchy access
     * @param tag          the tag
     */
    public ControllerEvent(Controller generator, CacheCoherenceFlow producerFlow, MemoryHierarchyAccess access, int tag) {
        super(generator, producerFlow, access, tag);
    }
}
