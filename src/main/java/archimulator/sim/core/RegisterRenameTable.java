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
    private final String name;
    private final Map<Integer, PhysicalRegister> entries;

    public RegisterRenameTable(String name) {
        this.name = name;
        this.entries = new TreeMap<Integer, PhysicalRegister>();
    }

    public PhysicalRegister get(int dep) {
        return this.entries.get(dep);
    }

    public void put(int dep, PhysicalRegister physReg) {
        this.entries.put(dep, physReg);
    }

    public String getName() {
        return name;
    }
}
