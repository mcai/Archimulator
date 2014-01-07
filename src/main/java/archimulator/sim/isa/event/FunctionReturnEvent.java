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
import archimulator.sim.os.Context;
import archimulator.sim.os.FunctionCallContext;

/**
 * Function return event.
 *
 * @author Min Cai
 */
public class FunctionReturnEvent extends SimulationEvent {
    private FunctionCallContext functionCallContext;

    /**
     * Create a function return event.
     *
     * @param functionCallContext the function call context
     */
    public FunctionReturnEvent(FunctionCallContext functionCallContext) {
        super(functionCallContext.getContext());
        this.functionCallContext = functionCallContext;
    }

    /**
     * Get the function call context.
     *
     * @return the function call context
     */
    public FunctionCallContext getFunctionCallContext() {
        return functionCallContext;
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    public Context getContext() {
        return getFunctionCallContext().getContext();
    }
}
