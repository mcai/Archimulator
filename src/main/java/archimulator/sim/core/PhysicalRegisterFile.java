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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class PhysicalRegisterFile {
    private String name;
    private List<PhysicalRegister> registers;

    private int numFreePhysicalRegisters;

    /**
     *
     * @param name
     * @param capacity
     */
    public PhysicalRegisterFile(String name, int capacity) {
        this.name = name;

        this.registers = new ArrayList<PhysicalRegister>();
        for (int i = 0; i < capacity; i++) {
            this.registers.add(new PhysicalRegister());
        }

        this.numFreePhysicalRegisters = capacity;
    }

    /**
     *
     * @param dependency
     * @return
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
     *
     * @return
     */
    public boolean isFull() {
        return this.numFreePhysicalRegisters == 0;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public List<PhysicalRegister> getRegisters() {
        return registers;
    }

    /**
     *
     * @return
     */
    public int getNumFreePhysicalRegisters() {
        return numFreePhysicalRegisters;
    }

    /**
     *
     */
    public class PhysicalRegister {
        private PhysicalRegisterState state;

        private int dependency;

        private List<ReorderBufferEntry> effectiveAddressComputationOperandDependents;
        private List<LoadStoreQueueEntry> storeAddressDependents;
        private List<AbstractReorderBufferEntry> dependents;

        private PhysicalRegister() {
            this.state = PhysicalRegisterState.AVAILABLE;

            this.effectiveAddressComputationOperandDependents = new ArrayList<ReorderBufferEntry>();
            this.storeAddressDependents = new ArrayList<LoadStoreQueueEntry>();
            this.dependents = new ArrayList<AbstractReorderBufferEntry>();
        }

        /**
         *
         * @param dependency
         */
        public void reserve(int dependency) {
            if (this.state != PhysicalRegisterState.AVAILABLE) {
                throw new IllegalArgumentException();
            }

            this.dependency = dependency;
            this.state = PhysicalRegisterState.ARCHITECTURAL_REGISTER;

            numFreePhysicalRegisters--;
        }

        /**
         *
         * @param dependency
         */
        public void allocate(int dependency) {
            if (this.state != PhysicalRegisterState.AVAILABLE) {
                throw new IllegalArgumentException();
            }

            this.dependency = dependency;
            this.state = PhysicalRegisterState.RENAME_BUFFER_NOT_VALID;

            numFreePhysicalRegisters--;
        }

        /**
         *
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
         *
         */
        public void commit() {
            if (this.state != PhysicalRegisterState.RENAME_BUFFER_VALID) {
                throw new IllegalArgumentException();
            }

            this.state = PhysicalRegisterState.ARCHITECTURAL_REGISTER;
        }

        /**
         *
         */
        public void recover() {
            if (this.state != PhysicalRegisterState.RENAME_BUFFER_NOT_VALID && this.state != PhysicalRegisterState.RENAME_BUFFER_VALID) {
                throw new IllegalArgumentException();
            }

            this.dependency = -1;
            this.state = PhysicalRegisterState.AVAILABLE;

            numFreePhysicalRegisters++;
        }

        /**
         *
         */
        public void reclaim() {
            if (this.state != PhysicalRegisterState.ARCHITECTURAL_REGISTER) {
                throw new IllegalArgumentException();
            }

            this.dependency = -1;
            this.state = PhysicalRegisterState.AVAILABLE;

            numFreePhysicalRegisters++;
        }

        /**
         *
         * @return
         */
        public boolean isReady() {
            return this.state == PhysicalRegisterState.RENAME_BUFFER_VALID || this.state == PhysicalRegisterState.ARCHITECTURAL_REGISTER;
        }

        /**
         *
         * @return
         */
        public int getDependency() {
            return dependency;
        }

        /**
         *
         * @return
         */
        public PhysicalRegisterState getState() {
            return state;
        }

        /**
         *
         * @return
         */
        public List<AbstractReorderBufferEntry> getDependents() {
            return dependents;
        }

        /**
         *
         * @return
         */
        public List<LoadStoreQueueEntry> getStoreAddressDependents() {
            return storeAddressDependents;
        }

        /**
         *
         * @return
         */
        public List<ReorderBufferEntry> getEffectiveAddressComputationOperandDependents() {
            return effectiveAddressComputationOperandDependents;
        }
    }
}
