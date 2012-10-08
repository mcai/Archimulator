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
package archimulator.sim.uncore.dram;

import archimulator.sim.uncore.CacheHierarchy;
import net.pickapack.action.Action;
import net.pickapack.math.Counter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class BasicMemoryController extends MemoryController {
    private int rowBits;
    private List<Bank> banks;
    private int previousBank = 0;

    /**
     *
     * @param cacheHierarchy
     */
    public BasicMemoryController(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy);

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
     *
     * @param address
     * @param onCompletedCallback
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
                            counterPending.dec();

                            if (counterPending.getValue() == 0) {
                                onCompletedCallback.apply();
                            }
                        }
                    });
                }
            }, this.getToDramLatency());

            counterPending.inc();

            offset += this.getBusWidth();
        }
    }

    private void accessDram(int addr, final Action onCompletedCallback) {
        final int targetRow = (addr >> this.rowBits) / this.getNumBanks();
        final int targetBank = (addr >> this.rowBits) % this.getNumBanks();

        final boolean contiguous = (targetBank == previousBank);

        this.banks.get(targetBank).startAccess(targetRow, contiguous, new Action() {
            @Override
            public void apply() {
                getCycleAccurateEventQueue().schedule(this, onCompletedCallback, getFromDramLatency());
            }
        });

        previousBank = targetBank;
    }

    /**
     *
     * @return
     */
    public int getToDramLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerToDramLatency();
    }

    /**
     *
     * @return
     */
    public int getFromDramLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerFromDramLatency();
    }

    /**
     *
     * @return
     */
    public int getPrechargeLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerPrechargeLatency();
    }

    /**
     *
     * @return
     */
    public int getClosedLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerClosedLatency();
    }

    /**
     *
     * @return
     */
    public int getConflictLatency() {
        return getExperiment().getArchitecture().getBasicMemoryControllerConflictLatency();
    }

    /**
     *
     * @return
     */
    public int getBusWidth() {
        return getExperiment().getArchitecture().getBasicMemoryControllerBusWidth();
    }

    /**
     *
     * @return
     */
    public int getNumBanks() {
        return getExperiment().getArchitecture().getBasicMemoryControllerNumBanks();
    }

    /**
     *
     * @return
     */
    public int getRowSize() {
        return getExperiment().getArchitecture().getBasicMemoryControllerRowSize();
    }

    private enum BankStatus {
        CLOSED,
        PRECHARGED
    }

    private class Bank {
        private class PendingAccess {
            private int row;
            private boolean contiguous;
            private Action onCompletedCallback;

            private PendingAccess(int row, boolean contiguous, Action onCompletedCallback) {
                this.row = row;
                this.contiguous = contiguous;
                this.onCompletedCallback = onCompletedCallback;
            }

            private void complete() {
                this.onCompletedCallback.apply();
            }
        }

        private BankStatus status;
        private int currentRow;
        private List<PendingAccess> pendingAccesses;

        private Bank() {
            this.status = BankStatus.CLOSED;
            this.currentRow = -1;
            this.pendingAccesses = new ArrayList<PendingAccess>();
        }

        private void precharge(final Action onCompletedCallback) {
            getCycleAccurateEventQueue().schedule(this, new Action() {
                @Override
                public void apply() {
                    status = BankStatus.PRECHARGED;
                    onCompletedCallback.apply();
                }
            }, getClosedLatency());
        }

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

        private void startAccess(int row, boolean contiguous, Action onCompletedCallback) {
            final PendingAccess pendingAccess = new PendingAccess(row, contiguous, onCompletedCallback);
            this.pendingAccesses.add(pendingAccess);

            if (this.pendingAccesses.size() == 1) {
                this.refresh();
            }
        }

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
