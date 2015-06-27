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
package archimulator.core.functionalUnit;

import archimulator.common.Named;
import archimulator.core.AbstractBasicCore;
import archimulator.core.ReorderBufferEntry;
import archimulator.util.action.Action;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Functional unit pool.
 *
 * @author Min Cai
 */
public class FunctionalUnitPool implements Named {
    private EnumMap<FunctionalUnitType, FunctionalUnitDescriptor> descriptors;
    private EnumMap<FunctionalUnitOperationType, FunctionalUnitType> functionalUnitOperationToFunctionalUnitMap;
    private AbstractBasicCore core;

    private EnumMap<FunctionalUnitType, Long> numStallsOnNoFreeFunctionalUnit;
    private EnumMap<FunctionalUnitOperationType, Long> numStallsOnAcquireFailedOnNoFreeFunctionalUnit;

    /**
     * Create a functional unit pool.
     *
     * @param core the core
     */
    public FunctionalUnitPool(AbstractBasicCore core) {
        this.core = core;

        this.descriptors = new EnumMap<>(FunctionalUnitType.class);

        this.functionalUnitOperationToFunctionalUnitMap = new EnumMap<>(FunctionalUnitOperationType.class);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.INTEGER_ALU, 8)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.INT_ALU, 2, 1);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.INTEGER_MULTIPLY_DIVIDE, 2)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.INT_MULTIPLY, 3, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.INT_DIVIDE, 20, 19);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.FLOAT_ADD, 8)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_ADD, 4, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_COMPARE, 4, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_CONVERT, 4, 1);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.FLOAT_MULTIPLY_DIVIDE, 2)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_MULTIPLY, 8, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_DIVIDE, 40, 20)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_SQRT, 80, 40);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.MEMORY_PORT, 4)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.READ_PORT, 1, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.WRITE_PORT, 1, 1);

        this.numStallsOnNoFreeFunctionalUnit = new EnumMap<>(FunctionalUnitType.class);
        EnumSet<FunctionalUnitType> fuTypes = EnumSet.allOf(FunctionalUnitType.class);
        for (FunctionalUnitType fuTye : fuTypes) {
            this.numStallsOnNoFreeFunctionalUnit.put(fuTye, 0L);
        }

        this.numStallsOnAcquireFailedOnNoFreeFunctionalUnit = new EnumMap<>(FunctionalUnitOperationType.class);
        EnumSet<FunctionalUnitOperationType> fuOperationTypes = EnumSet.allOf(FunctionalUnitOperationType.class);
        for (FunctionalUnitOperationType fuOperationType : fuOperationTypes) {
            this.numStallsOnAcquireFailedOnNoFreeFunctionalUnit.put(fuOperationType, 0L);
        }
    }

    /**
     * Add a functional unit descriptor.
     *
     * @param type     the functional unit type
     * @param quantity the quantity
     * @return the newly added functional unit descriptor
     */
    private FunctionalUnitDescriptor addFunctionalUnitDescriptor(FunctionalUnitType type, int quantity) {
        FunctionalUnitDescriptor desc = new FunctionalUnitDescriptor(this, type, quantity);
        this.descriptors.put(type, desc);
        return desc;
    }

    /**
     * Acquire.
     *
     * @param reorderBufferEntry  the reorder buffer entry
     * @param onCompletedCallback the callback action performed when the operation is completed
     * @return a value indicating whether the acquiring succeeds or not
     */
    public boolean acquire(final ReorderBufferEntry reorderBufferEntry, final Action onCompletedCallback) {
        FunctionalUnitOperationType functionalUnitOperationType = reorderBufferEntry.getDynamicInstruction().getStaticInstruction().getMnemonic().getFunctionalUnitOperationType();
        FunctionalUnitType functionalUnitType = this.functionalUnitOperationToFunctionalUnitMap.get(functionalUnitOperationType);
        FunctionalUnitOperation functionalUnitOperation = this.descriptors.get(functionalUnitType).getOperations().get(functionalUnitOperationType);

        final FunctionalUnitDescriptor functionalUnitDescriptor = this.descriptors.get(functionalUnitType);

        if (functionalUnitDescriptor.isFull()) {
            this.numStallsOnAcquireFailedOnNoFreeFunctionalUnit.put(
                    functionalUnitOperationType,
                    this.numStallsOnAcquireFailedOnNoFreeFunctionalUnit.get(functionalUnitOperationType) + 1
            );

            return false;
        }

        this.core.getCycleAccurateEventQueue()
                .schedule(this, () -> functionalUnitDescriptor.setNumFree(functionalUnitDescriptor.getNumFree() + 1), functionalUnitOperation.getIssueLatency())
                .schedule(this, () -> {
                    if (!reorderBufferEntry.isSquashed()) {
                        onCompletedCallback.apply();
                    }
                }, functionalUnitOperation.getOperationLatency());

        functionalUnitDescriptor.setNumFree(functionalUnitDescriptor.getNumFree() - 1);

        return true;
    }

    /**
     * Release all functional unit descriptors.
     */
    public void releaseAll() {
        this.descriptors.values().forEach(FunctionalUnitDescriptor::releaseAll);
    }

    /**
     * Update statistics per cycle.
     */
    public void updatePerCycleStats() {
        FunctionalUnitPool.this.numStallsOnNoFreeFunctionalUnit.keySet().stream().filter(fuType -> this.descriptors.get(fuType).isFull()).forEach(fuType -> {
            this.numStallsOnNoFreeFunctionalUnit.put(fuType, this.numStallsOnNoFreeFunctionalUnit.get(fuType) + 1);
        });
    }

    /**
     * Get the map of descriptors.
     *
     * @return the map of descriptors
     */
    public EnumMap<FunctionalUnitType, FunctionalUnitDescriptor> getDescriptors() {
        return descriptors;
    }

    /**
     * Get the map of functional unit operation types to functional unit types.
     *
     * @return the map of functional unit operation types to functional unit types
     */
    public EnumMap<FunctionalUnitOperationType, FunctionalUnitType> getFunctionalUnitOperationToFunctionalUnitMap() {
        return functionalUnitOperationToFunctionalUnitMap;
    }

    /**
     * Get the map of the numbers of stalls when there is no free functional unit for a specific functional unit type.
     *
     * @return the map of the numbers of stalls when there is no free functional unit for a specific functional unit type
     */
    public EnumMap<FunctionalUnitType, Long> getNumStallsOnNoFreeFunctionalUnit() {
        return numStallsOnNoFreeFunctionalUnit;
    }

    /**
     * Get the map of the numbers of stalls on acquiring failed when there is no free functional unit for a specific functional unit type.
     *
     * @return the map of the numbers of stalls on acquiring failed when there is no free functional unit for a specific functional unit type
     */
    public EnumMap<FunctionalUnitOperationType, Long> getNumStallsOnAcquireFailedOnNoFreeFunctionalUnit() {
        return numStallsOnAcquireFailedOnNoFreeFunctionalUnit;
    }

    @Override
    public String getName() {
        return core.getName() + "/functionalUnitPool";
    }
}
