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

import java.util.ArrayList;
import java.util.List;

/**
 * Physical register.
 *
 * @author Min Cai
 */
public class PhysicalRegister {
    private PhysicalRegisterFile physicalRegisterFile;
    private PhysicalRegisterState state;
    private int dependency;
    private List<ReorderBufferEntry> effectiveAddressComputationOperandDependents;
    private List<LoadStoreQueueEntry> storeAddressDependents;
    private List<AbstractReorderBufferEntry> dependents;

    /**
     * Create a physical register.
     *
     * @param physicalRegisterFile the parent physical register file
     */
    public PhysicalRegister(PhysicalRegisterFile physicalRegisterFile) {
        this.physicalRegisterFile = physicalRegisterFile;
        this.state = PhysicalRegisterState.AVAILABLE;
        this.effectiveAddressComputationOperandDependents = new ArrayList<>();
        this.storeAddressDependents = new ArrayList<>();
        this.dependents = new ArrayList<>();
    }

    /**
     * Reserve for the specified dependency.
     *
     * @param dependency the dependency
     */
    public void reserve(int dependency) {
        if (this.state != PhysicalRegisterState.AVAILABLE) {
            throw new IllegalArgumentException();
        }

        this.dependency = dependency;
        this.state = PhysicalRegisterState.ARCHITECTURAL_REGISTER;

        this.physicalRegisterFile.setNumFreePhysicalRegisters(this.physicalRegisterFile.getNumFreePhysicalRegisters() - 1);
    }

    /**
     * Allocate for the specified dependency.
     *
     * @param dependency the dependency
     */
    public void allocate(int dependency) {
        if (this.state != PhysicalRegisterState.AVAILABLE) {
            throw new IllegalArgumentException();
        }

        this.dependency = dependency;
        this.state = PhysicalRegisterState.RENAME_BUFFER_NOT_VALID;

        this.physicalRegisterFile.setNumFreePhysicalRegisters(this.physicalRegisterFile.getNumFreePhysicalRegisters() - 1);
    }

    /**
     * Write back.
     */
    public void writeback() {
        if (this.state != PhysicalRegisterState.RENAME_BUFFER_NOT_VALID) {
            throw new IllegalArgumentException();
        }

        this.state = PhysicalRegisterState.RENAME_BUFFER_VALID;

        for (ReorderBufferEntry effectiveAddressComputationOperandDependent : this.effectiveAddressComputationOperandDependents) {
            effectiveAddressComputationOperandDependent.setEffectiveAddressComputationOperandReady(true);
        }

        for (LoadStoreQueueEntry storeAddressDependent : this.storeAddressDependents) {
            storeAddressDependent.setStoreAddressReady(true);
        }

        for (AbstractReorderBufferEntry dependent : this.dependents) {
            dependent.setNumNotReadyOperands(dependent.getNumNotReadyOperands() - 1);
        }

        this.effectiveAddressComputationOperandDependents.clear();
        this.storeAddressDependents.clear();
        this.dependents.clear();
    }

    /**
     * Commit.
     */
    public void commit() {
        if (this.state != PhysicalRegisterState.RENAME_BUFFER_VALID) {
            throw new IllegalArgumentException();
        }

        this.state = PhysicalRegisterState.ARCHITECTURAL_REGISTER;
    }

    /**
     * Recover.
     */
    public void recover() {
        if (this.state != PhysicalRegisterState.RENAME_BUFFER_NOT_VALID && this.state != PhysicalRegisterState.RENAME_BUFFER_VALID) {
            throw new IllegalArgumentException();
        }

        this.dependency = -1;
        this.state = PhysicalRegisterState.AVAILABLE;

        this.physicalRegisterFile.setNumFreePhysicalRegisters(this.physicalRegisterFile.getNumFreePhysicalRegisters() + 1);
    }

    /**
     * Reclaim.
     */
    public void reclaim() {
        if (this.state != PhysicalRegisterState.ARCHITECTURAL_REGISTER) {
            throw new IllegalArgumentException();
        }

        this.dependency = -1;
        this.state = PhysicalRegisterState.AVAILABLE;

        this.physicalRegisterFile.setNumFreePhysicalRegisters(this.physicalRegisterFile.getNumFreePhysicalRegisters() + 1);
    }

    /**
     * Get a value indicating whether the physical register is ready or not.
     *
     * @return a value indicating whether the physical register is ready or not
     */
    public boolean isReady() {
        return this.state == PhysicalRegisterState.RENAME_BUFFER_VALID || this.state == PhysicalRegisterState.ARCHITECTURAL_REGISTER;
    }

    /**
     * Get the parent physical register file.
     *
     * @return the parent physical register file
     */
    public PhysicalRegisterFile getPhysicalRegisterFile() {
        return physicalRegisterFile;
    }

    /**
     * Get the current dependency.
     *
     * @return the current dependency
     */
    public int getDependency() {
        return dependency;
    }

    /**
     * Get the state of the physical register.
     *
     * @return the state of the physical register
     */
    public PhysicalRegisterState getState() {
        return state;
    }

    /**
     * Get the list of dependents.
     *
     * @return the list of dependents
     */
    public List<AbstractReorderBufferEntry> getDependents() {
        return dependents;
    }

    /**
     * Get the list of store address dependents.
     *
     * @return the list of store address dependents
     */
    public List<LoadStoreQueueEntry> getStoreAddressDependents() {
        return storeAddressDependents;
    }

    /**
     * Get the list of effective address computation operand dependents.
     *
     * @return the list of effective address computation operand dependents
     */
    public List<ReorderBufferEntry> getEffectiveAddressComputationOperandDependents() {
        return effectiveAddressComputationOperandDependents;
    }
}
