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

import archimulator.sim.isa.memory.Memory;

import java.io.Serializable;

public class SignalAction implements Serializable {
    private int flags;
    private int handler;
    private int restorer;
    private SignalMask mask;

    public SignalAction() {
        this.mask = new SignalMask();
    }

    public void loadFrom(Memory memory, int addr) {
        this.flags = memory.readWord(addr);
        this.handler = memory.readWord(addr + HANDLER_OFFSET);
        this.restorer = memory.readWord(addr + RESTORER_OFFSET);

        this.mask.loadFrom(memory, addr + MASK_OFFSET);
    }

    public void saveTo(Memory memory, int addr) {
        memory.writeWord(addr, this.flags);
        memory.writeWord(addr + HANDLER_OFFSET, this.handler);
        memory.writeWord(addr + RESTORER_OFFSET, this.restorer);

        this.mask.saveTo(memory, addr + MASK_OFFSET);
    }

    public int getFlags() {
        return flags;
    }

    public int getHandler() {
        return handler;
    }

    public int getRestorer() {
        return restorer;
    }

    public SignalMask getMask() {
        return mask;
    }

    private static final int HANDLER_OFFSET = 4;
    private static final int RESTORER_OFFSET = 136;
    private static final int MASK_OFFSET = 8;
}
