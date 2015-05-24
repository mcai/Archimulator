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
package archimulator.analysis;

import archimulator.os.elf.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Function.
 *
 * @author Min Cai
 */
public class Function implements Comparable<Function> {
    private Program program;
    private String sectionName;
    private Symbol symbol;

    private int numInstructions;
    private List<BasicBlock> basicBlocks;

    /**
     * Create a function.
     *
     * @param program     the program
     * @param sectionName the section name
     * @param symbol      the symbol
     */
    public Function(Program program, String sectionName, Symbol symbol) {
        this.program = program;
        this.sectionName = sectionName;
        this.symbol = symbol;
        this.basicBlocks = new ArrayList<>();
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
     * Get the section name.
     *
     * @return the section name
     */
    public String getSectionName() {
        return sectionName;
    }

    /**
     * Get the symbol.
     *
     * @return the symbol
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Get the number of instructions contained in this function.
     *
     * @return the number of instructions contained in this function
     */
    public int getNumInstructions() {
        return numInstructions;
    }

    /**
     * Set the number of instructions contained in this function.
     *
     * @param numInstructions the number of instructions contained in this function
     */
    public void setNumInstructions(int numInstructions) {
        this.numInstructions = numInstructions;
    }

    /**
     * Get the list of basic blocks contained in this function.
     *
     * @return the list of basic blocks contained in this function
     */
    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public int compareTo(Function o) {
        return new Long(this.symbol.getValue()).compareTo(o.symbol.getValue());
    }

    @Override
    public String toString() {
        return String.format("Function{program=%s, sectionName=%s, symbol=%s}", program, sectionName, symbol);
    }
}
