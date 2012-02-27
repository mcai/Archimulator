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

public class PollEvent extends SystemEvent {
    private TimeCriterion timeCriterion;
    private WaitFdCriterion waitFdCriterion;

    public PollEvent(Context context) {
        super(context, SystemEventType.POLL);

        this.timeCriterion = new TimeCriterion();
        this.waitFdCriterion = new WaitFdCriterion();
    }

    @Override
    public boolean needProcess() {
        return this.timeCriterion.needProcess(this.getContext()) || this.waitFdCriterion.needProcess(this.getContext());
    }

    @Override
    public void process() {
        if (!this.waitFdCriterion.getBuffer().isEmpty()) {
            this.getContext().getProcess().getMemory().writeHalfWord(this.waitFdCriterion.getPufds() + 6, (short) 1);
            this.getContext().getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 1);
        } else {
            this.getContext().getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
        }

        this.getContext().getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        this.getContext().resume();
    }

    public TimeCriterion getTimeCriterion() {
        return timeCriterion;
    }

    public WaitFdCriterion getWaitFdCriterion() {
        return waitFdCriterion;
    }
}