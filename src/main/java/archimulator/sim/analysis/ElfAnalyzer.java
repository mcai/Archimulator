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
package archimulator.sim.analysis;

import archimulator.sim.isa.BasicMipsInstructionExecutor;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.elf.ElfFile;
import archimulator.sim.os.elf.Symbol;
import net.pickapack.DateHelper;
import net.pickapack.StringHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

public class ElfAnalyzer {
    private Program program;
    private ElfFile elfFile;
    private Map<String, SortedMap<Integer, Instruction>> insts;
    private int programEntry;

    public ElfAnalyzer(String fileName, ElfFile elfFile, Map<String, SortedMap<Integer, Instruction>> insts, int programEntry) {
        this.elfFile = elfFile;
        this.insts = insts;
        this.programEntry = programEntry;
        this.program = new Program(fileName);
    }

    public void buildControlFlowGraphs() {
        for (String sectionName : this.insts.keySet()) {
            SortedMap<Integer, Instruction> instsInSection = this.insts.get(sectionName);

            Function currentFunction = null;

            for (int pc : instsInSection.keySet()) {
                boolean isEntry = this.elfFile.getLocalFunctionSymbols().containsKey(pc);

                if (isEntry) {
                    Symbol localFunctionSymbol = this.elfFile.getLocalFunctionSymbols().get(pc);
                    currentFunction = new Function(this.program, sectionName, localFunctionSymbol);
                    this.program.getFunctions().add(currentFunction);
                    currentFunction.setNumInsts(1);
                } else {
                    if (currentFunction != null) {
                        currentFunction.setNumInsts(currentFunction.getNumInsts() + 1);
                    }
                }
            }
        }

        for (String sectionName : this.insts.keySet()) {
            for (Instruction instruction : this.insts.get(sectionName).values()) {
                instruction.setSectionName(sectionName);
            }
        }

        for (Function function : this.program.getFunctions()) {
            this.createControlFlowGraph(function);
        }
    }

    private void createControlFlowGraph(Function function) {
        this.scanBasicBlocks(function);

        BasicBlock newBasicBlock = null;

        for (int i = 0; i < function.getNumInsts(); i++) {
            int pc = (int) function.getSymbol().getSt_value() + i * 4;

            Instruction instruction = this.insts.get(function.getSectionName()).get(pc);
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

        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            if (basicBlock.getType() == BasicBlockType.FUNCTION_CALL) {
                basicBlock.setType(BasicBlockType.SEQUENTIAL); //TODO: caller-callee information!!!
            }
        }

        //TODO: ... identify loops
    }

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

    private void createControlFlowGraphEdge(BasicBlock from, BasicBlock to, ControlFlowGraphEdgeType edgeType) {
        ControlFlowGraphEdge edge = new ControlFlowGraphEdge(from, to);

        if (edgeType == ControlFlowGraphEdgeType.NOT_TAKEN) {
            from.setOutgoingNotTakenEdge(edge);
        } else {
            from.setOutgoingTakenEdge(edge);
        }

        to.getIncomingEdges().add(edge);
    }

    private void scanBasicBlocks(Function function) {
        this.insts.get(function.getSectionName()).get((int) function.getSymbol().getSt_value()).setLeader(true, 0);

        for (int i = 0; i < function.getNumInsts(); i++) {
            int pc = (int) function.getSymbol().getSt_value() + i * 4;

            Instruction instruction = this.insts.get(function.getSectionName()).get(pc);
            Instruction nextNextInstruction = this.insts.get(function.getSectionName()).get(pc + 8);

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
                if (nextNextInstruction != null && i < function.getNumInsts() - 2 && !nextNextInstruction.isLeader()) {
                    nextNextInstruction.setLeader(true, 4);
                }
            }
        }
    }

    public void dumpAnalysisResult(Writer out) {
        PrintWriter bw = new PrintWriter(out);

        for (Function function : this.program.getFunctions()) {
            bw.println(String.format("0x%08x <%s>: ", function.getBasicBlocks().get(0).getInstructions().get(0).getPc(), function.getSymbol().getName()));

            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                bw.print("\tbb" + basicBlock.getNum() + ": \t; type = " + basicBlock.getType() + "; ");
                bw.print("  preds = ");

                List<String> preds = new ArrayList<String>();

                for (ControlFlowGraphEdge incomingEdge : basicBlock.getIncomingEdges()) {
                    preds.add("bb" + incomingEdge.getFrom().getNum());
                }

                bw.println(StringHelper.join(preds, ", "));

                for (Instruction instruction : basicBlock.getInstructions()) {
                    bw.println("\t\t" + instruction + (instruction.isLeader() ? " <leader:" + instruction.getLeaderType() + ">" : ""));
                }

                bw.println();
            }
        }

        bw.close();
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Instruction getTargetInstruction(Function function, Instruction instruction) {
        int targetPc = BasicMipsInstructionExecutor.getTargetPcForControl(instruction.getPc() + 4, instruction.getStaticInstruction().getMachInst(), instruction.getStaticInstruction().getMnemonic());
        return this.insts.get(function.getSectionName()).get(targetPc);
    }

    public Program getProgram() {
        return this.program;
    }

    public ElfFile getElfFile() {
        return this.elfFile;
    }

    public Map<String, SortedMap<Integer, Instruction>> getInsts() {
        return this.insts;
    }

    public int getProgramEntry() {
        return this.programEntry;
    }
}
