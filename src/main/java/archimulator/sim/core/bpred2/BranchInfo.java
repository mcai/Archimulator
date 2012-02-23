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
package archimulator.sim.core.bpred2;

public class BranchInfo {
    private int address;
    private int opcode;
    private int branchFlags;

    public BranchInfo(int address, int opcode, int branchFlags) {
        this.address = address;
        this.opcode = opcode;
        this.branchFlags = branchFlags;
    }

    public int getAddress() {
        return address;
    }

    public int getOpcode() {
        return opcode;
    }

    public int getBranchFlags() {
        return branchFlags;
    }

    public static final int OP_JO = 0;
    public static final int OP_JNO = 1;
    public static final int OP_JC = 2;
    public static final int OP_JNC = 3;
    public static final int OP_JZ = 4;
    public static final int OP_JNZ = 5;
    public static final int OP_JBE = 6;
    public static final int OP_JA = 7;
    public static final int OP_JS = 8;
    public static final int OP_JNS = 9;
    public static final int OP_JP = 10;
    public static final int OP_JNP = 11;
    public static final int OP_JL = 12;
    public static final int OP_JGE = 13;
    public static final int OP_JLE = 14;
    public static final int OP_JG = 15;

    public static final int BR_CONDITIONAL = 1;
    public static final int BR_INDIRECT = 2;
    public static final int BR_CALL = 4;
    public static final int BR_RETURN = 8;
}
