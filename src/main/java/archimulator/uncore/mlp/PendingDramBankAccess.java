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
package archimulator.uncore.mlp;

/**
 * Pending DRAM bank access.
 *
 * @author Min Cai
 */
public class PendingDramBankAccess {
    private int address;
    private int bank;
    private long beginCycle;
    private long endCycle;
    private double blpCost;

    /**
     * Create a pending DRAM bank access.
     *
     * @param address the address
     * @param bank the bank
     * @param beginCycle the time in cycles when the access begins
     */
    public PendingDramBankAccess(int address, int bank, long beginCycle) {
        this.address = address;
        this.bank = bank;
        this.beginCycle = beginCycle;
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

    /**
     * Get the time in cycles when the access begins.
     *
     * @return the time in cycles when the access begins
     */
    public long getBeginCycle() {
        return beginCycle;
    }

    /**
     * Get the time in cycles when the access ends.
     *
     * @return the time in cycles when the access ends
     */
    public long getEndCycle() {
        return endCycle;
    }

    /**
     * Set the time in cycles when the access ends.
     *
     * @param endCycle the time in cycles when the access ends
     */
    public void setEndCycle(long endCycle) {
        this.endCycle = endCycle;
    }

    /**
     * Get the time in cycles spent servicing the access.
     *
     * @return the time in cycles spent servicing the access
     */
    public int getNumCycles() {
        return (int) (this.endCycle - this.beginCycle);
    }

    /**
     * Get the DRAM BLP-cost.
     *
     * @return the DRAM BLP-cost
     */
    public double getBlpCost() {
        return blpCost;
    }

    /**
     * Set the DRAM BLP-cost.
     *
     * @param blpCost the DRAM BLP-cost
     */
    public void setBlpCost(double blpCost) {
        this.blpCost = blpCost;
    }

    @Override
    public String toString() {
        return String.format("PendingDramBankAccess{address=0x%08x, bank=%d, beginCycle=%d, endCycle=%d, blpCost=%s}", address, bank, beginCycle, endCycle, blpCost);
    }
}
