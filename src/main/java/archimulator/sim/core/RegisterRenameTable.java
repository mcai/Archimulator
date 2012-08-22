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
package archimulator.sim.core;

import java.util.Map;
import java.util.TreeMap;

public class RegisterRenameTable {
    private String name;
    private Map<Integer, PhysicalRegisterFile.PhysicalRegister> entries;

    public RegisterRenameTable(String name) {
        this.name = name;
        this.entries = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
    }

    public PhysicalRegisterFile.PhysicalRegister get(int dependency) {
        return this.entries.get(dependency);
    }

    public void put(int dependency, PhysicalRegisterFile.PhysicalRegister physicalRegister) {
        this.entries.put(dependency, physicalRegister);
    }

    public String getName() {
        return name;
    }
}
