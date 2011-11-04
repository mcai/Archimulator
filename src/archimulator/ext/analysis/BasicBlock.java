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
package archimulator.ext.analysis;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private Function function;
    private int num;
    private List<Instruction> instructions;

    private List<ControlFlowGraphEdge> incomingEdges;

    private ControlFlowGraphEdge OutgoingNotTakenEdge;
    private ControlFlowGraphEdge OutgoingTakenEdge;

    private BasicBlockType type;

    public BasicBlock(Function function, int num) {
        this.function = function;
        this.num = num;
        this.instructions = new ArrayList<Instruction>();

        this.incomingEdges = new ArrayList<ControlFlowGraphEdge>();

        this.type = BasicBlockType.UNKNOWN;
    }

    public Function getFunction() {
        return function;
    }

    public int getNum() {
        return num;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<ControlFlowGraphEdge> getIncomingEdges() {
        return incomingEdges;
    }

    public ControlFlowGraphEdge getOutgoingNotTakenEdge() {
        return OutgoingNotTakenEdge;
    }

    public void setOutgoingNotTakenEdge(ControlFlowGraphEdge outgoingNotTakenEdge) {
        OutgoingNotTakenEdge = outgoingNotTakenEdge;
    }

    public ControlFlowGraphEdge getOutgoingTakenEdge() {
        return OutgoingTakenEdge;
    }

    public void setOutgoingTakenEdge(ControlFlowGraphEdge outgoingTakenEdge) {
        OutgoingTakenEdge = outgoingTakenEdge;
    }

    public BasicBlockType getType() {
        return type;
    }

    public void setType(BasicBlockType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("BasicBlock{instructions=%s}", instructions);
    }
}
