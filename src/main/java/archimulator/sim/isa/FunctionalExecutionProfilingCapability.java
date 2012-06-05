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

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.MyBlockingEventDispatcher;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.experiment.capability.KernelCapability;
import archimulator.sim.os.Kernel;
import net.pickapack.action.Action1;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FunctionalExecutionProfilingCapability implements KernelCapability {
    private Kernel kernel;
    private EnumSet<Mnemonic> executedMnemonics;
    private Set<String> executedSyscalls;

    public FunctionalExecutionProfilingCapability(final Kernel kernel) {
        this.kernel = kernel;

        this.executedMnemonics = EnumSet.noneOf(Mnemonic.class);
        this.executedSyscalls = new HashSet<String>();

        kernel.getBlockingEventDispatcher().addListener2(Kernel.KernelCapabilitiesInitializedEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<Kernel.KernelCapabilitiesInitializedEvent>() {
            public void apply(Kernel.KernelCapabilitiesInitializedEvent event) {
                kernel.getBlockingEventDispatcher().addListener2(InstructionFunctionallyExecutedEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<InstructionFunctionallyExecutedEvent>() {
                    public void apply(InstructionFunctionallyExecutedEvent event1) {
                        Mnemonic mnemonic = event1.getStaticInst().getMnemonic();
                        if (!executedMnemonics.contains(mnemonic)) {
                            executedMnemonics.add(mnemonic);
                        }
                    }
                });
            }
        });

        kernel.getBlockingEventDispatcher().addListener2(SyscallExecutedEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<SyscallExecutedEvent>() {
            public void apply(SyscallExecutedEvent event) {
//                System.out.println(event.getContext().getRegs().dump());

                System.out.printf("0x%08x: syscall %s\n", event.getContext().getRegs().getPc(), event.getSyscallName());

                String syscallName = event.getSyscallName();
                if (!executedSyscalls.contains(syscallName)) {
                    executedSyscalls.add(syscallName);
                }
            }
        });

        kernel.getBlockingEventDispatcher().addListener2(PollStatsEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        kernel.getBlockingEventDispatcher().addListener2(DumpStatEvent.class, MyBlockingEventDispatcher.ListenerType.SIMULATION_WIDE, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                dumpStats(event.getStats());
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put("FunctionalExecutionProfilingCapability" + ".executedMnemonics.size", executedMnemonics.size());

        for (Mnemonic mnemonic : this.executedMnemonics) {
            stats.put("FunctionalExecutionProfilingCapability" + ".executedMnemonics." + mnemonic, "");
        }

        stats.put("FunctionalExecutionProfilingCapability" + ".executedSyscalls.size", executedSyscalls.size());

        for (String syscallName : this.executedSyscalls) {
            stats.put("FunctionalExecutionProfilingCapability" + ".executedSyscalls." + syscallName, "");
        }
    }
}
