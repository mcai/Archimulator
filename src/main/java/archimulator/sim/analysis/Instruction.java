/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.sim.os.BasicProcess;
import archimulator.sim.os.Process;

/**
 * Instruction.
 *
 * @author Min Cai
 */
public class Instruction {
    private Process process;
    private int pc;
    private StaticInstruction staticInstruction;
    private boolean leader;
    private int leaderType;

    private BasicBlock basicBlock;
    private String sectionName;

    /**
     * Create a instruction.
     *
     * @param process the process
     * @param pc the program counter (PC) value
     * @param staticInstruction the static instruction
     */
    public Instruction(Process process, int pc, StaticInstruction staticInstruction) {
        this.process = process;
        this.pc = pc;
        this.staticInstruction = staticInstruction;
    }

    /**
     * Get the process.
     *
     * @return the process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get the program counter (PC) value.
     *
     * @return the program counter (PC) value
     */
    public int getPc() {
        return pc;
    }

    /**
     * Get the static instruction.
     *
     * @return the static instruction
     */
    public StaticInstruction getStaticInstruction() {
        return staticInstruction;
    }

    /**
     * Get a value indicating whether the instruction is a leader or not.
     *
     * @return a value indicating whether the instruction is a leader or not
     */
    public boolean isLeader() {
        return leader;
    }

    /**
     * Get the leader type.
     *
     * @return the leader type
     */
    public int getLeaderType() {
        return leaderType;
    }

    /**
     * Set the leader information.
     *
     * @param leader a value indicating whether the instruction is a leader or not
     * @param leaderType the leader type
     */
    public void setLeader(boolean leader, int leaderType) {
        this.leader = leader;
        this.leaderType = leaderType;
    }

    /**
     * Get the parent basic block.
     *
     * @return the parent basic block
     */
    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    /**
     * Set the parent basic block.
     *
     * @param basicBlock the parent basic block
     */
    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    /**
     * Get the section name.
     *
     * @return the section name
     */
    public String getSectionName() {
        return sectionName;
    }

    /**
     * Set the section name.
     *
     * @param sectionName the section name
     */
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    @Override
    public String toString() {
        return String.format("%s%s", this.basicBlock != null ? this.basicBlock.getFunction().getSymbol().getName() + "/bb" + this.basicBlock.getNum() + ": " : "", BasicProcess.getDisassemblyInstruction(process, this.pc));
    }
}
