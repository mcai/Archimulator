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
package archimulator.sim.uncore.helperThread.hotspot;

import archimulator.sim.analysis.Instruction;

/**
 * The load instruction entry.
 *
 * @author Min Cai
 */
public class LoadInstructionEntry {
    private Instruction instruction;
    private int l1DAccesses;
    private int l1DHits;
    private int l2Accesses;
    private int l2Hits;

    /**
     * Create a load instruction entry from the specified instruction object.
     *
     * @param instruction the instruction object
     */
    LoadInstructionEntry(Instruction instruction) {
        this.instruction = instruction;
    }

    /**
     * Get the instruction object.
     *
     * @return the instruction object
     */
    public Instruction getInstruction() {
        return instruction;
    }

    /**
     * Get the number of L1D cache accesses.
     *
     * @return the number of L1D cache accesses
     */
    public int getL1DAccesses() {
        return l1DAccesses;
    }

    /**
     * Set the number of L1D cache accesses.
     *
     * @param l1DAccesses the number of L1D cache accesses
     */
    public void setL1DAccesses(int l1DAccesses) {
        this.l1DAccesses = l1DAccesses;
    }

    /**
     * Get the number of L1D cache hits.
     *
     * @return the number of L1D cache hits
     */
    public int getL1DHits() {
        return l1DHits;
    }

    /**
     * Set the  number of L1D cache hits.
     *
     * @param l1DHits the number of L1D cache hits
     */
    public void setL1DHits(int l1DHits) {
        this.l1DHits = l1DHits;
    }

    /**
     * Get the number of L1D cache misses.
     *
     * @return the number of L1D cache misses
     */
    public int getL1DMisses() {
        return getL1DAccesses() - getL1DHits();
    }

    /**
     * Get the L1D cache hit ratio.
     *
     * @return the L1D cache hit ratio
     */
    public double getL1DHitRatio() {
        return this.getL1DAccesses() > 0 ? (double) this.getL1DHits() / this.getL1DAccesses() : 0.0;
    }

    /**
     * Get the number of L2 cache accesses.
     *
     * @return the number of L2 cache accesses
     */
    public int getL2Accesses() {
        return l2Accesses;
    }

    /**
     * Set the number of L2 cache accesses.
     *
     * @param l2Accesses the number of L2 cache accesses
     */
    public void setL2Accesses(int l2Accesses) {
        this.l2Accesses = l2Accesses;
    }

    /**
     * Get the number of L2 cache hits.
     *
     * @return the number of L2 cache hits
     */
    public int getL2Hits() {
        return l2Hits;
    }

    /**
     * Set the number of L2 cache hits.
     *
     * @param l2Hits the number of L2 cache hits
     */
    public void setL2Hits(int l2Hits) {
        this.l2Hits = l2Hits;
    }

    /**
     * Get the number of L2 cache misses.
     *
     * @return the number of L2 cache misses
     */
    public int getL2Misses() {
        return l2Accesses - l2Hits;
    }

    /**
     * Get the L2 cache hit ratio.
     *
     * @return the L2 cache hit ratio
     */
    public double getL2HitRatio() {
        return l2Accesses > 0 ? (double) l2Hits / l2Accesses : 0.0;
    }

    @Override
    public String toString() {
        return String.format(
                "LoadInstructionEntry{instruction=%s, " +
                        "l1d.accesses=%d, l1d.hits=%d, l1d.misses=%d, l1d.hitRatio=%.4f, " +
                        "l2.accesses=%d, l2.hits=%d, l2.misses=%d, l2.hitRatio=%.4f}",
                instruction,
                l1DAccesses, l1DHits, getL1DMisses(), getL1DHitRatio(),
                l2Accesses, l2Hits, getL2Misses(), getL2HitRatio()
        );
    }
}
