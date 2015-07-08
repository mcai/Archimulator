/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.delinquentLoad;

/**
 * Delinquent load.
 *
 * @author Min Cai
 */
public class DelinquentLoad {
    private int pc;
    private int functionCallPc;

    private int numExecutions;
    private int numInstructions;
    private int numCyclesSpentAtHeadOfReorderBuffer;

    private boolean steady;

    /**
     * Create a delinquent load.
     *
     * @param pc             the value of the program counter (PC)
     * @param functionCallPc the value of the function call's program counter (PC)
     */
    public DelinquentLoad(int pc, int functionCallPc) {
        this.pc = pc;
        this.functionCallPc = functionCallPc;
    }

    /**
     * Get the value of the program counter (PC).
     *
     * @return the value of the program counter (PC)
     */
    public int getPc() {
        return pc;
    }

    /**
     * Get the value of the function call's program counter (PC).
     *
     * @return the value of the function call's program counter (PC)
     */
    public int getFunctionCallPc() {
        return functionCallPc;
    }

    /**
     * Get the number of executions.
     *
     * @return the number of executions
     */
    public int getNumExecutions() {
        return numExecutions;
    }

    /**
     * Set the number of executions.
     *
     * @param numExecutions the number of executions
     */
    public void setNumExecutions(int numExecutions) {
        this.numExecutions = numExecutions;
    }

    /**
     * Get the number of instructions.
     *
     * @return the number of instructions
     */
    public int getNumInstructions() {
        return numInstructions;
    }

    /**
     * Set the number of instructions.
     *
     * @param numInstructions the number of instructions
     */
    public void setNumInstructions(int numInstructions) {
        this.numInstructions = numInstructions;
    }

    /**
     * Get the number of cycles spent at the head of the reorder buffer.
     *
     * @return the number of cycles spent at the head of the reorder buffer
     */
    public int getNumCyclesSpentAtHeadOfReorderBuffer() {
        return numCyclesSpentAtHeadOfReorderBuffer;
    }

    /**
     * Set the number of cycles spent at the head of the reorder buffer.
     *
     * @param numCyclesSpentAtHeadOfReorderBuffer
     *         the number of cycles spent at the head of the reorder buffer
     */
    public void setNumCyclesSpentAtHeadOfReorderBuffer(int numCyclesSpentAtHeadOfReorderBuffer) {
        this.numCyclesSpentAtHeadOfReorderBuffer = numCyclesSpentAtHeadOfReorderBuffer;
    }

    /**
     * Get a value indicating whether the delinquent load is steady or not.
     *
     * @return a value indicating whether the delinquent load is steady or not
     */
    public boolean isSteady() {
        return steady;
    }

    /**
     * Set a value indicating whether the delinquent load is steady or not.
     *
     * @param steady a value indicating whether the delinquent load is steady or not
     */
    public void setSteady(boolean steady) {
        this.steady = steady;
    }

    @Override
    public String toString() {
        return String.format("DelinquentLoad{pc=0x%08x, functionCallPc=0x%08x, numExecutions=%d, numInstructions=%d, numCyclesSpentAtHeadOfReorderBuffer=%d, steady=%s}", pc, functionCallPc, numExecutions, numInstructions, numCyclesSpentAtHeadOfReorderBuffer, steady);
    }
}
