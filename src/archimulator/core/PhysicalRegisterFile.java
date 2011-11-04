/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core;

import java.util.ArrayList;
import java.util.List;

public class PhysicalRegisterFile {
    private String name;
    private List<PhysicalRegister> entries;

    private int numFreePhysicalRegs;

    public PhysicalRegisterFile(String name, int capacity) {
        this.name = name;

        this.entries = new ArrayList<PhysicalRegister>();
        for (int i = 0; i < capacity; i++) {
            this.entries.add(new PhysicalRegister(this));
        }

        this.numFreePhysicalRegs = capacity;
    }

    public PhysicalRegister allocate(int dep) {
        for (PhysicalRegister physReg : this.entries) {
            if (physReg.getState() == PhysicalRegisterState.AVAILABLE) {
                physReg.allocate(dep);
                return physReg;
            }
        }

        throw new IllegalArgumentException();
    }

    public boolean isFull() {
        return this.numFreePhysicalRegs == 0;
    }

    public String getName() {
        return name;
    }

    public List<PhysicalRegister> getEntries() {
        return entries;
    }

    public int getNumFreePhysicalRegs() {
        return numFreePhysicalRegs;
    }

    public void setNumFreePhysicalRegs(int numFreePhysicalRegs) {
        this.numFreePhysicalRegs = numFreePhysicalRegs;
    }
}
