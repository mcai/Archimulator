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
package archimulator.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical register file.
 *
 * @author Min Cai
 */
public class PhysicalRegisterFile {
    private String name;
    private List<PhysicalRegister> registers;

    private int numFreePhysicalRegisters;

    /**
     * Create a physical register file.
     *
     * @param name     the name of the physical register file
     * @param capacity the capacity
     */
    public PhysicalRegisterFile(String name, int capacity) {
        this.name = name;

        this.registers = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            this.registers.add(new PhysicalRegister(this));
        }

        this.setNumFreePhysicalRegisters(capacity);
    }

    /**
     * Allocate a physical register for the specified dependency.
     *
     * @param dependency the dependency
     * @return the newly allocated physical register for the specified dependency
     */
    public PhysicalRegister allocate(int dependency) {
        for (PhysicalRegister physReg : this.registers) {
            if (physReg.getState() == PhysicalRegisterState.AVAILABLE) {
                physReg.allocate(dependency);
                return physReg;
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Get a value indicating whether the physical register file is full or not.
     *
     * @return a value indicating whether the physical register file is full or not
     */
    public boolean isFull() {
        return this.numFreePhysicalRegisters == 0;
    }

    /**
     * Get the name of the physical register file.
     *
     * @return the name of the physical register file
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of physical registers.
     *
     * @return the list of physical registers
     */
    public List<PhysicalRegister> getRegisters() {
        return registers;
    }

    /**
     * Get the number of free physical registers.
     *
     * @return the number of free physical registers
     */
    public int getNumFreePhysicalRegisters() {
        return numFreePhysicalRegisters;
    }

    /**
     * Set the number of free physical registers.
     *
     * @param numFreePhysicalRegisters the number of free physical registers
     */
    public void setNumFreePhysicalRegisters(int numFreePhysicalRegisters) {
        this.numFreePhysicalRegisters = numFreePhysicalRegisters;
    }
}
