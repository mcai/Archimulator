/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.core.functionalUnit;

import java.util.EnumMap;

/**
 * Functional unit descriptor.
 *
 * @author Min Cai
 */
public class FunctionalUnitDescriptor {
    private FunctionalUnitPool functionalUnitPool;
    private FunctionalUnitType type;
    private int quantity;
    private int numFree;

    private EnumMap<FunctionalUnitOperationType, FunctionalUnitOperation> operations;

    /**
     * Create a functional unit descriptor.
     *
     * @param functionalUnitPool the functional unit pool
     * @param type               the functional unit type
     * @param quantity           the quantity
     */
    public FunctionalUnitDescriptor(FunctionalUnitPool functionalUnitPool, FunctionalUnitType type, int quantity) {
        this.functionalUnitPool = functionalUnitPool;
        this.type = type;
        this.quantity = quantity;
        this.numFree = this.quantity;

        this.operations = new EnumMap<>(FunctionalUnitOperationType.class);
    }

    /**
     * Add a functional unit operation definition.
     *
     * @param functionalUnitOperationType the functional unit operation type
     * @param operationLatency            the operation latency in cycles
     * @param issueLatency                the operation latency in cycles
     * @return the functional unit descriptor itself
     */
    public FunctionalUnitDescriptor addFunctionalUnitOperation(FunctionalUnitOperationType functionalUnitOperationType, int operationLatency, int issueLatency) {
        this.operations.put(functionalUnitOperationType, new FunctionalUnitOperation(operationLatency, issueLatency));
        this.functionalUnitPool.getFunctionalUnitOperationToFunctionalUnitMap().put(functionalUnitOperationType, this.type);
        return this;
    }

    /**
     * Release all.
     */
    public void releaseAll() {
        this.numFree = this.quantity;
    }

    /**
     * Get a value indicating whether it is full or not.
     *
     * @return a value indicating whether it is full or not
     */
    public boolean isFull() {
        return this.numFree == 0;
    }

    /**
     * Get the number of free slots.
     *
     * @return the number of free slots
     */
    public int getNumFree() {
        return numFree;
    }

    /**
     * Set the number of free slots.
     *
     * @param numFree the number of free slots
     */
    public void setNumFree(int numFree) {
        this.numFree = numFree;
    }

    /**
     * Get the map of operations.
     *
     * @return the map of operations
     */
    public EnumMap<FunctionalUnitOperationType, FunctionalUnitOperation> getOperations() {
        return operations;
    }
}
