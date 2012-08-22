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
package archimulator.sim.os.signal;

import archimulator.sim.isa.Memory;
import archimulator.sim.os.Kernel;
import net.pickapack.math.MathHelper;

public class SignalMask implements Cloneable {
    private int[] signals = new int[Kernel.MAX_SIGNAL / 32];

    public SignalMask() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SignalMask signalMask = new SignalMask();
        signalMask.signals = this.signals.clone();
        return signalMask;
    }

    public void set(int signal) {
        if (signal < 1 || signal > Kernel.MAX_SIGNAL) {
            return;
        }

        signal--;

        this.signals[signal / 32] = MathHelper.setBit(this.signals[signal / 32], signal % 32);
    }

    public void clear(int signal) {
        if (signal < 1 || signal > Kernel.MAX_SIGNAL) {
            return;
        }

        signal--;

        this.signals[signal / 32] = MathHelper.clearBit(this.signals[signal / 32], signal % 32);
    }

    public boolean contains(int signal) {
        if (signal < 1 || signal > Kernel.MAX_SIGNAL) {
            return false;
        }

        signal--;

        return MathHelper.containsBit(this.signals[signal / 32], signal % 32);
    }

    public void loadFrom(Memory memory, int address) {
        for (int i = 0; i < Kernel.MAX_SIGNAL / 32; i++) {
            this.signals[i] = memory.readWord(address + i * 4);
        }
    }

    public void saveTo(Memory memory, int address) {
        for (int i = 0; i < Kernel.MAX_SIGNAL / 32; i++) {
            memory.writeWord(address + i * 4, this.signals[i]);
        }
    }
}
