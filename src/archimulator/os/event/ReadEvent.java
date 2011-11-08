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
package archimulator.os.event;

import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.os.Context;

public class ReadEvent extends SystemEvent {
    private WaitFdCriterion waitFdCriterion;

    public ReadEvent(Context context) {
        super(context, SystemEventType.READ);

        this.waitFdCriterion = new WaitFdCriterion();
    }

    @Override
    public boolean needProcess() {
        return this.waitFdCriterion.needProcess(this.getContext());
    }

    @Override
    public void process() {
        this.getContext().resume();

        byte[] buf = new byte[this.waitFdCriterion.getSize()];

        int nRead = this.waitFdCriterion.getBuffer().read(buf, 0, buf.length);

        this.getContext().getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, nRead);
        this.getContext().getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);

        this.getContext().getProcess().getMemory().writeBlock(this.waitFdCriterion.getAddress(), nRead, buf);
    }

    public WaitFdCriterion getWaitFdCriterion() {
        return waitFdCriterion;
    }
}
