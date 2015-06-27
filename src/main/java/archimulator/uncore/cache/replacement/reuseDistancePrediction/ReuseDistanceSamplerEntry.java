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
package archimulator.uncore.cache.replacement.reuseDistancePrediction;

/**
 * Reuse distance sampler entry.
 *
 * @author Min Cai
 */
public class ReuseDistanceSamplerEntry {
    private boolean valid;
    private int threadId;
    private int pc;
    private int address;

    /**
     * Create a reuse distance sampler entry.
     */
    public ReuseDistanceSamplerEntry() {
        this.valid = false;
        this.threadId = -1;
        this.pc = -1;
        this.address = -1;
    }

    /**
     * Get a value indicating whether the entry is valid or not.
     *
     * @return a value indicating whether the entry is valid or not
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Set a value indicating whether the entry is valid or not.
     *
     * @param valid a value indicating whether the entry is valid or not
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Get the thread ID.
     *
     * @return the thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Set the thread ID.
     *
     * @param threadId the thread ID
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     * Get the value of the program counter (PC).
     *
     * @return the value of the program counter (PC)
     */
    public int getPc() {
        return pc;
    }

    /**
     * Set the value of the program counter (PC).
     *
     * @param pc the value of the program counter (PC)
     */
    public void setPc(int pc) {
        this.pc = pc;
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
     * Set the address.
     *
     * @param address the address
     */
    public void setAddress(int address) {
        this.address = address;
    }
}
