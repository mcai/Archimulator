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
package archimulator.sim.isa.dissembler;

import archimulator.sim.isa.BitField;
import archimulator.sim.isa.StaticInstruction;
import net.pickapack.math.MathHelper;

/**
 *
 * @author Min Cai
 */
public class MipsDisassembler {
    private static final String[] MIPS_GPR_NAMES = new String[]{"zero", "at", "v0", "v1", "a0", "a1", "a2", "a3", "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "t8", "t9",
            "k0", "k1", "gp", "sp", "s8", "ra"};

    /**
     *
     * @param machineInstruction
     * @return
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
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isRMt(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x10 || func == 0x11);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isRMf(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x12 || func == 0x13);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isROneOp(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x08 || func == 0x09);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isRTwoOp(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func >= 0x18 && func <= 0x1b);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isLoadStore(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);
        return (((opcode >= 0x20) && (opcode <= 0x2e)) || (opcode == 0x30) || (opcode == 0x38));
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isFPLoadStore(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);
        return (opcode == 0x31 || opcode == 0x39);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isOneOpBranch(int machineInstruction) {
        int opcode = BitField.OPCODE.valueOf(machineInstruction);
        return ((opcode == 0x00) || (opcode == 0x01) || (opcode == 0x06) || (opcode == 0x07));
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isShift(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 0x00 || func == 0x01 || func == 0x03);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isCVT(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func == 32 || func == 33 || func == 36);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isCompare(int machineInstruction) {
        int func = BitField.FUNC.valueOf(machineInstruction);
        return (func >= 48);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isGprFpMove(int machineInstruction) {
        int rs = BitField.RS.valueOf(machineInstruction);
        return (rs == 0 || rs == 4);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isGprFcrMove(int machineInstruction) {
        int rs = BitField.RS.valueOf(machineInstruction);
        return (rs == 2 || rs == 6);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isFpBranch(int machineInstruction) {
        int rs = BitField.RS.valueOf(machineInstruction);
        return (rs == 8);
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public static boolean isSystemCall(int machineInstruction) {
        return (BitField.OPCODE_LO.valueOf(machineInstruction) == 0x0 && BitField.FUNC_HI.valueOf(machineInstruction) == 0x1 && BitField.FUNC_LO.valueOf(machineInstruction) == 0x4);
    }

    /**
     *
     * @param pc
     * @param staticInstruction
     * @return
     */
    public static String disassemble(int pc, StaticInstruction staticInstruction) {
        StringBuilder buf = new StringBuilder();

        int machInst = staticInstruction.getMachineInstruction();
        buf.append(String.format("0x%08x: 0x%08x %s ", pc, machInst, staticInstruction.getMnemonic().toString().toLowerCase()));
//        buf.append(String.format("0x%08x: %s ", pc, staticInstruction.getMnemonic().toString().toLowerCase()));

        if (machInst == 0x00000000) {
            return buf.toString().trim();
        }

        MachineInstructionType machineInstructionType = getMachineInstructionType(machInst);

        int imm = MathHelper.signExtend(BitField.INTIMM.valueOf(machInst));

        int rs = BitField.RS.valueOf(machInst);
        int rt = BitField.RT.valueOf(machInst);
        int rd = BitField.RD.valueOf(machInst);

        int fs = BitField.FS.valueOf(machInst);
        int ft = BitField.FT.valueOf(machInst);
        int fd = BitField.FD.valueOf(machInst);

        int shift = BitField.SHIFT.valueOf(machInst);

        int target = BitField.TARGET.valueOf(machInst);

        switch (machineInstructionType) {
            case J:
                buf.append(String.format("%x", target));
                break;
            case I:
                if (isOneOpBranch(machInst)) {
                    buf.append(String.format("$%s, %d", MIPS_GPR_NAMES[rs], imm));
                } else if (isLoadStore(machInst)) {
                    buf.append(String.format("$%s, %d($%s)", MIPS_GPR_NAMES[rt], imm, MIPS_GPR_NAMES[rs]));
                } else if (isFPLoadStore(machInst)) {
                    buf.append(String.format("$f%d, %d($%s)", ft, imm, MIPS_GPR_NAMES[rs]));
                } else {
                    buf.append(String.format("$%s, $%s, %d", MIPS_GPR_NAMES[rt], MIPS_GPR_NAMES[rs], imm));
                }
                break;
            case F:
                if (isCVT(machInst)) {
                    buf.append(String.format("$f%d, $f%d", fd, fs));
                } else if (isCompare(machInst)) {
                    buf.append(String.format("%d, $f%d, $f%d", fd >> 2, fs, ft));
                } else if (isFpBranch(machInst)) {
                    buf.append(String.format("%d, %d", fd >> 2, imm));
                } else if (isGprFpMove(machInst)) {
                    buf.append(String.format("$%s, $f%d", MIPS_GPR_NAMES[rt], fs));
                } else if (isGprFcrMove(machInst)) {
                    buf.append(String.format("$%s, $%d", MIPS_GPR_NAMES[rt], fs));
                } else {
                    buf.append(String.format("$f%d, $f%d, $f%d", fd, fs, ft));
                }
                break;
            case R:
                if (isSystemCall(machInst)) {
                } else if (isShift(machInst)) {
                    buf.append(String.format("$%s, $%s, %d", MIPS_GPR_NAMES[rd], MIPS_GPR_NAMES[rt], shift));
                } else if (isROneOp(machInst)) {
                    buf.append(String.format("$%s", MIPS_GPR_NAMES[rs]));
                } else if (isRTwoOp(machInst)) {
                    buf.append(String.format("$%s, $%s", MIPS_GPR_NAMES[rs], MIPS_GPR_NAMES[rt]));
                } else if (isRMt(machInst)) {
                    buf.append(String.format("$%s", MIPS_GPR_NAMES[rs]));
                } else if (isRMf(machInst)) {
                    buf.append(String.format("$%s", MIPS_GPR_NAMES[rd]));
                } else {
                    buf.append(String.format("$%s, $%s, $%s", MIPS_GPR_NAMES[rd], MIPS_GPR_NAMES[rs], MIPS_GPR_NAMES[rt]));
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        return buf.toString().trim();
    }
}
