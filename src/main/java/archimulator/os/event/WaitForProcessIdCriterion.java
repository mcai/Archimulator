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
package archimulator.os.event;

import archimulator.os.Context;
import archimulator.os.ContextKilledEvent;

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
            context.getBlockingEventDispatcher().addListener(ContextKilledEvent.class, event -> {
                hasProcessIdKilled = true;
            });
        }
    }

    public boolean needProcess(Context context) {
        return this.getProcessId() == -1 && this.hasProcessIdKilled || this.getProcessId() > 0 && context.getKernel().getContextFromProcessId(this.processId) == null;
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
