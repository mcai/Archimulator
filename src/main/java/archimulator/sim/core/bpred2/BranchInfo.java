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
package archimulator.sim.core.bpred2;

/**
 * Branch information.
 *
 * @author Min Cai
 */
public class BranchInfo {
    private int address;
    private int opcode;
    private int branchFlags;

    /**
     * Create a branch information object.
     *
     * @param address     the address
     * @param opcode      the op code
     * @param branchFlags the branch flags
     */
    public BranchInfo(int address, int opcode, int branchFlags) {
        this.address = address;
        this.opcode = opcode;
        this.branchFlags = branchFlags;
    }

    /**
     * Get the address.
     *
     * @return the address
     */
    public int getAddress() {
        return address;
    }

    /**
     * Get the op code.
     *
     * @return the op code
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Get the branch flags.
     *
     * @return the branch flags
     */
    public int getBranchFlags() {
        return branchFlags;
    }

    /**
     * Opcode JO.
     */
    public static final int OP_JO = 0;

    /**
     * Opcode JNO.
     */
    public static final int OP_JNO = 1;

    /**
     * Opcode JC.
     */
    public static final int OP_JC = 2;

    /**
     * Opcode JNC.
     */
    public static final int OP_JNC = 3;

    /**
     * Opcode JZ.
     */
    public static final int OP_JZ = 4;

    /**
     * Opcode JNZ.
     */
    public static final int OP_JNZ = 5;

    /**
     * Opcode JBE.
     */
    public static final int OP_JBE = 6;

    /**
     * Opcode JA.
     */
    public static final int OP_JA = 7;

    /**
     * Opcode JS.
     */
    public static final int OP_JS = 8;

    /**
     * Opcode JNS.
     */
    public static final int OP_JNS = 9;

    /**
     * Opcode JP.
     */
    public static final int OP_JP = 10;

    /**
     * Opcode JNP.
     */
    public static final int OP_JNP = 11;

    /**
     * Opcode JL.
     */
    public static final int OP_JL = 12;

    /**
     * Opcode JGE.
     */
    public static final int OP_JGE = 13;

    /**
     * Opcode JLE.
     */
    public static final int OP_JLE = 14;

    /**
     * Opcode JG.
     */
    public static final int OP_JG = 15;

    /**
     * Conditional branch.
     */
    public static final int BR_CONDITIONAL = 1;

    /**
     * Indirect branch.
     */
    public static final int BR_INDIRECT = 2;

    /**
     * Function call.
     */
    public static final int BR_CALL = 4;

    /**
     * Function return.
     */
    public static final int BR_RETURN = 8;
}
