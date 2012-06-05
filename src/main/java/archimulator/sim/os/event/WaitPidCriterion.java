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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.os.event;

import archimulator.sim.base.event.MyBlockingEventDispatcher;
import archimulator.sim.os.Context;
import archimulator.sim.os.ContextKilledEvent;
import net.pickapack.action.Action1;

public class WaitPidCriterion implements SystemEventCriterion {
    private int pid;
    private boolean hasPidKilled;

    public WaitPidCriterion(Context context, int pid) {
        this.pid = pid;

        this.hasPidKilled = false;

        if (this.pid == -1) {
            context.getBlockingEventDispatcher().addListener2(ContextKilledEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<ContextKilledEvent>() {
                public void apply(ContextKilledEvent event) {
                    hasPidKilled = true;
                }
            });
        }
    }

    public boolean needProcess(Context context) {
        return ((this.getPid() == -1) && this.hasPidKilled) || ((this.getPid() > 0) && (context.getKernel().getContextFromPid(this.pid) == null));
    }

    public int getPid() {
        return pid;
    }
}
