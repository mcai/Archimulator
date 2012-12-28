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
public class PollEvent extends SystemEvent {
    private TimeCriterion timeCriterion;
    private WaitFileDescriptorCriterion waitFileDescriptorCriterion;

    /**
     *
     * @param context
     */
    public PollEvent(Context context) {
        super(context, SystemEventType.POLL);

        this.timeCriterion = new TimeCriterion();
        this.waitFileDescriptorCriterion = new WaitFileDescriptorCriterion();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean needProcess() {
        return this.timeCriterion.needProcess(this.getContext()) || this.waitFileDescriptorCriterion.needProcess(this.getContext());
    }

    /**
     *
     */
    @Override
    public void process() {
        if (!this.waitFileDescriptorCriterion.getBuffer().isEmpty()) {
            this.getContext().getProcess().getMemory().writeHalfWord(this.waitFileDescriptorCriterion.getPufds() + 6, (short) 1);
            this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, 1);
        } else {
            this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, 0);
        }

        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A3, 0);
        this.getContext().resume();
    }

    /**
     *
     * @return
     */
    public TimeCriterion getTimeCriterion() {
        return timeCriterion;
    }

    /**
     *
     * @return
     */
    public WaitFileDescriptorCriterion getWaitFileDescriptorCriterion() {
        return waitFileDescriptorCriterion;
    }
}
