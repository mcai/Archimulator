/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core.event;

import archimulator.common.SimulationEvent;
import archimulator.core.DynamicInstruction;

/**
 * The event fired when a dynamic instruction is decoded.
 *
 * @author Min Cai
 */
public class DynamicInstructionDecodedEvent extends SimulationEvent {
    private DynamicInstruction dynamicInstruction;

    /**
     * Create an event when a dynamic instruction is decoded.
     *
     * @param dynamicInstruction the dynamic instruction
     */
    public DynamicInstructionDecodedEvent(DynamicInstruction dynamicInstruction) {
        super(dynamicInstruction.getThread());
        this.dynamicInstruction = dynamicInstruction;
    }

    /**
     * Get the dynamic instruction.
     *
     * @return the dynamic instruction
     */
    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    @Override
    public String toString() {
        return String.format("DynamicInstructionDecodedEvent{dynamicInstruction=%s}", dynamicInstruction);
    }
}
