/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.sim.os.Context;
import archimulator.sim.os.ContextKilledEvent;
import net.pickapack.action.Action1;

/**
 * Wait for process ID criterion.
 *
 * @author Min Cai
 */
public class WaitForProcessIdCriterion implements SystemEventCriterion {
    private int processId;
    private boolean hasProcessIdKilled;

    /**
     * Create a wait for process ID criterion.
     *
     * @param context   the context
     * @param processId the process ID
     */
    public WaitForProcessIdCriterion(Context context, int processId) {
        this.processId = processId;

        this.hasProcessIdKilled = false;

        if (this.processId == -1) {
            context.getBlockingEventDispatcher().addListener(ContextKilledEvent.class, new Action1<ContextKilledEvent>() {
                public void apply(ContextKilledEvent event) {
                    hasProcessIdKilled = true;
                }
            });
        }
    }

    public boolean needProcess(Context context) {
        return ((this.getProcessId() == -1) && this.hasProcessIdKilled) || ((this.getProcessId() > 0) && (context.getKernel().getContextFromProcessId(this.processId) == null));
    }

    /**
     * Get the process ID.
     *
     * @return the process ID
     */
    public int getProcessId() {
        return processId;
    }
}
