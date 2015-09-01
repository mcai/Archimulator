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
package archimulator.core;

import archimulator.common.SimulationObject;
import archimulator.common.report.Reportable;

/**
 * Memory hierarchy thread.
 *
 * @author Min Cai
 */
public interface MemoryHierarchyThread extends SimulationObject, Reportable {
    /**
     * Get the number of the thread.
     *
     * @return the number of the thread
     */
    int getNum();

    /**
     * Get the ID of the thread.
     *
     * @return the ID of the thread
     */
    int getId();

    /**
     * Get the name of the thread.
     *
     * @return the name of the thread
     */
    String getName();

    /**
     * Get the parent core.
     *
     * @return the parent core
     */
    MemoryHierarchyCore getCore();
}
