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

import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.os.Context;

/**
 *
 * @author Min Cai
 */
public class WaitEvent extends SystemEvent {
    private WaitProcessIdCriterion waitProcessIdCriterion;
    private SignalCriterion signalCriterion;

    /**
     *
     * @param context
     * @param pid
     */
    public WaitEvent(Context context, int pid) {
        super(context, SystemEventType.WAIT);

        this.waitProcessIdCriterion = new WaitProcessIdCriterion(context, pid);
        this.signalCriterion = new SignalCriterion();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean needProcess() {
        return this.waitProcessIdCriterion.needProcess(this.getContext()) || this.signalCriterion.needProcess(this.getContext());
    }

    /**
     *
     */
    @Override
    public void process() {
        this.getContext().resume();

        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, this.waitProcessIdCriterion.getProcessId());
        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A3, 0);
    }

    /**
     *
     * @return
     */
    public WaitProcessIdCriterion getWaitProcessIdCriterion() {
        return waitProcessIdCriterion;
    }

    /**
     *
     * @return
     */
    public SignalCriterion getSignalCriterion() {
        return signalCriterion;
    }
}
