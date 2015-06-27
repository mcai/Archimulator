/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os;

import archimulator.common.SimulationEvent;

/**
 * The event fired when a context is killed.
 *
 * @author Min Cai
 */
public class ContextKilledEvent extends SimulationEvent {
    private Context context;

    /**
     * Create an event when the specified context is killed.
     *
     * @param context the context
     */
    public ContextKilledEvent(Context context) {
        super(context);

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
}