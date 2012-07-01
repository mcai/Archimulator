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
package archimulator.sim.isa;

import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.Simulation;
import net.pickapack.action.Action1;

import java.util.ArrayList;
import java.util.List;

public class FunctionalExecutionProfilingCapability implements SimulationCapability {
    private List<Mnemonic> executedMnemonics;
    private List<String> executedSyscalls;

    public FunctionalExecutionProfilingCapability(Simulation simulation) {
        this.executedMnemonics = new ArrayList<Mnemonic>();
        this.executedSyscalls = new ArrayList<String>();

        simulation.getBlockingEventDispatcher().addListener(InstructionFunctionallyExecutedEvent.class, new Action1<InstructionFunctionallyExecutedEvent>() {
            public void apply(InstructionFunctionallyExecutedEvent event) {
                Mnemonic mnemonic = event.getStaticInst().getMnemonic();
                if (!executedMnemonics.contains(mnemonic)) {
                    executedMnemonics.add(mnemonic);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(SyscallExecutedEvent.class, new Action1<SyscallExecutedEvent>() {
            public void apply(SyscallExecutedEvent event) {
                String syscallName = event.getSyscallName();
                if (!executedSyscalls.contains(syscallName)) {
                    executedSyscalls.add(syscallName);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            @Override
            public void apply(ResetStatEvent event) {
                executedMnemonics.clear();
                executedSyscalls.clear();
            }
        });
    }

    public List<Mnemonic> getExecutedMnemonics() {
        return executedMnemonics;
    }

    public List<String> getExecutedSyscalls() {
        return executedSyscalls;
    }
}
