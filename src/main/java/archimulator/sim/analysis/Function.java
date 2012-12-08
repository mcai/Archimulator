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

import archimulator.sim.os.elf.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
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
     *
     * @param program
     * @param sectionName
     * @param symbol
     */
    public Function(Program program, String sectionName, Symbol symbol) {
        this.program = program;
        this.sectionName = sectionName;
        this.symbol = symbol;
        this.basicBlocks = new ArrayList<BasicBlock>();
    }

    /**
     *
     * @return
     */
    public Program getProgram() {
        return program;
    }

    /**
     *
     * @return
     */
    public String getSectionName() {
        return sectionName;
    }

    /**
     *
     * @return
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     *
     * @return
     */
    public int getNumInstructions() {
        return numInstructions;
    }

    /**
     *
     * @param numInstructions
     */
    public void setNumInstructions(int numInstructions) {
        this.numInstructions = numInstructions;
    }

    /**
     *
     * @return
     */
    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public int compareTo(Function o) {
        return new Long(this.symbol.st_value).compareTo(o.symbol.st_value);
    }

    @Override
    public String toString() {
        return String.format("Function{program=%s, sectionName=%s, symbol=%s}", program, sectionName, symbol);
    }
}
