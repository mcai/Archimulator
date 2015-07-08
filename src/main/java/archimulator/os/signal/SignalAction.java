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

/**
 * Signal action.
 *
 * @author Min Cai
 */
public class SignalAction {
    private int flags;
    private int handler;
    private int restorer;
    private SignalMask mask;

    /**
     * Create a signal action.
     */
    public SignalAction() {
        this.mask = new SignalMask();
    }

    /**
     * Load the signal action from the specified address in the specified memory.
     *
     * @param memory  the memory
     * @param address the address
     */
    public void loadFrom(Memory memory, int address) {
        this.flags = memory.readWord(address);
        this.handler = memory.readWord(address + HANDLER_OFFSET);
        this.restorer = memory.readWord(address + RESTORER_OFFSET);

        this.mask.loadFrom(memory, address + MASK_OFFSET);
    }

    /**
     * Save the signal action to the specified address in the specified memory.
     *
     * @param memory  the memory
     * @param address the address
     */
    public void saveTo(Memory memory, int address) {
        memory.writeWord(address, this.flags);
        memory.writeWord(address + HANDLER_OFFSET, this.handler);
        memory.writeWord(address + RESTORER_OFFSET, this.restorer);

        this.mask.saveTo(memory, address + MASK_OFFSET);
    }

    /**
     * Get the flags.
     *
     * @return the flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Get the address of the handler procedure.
     *
     * @return the address of the handler procedure
     */
    public int getHandler() {
        return handler;
    }

    /**
     * Get the address of the restorer procedure.
     *
     * @return the address of the restore procedure
     */
    public int getRestorer() {
        return restorer;
    }

    /**
     * Get the mask.
     *
     * @return the mask
     */
    public SignalMask getMask() {
        return mask;
    }

    private static final int HANDLER_OFFSET = 4;
    private static final int RESTORER_OFFSET = 136;
    private static final int MASK_OFFSET = 8;
}
