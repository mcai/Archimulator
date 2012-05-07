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

import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.os.BasicProcess;
import archimulator.sim.os.Process;

public class Instruction {
    private Process process;
    private int pc;
    private StaticInstruction staticInstruction;
    private boolean leader;
    private int leaderType;

    private BasicBlock basicBlock;
    private String sectionName;

    public Instruction(Process process, int pc, StaticInstruction staticInstruction) {
        this.process = process;
        this.pc = pc;
        this.staticInstruction = staticInstruction;
    }

    public Process getProcess() {
        return process;
    }

    public int getPc() {
        return pc;
    }

    public StaticInstruction getStaticInstruction() {
        return staticInstruction;
    }

    public boolean isLeader() {
        return leader;
    }

    public int getLeaderType() {
        return leaderType;
    }

    public void setLeader(boolean leader, int leaderType) {
        this.leader = leader;
        this.leaderType = leaderType;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    @Override
    public String toString() {
        return String.format("%s%s", this.basicBlock != null ? this.basicBlock.getFunction().getSymbol().getName() + ".bb" + this.basicBlock.getNum() + ": " : "", BasicProcess.getDissemblyInstruction(process, this.pc));
    }
}