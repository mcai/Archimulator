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
package archimulator.sim.uncore.dram;

import archimulator.sim.uncore.MemoryHierarchy;
import net.pickapack.action.Action;
import net.pickapack.math.Counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic memory controller.
 *
 * @author Min Cai
 */
public class BasicMemoryController extends MemoryController {
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

        this.banks = new ArrayList<Bank>();

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
    protected void access(int address, final Action onCompletedCallback) {
        final Counter counterPending = new Counter(0);

        int offset = 0;

        int size = this.getLineSize();

        while (size > 0) {
            size -= this.getBusWidth();

            final int currentAddress = address + offset;
            this.getCycleAccurateEventQueue().schedule(this, new Action() {
                @Override
                public void apply() {
                    accessDram(currentAddress, new Action() {
                        @Override
                        public void apply() {
                            counterPending.decrement();

                            if (counterPending.getValue() == 0) {
                                onCompletedCallback.apply();
                            }
                        }
                    });
                }
            }, this.getToDramLatency());

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
    private void accessDram(int address, final Action onCompletedCallback) {
        final int targetRow = (address >> this.rowBits) / this.getNumBanks();
        final int targetBank = (address >> this.rowBits) % this.getNumBanks();

        final boolean contiguous = (targetBank == previousBank);

        this.banks.get(targetBank).beginAccess(targetRow, contiguous, new Action() {
            @Override
            public void apply() {
                getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getFromDramLatency());
            }
        });

        previousBank = targetBank;
    }

    /**
     * Get the "to DRAM" latency.
     *
     * @return the "to DRAM" latency
     */
    public int getToDramLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerToDramLatency();
    }

    /**
     * Get the "from DRAM" latency.
     *
     * @return the "from DRAM" latency
     */
    public int getFromDramLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerFromDramLatency();
    }

    /**
     * Get the precharge latency.
     *
     * @return the precharge latency
     */
    public int getPrechargeLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerPrechargeLatency();
    }

    /**
     * Get the closed latency.
     *
     * @return the closed latency
     */
    public int getClosedLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerClosedLatency();
    }

    /**
     * Get the conflict latency.
     *
     * @return the conflict latency
     */
    public int getConflictLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerConflictLatency();
    }

    /**
     * Get the bus width.
     *
     * @return the bus width
     */
    public int getBusWidth() {
        return getExperiment().getArchitecture().getBasicMemoryControllerBusWidth();
    }

    /**
     * Get the number of banks.
     *
     * @return the number of banks
     */
    public int getNumBanks() {
        return getExperiment().getArchitecture().getBasicMemoryControllerNumBanks();
    }

    /**
     * Get the size of a row.
     *
     * @return the size of a row
     */
    public int getRowSize() {
        return getExperiment().getArchitecture().getBasicMemoryControllerRowSize();
    }

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
            private Action onCompletedCallback;

            /**
             * Create a pending access on the specified row of the bank.
             *
             * @param row                 the row
             * @param contiguous          a value indicating whether access is contiguous or not
             * @param onCompletedCallback the callback action performed when the access is completed
             */
            private PendingAccess(int row, boolean contiguous, Action onCompletedCallback) {
                this.row = row;
                this.contiguous = contiguous;
                this.onCompletedCallback = onCompletedCallback;
            }

            /**
             * Complete the pending access.
             */
            private void complete() {
                this.onCompletedCallback.apply();
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
            this.pendingAccesses = new ArrayList<PendingAccess>();
        }

        /**
         * Act on a precharge.
         *
         * @param onCompletedCallback the callback action performed when the precharge is completed
         */
        private void precharge(final Action onCompletedCallback) {
            getCycleAccurateEventQueue().schedule(this, new Action() {
                @Override
                public void apply() {
                    status = BankStatus.PRECHARGED;
                    onCompletedCallback.apply();
                }
            }, getClosedLatency());
        }

        /**
         * Refresh the bank.
         */
        private void refresh() {
            if (this.pendingAccesses.size() > 0) {
                final PendingAccess pendingAccess = this.pendingAccesses.get(0);

                access(pendingAccess.row, pendingAccess.contiguous, new Action() {
                    @Override
                    public void apply() {
                        pendingAccess.complete();
                        pendingAccesses.remove(0);

                        refresh();
                    }
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
        private void beginAccess(int row, boolean contiguous, Action onCompletedCallback) {
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
        private void access(final int row, final boolean contiguous, final Action onCompletedCallback) {
            if (this.status == BankStatus.CLOSED) {
                this.precharge(new Action() {
                    @Override
                    public void apply() {
                        access(row, contiguous, onCompletedCallback);
                    }
                });
            } else {
                if (currentRow == row) {
                    if (contiguous) {
                        getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getFromDramLatency());
                    } else {
                        getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getPrechargeLatency() + getFromDramLatency());
                    }
                } else {
                    getCycleAccurateEventQueue().schedule(this, new Action() {
                        @Override
                        public void apply() {
                            currentRow = row;

                            precharge(new Action() {
                                public void apply() {
                                    access(row, contiguous, onCompletedCallback);
                                }
                            });
                        }
                    }, getConflictLatency());
                }
            }
        }
    }
}
