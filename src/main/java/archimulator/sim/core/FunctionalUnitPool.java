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

import archimulator.sim.common.Named;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 *
 * @author Min Cai
 */
public class FunctionalUnitPool implements Named {
    private EnumMap<FunctionalUnitType, FunctionalUnitDescriptor> descriptors;
    private EnumMap<FunctionalUnitOperationType, FunctionalUnitType> functionalUnitOperationToFunctionalUnitMap;
    private AbstractBasicCore core;

    private EnumMap<FunctionalUnitType, Long> noFreeFunctionalUnit;
    private EnumMap<FunctionalUnitOperationType, Long> acquireFailedOnNoFreeFunctionalUnit;

    private enum FunctionalUnitType {
        INTEGER_ALU,
        INTEGER_MULT_DIV,
        FP_ADDER,
        FP_MULT_DIV,
        MEMORY_PORT
    }

    private class FunctionalUnitDescriptor {
        private FunctionalUnitType type;
        private int quantity;
        private int numFree;

        private EnumMap<FunctionalUnitOperationType, FunctionalUnitOperation> operations;

        private FunctionalUnitDescriptor(FunctionalUnitType type, int quantity) {
            this.type = type;
            this.quantity = quantity;
            this.numFree = this.quantity;

            this.operations = new EnumMap<FunctionalUnitOperationType, FunctionalUnitOperation>(FunctionalUnitOperationType.class);
        }

        private FunctionalUnitDescriptor addFunctionalUnitOperation(FunctionalUnitOperationType fuOperationType, int operationLatency, int issueLatency) {
            this.operations.put(fuOperationType, new FunctionalUnitOperation(operationLatency, issueLatency));
            functionalUnitOperationToFunctionalUnitMap.put(fuOperationType, this.type);
            return this;
        }

        private void releaseAll() {
            this.numFree = this.quantity;
        }

        private boolean isFull() {
            return this.numFree == 0;
        }
    }

    private class FunctionalUnitOperation {
        private int operationLatency;
        private int issueLatency;

        private FunctionalUnitOperation(int operationLatency, int issueLatency) {
            this.operationLatency = operationLatency;
            this.issueLatency = issueLatency;
        }
    }

    /**
     *
     * @param core
     */
    public FunctionalUnitPool(AbstractBasicCore core) {
        this.core = core;

        this.descriptors = new EnumMap<FunctionalUnitType, FunctionalUnitDescriptor>(FunctionalUnitType.class);

        this.functionalUnitOperationToFunctionalUnitMap = new EnumMap<FunctionalUnitOperationType, FunctionalUnitType>(FunctionalUnitOperationType.class);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.INTEGER_ALU, 8)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.INT_ALU, 2, 1);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.INTEGER_MULT_DIV, 2)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.INT_MULTIPLY, 3, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.INT_DIVIDE, 20, 19);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.FP_ADDER, 8)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_ADD, 4, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_COMPARE, 4, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_CONVERT, 4, 1);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.FP_MULT_DIV, 2)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_MULTIPLY, 8, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_DIVIDE, 40, 20)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.FLOAT_SQRT, 80, 40);

        this.addFunctionalUnitDescriptor(FunctionalUnitType.MEMORY_PORT, 4)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.READ_PORT, 1, 1)
                .addFunctionalUnitOperation(FunctionalUnitOperationType.WRITE_PORT, 1, 1);

        this.noFreeFunctionalUnit = new EnumMap<FunctionalUnitType, Long>(FunctionalUnitType.class);
        EnumSet<FunctionalUnitType> fuTypes = EnumSet.allOf(FunctionalUnitType.class);
        for (FunctionalUnitType fuTye : fuTypes) {
            this.noFreeFunctionalUnit.put(fuTye, 0L);
        }

        this.acquireFailedOnNoFreeFunctionalUnit = new EnumMap<FunctionalUnitOperationType, Long>(FunctionalUnitOperationType.class);
        EnumSet<FunctionalUnitOperationType> fuOperationTypes = EnumSet.allOf(FunctionalUnitOperationType.class);
        for (FunctionalUnitOperationType fuOperationType : fuOperationTypes) {
            this.acquireFailedOnNoFreeFunctionalUnit.put(fuOperationType, 0L);
        }
    }

    private FunctionalUnitDescriptor addFunctionalUnitDescriptor(FunctionalUnitType type, int quantity) {
        FunctionalUnitDescriptor desc = new FunctionalUnitDescriptor(type, quantity);
        this.descriptors.put(type, desc);
        return desc;
    }

    /**
     *
     * @param reorderBufferEntry
     * @param onCompletedCallback
     * @return
     */
    public boolean acquire(final ReorderBufferEntry reorderBufferEntry, final Action1<ReorderBufferEntry> onCompletedCallback) {
        FunctionalUnitOperationType functionalUnitOperationType = reorderBufferEntry.getDynamicInstruction().getStaticInstruction().getMnemonic().getFunctionalUnitOperationType();
        FunctionalUnitType functionalUnitType = this.functionalUnitOperationToFunctionalUnitMap.get(functionalUnitOperationType);
        FunctionalUnitOperation functionalUnitOperation = this.descriptors.get(functionalUnitType).operations.get(functionalUnitOperationType);

        final FunctionalUnitDescriptor functionalUnitDescriptor = this.descriptors.get(functionalUnitType);

        if (functionalUnitDescriptor.isFull()) {
            this.acquireFailedOnNoFreeFunctionalUnit.put(functionalUnitOperationType, this.acquireFailedOnNoFreeFunctionalUnit.get(functionalUnitOperationType) + 1);

            return false;
        }

        this.core.getCycleAccurateEventQueue()
                .schedule(this, new Action() {
                    @Override
                    public void apply() {
                        functionalUnitDescriptor.numFree++;
                    }
                }, functionalUnitOperation.issueLatency)
                .schedule(this, new Action() {
                    @Override
                    public void apply() {
                        if (!reorderBufferEntry.isSquashed()) {
                            onCompletedCallback.apply(reorderBufferEntry);
                        }
                    }
                }, functionalUnitOperation.operationLatency);

        functionalUnitDescriptor.numFree--;

        return true;
    }

    /**
     *
     */
    public void releaseAll() {
        for (FunctionalUnitDescriptor fuDescriptor : this.descriptors.values()) {
            fuDescriptor.releaseAll();
        }
    }

    /**
     *
     */
    public void updatePerCycleStats() {
        for (FunctionalUnitType fuType : FunctionalUnitPool.this.noFreeFunctionalUnit.keySet()) {
            if (this.descriptors.get(fuType).isFull()) {
                this.noFreeFunctionalUnit.put(fuType, this.noFreeFunctionalUnit.get(fuType) + 1);
            }
        }
    }

    /**
     *
     * @return
     */
    public EnumMap<FunctionalUnitType, Long> getNoFreeFunctionalUnit() {
        return noFreeFunctionalUnit;
    }

    /**
     *
     * @return
     */
    public EnumMap<FunctionalUnitOperationType, Long> getAcquireFailedOnNoFreeFunctionalUnit() {
        return acquireFailedOnNoFreeFunctionalUnit;
    }

    @Override
    public String getName() {
        return core.getName() + "/functionalUnitPool";
    }
}
