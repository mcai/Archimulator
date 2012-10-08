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
package archimulator.sim.os;

import archimulator.model.ContextMapping;
import archimulator.service.ServiceManager;
import archimulator.sim.analysis.ElfAnalyzer;
import archimulator.sim.analysis.Instruction;
import archimulator.sim.isa.Memory;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.dissembler.MipsDisassembler;
import archimulator.sim.os.elf.ElfFile;
import archimulator.sim.os.elf.ElfSectionHeader;

import java.io.File;
import java.util.*;

/**
 *
 * @author Min Cai
 */
public class BasicProcess extends Process {
    private Map<Integer, Integer> pcsToMachineInstructions;
    private Map<Integer, StaticInstruction> machineInstructionsToStaticInstructions;

    private Map<String, SortedMap<Integer, Instruction>> instructions;
    private ElfAnalyzer elfAnalyzer;

    /**
     *
     * @param kernel
     * @param simulationDirectory
     * @param contextMapping
     */
    public BasicProcess(Kernel kernel, String simulationDirectory, ContextMapping contextMapping) {
        super(kernel, simulationDirectory, contextMapping);
    }

    /**
     *
     * @param kernel
     * @param simulationDirectory
     * @param contextMapping
     */
    @Override
    protected void loadProgram(Kernel kernel, String simulationDirectory, ContextMapping contextMapping) {
        this.pcsToMachineInstructions = new TreeMap<Integer, Integer>();
        this.machineInstructionsToStaticInstructions = new TreeMap<Integer, StaticInstruction>();

        this.instructions = new HashMap<String, SortedMap<Integer, Instruction>>();

        List<String> commandLineArgumentList = Arrays.asList((contextMapping.getBenchmark().getWorkingDirectory() + File.separator + contextMapping.getBenchmark().getExecutable() + " " + contextMapping.getArguments()).replaceAll(ServiceManager.USER_HOME_TEMPLATE_ARG, System.getProperty("user.home")).split(" "));

        String elfFileName = commandLineArgumentList.get(0);

        ElfFile elfFile = new ElfFile(elfFileName);

        for (ElfSectionHeader sectionHeader : elfFile.getSectionHeaders()) {
            if (sectionHeader.getName().equals(".dynamic")) {
                throw new IllegalArgumentException("dynamic linking is not supported");
            }

            if (sectionHeader.getSh_type() == ElfSectionHeader.SHT_PROGBITS || sectionHeader.getSh_type() == ElfSectionHeader.SHT_NOBITS) {
                if (sectionHeader.getSh_size() > 0 && (sectionHeader.getSh_flags() & ElfSectionHeader.SHF_ALLOC) != 0) {
//                        this.memory.map((int) sectionHeader.getSh_addr(), (int) sectionHeader.getSh_size()));

                    if (sectionHeader.getSh_type() == ElfSectionHeader.SHT_NOBITS) {
                        this.getMemory().zero((int) sectionHeader.getSh_addr(), (int) sectionHeader.getSh_size());
                    } else {
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
        this.setEnvironmentBase(STACK_BASE - MAX_ENVIRON);

//            this.memory.map(this.stackBase - this.stackSize, this.stackSize);
        this.getMemory().zero(this.getStackBase() - this.getStackSize(), this.getStackSize());

        int stackPointer = this.getEnvironmentBase();
        this.getMemory().writeWord(stackPointer, commandLineArgumentList.size());
        stackPointer += 4;

        int argAddress = stackPointer;
        stackPointer += (commandLineArgumentList.size() + 1) * 4;

        int environmentAddress = stackPointer;
        stackPointer += (this.getEnvironments().size() + 1) * 4;

        for (int i = 0; i < commandLineArgumentList.size(); i++) {
            this.getMemory().writeWord(argAddress + i * 4, stackPointer);
            stackPointer += this.getMemory().writeString(stackPointer, commandLineArgumentList.get(i));
        }
        this.getMemory().writeWord(argAddress + commandLineArgumentList.size() * 4, 0);

        for (int i = 0; i < this.getEnvironments().size(); i++) {
            this.getMemory().writeWord(environmentAddress + i * 4, stackPointer);
            stackPointer += this.getMemory().writeString(stackPointer, this.getEnvironments().get(i));
        }
        this.getMemory().writeWord(environmentAddress + this.getEnvironments().size() * 4, 0);

        if (stackPointer > this.getStackBase()) {
            throw new IllegalArgumentException("'environ' overflow, increment MAX_ENVIRON");
        }

        this.elfAnalyzer = new ElfAnalyzer(elfFileName, elfFile, this.instructions, this.getProgramEntry());
        this.elfAnalyzer.buildControlFlowGraphs();
    }

    private void predecode(String sectionName, Memory memory, int pc) {
        int machineInstruction = memory.readWord(pc);

        this.pcsToMachineInstructions.put(pc, machineInstruction);

        if (!this.machineInstructionsToStaticInstructions.containsKey(machineInstruction)) {
            StaticInstruction staticInstruction = this.decode(machineInstruction);
            this.machineInstructionsToStaticInstructions.put(machineInstruction, staticInstruction);
        }

        this.instructions.get(sectionName).put(pc, new Instruction(this, pc, this.getStaticInstruction(pc)));
    }

    /**
     *
     * @param process
     * @param pc
     * @return
     */
    public static String getDisassemblyInstruction(Process process, int pc) {
        return MipsDisassembler.disassemble(pc, process.getStaticInstruction(pc));
    }

    /**
     *
     * @param pc
     * @return
     */
    @Override
    public StaticInstruction getStaticInstruction(int pc) {
        return this.machineInstructionsToStaticInstructions.get(this.pcsToMachineInstructions.get(pc));
    }

    /**
     *
     * @return
     */
    public ElfAnalyzer getElfAnalyzer() {
        return elfAnalyzer;
    }
}
