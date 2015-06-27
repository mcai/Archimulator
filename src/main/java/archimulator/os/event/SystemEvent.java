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
package archimulator.os.event;

import archimulator.os.Context;

/**
 * System event.
 *
 * @author Min Cai
 */
public abstract class SystemEvent {
    private Context context;
    private SystemEventType type;

    /**
     * Create a system event of the specified type for the specified context.
     *
     * @param context the context
     * @param type    the type
     */
    public SystemEvent(Context context, SystemEventType type) {
        this.context = context;
        this.type = type;
    }

    /**
     * Get a value indicating whether the event need be processed immediately or not.
     *
     * @return a value indicating whether the event need be processed immediately or not
     */
    public abstract boolean needProcess();

    /**
     * Process the event.
     */
    public abstract void process();

    /**
     * Get the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get the event type.
     *
     * @return the event type
     */
    public SystemEventType getType() {
        return type;
    }
}
