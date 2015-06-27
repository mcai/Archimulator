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

import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.os.Context;

/**
 * Poll event.
 *
 * @author Min Cai
 */
public class PollEvent extends SystemEvent {
    private TimeCriterion timeCriterion;
    private WaitForFileDescriptorCriterion waitForFileDescriptorCriterion;

    /**
     * Create a poll event for the specified context.
     *
     * @param context the context
     */
    public PollEvent(Context context) {
        super(context, SystemEventType.POLL);

        this.timeCriterion = new TimeCriterion();
        this.waitForFileDescriptorCriterion = new WaitForFileDescriptorCriterion();
    }

    @Override
    public boolean needProcess() {
        return this.timeCriterion.needProcess(this.getContext()) || this.waitForFileDescriptorCriterion.needProcess(this.getContext());
    }

    @Override
    public void process() {
        if (!this.waitForFileDescriptorCriterion.getBuffer().isEmpty()) {
            this.getContext().getProcess().getMemory().writeHalfWord(this.waitForFileDescriptorCriterion.getPufds() + 6, (short) 1);
            this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, 1);
        } else {
            this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, 0);
        }

        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A3, 0);
        this.getContext().resume();
    }

    /**
     * Get the time criterion.
     *
     * @return the time criterion
     */
    public TimeCriterion getTimeCriterion() {
        return timeCriterion;
    }

    /**
     * Get the wait for file descriptor criterion.
     *
     * @return the wait for file descriptor criterion
     */
    public WaitForFileDescriptorCriterion getWaitForFileDescriptorCriterion() {
        return waitForFileDescriptorCriterion;
    }
}
