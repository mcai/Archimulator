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
 * MERCHANpTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.os.event;

import archimulator.sim.os.Context;

import java.io.Serializable;

public abstract class SystemEvent implements Serializable {
    private Context context;
    private SystemEventType type;

    public SystemEvent(Context context, SystemEventType type) {
        this.context = context;
        this.type = type;
    }

    public abstract boolean needProcess();

    public abstract void process();

    public Context getContext() {
        return context;
    }

    public SystemEventType getType() {
        return type;
    }
}
