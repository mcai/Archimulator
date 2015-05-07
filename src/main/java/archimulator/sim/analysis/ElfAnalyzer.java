/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.analysis;

import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.elf.ElfFile;
import archimulator.sim.os.elf.Symbol;
import net.pickapack.dateTime.DateHelper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * ELF file analyzer.
 *
 * @author Min Cai
 */
public class ElfAnalyzer {
    private Program program;
    private ElfFile elfFile;
    private Map<String, SortedMap<Integer, Instruction>> instructions;
    private int programEntry;

    /**
     * Create an ELF file analyzer.
     *
     * @param fileName     the file name
     * @param elfFile      the ELF file object
     * @param instructions the map of constituent instructions sorted by section names
     * @param programEntry the program entry's program counter (PC) value
     */
    public ElfAnalyzer(String fileName, ElfFile elfFile, Map<String, SortedMap<Integer, Instruction>> instructions, int programEntry) {
        this.elfFile = elfFile;
        this.instructions = instructions;
        this.programEntry = programEntry;
        this.program = new Program(fileName);
    }

    /**
     * Build the control flow graphs.
     */
    public void buildControlFlowGraphs() {
        this.instructions.forEach((sectionName, instructionsInSection) -> {
            Function currentFunction = null;

            for (int pc : instructionsInSection.keySet()) {
                boolean isEntry = this.elfFile.getLocalFunctionSymbols().containsKey(pc);

                if (isEntry) {
                    Symbol localFunctionSymbol = this.elfFile.getLocalFunctionSymbols().get(pc);
                    currentFunction = new Function(this.program, sectionName, localFunctionSymbol);
                    this.program.getFunctions().add(currentFunction);
                    currentFunction.setNumInstructions(1);
                } else {
                    if (currentFunction != null) {
                        currentFunction.setNumInstructions(currentFunction.getNumInstructions() + 1);
                    }
                }
            }
        });

        this.instructions.forEach((sectionName, instructionsInSection) -> {
            instructionsInSection.values().forEach(instruction -> instruction.setSectionName(sectionName));
        });

        this.program.getFunctions().forEach(this::createControlFlowGraph);
    }

    /**
     * Create the control flow graph for the specified function.
     *
     * @param function the function
     */
    private void createControlFlowGraph(Function function) {
        this.scanBasicBlocks(function);

        BasicBlock newBasicBlock = null;

        for (int i = 0; i < function.getNumInstructions(); i++) {
            int pc = (int) function.getSymbol().getValue() + i * 4;

            Instruction instruction = this.instructions.get(function.getSectionName()).get(pc);
            if (instruction.isLeader()) {
                newBasicBlock = new BasicBlock(function, function.getBasicBlocks().size());
                function.getBasicBlocks().add(newBasicBlock);
            }
            if (newBasicBlock == null) {
                throw new IllegalArgumentException();
            }

            instruction.setBasicBlock(newBasicBlock);
            newBasicBlock.getInstructions().add(instruction);
        }

        this.createControlFlowGraphEdges(function);

        function.getBasicBlocks().stream()
                .filter(basicBlock -> basicBlock.getType() == BasicBlockType.FUNCTION_CALL)
                .forEach(basicBlock -> basicBlock.setType(BasicBlockType.SEQUENTIAL)); //TODO: caller-callee information!!!

        //TODO: ... identify loops
    }

    /**
     * Create the control flow graph edges for the specified function.
     *
     * @param function the function
     */
    private void createControlFlowGraphEdges(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            if (basicBlock.getInstructions().size() >= 2) {
                Instruction lastInstruction = basicBlock.getInstructions().get(basicBlock.getInstructions().size() - 2);

                StaticInstructionType staticInstructionType = lastInstruction.getStaticInstruction().getMnemonic().getType();

                if (staticInstructionType == StaticInstructionType.CONDITIONAL) {
                    BasicBlock nextBasicBlock = function.getBasicBlocks().get(basicBlock.getNum() + 1);
                    this.createControlFlowGraphEdge(basicBlock, nextBasicBlock, ControlFlowGraphEdgeType.NOT_TAKEN);

                    Instruction targetInstruction = this.getTargetInstruction(function, lastInstruction);

                    if (targetInstruction.getBasicBlock() != null) {
                        this.createControlFlowGraphEdge(basicBlock, targetInstruction.getBasicBlock(), ControlFlowGraphEdgeType.TAKEN);
                    } else {
                        System.out.print(String.format("[%s WARN] Cannot find parent basic block for instruction: %s in section %s\n", DateHelper.toString(new Date()), targetInstruction, targetInstruction.getSectionName()));
                    }
                    basicBlock.setType(BasicBlockType.CONDITIONAL);
                } else if (staticInstructionType == StaticInstructionType.UNCONDITIONAL) {
                    Instruction targetInstruction = this.getTargetInstruction(function, lastInstruction);
                    this.createControlFlowGraphEdge(basicBlock, targetInstruction.getBasicBlock(), ControlFlowGraphEdgeType.TAKEN);
                    basicBlock.setType(BasicBlockType.UNCONDITIONAL);
                } else if (staticInstructionType == StaticInstructionType.FUNCTION_RETURN) {
                    basicBlock.setType(BasicBlockType.FUNCTION_RETURN);
                } else {
                    if (basicBlock.getNum() < function.getBasicBlocks().size() - 1) {
                        BasicBlock nextBasicBlock = function.getBasicBlocks().get(basicBlock.getNum() + 1);
                        this.createControlFlowGraphEdge(basicBlock, nextBasicBlock, ControlFlowGraphEdgeType.NOT_TAKEN);
                    }

                    if (staticInstructionType == StaticInstructionType.FUNCTION_CALL) {
                        basicBlock.setType(BasicBlockType.FUNCTION_CALL);
                    } else {
                        basicBlock.setType(BasicBlockType.SEQUENTIAL);
                    }
                }
            }
        }
    }

    /**
     * Create a control flow graph edge.
     *
     * @param from     the "from" basic block
     * @param to       the "to" basic block
     * @param edgeType the edge type
     */
    private void createControlFlowGraphEdge(BasicBlock from, BasicBlock to, ControlFlowGraphEdgeType edgeType) {
        ControlFlowGraphEdge edge = new ControlFlowGraphEdge(from, to);

        if (edgeType == ControlFlowGraphEdgeType.NOT_TAKEN) {
            from.setOutgoingNotTakenEdge(edge);
        } else {
            from.setOutgoingTakenEdge(edge);
        }

        to.getIncomingEdges().add(edge);
    }

    /**
     * Scan the list of basic blocks for the specified function.
     *
     * @param function the function
     */
    private void scanBasicBlocks(Function function) {
        this.instructions.get(function.getSectionName()).get((int) function.getSymbol().getValue()).setLeader(true, 0);

        for (int i = 0; i < function.getNumInstructions(); i++) {
            int pc = (int) function.getSymbol().getValue() + i * 4;

            Instruction instruction = this.instructions.get(function.getSectionName()).get(pc);
            Instruction nextNextInstruction = this.instructions.get(function.getSectionName()).get(pc + 8);

            StaticInstructionType staticInstructionType = instruction.getStaticInstruction().getMnemonic().getType();

            if (staticInstructionType == StaticInstructionType.CONDITIONAL || staticInstructionType == StaticInstructionType.UNCONDITIONAL) {
                Instruction targetInstruction = this.getTargetInstruction(function, instruction);

                if (!targetInstruction.isLeader()) {
                    targetInstruction.setLeader(true, 1);
                }

                if (nextNextInstruction != null && !nextNextInstruction.isLeader()) {
                    nextNextInstruction.setLeader(true, 2);
                }
            } else if (staticInstructionType == StaticInstructionType.FUNCTION_CALL) {
                if (nextNextInstruction != null && !nextNextInstruction.isLeader()) {
                    nextNextInstruction.setLeader(true, 3);
                }
            } else if (staticInstructionType == StaticInstructionType.FUNCTION_RETURN) {
                if (nextNextInstruction != null && i < function.getNumInstructions() - 2 && !nextNextInstruction.isLeader()) {
                    nextNextInstruction.setLeader(true, 4);
                }
            }
        }
    }

    /**
     * Dump the analysis result using the specified writer.
     *
     * @param writer the writer
     */
    public void dumpAnalysisResult(Writer writer) {
        PrintWriter pw = new PrintWriter(writer);

        for (Function function : this.program.getFunctions()) {
            pw.println(String.format("0x%08x <%s>: ", function.getBasicBlocks().get(0).getInstructions().get(0).getPc(), function.getSymbol().getName()));

            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                pw.print("\tbb" + basicBlock.getNum() + ": \t; type = " + basicBlock.getType() + "; ");
                pw.print("  preds = ");

                List<String> preds = new ArrayList<>();

                for (ControlFlowGraphEdge incomingEdge : basicBlock.getIncomingEdges()) {
                    preds.add("bb" + incomingEdge.getFrom().getNum());
                }

                pw.println(StringUtils.join(preds, ", "));

                for (Instruction instruction : basicBlock.getInstructions()) {
                    pw.println("\t\t" + instruction + (instruction.isLeader() ? " <leader:" + instruction.getLeaderType() + ">" : ""));
                }

                pw.println();
            }
        }

        pw.close();
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the target instruction for the specified function and instruction.
     *
     * @param function    the function
     * @param instruction the instruction
     * @return the target instruction
     */
    private Instruction getTargetInstruction(Function function, Instruction instruction) {
        int targetPc = StaticInstruction.getTargetPcForControl(instruction.getPc() + 4, instruction.getStaticInstruction().getMachineInstruction(), instruction.getStaticInstruction().getMnemonic());
        return this.instructions.get(function.getSectionName()).get(targetPc);
    }

    /**
     * Get the program.
     *
     * @return the program
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Get the ELF file.
     *
     * @return the ELF file
     */
    public ElfFile getElfFile() {
        return elfFile;
    }

    /**
     * Get the map of constituent instructions sorted by section names.
     *
     * @return the map of constituent instructions sorted by section names
     */
    public Map<String, SortedMap<Integer, Instruction>> getInstructions() {
        return instructions;
    }

    /**
     * Get the program entry's program counter (PC) value,
     *
     * @return the program entry's program counter (PC) value
     */
    public int getProgramEntry() {
        return programEntry;
    }
}
