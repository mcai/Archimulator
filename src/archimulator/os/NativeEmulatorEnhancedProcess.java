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
package archimulator.os;

import archimulator.isa.NativeMipsIsaEmulatorCapability;
import archimulator.isa.StaticInstruction;
import archimulator.sim.ContextConfig;

public class NativeEmulatorEnhancedProcess extends Process {
    public NativeEmulatorEnhancedProcess(Kernel kernel, String simulationDirectory, ContextConfig contextConfig) {
        super(kernel, simulationDirectory, contextConfig);
    }

    @Override
    protected void loadProgram(Kernel kernel, String simulationDirectory, ContextConfig contextConfig) {
        //TODO: correct Archimulator's implementation accordingly
        String[] args = contextConfig.toCmdArgList().toArray(new String[]{});
        kernel.getCapability(NativeMipsIsaEmulatorCapability.class).load_prog(this.getId(), args.length, args);
    }

    @Override
    public StaticInstruction getStaticInst(int pc) {
        return this.decode(this.getMemory().readWord(pc));
    }
}
