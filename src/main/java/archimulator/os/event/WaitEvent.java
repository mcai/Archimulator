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
package archimulator.os.event;

import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.os.Context;

/**
 * Wait event.
 *
 * @author Min Cai
 */
public class WaitEvent extends SystemEvent {
    private WaitForProcessIdCriterion waitForProcessIdCriterion;
    private SignalCriterion signalCriterion;

    /**
     * Create a wait event.
     *
     * @param context the context
     * @param pid     the process ID
     */
    public WaitEvent(Context context, int pid) {
        super(context, SystemEventType.WAIT);

        this.waitForProcessIdCriterion = new WaitForProcessIdCriterion(context, pid);
        this.signalCriterion = new SignalCriterion();
    }

    @Override
    public boolean needProcess() {
        return this.waitForProcessIdCriterion.needProcess(this.getContext()) || this.signalCriterion.needProcess(this.getContext());
    }

    @Override
    public void process() {
        this.getContext().resume();

        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, this.waitForProcessIdCriterion.getProcessId());
        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A3, 0);
    }

    /**
     * Get the wait for process ID criterion.
     *
     * @return the wait for process ID criterion
     */
    public WaitForProcessIdCriterion getWaitForProcessIdCriterion() {
        return waitForProcessIdCriterion;
    }

    /**
     * Get the signal criterion.
     *
     * @return the signal criterion
     */
    public SignalCriterion getSignalCriterion() {
        return signalCriterion;
    }
}
