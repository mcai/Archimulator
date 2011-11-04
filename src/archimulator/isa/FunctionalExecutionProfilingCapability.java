/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.isa;

import archimulator.os.Kernel;
import archimulator.os.KernelCapability;
import archimulator.os.KernelCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.util.action.Action1;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FunctionalExecutionProfilingCapability implements KernelCapability {
    private Kernel kernel;
    private EnumSet<Mnemonic> executedMnemonics;
    private Set<String> executedSyscalls;

    public FunctionalExecutionProfilingCapability(Kernel kernel) {
        this.kernel = kernel;

        this.executedMnemonics = EnumSet.noneOf(Mnemonic.class);
        this.executedSyscalls = new HashSet<String>();

//        final String[] args = {"", "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/baseline/462.libquantum.mips", "33", "5"}; //TODO: path should not be hard coded
//        final String[] args = {"", "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/em3d/baseline/em3d.mips", "400000", "128", "75", "1"}; //TODO: path should not be hard coded
//        final String[] args = {"", "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/mst/baseline/mst.mips", "1000"}; //TODO: path should not be hard coded
        final String[] args = {"", "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/429.mcf.mips", "<", "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in"}; //TODO: path should not be hard coded

        final NativeMipsIsaEmulatorCapability nativeMipsIsaEmulatorCapability = this.kernel.getCapability(NativeMipsIsaEmulatorCapability.class);

        nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_init(args.length, args);

//        nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_quit();

        kernel.getBlockingEventDispatcher().addListener(InstructionFunctionallyExecutedEvent.class, new Action1<InstructionFunctionallyExecutedEvent>() {
            public void apply(InstructionFunctionallyExecutedEvent event) {
                if (!nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_execute_next_instruction()) {
                    System.exit(0);
                }

                for (int i = 0; i < ArchitecturalRegisterFile.NUM_INT_REGS; i++) {
                    int gprInArchimulator = event.getContext().getRegs().getGpr(i);
                    int gprInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_gpr(i);
                    assert gprInArchimulator == gprInNative;
                }

                for (int i = 0; i < ArchitecturalRegisterFile.NUM_FLOAT_REGS; i++) {
                    float fprsInArchimulator = event.getContext().getRegs().getFpr().getFloat(i);
                    float fprsInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_fprs(i);
                    assert Float.valueOf(fprsInArchimulator).equals(Float.valueOf(fprsInNative));
                }

                for (int i = 0; i < ArchitecturalRegisterFile.NUM_FLOAT_REGS; i++) {
                    double fprdInArchimulator = event.getContext().getRegs().getFpr().getDouble(i);
                    double fprdInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_fprd(i);

                    assert Double.valueOf(fprdInArchimulator).equals(Double.valueOf(fprdInNative));
                }

                int hiInArchimulator = event.getContext().getRegs().getHi();
                int hiInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_hi();
                assert hiInArchimulator == hiInNative;

                int loInArchimulator = event.getContext().getRegs().getLo();
                int loInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_lo();
                assert loInArchimulator == loInNative;

                int fcsrInArchimulator = event.getContext().getRegs().getFcsr();
                int fcsrInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_fcsr();
                assert fcsrInArchimulator == fcsrInNative;

                int firInNative = nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_fir();
                assert firInNative == 0;

                assert (event.getContext().getRegs().getPc() == nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_pc());
                assert (event.getContext().getRegs().getNpc() == nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_npc());
                assert (event.getContext().getRegs().getNnpc() == nativeMipsIsaEmulatorCapability.getNativeMipsIsaEmulator().single_thread_program_get_nnpc());

                Mnemonic mnemonic = event.getStaticInst().getMnemonic();
                if (!executedMnemonics.contains(mnemonic)) {
                    executedMnemonics.add(mnemonic);
                }
            }
        });

        kernel.getBlockingEventDispatcher().addListener(SyscallExecutedEvent.class, new Action1<SyscallExecutedEvent>() {
            public void apply(SyscallExecutedEvent event) {
//                System.out.println(event.getContext().getRegs().dump());

                System.out.printf("0x%08x: syscall %s\n", event.getContext().getRegs().getPc(), event.getSyscallName());

                String syscallName = event.getSyscallName();
                if (!executedSyscalls.contains(syscallName)) {
                    executedSyscalls.add(syscallName);
                }
            }
        });

        kernel.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        kernel.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
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

    public static final KernelCapabilityFactory FACTORY = new KernelCapabilityFactory() {
        public KernelCapability createCapability(Kernel kernel) {
            return new FunctionalExecutionProfilingCapability(kernel);
        }
    };
}
