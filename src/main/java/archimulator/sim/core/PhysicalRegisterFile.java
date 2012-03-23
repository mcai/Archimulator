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

import java.io.Serializable;
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
            this.entries.add(new PhysicalRegister());
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

    public class PhysicalRegister implements Serializable {
        private PhysicalRegisterState state;

        private int dep;

        private List<ReorderBufferEntry> effectiveAddressComputationOperandDependents;
        private List<LoadStoreQueueEntry> storeAddressDependents;
        private List<AbstractReorderBufferEntry> dependents;

        private PhysicalRegister() {
            this.state = PhysicalRegisterState.AVAILABLE;

            this.effectiveAddressComputationOperandDependents = new ArrayList<ReorderBufferEntry>();
            this.storeAddressDependents = new ArrayList<LoadStoreQueueEntry>();
            this.dependents = new ArrayList<AbstractReorderBufferEntry>();
        }

        public void reserve(int dep) {
            assert (this.state == PhysicalRegisterState.AVAILABLE);

            this.dep = dep;
            this.state = PhysicalRegisterState.ARCHITECTURAL_REGISTER;

            numFreePhysicalRegs--;
        }

        public void allocate(int dep) {
            assert (this.state == PhysicalRegisterState.AVAILABLE);

            this.dep = dep;
            this.state = PhysicalRegisterState.RENAME_BUFFER_NOT_VALID;

            numFreePhysicalRegs--;
        }

        public void writeback() {
            assert (this.state == PhysicalRegisterState.RENAME_BUFFER_NOT_VALID);

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

        public void commit() {
            assert (this.state == PhysicalRegisterState.RENAME_BUFFER_VALID);

            this.state = PhysicalRegisterState.ARCHITECTURAL_REGISTER;
        }

        public void recover() {
            assert (this.state == PhysicalRegisterState.RENAME_BUFFER_NOT_VALID || this.state == PhysicalRegisterState.RENAME_BUFFER_VALID);

            this.dep = -1;
            this.state = PhysicalRegisterState.AVAILABLE;

            numFreePhysicalRegs++;
        }

        public void reclaim() {
            assert (this.state == PhysicalRegisterState.ARCHITECTURAL_REGISTER);

            this.dep = -1;
            this.state = PhysicalRegisterState.AVAILABLE;

            numFreePhysicalRegs++;
        }

        public boolean isReady() {
            return this.state == PhysicalRegisterState.RENAME_BUFFER_VALID || this.state == PhysicalRegisterState.ARCHITECTURAL_REGISTER;
        }

        public int getDep() {
            return dep;
        }

        public PhysicalRegisterState getState() {
            return state;
        }

        public List<AbstractReorderBufferEntry> getDependents() {
            return dependents;
        }

        public List<LoadStoreQueueEntry> getStoreAddressDependents() {
            return storeAddressDependents;
        }

        public List<ReorderBufferEntry> getEffectiveAddressComputationOperandDependents() {
            return effectiveAddressComputationOperandDependents;
        }
    }
}
