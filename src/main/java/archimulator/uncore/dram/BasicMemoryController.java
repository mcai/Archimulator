/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.dram;

import archimulator.common.SimulationEvent;
import archimulator.uncore.MemoryHierarchy;
import archimulator.util.math.Counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic memory controller.
 *
 * @author Min Cai
 */
public class BasicMemoryController extends MemoryController {
    /**
     * Bank status.
     */
    private enum BankStatus {
        /**
         * Closed.
         */
        CLOSED,

        /**
         * Precharged.
         */
        PRECHARGED
    }

    /**
     * Bank.
     */
    private class Bank {
        /**
         * Pending access.
         */
        private class PendingAccess {
            private int row;
            private boolean contiguous;
            private Runnable onCompletedCallback;

            /**
             * Create a pending access on the specified row of the bank.
             *
             * @param row                 the row
             * @param contiguous          a value indicating whether access is contiguous or not
             * @param onCompletedCallback the callback action performed when the access is completed
             */
            private PendingAccess(int row, boolean contiguous, Runnable onCompletedCallback) {
                this.row = row;
                this.contiguous = contiguous;
                this.onCompletedCallback = onCompletedCallback;
            }

            /**
             * Complete the pending access.
             */
            private void complete() {
                this.onCompletedCallback.run();
            }
        }

        private BankStatus status;
        private int currentRow;
        private List<PendingAccess> pendingAccesses;

        /**
         * Create a bank.
         */
        private Bank() {
            this.status = BankStatus.CLOSED;
            this.currentRow = -1;
            this.pendingAccesses = new ArrayList<>();
        }

        /**
         * Act on a precharge.
         *
         * @param onCompletedCallback the callback action performed when the precharge is completed
         */
        private void precharge(final Runnable onCompletedCallback) {
            getCycleAccurateEventQueue().schedule(this, () -> {
                status = BankStatus.PRECHARGED;
                onCompletedCallback.run();
            }, getClosedLatency());
        }

        /**
         * Refresh the bank.
         */
        private void refresh() {
            if (this.pendingAccesses.size() > 0) {
                final PendingAccess pendingAccess = this.pendingAccesses.get(0);

                access(pendingAccess.row, pendingAccess.contiguous, () -> {
                    pendingAccess.complete();
                    pendingAccesses.remove(0);

                    refresh();
                });
            }
        }

        /**
         * Begin the access on the specified row of the bank.
         *
         * @param row                 the row
         * @param contiguous          a value indicating whether the access is contiguous or not
         * @param onCompletedCallback the callback action performed when the access is completed
         */
        private void beginAccess(int row, boolean contiguous, Runnable onCompletedCallback) {
            final PendingAccess pendingAccess = new PendingAccess(row, contiguous, onCompletedCallback);
            this.pendingAccesses.add(pendingAccess);

            if (this.pendingAccesses.size() == 1) {
                this.refresh();
            }
        }

        /**
         * Access the specified row of the bank.
         *
         * @param row                 the row
         * @param contiguous          a value indicating whether the access is contiguous or not
         * @param onCompletedCallback the callback action performed when the access is completed
         */
        private void access(final int row, final boolean contiguous, final Runnable onCompletedCallback) {
            if (this.status == BankStatus.CLOSED) {
                this.precharge(() -> access(row, contiguous, onCompletedCallback));
            } else {
                if (currentRow == row) {
                    if (contiguous) {
                        getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getFromDramLatency());
                    } else {
                        getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getPrechargeLatency() + getFromDramLatency());
                    }
                } else {
                    getCycleAccurateEventQueue().schedule(this, () -> {
                        currentRow = row;
                        precharge(() -> access(row, contiguous, onCompletedCallback));
                    }, getConflictLatency());
                }
            }
        }
    }

    /**
     * Access event.
     */
    protected abstract class AccessEvent extends SimulationEvent {
        private int address;
        private int bank;

        /**
         * Create an access event.
         *
         * @param address the address
         * @param bank the bank
         */
        public AccessEvent(int address, int bank) {
            super(BasicMemoryController.this);
            this.address = address;
            this.bank = bank;
        }

        /**
         * Get the address.
         *
         * @return the address
         */
        public int getAddress() {
            return address;
        }

        /**
         * Get the bank.
         *
         * @return the bank
         */
        public int getBank() {
            return bank;
        }
    }

    /**
     * Begin access event.
     */
    public class BeginAccessEvent extends AccessEvent {
        /**
         * Create a begin access event.
         *
         * @param address the address
         * @param bank    the bank
         */
        public BeginAccessEvent(int address, int bank) {
            super(address, bank);
        }

        @Override
        public String toString() {
            return String.format("BeginAccessEvent{address=0x%08x, bank=%d}", getAddress(), getBank());
        }
    }

    /**
     * End access event.
     */
    public class EndAccessEvent extends AccessEvent {
        /**
         * Create an end access event.
         *
         * @param address the address
         * @param bank    the bank
         */
        public EndAccessEvent(int address, int bank) {
            super(address, bank);
        }

        @Override
        public String toString() {
            return String.format("EndAccessEvent{address=0x%08x, bank=%d}", getAddress(), getBank());
        }
    }

    private int rowBits;
    private List<Bank> banks;
    private int previousBank = 0;

    /**
     * Create a basic memory controller.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    public BasicMemoryController(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);

        this.banks = new ArrayList<>();

        for (int i = 0; i < this.getNumBanks(); i++) {
            this.banks.add(new Bank());
        }

        int rowSize = this.getRowSize();

        this.rowBits = 0;

        while (rowSize > 0) {
            this.rowBits++;
            rowSize >>= 1;
        }
    }

    /**
     * Act on an access of the specified address.
     *
     * @param address             the address
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    @Override
    protected void access(int address, final Runnable onCompletedCallback) {
        final Counter counterPending = new Counter(0);

        int offset = 0;

        int size = this.getLineSize();

        while (size > 0) {
            size -= this.getBusWidth();

            final int currentAddress = address + offset;
            this.getCycleAccurateEventQueue().schedule(this, () -> accessDram(currentAddress, () -> {
                counterPending.decrement();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.run();
                }
            }), this.getToDramLatency());

            counterPending.increment();

            offset += this.getBusWidth();
        }
    }

    /**
     * Access the specified address of the DRAM.
     *
     * @param address             the address
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    private void accessDram(final int address, final Runnable onCompletedCallback) {
        final int targetRow = (address >> this.rowBits) / this.getNumBanks();
        final int targetBank = (address >> this.rowBits) % this.getNumBanks();

        final boolean contiguous = (targetBank == previousBank);

        getBlockingEventDispatcher().dispatch(new BeginAccessEvent(address, targetBank));

        this.banks.get(targetBank).beginAccess(targetRow, contiguous, () -> {
            getBlockingEventDispatcher().dispatch(new EndAccessEvent(address, targetBank));
            getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getFromDramLatency());
        });

        previousBank = targetBank;
    }

    /**
     * Get the "to DRAM" latency.
     *
     * @return the "to DRAM" latency
     */
    public int getToDramLatency() {
        return getExperiment().getConfig().getBasicMemoryControllerToDramLatency();
    }

    /**
     * Get the "from DRAM" latency.
     *
     * @return the "from DRAM" latency
     */
    public int getFromDramLatency() {
        return getExperiment().getConfig().getBasicMemoryControllerFromDramLatency();
    }

    /**
     * Get the precharge latency.
     *
     * @return the precharge latency
     */
    public int getPrechargeLatency() {
        return getExperiment().getConfig().getBasicMemoryControllerPrechargeLatency();
    }

    /**
     * Get the closed latency.
     *
     * @return the closed latency
     */
    public int getClosedLatency() {
        return getExperiment().getConfig().getBasicMemoryControllerClosedLatency();
    }

    /**
     * Get the conflict latency.
     *
     * @return the conflict latency
     */
    public int getConflictLatency() {
        return getExperiment().getConfig().getBasicMemoryControllerConflictLatency();
    }

    /**
     * Get the bus width.
     *
     * @return the bus width
     */
    public int getBusWidth() {
        return getExperiment().getConfig().getBasicMemoryControllerBusWidth();
    }

    /**
     * Get the number of banks.
     *
     * @return the number of banks
     */
    public int getNumBanks() {
        return getExperiment().getConfig().getBasicMemoryControllerNumBanks();
    }

    /**
     * Get the size of a row.
     *
     * @return the size of a row
     */
    public int getRowSize() {
        return getExperiment().getConfig().getBasicMemoryControllerRowSize();
    }
}
