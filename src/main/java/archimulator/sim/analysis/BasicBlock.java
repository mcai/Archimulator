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

import java.util.ArrayList;
import java.util.List;

/**
 * Basic block.
 *
 * @author Min Cai
 */
public class BasicBlock {
    private Function function;
    private int num;
    private List<Instruction> instructions;

    private List<ControlFlowGraphEdge> incomingEdges;

    private ControlFlowGraphEdge OutgoingNotTakenEdge;
    private ControlFlowGraphEdge OutgoingTakenEdge;

    private BasicBlockType type;

    /**
     * Create a basic block.
     *
     * @param function the function
     * @param num      the basic block's number
     */
    public BasicBlock(Function function, int num) {
        this.function = function;
        this.num = num;
        this.instructions = new ArrayList<>();

        this.incomingEdges = new ArrayList<>();

        this.type = BasicBlockType.UNKNOWN;
    }

    /**
     * Get the function.
     *
     * @return the function
     */
    public Function getFunction() {
        return function;
    }

    /**
     * Get the basic block's number.
     *
     * @return the basic block's number
     */
    public int getNum() {
        return num;
    }

    /**
     * Get the constituent list of instructions.
     *
     * @return the constituent list of instructions
     */
    public List<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * Get the list of incoming edges.
     *
     * @return the list of incoming edges
     */
    public List<ControlFlowGraphEdge> getIncomingEdges() {
        return incomingEdges;
    }

    /**
     * Get the outgoing "not taken" edge.
     *
     * @return the outgoing "not taken" edge
     */
    public ControlFlowGraphEdge getOutgoingNotTakenEdge() {
        return OutgoingNotTakenEdge;
    }

    /**
     * Set the outgoing "not taken" edge.
     *
     * @param outgoingNotTakenEdge the outgoing "not taken" edge
     */
    public void setOutgoingNotTakenEdge(ControlFlowGraphEdge outgoingNotTakenEdge) {
        OutgoingNotTakenEdge = outgoingNotTakenEdge;
    }

    /**
     * Get the outgoing "taken" edge.
     *
     * @return the outgoing "taken" edge
     */
    public ControlFlowGraphEdge getOutgoingTakenEdge() {
        return OutgoingTakenEdge;
    }

    /**
     * Set the outgoing "taken" edge.
     *
     * @param outgoingTakenEdge the outgoing "taken" edge
     */
    public void setOutgoingTakenEdge(ControlFlowGraphEdge outgoingTakenEdge) {
        OutgoingTakenEdge = outgoingTakenEdge;
    }

    /**
     * Get the basic block's type.
     *
     * @return the basic block's type
     */
    public BasicBlockType getType() {
        return type;
    }

    /**
     * Set the basic block's type.
     *
     * @param type the basic block's type
     */
    public void setType(BasicBlockType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("BasicBlock{instructions=%s}", instructions);
    }
}
