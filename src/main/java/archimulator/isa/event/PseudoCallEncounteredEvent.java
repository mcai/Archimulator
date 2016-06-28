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
import archimulator.isa.PseudoCall;
import archimulator.os.Context;

/**
 * The event indicating a pseudocall is encountered during simulation.
 *
 * @author Min Cai
 */
public class PseudoCallEncounteredEvent extends SimulationEvent {
    private Context context;
    private PseudoCall pseudoCall;

    /**
     * Create a pseudocall encountered event.
     *
     * @param context    the context
     * @param pseudoCall the pseudocall
     */
    public PseudoCallEncounteredEvent(Context context, PseudoCall pseudoCall) {
        super(context);
        this.context = context;
        this.pseudoCall = pseudoCall;
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
     * Get the pseudocall.
     *
     * @return the pseudocall
     */
    public PseudoCall getPseudoCall() {
        return pseudoCall;
    }
}
