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
package archimulator.os;

import archimulator.ext.analysis.Instruction;
import archimulator.isa.memory.Memory;
import archimulator.isa.StaticInstruction;
import archimulator.isa.dissembler.MipsDissembler;
import archimulator.os.elf.ElfFile;
import archimulator.os.elf.ElfSectionHeader;
import archimulator.sim.ContextConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class BasicProcess extends Process {
    private Map<Integer, Integer> pcsToMachInsts;
    private Map<Integer, StaticInstruction> machInstsToStaticInsts;

    private Map<String, SortedMap<Integer, Instruction>> instructions;

    public BasicProcess(Kernel kernel, String simulationDirectory, ContextConfig contextConfig) {
        super(kernel, simulationDirectory, contextConfig);
    }

    @Override
    protected void loadProgram(Kernel kernel, String simulationDirectory, ContextConfig contextConfig) {
        this.pcsToMachInsts = new TreeMap<Integer, Integer>();
        this.machInstsToStaticInsts = new TreeMap<Integer, StaticInstruction>();

        this.instructions = new HashMap<String, SortedMap<Integer, Instruction>>();

        String elfFileName = contextConfig.toCmdArgList().get(0);

        ElfFile elfFile = new ElfFile(elfFileName);

        for (ElfSectionHeader sectionHeader : elfFile.getSectionHeaders()) {
//                System.out.printf("section '%s'; addr=0x%08x; size=%d\n", sectionHeader.getName(), sectionHeader.getSh_addr(), sectionHeader.getSh_size());

            if (sectionHeader.getName().equals(".dynamic")) {
                throw new IllegalArgumentException("dynamic linking is not supported");
            }

            if (sectionHeader.getSh_type() == ElfSectionHeader.SHT_PROGBITS || sectionHeader.getSh_type() == ElfSectionHeader.SHT_NOBITS) {
                if (sectionHeader.getSh_size() > 0 && (sectionHeader.getSh_flags() & ElfSectionHeader.SHF_ALLOC) != 0) {
//                        this.memory.map((int) sectionHeader.getSh_addr(), (int) sectionHeader.getSh_size()));

                    if (sectionHeader.getSh_type() == ElfSectionHeader.SHT_NOBITS) {
//                            System.out.printf("zero section '%s'; addr=0x%08x; size=%d\n", sectionHeader.getName(), sectionHeader.getSh_addr(), sectionHeader.getSh_size());
                        this.getMemory().zero((int) sectionHeader.getSh_addr(), (int) sectionHeader.getSh_size());
                    } else {
//                            System.out.printf("writeblock section '%s'; addr=0x%08x; size=%d\n", sectionHeader.getName(), sectionHeader.getSh_addr(), sectionHeader.getSh_size());
                        this.getMemory().writeBlock((int) sectionHeader.getSh_addr(), (int) sectionHeader.getSh_size(), sectionHeader.readContent(elfFile));

                        if ((sectionHeader.getSh_flags() & ElfSectionHeader.SHF_EXECINSTR) != 0) {
                            this.instructions.put(sectionHeader.getName(), new TreeMap<Integer, Instruction>());

                            for (int i = 0; i < (int) sectionHeader.getSh_size(); i += 4) {
                                int pc = (int) sectionHeader.getSh_addr() + i;
                                this.predecode(sectionHeader.getName(), this.getMemory(), pc);
                            }
                        }
                    }

                    if (sectionHeader.getSh_addr() >= DATA_BASE) {
                        this.setDataTop((int) Math.max(this.getDataTop(), sectionHeader.getSh_addr() + sectionHeader.getSh_size() - 1));
//                            System.out.printf("%s: set dataTop to 0x%08x\n", sectionHeader.getName(), this.dataTop);
                    }
                }
            }

            if (sectionHeader.getName().equals(".text")) {
                this.setTextSize((int) (sectionHeader.getSh_addr() + sectionHeader.getSh_size() - TEXT_BASE));
            }
        }

        this.setProgramEntry((int) elfFile.getHeader().getE_entry());
        this.setHeapTop(roundUp(this.getDataTop(), Memory.getPageSize()));

        this.setStackBase(STACK_BASE);
//            this.stackSize = STACK_SIZE; //TODO
        this.setStackSize(MAX_ENVIRON);
        this.setEnvironBase(STACK_BASE - MAX_ENVIRON);

//            this.memory.map(this.stackBase - this.stackSize, this.stackSize);
        this.getMemory().zero(this.getStackBase() - this.getStackSize(), this.getStackSize());

        int stackPtr = this.getEnvironBase();
        this.getMemory().writeWord(stackPtr, contextConfig.toCmdArgList().size());
        stackPtr += 4;

        int argAddr = stackPtr;
        stackPtr += (contextConfig.toCmdArgList().size() + 1) * 4;

        int envAddr = stackPtr;
        stackPtr += (this.getEnvs().size() + 1) * 4;

        for (int i = 0; i < contextConfig.toCmdArgList().size(); i++) {
            this.getMemory().writeWord(argAddr + i * 4, stackPtr);
            stackPtr += this.getMemory().writeString(stackPtr, contextConfig.toCmdArgList().get(i));
        }
        this.getMemory().writeWord(argAddr + contextConfig.toCmdArgList().size() * 4, 0);

        for (int i = 0; i < this.getEnvs().size(); i++) {
            this.getMemory().writeWord(envAddr + i * 4, stackPtr);
            stackPtr += this.getMemory().writeString(stackPtr, this.getEnvs().get(i));
        }
        this.getMemory().writeWord(envAddr + this.getEnvs().size() * 4, 0);

        if (stackPtr > this.getStackBase()) {
            throw new IllegalArgumentException("'environ' overflow, increment MAX_ENVIRON");
        }

//        new ElfAnalyzer(elfFileName, elfFile, this.instructions, this.getProgramEntry()).buildControlFlowGraphs();
    }

    private void predecode(String sectionName, Memory memory, int pc) {
        int machInst = memory.readWord(pc);

        assert (!this.pcsToMachInsts.containsKey(pc));
        this.pcsToMachInsts.put(pc, machInst);

        if (!this.machInstsToStaticInsts.containsKey(machInst)) {
            StaticInstruction staticInst = this.decode(machInst);
            this.machInstsToStaticInsts.put(machInst, staticInst);
        }

        this.instructions.get(sectionName).put(pc, new Instruction(this, pc, this.getStaticInst(pc)));
    }

    public static String getDissemblyInstruction(Process process, int pc) {
        return MipsDissembler.disassemble(pc, process.getStaticInst(pc));
    }

    @Override
    public StaticInstruction getStaticInst(int pc) {
        assert (this.pcsToMachInsts.containsKey(pc));
        return this.machInstsToStaticInsts.get(this.pcsToMachInsts.get(pc));
    }
}
