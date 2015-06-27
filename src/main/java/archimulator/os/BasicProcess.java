/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.common.ContextMapping;
import archimulator.analysis.BasicBlock;
import archimulator.analysis.ElfAnalyzer;
import archimulator.analysis.Function;
import archimulator.analysis.Instruction;
import archimulator.isa.Memory;
import archimulator.isa.StaticInstruction;
import archimulator.isa.dissembler.MipsDisassembler;
import archimulator.os.elf.ElfFile;
import archimulator.os.elf.ElfSectionHeader;

import java.util.*;

/**
 * Basic process.
 *
 * @author Min Cai
 */
public class BasicProcess extends Process {
    private Map<Integer, Integer> pcsToMachineInstructions;
    private Map<Integer, StaticInstruction> machineInstructionsToStaticInstructions;

    private Map<String, SortedMap<Integer, Instruction>> instructions;
    private ElfAnalyzer elfAnalyzer;

    private Map<Integer, String> pcToFunctionNameMappingCache;

    /**
     * Create a basic process.
     *
     * @param kernel              the kernel
     * @param contextMapping      the context mapping
     */
    public BasicProcess(Kernel kernel, ContextMapping contextMapping) {
        super(kernel, contextMapping);
    }

    /**
     * Load the program.
     *  @param kernel              the kernel
     * @param contextMapping      the context mapping
     */
    @Override
    protected void loadProgram(Kernel kernel, ContextMapping contextMapping) {
        this.pcsToMachineInstructions = new TreeMap<>();
        this.machineInstructionsToStaticInstructions = new TreeMap<>();

        this.instructions = new HashMap<>();

        List<String> commandLineArgumentList = Arrays.asList((contextMapping.getExecutable() + " " + contextMapping.getArguments()).split(" "));

        String elfFileName = commandLineArgumentList.get(0);

        ElfFile elfFile = new ElfFile(elfFileName);

        for (ElfSectionHeader sectionHeader : elfFile.getSectionHeaders()) {
            if (sectionHeader.getName().equals(".dynamic")) {
                throw new IllegalArgumentException("dynamic linking is not supported");
            }

            if (sectionHeader.getType() == ElfSectionHeader.SHT_PROGBITS || sectionHeader.getType() == ElfSectionHeader.SHT_NOBITS) {
                if (sectionHeader.getSize() > 0 && (sectionHeader.getFlags() & ElfSectionHeader.SHF_ALLOC) != 0) {
//                        this.memory.map((int) sectionHeader.getAddress(), (int) sectionHeader.getSize()));

                    if (sectionHeader.getType() == ElfSectionHeader.SHT_NOBITS) {
                        this.getMemory().zero((int) sectionHeader.getAddress(), (int) sectionHeader.getSize());
                    } else {
                        this.getMemory().writeBlock((int) sectionHeader.getAddress(), (int) sectionHeader.getSize(), sectionHeader.readContent(elfFile));

                        if ((sectionHeader.getFlags() & ElfSectionHeader.SHF_EXECINSTR) != 0) {
                            this.instructions.put(sectionHeader.getName(), new TreeMap<>());

                            for (int i = 0; i < (int) sectionHeader.getSize(); i += 4) {
                                int pc = (int) sectionHeader.getAddress() + i;
                                this.predecode(sectionHeader.getName(), this.getMemory(), pc);
                            }
                        }
                    }

                    if (sectionHeader.getAddress() >= DATA_BASE) {
                        this.setDataTop((int) Math.max(this.getDataTop(), sectionHeader.getAddress() + sectionHeader.getSize() - 1));
                    }
                }
            }

            if (sectionHeader.getName().equals(".text")) {
                this.setTextSize((int) (sectionHeader.getAddress() + sectionHeader.getSize() - TEXT_BASE));
            }
        }

        this.setProgramEntry((int) elfFile.getHeader().getEntry());
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

        this.pcToFunctionNameMappingCache = new TreeMap<>();
    }

    /**
     * Predecode the instruction at the specified program counter (PC).
     *
     * @param sectionName the section name
     * @param memory      the memory
     * @param pc          the program counter (PC)
     */
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
     * Get the disassembly instruction at the specified program counter (PC) for the specified process.
     *
     * @param process the process
     * @param pc      the program counter (PC)
     * @return the disassembly instruction at the specified program counter (PC) for the specified progress
     */
    public static String getDisassemblyInstruction(Process process, int pc) {
        return MipsDisassembler.disassemble(pc, process.getStaticInstruction(pc));
    }

    @Override
    public StaticInstruction getStaticInstruction(int pc) {
        return this.machineInstructionsToStaticInstructions.get(this.pcsToMachineInstructions.get(pc));
    }

    @Override
    public String getFunctionNameFromPc(int pc) {
        if (this.pcToFunctionNameMappingCache.containsKey(pc)) {
            return this.pcToFunctionNameMappingCache.get(pc);
        }

        for (Function function : this.elfAnalyzer.getProgram().getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                for (Instruction instruction : basicBlock.getInstructions()) {
                    if (instruction.getPc() == pc) {
                        String functionName = function.getSymbol().getName();
                        this.pcToFunctionNameMappingCache.put(pc, functionName);
                        return functionName;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ElfAnalyzer getElfAnalyzer() {
        return elfAnalyzer;
    }
}
