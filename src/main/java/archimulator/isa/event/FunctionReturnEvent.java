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
package archimulator.isa.event;

import archimulator.common.SimulationEvent;
import archimulator.os.Context;
import archimulator.os.FunctionCallContext;

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
