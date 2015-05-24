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
package archimulator.isa.dissembler;

import archimulator.isa.BitField;
import archimulator.isa.StaticInstruction;
import archimulator.util.math.MathHelper;

/**
 * MIPS disassembler.
 *
 * @author Min Cai
 */
public class MipsDisassembler {
    private static final String[] MIPS_GPR_NAMES = new String[]{
            "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3", "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "t8", "t9",
            "k0", "k1", "gp", "sp", "s8", "ra"
    };

    /**
     * Get the type of the specified machine instruction.
     *
     * @param machineInstruction the machine instruction
     * @return the type of the specified machine instruction
     */
    public static MachineInstructionType getMachineInstructionType(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);

        switch (opcode) {
            case 0:
                return MachineInstructionType.R;
            case 0x02:
            case 0x03:
                return MachineInstructionType.J;
            case 0x11:
                return MachineInstructionType.F;
            default:
                return MachineInstructionType.I;
        }
    }

    /**
     * Get a value indicating whether the specified machine instruction is RMT or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is RMT or not
     */
    public static boolean isRMt(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x10 || func == 0x11);
    }

    /**
     * Get a value indicating whether the specified machine instruction is RMF or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is RMF or not
     */
    public static boolean isRMf(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x12 || func == 0x13);
    }

    /**
     * Get a value indicating whether the specified machine instruction is ROneOp or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is ROneOp or not
     */
    public static boolean isROneOp(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x08 || func == 0x09);
    }

    /**
     * Get a value indicating whether the specified machine instruction is RTwoOp or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is RTwoOp or not
     */
    public static boolean isRTwoOp(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func >= 0x18 && func <= 0x1b);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a load/store or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a load/store or not
     */
    public static boolean isLoadStore(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);
        return (((opcode >= 0x20) && (opcode <= 0x2e)) || (opcode == 0x30) || (opcode == 0x38));
    }

    /**
     * Get a value indicating whether the specified machine instruction is a floating point load/store or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a floating point load/store or not
     */
    public static boolean isFPLoadStore(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);
        return (opcode == 0x31 || opcode == 0x39);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a one operand branch or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a one operand branch or not
     */
    public static boolean isOneOpBranch(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);
        return ((opcode == 0x00) || (opcode == 0x01) || (opcode == 0x06) || (opcode == 0x07));
    }

    /**
     * Get a value indicating whether the specified machine instruction is a shift operation or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a shift operation or not
     */
    public static boolean isShift(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x00 || func == 0x01 || func == 0x03);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a convert operation or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a convert operation or not
     */
    public static boolean isCVT(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 32 || func == 33 || func == 36);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a compare operation or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a compare operation or not
     */
    public static boolean isCompare(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func >= 48);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a move operation from a GPR to a floating point register or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a move operation from a GPR to a floating point register or not
     */
    public static boolean isGprFpMove(int machineInstruction) {
        int rs = BitField.RS.valueOf(machineInstruction);
        return (rs == 0 || rs == 4);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a move operation from a GPR to a floating point register or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a move operation from a GPR to the FCR or not
     */
    public static boolean isGprFcrMove(int machineInstruction) {
        int rs = BitField.RS.valueOf(machineInstruction);
        return (rs == 2 || rs == 6);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a floating point branch operation or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a floating point branch operation or not
     */
    public static boolean isFpBranch(int machineInstruction) {
        int rs = BitField.RS.valueOf(machineInstruction);
        return (rs == 8);
    }

    /**
     * Get a value indicating whether the specified machine instruction is a system call or not.
     *
     * @param machineInstruction the machine instruction
     * @return a value indicating whether the specified machine instruction is a system call or not
     */
    public static boolean isSystemCall(int machineInstruction) {
        return (BitField.OPCODE_LO.valueOf(machineInstruction) == 0x0 && BitField.FUNC_HI.valueOf(machineInstruction) == 0x1 && BitField.FUNC_LO.valueOf(machineInstruction) == 0x4);
    }

    /**
     * Disassemble the specified static instruction at the specified program counter (PC).
     *
     * @param pc                the program counter (PC) value
     * @param staticInstruction the static instruction
     * @return the text representation of the disassembled instruction
     */
    public static String disassemble(int pc, StaticInstruction staticInstruction) {
        StringBuilder sb = new StringBuilder();

        int machineInstruction = staticInstruction.getMachineInstruction();
        sb.append(String.format("0x%08x: 0x%08x %s ", pc, machineInstruction, staticInstruction.getMnemonic().toString().toLowerCase()));

        if (machineInstruction == 0x00000000) {
            return sb.toString().trim();
        }

        MachineInstructionType machineInstructionType = getMachineInstructionType(machineInstruction);

        int imm = MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction));

        int rs = BitField.RS.valueOf(machineInstruction);
        int rt = BitField.RT.valueOf(machineInstruction);
        int rd = BitField.RD.valueOf(machineInstruction);

        int fs = BitField.FS.valueOf(machineInstruction);
        int ft = BitField.FT.valueOf(machineInstruction);
        int fd = BitField.FD.valueOf(machineInstruction);

        int shift = BitField.SHIFT.valueOf(machineInstruction);

        int target = BitField.TARGET.valueOf(machineInstruction);

        switch (machineInstructionType) {
            case J:
                sb.append(String.format("%x", target));
                break;
            case I:
                if (isOneOpBranch(machineInstruction)) {
                    sb.append(String.format("$%s, %d", MIPS_GPR_NAMES[rs], imm));
                } else if (isLoadStore(machineInstruction)) {
                    sb.append(String.format("$%s, %d($%s)", MIPS_GPR_NAMES[rt], imm, MIPS_GPR_NAMES[rs]));
                } else if (isFPLoadStore(machineInstruction)) {
                    sb.append(String.format("$f%d, %d($%s)", ft, imm, MIPS_GPR_NAMES[rs]));
                } else {
                    sb.append(String.format("$%s, $%s, %d", MIPS_GPR_NAMES[rt], MIPS_GPR_NAMES[rs], imm));
                }
                break;
            case F:
                if (isCVT(machineInstruction)) {
                    sb.append(String.format("$f%d, $f%d", fd, fs));
                } else if (isCompare(machineInstruction)) {
                    sb.append(String.format("%d, $f%d, $f%d", fd >> 2, fs, ft));
                } else if (isFpBranch(machineInstruction)) {
                    sb.append(String.format("%d, %d", fd >> 2, imm));
                } else if (isGprFpMove(machineInstruction)) {
                    sb.append(String.format("$%s, $f%d", MIPS_GPR_NAMES[rt], fs));
                } else if (isGprFcrMove(machineInstruction)) {
                    sb.append(String.format("$%s, $%d", MIPS_GPR_NAMES[rt], fs));
                } else {
                    sb.append(String.format("$f%d, $f%d, $f%d", fd, fs, ft));
                }
                break;
            case R:
                if (!isSystemCall(machineInstruction)) {
                    if (isShift(machineInstruction)) {
                        sb.append(String.format("$%s, $%s, %d", MIPS_GPR_NAMES[rd], MIPS_GPR_NAMES[rt], shift));
                    } else if (isROneOp(machineInstruction)) {
                        sb.append(String.format("$%s", MIPS_GPR_NAMES[rs]));
                    } else if (isRTwoOp(machineInstruction)) {
                        sb.append(String.format("$%s, $%s", MIPS_GPR_NAMES[rs], MIPS_GPR_NAMES[rt]));
                    } else if (isRMt(machineInstruction)) {
                        sb.append(String.format("$%s", MIPS_GPR_NAMES[rs]));
                    } else if (isRMf(machineInstruction)) {
                        sb.append(String.format("$%s", MIPS_GPR_NAMES[rd]));
                    } else {
                        sb.append(String.format("$%s, $%s, $%s", MIPS_GPR_NAMES[rd], MIPS_GPR_NAMES[rs], MIPS_GPR_NAMES[rt]));
                    }
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        return sb.toString().trim();
    }
}
