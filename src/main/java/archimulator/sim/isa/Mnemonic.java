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
package archimulator.sim.isa;

import archimulator.sim.core.FunctionalUnitOperationType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public enum Mnemonic {
    /**
     *
     */
    NOP,
    /**
     *
     */
    BREAK,

    /**
     *
     */
    SYSTEM_CALL,

    /**
     *
     */
    ADD,
    /**
     *
     */
    ADDI,
    /**
     *
     */
    ADDIU,
    /**
     *
     */
    ADDU,
    /**
     *
     */
    SUB,
    /**
     *
     */
    SUBU,
    /**
     *
     */
    AND,
    /**
     *
     */
    ANDI,
    /**
     *
     */
    NOR,
    /**
     *
     */
    OR,
    /**
     *
     */
    ORI,
    /**
     *
     */
    XOR,
    /**
     *
     */
    XORI,
    /**
     *
     */
    MULT,
    /**
     *
     */
    MULTU,
    /**
     *
     */
    DIV,
    /**
     *
     */
    DIVU,
    /**
     *
     */
    SLL,
    /**
     *
     */
    SLLV,
    /**
     *
     */
    SLT,
    /**
     *
     */
    SLTI,
    /**
     *
     */
    SLTIU,
    /**
     *
     */
    SLTU,
    /**
     *
     */
    SRA,
    /**
     *
     */
    SRAV,
    /**
     *
     */
    SRL,
    /**
     *
     */
    SRLV,
    /**
     *
     */
    MADD,
    /**
     *
     */
    MSUB,

    /**
     *
     */
    B,
    /**
     *
     */
    BAL,
    /**
     *
     */
    BEQ,
    /**
     *
     */
    BEQL,
    /**
     *
     */
    BGEZ,
    /**
     *
     */
    BGEZL,
    /**
     *
     */
    BGEZAL,
    /**
     *
     */
    BGEZALL,
    /**
     *
     */
    BGTZ,
    /**
     *
     */
    BGTZL,
    /**
     *
     */
    BLEZ,
    /**
     *
     */
    BLEZL,
    /**
     *
     */
    BLTZ,
    /**
     *
     */
    BLTZL,
    /**
     *
     */
    BLTZAL,
    /**
     *
     */
    BLTZALL,
    /**
     *
     */
    BNE,
    /**
     *
     */
    BNEL,

    /**
     *
     */
    J,
    /**
     *
     */
    JAL,
    /**
     *
     */
    JALR,
    /**
     *
     */
    JR,

    /**
     *
     */
    LB,
    /**
     *
     */
    LBU,
    /**
     *
     */
    LH,
    /**
     *
     */
    LHU,
    /**
     *
     */
    LUI,
    /**
     *
     */
    LW,
    /**
     *
     */
    LWL,
    /**
     *
     */
    LWR,

    /**
     *
     */
    SB,
    /**
     *
     */
    SH,
    /**
     *
     */
    SW,
    /**
     *
     */
    SWL,
    /**
     *
     */
    SWR,

    /**
     *
     */
    LDC1,
    /**
     *
     */
    LWC1,

    /**
     *
     */
    SDC1,
    /**
     *
     */
    SWC1,

    /**
     *
     */
    MFHI,
    /**
     *
     */
    MFLO,
    /**
     *
     */
    MTHI,
    /**
     *
     */
    MTLO,

    /**
     *
     */
    CFC1,
    /**
     *
     */
    CTC1,
    /**
     *
     */
    MFC1,
    /**
     *
     */
    MTC1,

    /**
     *
     */
    LL,
    /**
     *
     */
    SC,

    /**
     *
     */
    NEG_D,
    /**
     *
     */
    MOV_D,
    /**
     *
     */
    SQRT_D,
    /**
     *
     */
    ABS_D,
    /**
     *
     */
    MUL_D,
    /**
     *
     */
    DIV_D,
    /**
     *
     */
    ADD_D,
    /**
     *
     */
    SUB_D,

    /**
     *
     */
    MUL_S,
    /**
     *
     */
    DIV_S,
    /**
     *
     */
    ADD_S,
    /**
     *
     */
    SUB_S,
    /**
     *
     */
    MOV_S,
    /**
     *
     */
    NEG_S,
    /**
     *
     */
    ABS_S,
    /**
     *
     */
    SQRT_S,

    /**
     *
     */
    C_COND_D,
    /**
     *
     */
    C_COND_S,

    /**
     *
     */
    CVT_D_L,
    /**
     *
     */
    CVT_S_L,
    /**
     *
     */
    CVT_D_W,
    /**
     *
     */
    CVT_S_W,
    /**
     *
     */
    CVT_L_D,
    /**
     *
     */
    CVT_W_D,
    /**
     *
     */
    CVT_S_D,
    /**
     *
     */
    CVT_L_S,
    /**
     *
     */
    CVT_W_S,
    /**
     *
     */
    CVT_D_S,

    /**
     *
     */
    BC1FL,
    /**
     *
     */
    BC1TL,
    /**
     *
     */
    BC1F,
    /**
     *
     */
    BC1T,

    /**
     *
     */
    MOVF,
    /**
     *
     */
    _MOVF,
    /**
     *
     */
    MOVN,
    /**
     *
     */
    _MOVN,
    /**
     *
     */
    _MOVT,
    /**
     *
     */
    MOVZ,
    /**
     *
     */
    _MOVZ,
    /**
     *
     */
    MUL,
    /**
     *
     */
    TRUNC_W,

    /**
     *
     */
    UNKNOWN;

    private Method method;

    private int mask;
    private int bits;
    private BitField extraBitField;
    private int extraBitFieldValue;

    private StaticInstructionType type;
    private FunctionalUnitOperationType functionalUnitOperationType;

    /**
     *
     * @param machineInstruction
     * @return
     */
    public List<Integer> getInputDependencies(final int machineInstruction) {
        return new ArrayList<Integer>() {{
            for (StaticInstruction.Dependency dependency : Mnemonic.this.getMethod().getAnnotation(InputDependencies.class).value()) {
                add(toRegisterDependency(dependency, machineInstruction));
            }
        }};
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public List<Integer> getOutputDependencies(final int machineInstruction) {
        return new ArrayList<Integer>() {{
            for (StaticInstruction.Dependency dependency : Mnemonic.this.getMethod().getAnnotation(OutputDependencies.class).value()) {
                add(toRegisterDependency(dependency, machineInstruction));
            }
        }};
    }

    /**
     *
     * @param machineInstruction
     * @return
     */
    public int getNonEffectiveAddressBaseDep(int machineInstruction) {
        return toRegisterDependency(this.getMethod().getAnnotation(NonEffectiveAddressBaseDependency.class).value(), machineInstruction);
    }

    /**
     *
     * @return
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     *
     * @param method
     */
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

        StaticInstructionIntrinsic staticInstructionIntrinsic = method.getAnnotation(StaticInstructionIntrinsic.class);

        Collection<StaticInstructionFlag> flags = Arrays.asList(method.getAnnotation(StaticInstructionFlags.class).value());

        this.determineInstructionType(flags);

        this.functionalUnitOperationType = staticInstructionIntrinsic.functionalUnitOperationType();

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

    /**
     *
     * @return
     */
    public int getMask() {
        return this.mask;
    }

    /**
     *
     * @return
     */
    public int getBits() {
        return this.bits;
    }

    /**
     *
     * @return
     */
    public BitField getExtraBitField() {
        return this.extraBitField;
    }

    /**
     *
     * @return
     */
    public int getExtraBitFieldValue() {
        return this.extraBitFieldValue;
    }

    /**
     *
     * @return
     */
    public StaticInstructionType getType() {
        return this.type;
    }

    /**
     *
     * @return
     */
    public FunctionalUnitOperationType getFunctionalUnitOperationType() {
        return this.functionalUnitOperationType;
    }

    /**
     *
     * @return
     */
    public boolean isControl() {
        return this.getType() == StaticInstructionType.CONDITIONAL || this.getType() == StaticInstructionType.FUNCTION_CALL || this.getType() == StaticInstructionType.UNCONDITIONAL || this.getType() == StaticInstructionType.FUNCTION_RETURN;
    }

    private static int toRegisterDependency(StaticInstruction.Dependency dependencyA, int machineInstruction) {
        switch (dependencyA) {
            case RS:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, BitField.RS.valueOf(machineInstruction));
            case RT:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, BitField.RT.valueOf(machineInstruction));
            case RD:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, BitField.RD.valueOf(machineInstruction));
            case FS:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, BitField.FS.valueOf(machineInstruction));
            case FT:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, BitField.FT.valueOf(machineInstruction));
            case FD:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, BitField.FD.valueOf(machineInstruction));
            case REGISTER_RA:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, ArchitecturalRegisterFile.REGISTER_RA);
            case REGISTER_V0:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, ArchitecturalRegisterFile.REGISTER_V0);
            case REGISTER_HI:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, ArchitecturalRegisterFile.REGISTER_MISC_HI);
            case REGISTER_LO:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, ArchitecturalRegisterFile.REGISTER_MISC_LO);
            case REGISTER_FCSR:
                return RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, ArchitecturalRegisterFile.REGISTER_MISC_FCSR);
            default:
                throw new IllegalArgumentException();
        }
    }
}
