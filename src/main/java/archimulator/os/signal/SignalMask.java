/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os.signal;

import archimulator.isa.Memory;
import archimulator.os.Kernel;
import archimulator.util.math.MathHelper;

/**
 * Signal mask.
 *
 * @author Min Cai
 */
public class SignalMask implements Cloneable {
    private int[] signals = new int[Kernel.MAX_SIGNAL / 32];

    /**
     * Create a signal mask.
     */
    public SignalMask() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SignalMask signalMask = new SignalMask();
        signalMask.signals = this.signals.clone();
        return signalMask;
    }

    /**
     * Set the specified signal.
     *
     * @param signal the signal to be set
     */
    public void set(int signal) {
        if (signal < 1 || signal > Kernel.MAX_SIGNAL) {
            return;
        }

        signal--;

        this.signals[signal / 32] = MathHelper.setBit(this.signals[signal / 32], signal % 32);
    }

    /**
     * Clear the specified signal.
     *
     * @param signal the signal to be cleared
     */
    public void clear(int signal) {
        if (signal < 1 || signal > Kernel.MAX_SIGNAL) {
            return;
        }

        signal--;

        this.signals[signal / 32] = MathHelper.clearBit(this.signals[signal / 32], signal % 32);
    }

    /**
     * Get a value indicating whether the signal mask contains the specified signal or not.
     *
     * @param signal the signal
     * @return a value indicating whether the signal mask contains the specified signal or not
     */
    public boolean contains(int signal) {
        if (signal < 1 || signal > Kernel.MAX_SIGNAL) {
            return false;
        }

        signal--;

        return MathHelper.containsBit(this.signals[signal / 32], signal % 32);
    }

    /**
     * Load the signal mask from the specified address in the specified memory.
     *
     * @param memory  the memory
     * @param address the address
     */
    public void loadFrom(Memory memory, int address) {
        for (int i = 0; i < Kernel.MAX_SIGNAL / 32; i++) {
            this.signals[i] = memory.readWord(address + i * 4);
        }
    }

    /**
     * Store the signal mask to the specified address in the specified memory.
     *
     * @param memory  the memory
     * @param address the address
     */
    public void saveTo(Memory memory, int address) {
        for (int i = 0; i < Kernel.MAX_SIGNAL / 32; i++) {
            memory.writeWord(address + i * 4, this.signals[i]);
        }
    }
}
