/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.isa.event;

import archimulator.sim.common.SimulationEvent;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.os.Context;

/**
 * "Instruction functionally executed" event.
 *
 * @author Min Cai
 */
public class InstructionFunctionallyExecutedEvent extends SimulationEvent {
    private Context context;
    private int pc;
    private StaticInstruction staticInstruction;

    /**
     * Create an "instruction functionally executed" event.
     *
     * @param context           the context
     * @param pc                the value of the program counter (PC)
     * @param staticInstruction the static instruction
     */
    public InstructionFunctionallyExecutedEvent(Context context, int pc, StaticInstruction staticInstruction) {
        super(context);
        this.pc = pc;
        this.staticInstruction = staticInstruction;
        this.context = context;
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
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
     * Get the static instruction.
     *
     * @return the static instruction
     */
    public StaticInstruction getStaticInstruction() {
        return staticInstruction;
    }
}
