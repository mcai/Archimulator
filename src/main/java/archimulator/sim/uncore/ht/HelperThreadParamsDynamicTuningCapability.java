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
package archimulator.sim.uncore.ht;

import archimulator.sim.base.event.PseudocallEncounteredEvent;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import net.pickapack.Reference;
import net.pickapack.action.Action1;

public class HelperThreadParamsDynamicTuningCapability implements SimulationCapability {
    private DirectoryController llc;

    public HelperThreadParamsDynamicTuningCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache());
    }

    public HelperThreadParamsDynamicTuningCapability(DirectoryController llc) {
        this.llc = llc;

        final Reference<Integer> savedRegisterValue = new Reference<Integer>(-1);

        llc.getBlockingEventDispatcher().addListener(PseudocallEncounteredEvent.class, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
                SimulatedProgram simulatedProgram = event.getContext().getProcess().getContextConfig().getSimulatedProgram();

                if(simulatedProgram.isHelperThreadedProgram()) {
                    if (event.getImm() == 3820) {
                        savedRegisterValue.set(event.getContext().getRegs().getGpr(event.getRs()));
                        event.getContext().getRegs().setGpr(event.getRs(), getHtLookahead(simulatedProgram));
                    } else if (event.getImm() == 3821) {
                        event.getContext().getRegs().setGpr(event.getRs(), savedRegisterValue.get());
                    } else if (event.getImm() == 3822) {
                        savedRegisterValue.set(event.getContext().getRegs().getGpr(event.getRs()));
                        event.getContext().getRegs().setGpr(event.getRs(), getHtStride(simulatedProgram));
                    } else if (event.getImm() == 3823) {
                        event.getContext().getRegs().setGpr(event.getRs(), savedRegisterValue.get());
                    }
                }
            }
        });
    }

    protected int getHtLookahead(SimulatedProgram simulatedProgram) {
        if (simulatedProgram.isHelperThreadedProgram()) {
            return simulatedProgram.isDynamicHtParams() ? 20 : simulatedProgram.getHtLookahead();
        }

        throw new IllegalArgumentException();
    }

    protected int getHtStride(SimulatedProgram simulatedProgram) {
        if (simulatedProgram.isHelperThreadedProgram()) {
            return simulatedProgram.isDynamicHtParams() ? 10 : simulatedProgram.getHtStride();
        }

        throw new IllegalArgumentException();
    }
}
