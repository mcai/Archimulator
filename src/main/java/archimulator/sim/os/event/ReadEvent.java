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
public class ReadEvent extends SystemEvent {
    private WaitFileDescriptorCriterion waitFileDescriptorCriterion;

    /**
     *
     * @param context
     */
    public ReadEvent(Context context) {
        super(context, SystemEventType.READ);

        this.waitFileDescriptorCriterion = new WaitFileDescriptorCriterion();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean needProcess() {
        return this.waitFileDescriptorCriterion.needProcess(this.getContext());
    }

    /**
     *
     */
    @Override
    public void process() {
        this.getContext().resume();

        byte[] buf = new byte[this.waitFileDescriptorCriterion.getSize()];

        int numRead = this.waitFileDescriptorCriterion.getBuffer().read(buf, 0, buf.length);

        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_V0, numRead);
        this.getContext().getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A3, 0);

        this.getContext().getProcess().getMemory().writeBlock(this.waitFileDescriptorCriterion.getAddress(), numRead, buf);
    }

    /**
     *
     * @return
     */
    public WaitFileDescriptorCriterion getWaitFileDescriptorCriterion() {
        return waitFileDescriptorCriterion;
    }
}
