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

import java.util.Map;
import java.util.TreeMap;

/**
 * Register rename table.
 *
 * @author Min Cai
 */
public class RegisterRenameTable {
    private String name;
    private Map<Integer, PhysicalRegister> entries;

    /**
     * Create a register rename table.
     *
     * @param name the name of the register rename table
     */
    public RegisterRenameTable(String name) {
        this.name = name;
        this.entries = new TreeMap<>();
    }

    /**
     * Get the corresponding physical register for the specified dependency.
     *
     * @param dependency the dependency
     * @return the corresponding physical register for the specified dependency
     */
    public PhysicalRegister get(int dependency) {
        return this.entries.get(dependency);
    }

    /**
     * Set the corresponding physical register for the specified dependency.
     *
     * @param dependency       the dependency
     * @param physicalRegister the physical register
     */
    public void put(int dependency, PhysicalRegister physicalRegister) {
        this.entries.put(dependency, physicalRegister);
    }

    /**
     * Get the name of the register rename table.
     *
     * @return the name of the register rename table
     */
    public String getName() {
        return name;
    }
}
