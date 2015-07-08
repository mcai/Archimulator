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
package archimulator.isa;

import archimulator.core.functionalUnit.FunctionalUnitOperationType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Mnemonic.
 *
 * @author Min Cai
 */
public enum Mnemonic {
    /**
     * No operation (NOP).
     */
    NOP,

    /**
     * Break.
     */
    BREAK,

    /**
     * System call.
     */
    SYSTEM_CALL,

    /**
     * Add.
     */
    ADD,

    /**
     * Addi.
     */
    ADDI,

    /**
     * Addiu.
     */
    ADDIU,

    /**
     * Addu.
     */
    ADDU,

    /**
     * Sub.
     */
    SUB,

    /**
     * Subu.
     */
    SUBU,

    /**
     * And.
     */
    AND,

    /**
     * Andi.
     */
    ANDI,

    /**
     * Nor.
     */
    NOR,

    /**
     * Or.
     */
    OR,

    /**
     * Ori.
     */
    ORI,

    /**
     * Xor.
     */
    XOR,

    /**
     * Xori.
     */
    XORI,

    /**
     * Mult.
     */
    MULT,

    /**
     * Multu.
     */
    MULTU,

    /**
     * Div.
     */
    DIV,

    /**
     * Divu.
     */
    DIVU,

    /**
     * Sll.
     */
    SLL,

    /**
     * Sllv.
     */
    SLLV,

    /**
     * Slt.
     */
    SLT,

    /**
     * Slti.
     */
    SLTI,

    /**
     * Sltiu.
     */
    SLTIU,

    /**
     * Sltu.
     */
    SLTU,

    /**
     * Sra.
     */
    SRA,

    /**
     * Srav.
     */
    SRAV,

    /**
     * Srl.
     */
    SRL,

    /**
     * Srlv.
     */
    SRLV,

    /**
     * Madd.
     */
    MADD,

    /**
     * Msub.
     */
    MSUB,

    /**
     * B.
     */
    B,

    /**
     * Bal.
     */
    BAL,

    /**
     * Beq.
     */
    BEQ,

    /**
     * Beql.
     */
    BEQL,

    /**
     * Bgez.
     */
    BGEZ,

    /**
     * Bgezl.
     */
    BGEZL,

    /**
     * Bgezal.
     */
    BGEZAL,

    /**
     * Bgezall.
     */
    BGEZALL,

    /**
     * Bgtz.
     */
    BGTZ,

    /**
     * Bgtzl.
     */
    BGTZL,

    /**
     * Blez.
     */
    BLEZ,

    /**
     * Blezl.
     */
    BLEZL,

    /**
     * Bltz.
     */
    BLTZ,

    /**
     * Bltzl.
     */
    BLTZL,

    /**
     * Bltzal.
     */
    BLTZAL,

    /**
     * Bltzall.
     */
    BLTZALL,

    /**
     * Bne.
     */
    BNE,

    /**
     * Bnel.
     */
    BNEL,


    /**
     * J.
     */
    J,

    /**
     * Jal.
     */
    JAL,

    /**
     * Jalr.
     */
    JALR,

    /**
     * Jr.
     */
    JR,

    /**
     * Lb.
     */
    LB,

    /**
     * Lbu.
     */
    LBU,

    /**
     * Lh.
     */
    LH,

    /**
     * Lhu.
     */
    LHU,

    /**
     * Lui.
     */
    LUI,

    /**
     * Lw.
     */
    LW,

    /**
     * Lwl.
     */
    LWL,

    /**
     * Lwr.
     */
    LWR,

    /**
     * Sb.
     */
    SB,

    /**
     * Sh.
     */
    SH,

    /**
     * Sw.
     */
    SW,

    /**
     * Swl.
     */
    SWL,

    /**
     * Swr.
     */
    SWR,

    /**
     * Ldc1.
     */
    LDC1,

    /**
     * Lwc1.
     */
    LWC1,

    /**
     * Sdc1.
     */
    SDC1,

    /**
     * Swc1.
     */
    SWC1,

    /**
     * Mfhi.
     */
    MFHI,

    /**
     * Mflo.
     */
    MFLO,

    /**
     * Mthi.
     */
    MTHI,

    /**
     * Mtlo.
     */
    MTLO,

    /**
     * Cfc1.
     */
    CFC1,

    /**
     * Ctc1.
     */
    CTC1,

    /**
     * Mfc1.
     */
    MFC1,

    /**
     * Mtc1.
     */
    MTC1,

    /**
     * Ll.
     */
    LL,

    /**
     * Sc.
     */
    SC,

    /**
     * Neg_d.
     */
    NEG_D,

    /**
     * Mov_d.
     */
    MOV_D,

    /**
     * Sqrt_d.
     */
    SQRT_D,

    /**
     * Abs_d.
     */
    ABS_D,

    /**
     * Mul_d.
     */
    MUL_D,

    /**
     * Div_d.
     */
    DIV_D,

    /**
     * Add_d.
     */
    ADD_D,

    /**
     * Sub_d.
     */
    SUB_D,

    /**
     * Mul_s.
     */
    MUL_S,

    /**
     * Div_s.
     */
    DIV_S,

    /**
     * Add_s.
     */
    ADD_S,

    /**
     * Sub_s.
     */
    SUB_S,

    /**
     * Mov_s.
     */
    MOV_S,

    /**
     * Neg_s.
     */
    NEG_S,

    /**
     * Abs_s.
     */
    ABS_S,

    /**
     * Sqrt_s.
     */
    SQRT_S,

    /**
     * C_cond_d.
     */
    C_COND_D,

    /**
     * C_cond_s.
     */
    C_COND_S,

    /**
     * Cvt_d_l.
     */
    CVT_D_L,

    /**
     * Cvt_s_l.
     */
    CVT_S_L,

    /**
     * Cvt_d_w.
     */
    CVT_D_W,

    /**
     * Cvt_s_w.
     */
    CVT_S_W,

    /**
     * Cvt_l_d.
     */
    CVT_L_D,

    /**
     * Cvt_w_d.
     */
    CVT_W_D,

    /**
     * Cvt_s_d.
     */
    CVT_S_D,

    /**
     * Cvt_l_s.
     */
    CVT_L_S,

    /**
     * Cvt_w_s.
     */
    CVT_W_S,

    /**
     * Cvt_d_s.
     */
    CVT_D_S,

    /**
     * Bc1fl.
     */
    BC1FL,

    /**
     * Bc1tl.
     */
    BC1TL,

    /**
     * Bc1f.
     */
    BC1F,

    /**
     * Bc1t.
     */
    BC1T,

    /**
     * Movf.
     */
    MOVF,

    /**
     * _movf.
     */
    _MOVF,

    /**
     * Movn.
     */
    MOVN,

    /**
     * _movn.
     */
    _MOVN,

    /**
     * _movt.
     */
    _MOVT,

    /**
     * Movz.
     */
    MOVZ,

    /**
     * _movz.
     */
    _MOVZ,

    /**
     * Mul.
     */
    MUL,

    /**
     * Trunc_w.
     */
    TRUNC_W,

    /**
     * Unknown.
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
     * Get the list of input dependencies for the specified machine instruction.
     *
     * @param machineInstruction the machine instruction
     * @return the list of input dependencies for the specified machine instruction
     */
    public List<Integer> getInputDependencies(final int machineInstruction) {
        return new ArrayList<Integer>() {{
            for (StaticInstruction.Dependency dependency : Mnemonic.this.getMethod().getAnnotation(InputDependencies.class).value()) {
                add(toRegisterDependency(dependency, machineInstruction));
            }
        }};
    }

    /**
     * Get the list of output dependencies for the specified machine instruction.
     *
     * @param machineInstruction the machine instruction
     * @return the list of output dependencies for the specified machine instruction
     */
    public List<Integer> getOutputDependencies(final int machineInstruction) {
        return new ArrayList<Integer>() {{
            for (StaticInstruction.Dependency dependency : Mnemonic.this.getMethod().getAnnotation(OutputDependencies.class).value()) {
                add(toRegisterDependency(dependency, machineInstruction));
            }
        }};
    }

    /**
     * Get the non effective address base dependency for the specified machine instruction.
     *
     * @param machineInstruction the machine instruction
     * @return the non effective address base dependency for the specified machine instruction
     */
    public int getNonEffectiveAddressBaseDep(int machineInstruction) {
        return toRegisterDependency(this.getMethod().getAnnotation(NonEffectiveAddressBaseDependency.class).value(), machineInstruction);
    }

    /**
     * Get the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Set the method.
     *
     * @param method the method
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

    /**
     * Determine the instruction type for the specified collection of tags.
     *
     * @param flags the collection of tags
     */
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
     * Get the mask.
     *
     * @return the mask
     */
    public int getMask() {
        return this.mask;
    }

    /**
     * Get the bits.
     *
     * @return the bits
     */
    public int getBits() {
        return this.bits;
    }

    /**
     * Get the extra bit field.
     *
     * @return the extra bit field
     */
    public BitField getExtraBitField() {
        return this.extraBitField;
    }

    /**
     * Get the value of the extra bit field.
     *
     * @return the value of the extra bit field
     */
    public int getExtraBitFieldValue() {
        return this.extraBitFieldValue;
    }

    /**
     * Get the static instruction type.
     *
     * @return the static instruction type
     */
    public StaticInstructionType getType() {
        return this.type;
    }

    /**
     * Get the functional unit operation type.
     *
     * @return the functional unit operation type
     */
    public FunctionalUnitOperationType getFunctionalUnitOperationType() {
        return this.functionalUnitOperationType;
    }

    /**
     * Get a value indicating whether the mnemonic is control or not.
     *
     * @return a value indicating whether the mnemonic is control or not
     */
    public boolean isControl() {
        return this.getType() == StaticInstructionType.CONDITIONAL || this.getType() == StaticInstructionType.FUNCTION_CALL || this.getType() == StaticInstructionType.UNCONDITIONAL || this.getType() == StaticInstructionType.FUNCTION_RETURN;
    }

    /**
     * Get the register dependency from the annotated machine instruction.
     *
     * @param dependencyA        the dependency annotation
     * @param machineInstruction the machine instruction
     * @return the register dependency from the annotated machine instruction
     */
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
