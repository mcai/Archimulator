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

/**
 * "System call executed" event.
 *
 * @author Min Cai
 */
public class SystemCallExecutedEvent extends SimulationEvent {
    private Context context;
    private String systemCallName;

    /**
     * Create a "system call executed" event.
     *
     * @param context        the context
     * @param systemCallName the name of the system call
     */
    public SystemCallExecutedEvent(Context context, String systemCallName) {
        super(context);
        this.systemCallName = systemCallName;
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
     * Get the name of the system call.
     *
     * @return the name of the system call
     */
    public String getSystemCallName() {
        return systemCallName;
    }
}
