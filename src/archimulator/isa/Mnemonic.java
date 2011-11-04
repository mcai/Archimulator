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
package archimulator.isa;

import archimulator.core.FunctionalUnitOperationType;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum Mnemonic implements Serializable {
    NOP,
    BREAK,

    SYSCALL,

    ADD,
    ADDI,
    ADDIU,
    ADDU,
    SUB,
    SUBU,
    AND,
    ANDI,
    NOR,
    OR,
    ORI,
    XOR,
    XORI,
    MULT,
    MULTU,
    DIV,
    DIVU,
    SLL,
    SLLV,
    SLT,
    SLTI,
    SLTIU,
    SLTU,
    SRA,
    SRAV,
    SRL,
    SRLV,
    MADD,
    MSUB,

    B,
    BAL,
    BEQ,
    BEQL,
    BGEZ,
    BGEZL,
    BGEZAL,
    BGEZALL,
    BGTZ,
    BGTZL,
    BLEZ,
    BLEZL,
    BLTZ,
    BLTZL,
    BLTZAL,
    BLTZALL,
    BNE,
    BNEL,

    J,
    JAL,
    JALR,
    JR,

    LB,
    LBU,
    LH,
    LHU,
    LUI,
    LW,
    LWL,
    LWR,

    SB,
    SH,
    SW,
    SWL,
    SWR,

    LDC1,
    LWC1,

    SDC1,
    SWC1,

    MFHI,
    MFLO,
    MTHI,
    MTLO,

    CFC1,
    CTC1,
    MFC1,
    MTC1,

    LL,
    SC,

    NEG_D,
    MOV_D,
    SQRT_D,
    ABS_D,
    MUL_D,
    DIV_D,
    ADD_D,
    SUB_D,

    MUL_S,
    DIV_S,
    ADD_S,
    SUB_S,
    MOV_S,
    NEG_S,
    ABS_S,
    SQRT_S,

    C_COND_D,
    C_COND_S,

    CVT_D_L,
    CVT_S_L,
    CVT_D_W,
    CVT_S_W,
    CVT_L_D,
    CVT_W_D,
    CVT_S_D,
    CVT_L_S,
    CVT_W_S,
    CVT_D_S,

    BC1FL,
    BC1TL,
    BC1F,
    BC1T,

    MOVF, _MOVF, MOVN, _MOVN, _MOVT, MOVZ, _MOVZ, MUL, TRUNC_W,

    UNKNOWN;

    private Method method;

    private int mask;
    private int bits;
    private BitField extraBitField;
    private int extraBitFieldValue;

    private StaticInstructionType type;
    private FunctionalUnitOperationType fuOperationType;

    public List<Integer> getIdeps(int machInst) {
        List<Integer> ideps = new ArrayList<Integer>();

        for (StaticInstruction.Dep depA : this.getMethod().getAnnotation(Ideps.class).value()) {
            ideps.add(toRegisterDependency(depA, machInst));
        }

        return ideps;
    }

    public List<Integer> getOdeps(int machInst) {
        List<Integer> odeps = new ArrayList<Integer>();

        for (StaticInstruction.Dep depA : this.getMethod().getAnnotation(Odeps.class).value()) {
            odeps.add(toRegisterDependency(depA, machInst));
        }

        return odeps;
    }

    public int getNonEffectiveAddressBaseDep(int machInst) {
        return toRegisterDependency(this.getMethod().getAnnotation(NonEffectiveAddressBaseDep.class).value(), machInst);
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;

        DecodeMethod decodeMethod = method.getAnnotation(DecodeMethod.class);
        DecodeCondition decodeCondition = method.getAnnotation(DecodeCondition.class);

        this.mask = decodeMethod.mask();
        this.bits = decodeMethod.bits();

        if (decodeCondition != null) {
            this.extraBitField = BitField.get(decodeCondition.bitField());
            this.extraBitFieldValue = decodeCondition.value();
        }

        StaticInstructionIntrinsic staticInstIntrinsic = method.getAnnotation(StaticInstructionIntrinsic.class);

        Collection<StaticInstructionFlag> flags = Arrays.asList(method.getAnnotation(StaticInstructionFlags.class).value());

        this.determineInstructionType(flags);

        this.fuOperationType = staticInstIntrinsic.fuOperationType();

    }

    private void determineInstructionType(Collection<StaticInstructionFlag> flags) {
        if (flags.contains(StaticInstructionFlag.INTEGER_COMPUTATION)) {
            this.type = StaticInstructionType.INTEGER_COMPUTATION;
        } else if (flags.contains(StaticInstructionFlag.FLOAT_COMPUTATION)) {
            this.type = StaticInstructionType.FLOAT_COMPUTATION;
        } else if (flags.contains(StaticInstructionFlag.LOAD)) {
            this.type = StaticInstructionType.LOAD;
        } else if (flags.contains(StaticInstructionFlag.STORE)) {
            this.type = StaticInstructionType.STORE;
        } else if (flags.contains(StaticInstructionFlag.CONDITIONAL)) {
            this.type = StaticInstructionType.CONDITIONAL;
        } else if (flags.contains(StaticInstructionFlag.FUNCTION_CALL)) {
            this.type = StaticInstructionType.FUNCTION_CALL;
        } else if (flags.contains(StaticInstructionFlag.UNCONDITIONAL) && flags.contains(StaticInstructionFlag.DIRECT_JUMP)) {
            this.type = StaticInstructionType.UNCONDITIONAL;
        } else if (flags.contains(StaticInstructionFlag.UNCONDITIONAL) && flags.contains(StaticInstructionFlag.INDIRECT_JUMP)) {
            this.type = StaticInstructionType.FUNCTION_RETURN;
        } else if (flags.contains(StaticInstructionFlag.TRAP)) {
            this.type = StaticInstructionType.TRAP;
        } else if (flags.contains(StaticInstructionFlag.NOP)) {
            this.type = StaticInstructionType.NOP;
        } else if (flags.contains(StaticInstructionFlag.UNIMPLEMENTED)) {
            this.type = StaticInstructionType.UNIMPLEMENTED;
        } else if (flags.contains(StaticInstructionFlag.UNKNOWN)) {
            this.type = StaticInstructionType.UNKNOWN;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getMask() {
        return this.mask;
    }

    public int getBits() {
        return this.bits;
    }

    public BitField getExtraBitField() {
        return this.extraBitField;
    }

    public int getExtraBitFieldValue() {
        return this.extraBitFieldValue;
    }

    public StaticInstructionType getType() {
        return this.type;
    }

    public FunctionalUnitOperationType getFuOperationType() {
        return this.fuOperationType;
    }

    public boolean isControl() {
        return this.getType() == StaticInstructionType.CONDITIONAL || this.getType() == StaticInstructionType.FUNCTION_CALL || this.getType() == StaticInstructionType.UNCONDITIONAL || this.getType() == StaticInstructionType.FUNCTION_RETURN;
    }

    private static int toRegisterDependency(StaticInstruction.Dep depA, int machInst) {
        switch (depA) {
            case RS:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, BitField.RS.valueOf(machInst));
            case RT:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, BitField.RT.valueOf(machInst));
            case RD:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, BitField.RD.valueOf(machInst));
            case FS:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, BitField.FS.valueOf(machInst));
            case FT:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, BitField.FT.valueOf(machInst));
            case FD:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, BitField.FD.valueOf(machInst));
            case REG_RA:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, ArchitecturalRegisterFile.REG_RA);
            case REG_V0:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, ArchitecturalRegisterFile.REG_V0);
            case REG_HI:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, ArchitecturalRegisterFile.MISC_REG_HI);
            case REG_LO:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, ArchitecturalRegisterFile.MISC_REG_LO);
            case REG_FCSR:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, ArchitecturalRegisterFile.MISC_REG_FCSR);
            default:
                throw new IllegalArgumentException();
        }
    }
}
