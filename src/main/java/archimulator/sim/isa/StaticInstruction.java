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

import archimulator.sim.common.Logger;
import archimulator.sim.core.FunctionalUnitOperationType;
import archimulator.sim.os.Context;
import archimulator.sim.os.FunctionCallContext;
import net.pickapack.Reference;
import net.pickapack.math.MathHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StaticInstruction {
    private Mnemonic mnemonic;
    private int machineInstruction;
    private List<Integer> inputDependencies;
    private List<Integer> outputDependencies;

    private int nonEffectiveAddressBaseDependency;

    private Map<RegisterDependencyType, Integer> numFreePhysicalRegistersToAllocate = new EnumMap<RegisterDependencyType, Integer>(RegisterDependencyType.class);

    public StaticInstruction(Mnemonic mnemonic, int machineInstruction) {
        this.mnemonic = mnemonic;
        this.machineInstruction = machineInstruction;
        this.inputDependencies = mnemonic.getInputDependencies(machineInstruction);
        this.outputDependencies = mnemonic.getOutputDependencies(machineInstruction);

        if (this.mnemonic.getType() == StaticInstructionType.LOAD || this.mnemonic.getType() == StaticInstructionType.STORE) {
            this.nonEffectiveAddressBaseDependency = mnemonic.getNonEffectiveAddressBaseDep(machineInstruction);
        }

        for (int outputDependency : this.outputDependencies) {
            if (outputDependency != 0) {
                RegisterDependencyType outputDependencyType = RegisterDependencyType.getType(outputDependency);

                if (!this.numFreePhysicalRegistersToAllocate.containsKey(outputDependencyType)) {
                    this.numFreePhysicalRegistersToAllocate.put(outputDependencyType, 0);
                }

                this.numFreePhysicalRegistersToAllocate.put(outputDependencyType, this.numFreePhysicalRegistersToAllocate.get(outputDependencyType) + 1);
            }
        }
    }

    public Mnemonic getMnemonic() {
        return mnemonic;
    }

    public int getMachineInstruction() {
        return machineInstruction;
    }

    public List<Integer> getInputDependencies() {
        return inputDependencies;
    }

    public List<Integer> getOutputDependencies() {
        return outputDependencies;
    }

    public int getNonEffectiveAddressBaseDependency() {
        return nonEffectiveAddressBaseDependency;
    }

    public Map<RegisterDependencyType, Integer> getNumFreePhysicalRegistersToAllocate() {
        return numFreePhysicalRegistersToAllocate;
    }

    private static final int FMT_SINGLE = 16;
    private static final int FMT_DOUBLE = 17;
    private static final int FMT_WORD = 20;
    private static final int FMT_LONG = 21;
    private static final int FMT_PS = 22;

    private static final int FMT3_SINGLE = 0;
    private static final int FMT3_DOUBLE = 1;
    private static final int FMT3_WORD = 4;
    private static final int FMT3_LONG = 5;
    private static final int FMT3_PS = 6;

    public static enum Dependency {
        RS, RT, RD, FS, FT, FD, REGISTER_RA, REGISTER_V0, REGISTER_HI, REGISTER_LO, REGISTER_FCSR
    }

    public static final List<Mnemonic> machineInstructionDecoderInfos = new ArrayList<Mnemonic>();

    private static void registerInstruction(Mnemonic mnemonic) {
        machineInstructionDecoderInfos.add(mnemonic);
    }

    static {
        Method[] declaredMethods = StaticInstruction.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            StaticInstructionIntrinsic staticInstIntrinsic = declaredMethod.getAnnotation(StaticInstructionIntrinsic.class);
            if (staticInstIntrinsic != null) {
                staticInstIntrinsic.mnemonic().setMethod(declaredMethod);
            }
        }

        registerInstruction(Mnemonic.NOP);
        registerInstruction(Mnemonic.BC1F);
        registerInstruction(Mnemonic.BC1T);
        registerInstruction(Mnemonic.MFC1);
        registerInstruction(Mnemonic.MTC1);
        registerInstruction(Mnemonic.CFC1);
        registerInstruction(Mnemonic.CTC1);
        registerInstruction(Mnemonic.ABS_S);
        registerInstruction(Mnemonic.ABS_D);
        registerInstruction(Mnemonic.ADD);
        registerInstruction(Mnemonic.ADD_S);
        registerInstruction(Mnemonic.ADD_D);
        registerInstruction(Mnemonic.ADDI);
        registerInstruction(Mnemonic.ADDIU);
        registerInstruction(Mnemonic.ADDU);
        registerInstruction(Mnemonic.AND);
        registerInstruction(Mnemonic.ANDI);
        registerInstruction(Mnemonic.B);
        registerInstruction(Mnemonic.BAL);
        registerInstruction(Mnemonic.BEQ);
        registerInstruction(Mnemonic.BGEZ);
        registerInstruction(Mnemonic.BGEZAL);
        registerInstruction(Mnemonic.BGTZ);
        registerInstruction(Mnemonic.BLEZ);
        registerInstruction(Mnemonic.BLTZ);
        registerInstruction(Mnemonic.BNE);
        registerInstruction(Mnemonic.BREAK);
        registerInstruction(Mnemonic.C_COND_S);
        registerInstruction(Mnemonic.C_COND_D);
        registerInstruction(Mnemonic.CVT_D_S);
        registerInstruction(Mnemonic.CVT_D_W);
        registerInstruction(Mnemonic.CVT_D_L);
        registerInstruction(Mnemonic.CVT_S_D);
        registerInstruction(Mnemonic.CVT_S_W);
        registerInstruction(Mnemonic.CVT_S_L);
        registerInstruction(Mnemonic.CVT_W_S);
        registerInstruction(Mnemonic.CVT_W_D);
        registerInstruction(Mnemonic.DIV);
        registerInstruction(Mnemonic.DIV_S);
        registerInstruction(Mnemonic.DIV_D);
        registerInstruction(Mnemonic.DIVU);
        registerInstruction(Mnemonic.J);
        registerInstruction(Mnemonic.JAL);
        registerInstruction(Mnemonic.JALR);
        registerInstruction(Mnemonic.JR);
        registerInstruction(Mnemonic.LB);
        registerInstruction(Mnemonic.LBU);
        registerInstruction(Mnemonic.LDC1);
        registerInstruction(Mnemonic.LH);
        registerInstruction(Mnemonic.LHU);
        registerInstruction(Mnemonic.LL);
        registerInstruction(Mnemonic.LUI);
        registerInstruction(Mnemonic.LW);
        registerInstruction(Mnemonic.LWC1);
        registerInstruction(Mnemonic.LWL);
        registerInstruction(Mnemonic.LWR);
        registerInstruction(Mnemonic.MADD);
        registerInstruction(Mnemonic.MFHI);
        registerInstruction(Mnemonic.MFLO);
        registerInstruction(Mnemonic.MOV_S);
        registerInstruction(Mnemonic.MOV_D);
        registerInstruction(Mnemonic.MOVF);
        registerInstruction(Mnemonic._MOVF);
        registerInstruction(Mnemonic.MOVN);
        registerInstruction(Mnemonic._MOVN);
        registerInstruction(Mnemonic._MOVT);
        registerInstruction(Mnemonic.MOVZ);
        registerInstruction(Mnemonic._MOVZ);
        registerInstruction(Mnemonic.MSUB);
        registerInstruction(Mnemonic.MTLO);
        registerInstruction(Mnemonic.MUL);
        registerInstruction(Mnemonic.MUL_S);
        registerInstruction(Mnemonic.MUL_D);
        registerInstruction(Mnemonic.MULT);
        registerInstruction(Mnemonic.MULTU);
        registerInstruction(Mnemonic.NEG_S);
        registerInstruction(Mnemonic.NEG_D);
        registerInstruction(Mnemonic.NOR);
        registerInstruction(Mnemonic.OR);
        registerInstruction(Mnemonic.ORI);
        registerInstruction(Mnemonic.SB);
        registerInstruction(Mnemonic.SC);
        registerInstruction(Mnemonic.SDC1);
        registerInstruction(Mnemonic.SH);
        registerInstruction(Mnemonic.SLL);
        registerInstruction(Mnemonic.SLLV);
        registerInstruction(Mnemonic.SLT);
        registerInstruction(Mnemonic.SLTI);
        registerInstruction(Mnemonic.SLTIU);
        registerInstruction(Mnemonic.SLTU);
        registerInstruction(Mnemonic.SQRT_S);
        registerInstruction(Mnemonic.SQRT_D);
        registerInstruction(Mnemonic.SRA);
        registerInstruction(Mnemonic.SRAV);
        registerInstruction(Mnemonic.SRL);
        registerInstruction(Mnemonic.SRLV);
        registerInstruction(Mnemonic.SUB_S);
        registerInstruction(Mnemonic.SUB_D);
        registerInstruction(Mnemonic.SUBU);
        registerInstruction(Mnemonic.SW);
        registerInstruction(Mnemonic.SWC1);
        registerInstruction(Mnemonic.SWL);
        registerInstruction(Mnemonic.SWR);
        registerInstruction(Mnemonic.SYSTEM_CALL);
        registerInstruction(Mnemonic.TRUNC_W);
        registerInstruction(Mnemonic.XOR);
        registerInstruction(Mnemonic.XORI);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADD, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000020, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void add(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) + context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADDI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x20000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void addi(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) + MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADDIU, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x24000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void addiu(Context context, int machineInstruction) {
        if (BitField.RT.valueOf(machineInstruction) == 0 && MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction)) != 0 && !context.isSpeculative()) {
            context.setPseudoCallEncounteredInLastInstructionExecution(true);
            context.getBlockingEventDispatcher().dispatch(new PseudoCallEncounteredEvent(context, BitField.RS.valueOf(machineInstruction), MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction))));
        } else {
            context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) + MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction)));
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADDU, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000021, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void addu(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) + context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.AND, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000024, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void and(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) & context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ANDI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x30000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void andi(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) & MathHelper.zeroExtend(BitField.INTIMM.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIV, functionalUnitOperationType = FunctionalUnitOperationType.INT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0000001a, mask = 0xfc00ffff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({Dependency.REGISTER_HI, Dependency.REGISTER_LO})
    private static void div(Context context, int machineInstruction) {
        int rs = context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction));
        int rt = context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction));

        context.getRegisterFile().setHi(rt != 0 ? rs % rt : 0);
        context.getRegisterFile().setLo(rt != 0 ? rs / rt : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIVU, functionalUnitOperationType = FunctionalUnitOperationType.INT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0000001b, mask = 0xfc00003f)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({Dependency.REGISTER_HI, Dependency.REGISTER_LO})
    private static void divu(Context context, int machineInstruction) {
        long rs = ((long) context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction))) << 32 >>> 32;
        long rt = ((long) context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) << 32 >>> 32;

        context.getRegisterFile().setHi(rt != 0 ? (int) (((rs % rt) << 32) >> 32) : 0);
        context.getRegisterFile().setLo(rt != 0 ? (int) (((rs / rt) << 32) >> 32) : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LUI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x3c000000, mask = 0xffe00000)
    @InputDependencies({})
    @OutputDependencies(Dependency.RT)
    private static void lui(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), BitField.INTIMM.valueOf(machineInstruction) << 16);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MADD, functionalUnitOperationType = FunctionalUnitOperationType.INT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x70000000, mask = 0xfc00ffff)
    @InputDependencies({Dependency.RS, Dependency.RT, Dependency.REGISTER_HI, Dependency.REGISTER_LO})
    @OutputDependencies({Dependency.REGISTER_HI, Dependency.REGISTER_LO})
    private static void madd(Context context, int machineInstruction) {
        long temp = (long) context.getRegisterFile().getHi() << 32 | (long) context.getRegisterFile().getLo() + context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) * context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction));

        context.getRegisterFile().setHi((int) (temp >> 32));
        context.getRegisterFile().setLo((int) ((temp << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MFHI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000010, mask = 0xffff07ff)
    @InputDependencies(Dependency.REGISTER_HI)
    @OutputDependencies(Dependency.RD)
    private static void mfhi(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getHi());
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MFLO, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000012, mask = 0xffff07ff)
    @InputDependencies(Dependency.REGISTER_LO)
    @OutputDependencies(Dependency.RD)
    private static void mflo(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getLo());
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MSUB, functionalUnitOperationType = FunctionalUnitOperationType.INT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x70000004, mask = 0xfc00ffff)
    @InputDependencies({Dependency.RS, Dependency.RT, Dependency.REGISTER_HI, Dependency.REGISTER_LO})
    @OutputDependencies({Dependency.REGISTER_HI, Dependency.REGISTER_LO})
    private static void msub(Context context, int machineInstruction) {
        long temp = (long) context.getRegisterFile().getHi() << 32 | (long) context.getRegisterFile().getLo() - context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) * context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction));

        context.getRegisterFile().setHi((int) (temp >> 32));
        context.getRegisterFile().setLo((int) ((temp << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MTHI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RD)
    @OutputDependencies(Dependency.REGISTER_HI)
    private static void mthi(Context context, int machineInstruction) {
        context.getRegisterFile().setHi(context.getRegisterFile().getGpr(BitField.RD.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MTLO, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000013, mask = 0xfc1fffff)
    @InputDependencies(Dependency.RD)
    @OutputDependencies(Dependency.REGISTER_LO)
    private static void mtlo(Context context, int machineInstruction) {
        context.getRegisterFile().setLo(context.getRegisterFile().getGpr(BitField.RD.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MULT, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000018, mask = 0xfc00003f)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({Dependency.REGISTER_LO, Dependency.REGISTER_HI})
    private static void mult(Context context, int machineInstruction) {
        long product = (long) (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction))) * (long) (context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));

        context.getRegisterFile().setHi((int) (product >> 32));
        context.getRegisterFile().setLo((int) ((product << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MULTU, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000019, mask = 0xfc00003f)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({Dependency.REGISTER_LO, Dependency.REGISTER_HI})
    private static void multu(Context context, int machineInstruction) {
        long product = (((long) context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction))) << 32 >>> 32) * (((long) context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) << 32 >>> 32);

        context.getRegisterFile().setHi((int) (product >> 32));
        context.getRegisterFile().setLo((int) ((product << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NOR, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000027, mask = 0xfc00003f)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void nor(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), ~(context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) | context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.OR, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000025, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void or(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) | context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ORI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x34000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void ori(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) | MathHelper.zeroExtend(BitField.INTIMM.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000000, mask = 0xffe0003f)
    @InputDependencies(Dependency.RT)
    @OutputDependencies(Dependency.RD)
    private static void sll(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)) << BitField.SHIFT.valueOf(machineInstruction));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLLV, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000004, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void sllv(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)) << MathHelper.bits(context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)), 4, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLT, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0000002a, mask = 0xfc00003f)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void slt(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) < context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) ? 1 : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLTI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x28000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void slti(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) < MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction))) ? 1 : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLTIU, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x2c000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void sltiu(Context context, int machineInstruction) {
        int rs = context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction));
        int imm = MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction));

        if (rs >= 0 && imm >= 0 || rs < 0 && imm < 0) {
            context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), (rs < imm) ? 1 : 0);
        } else {
            context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), (rs >= 0) ? 1 : 0);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLTU, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x0000002b, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void sltu(Context context, int machineInstruction) {
        int rs = context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction));
        int rt = context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction));

        if (rs >= 0 && rt >= 0 || rs < 0 && rt < 0) {
            context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), (rs < rt) ? 1 : 0);
        } else {
            context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), (rs >= 0) ? 1 : 0);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRA, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000003, mask = 0xffe0003f)
    @InputDependencies(Dependency.RT)
    @OutputDependencies(Dependency.RD)
    private static void sra(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)) >> BitField.SHIFT.valueOf(machineInstruction));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRAV, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000007, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void srav(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)) >> MathHelper.bits(context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)), 4, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000002, mask = 0xffe0003f)
    @InputDependencies(Dependency.RT)
    @OutputDependencies(Dependency.RD)
    private static void srl(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)) >>> BitField.SHIFT.valueOf(machineInstruction));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRLV, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000006, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void srlv(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)) >>> MathHelper.bits(context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)), 4, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUB, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) // TODO: missing decoding information
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void sub(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) - context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUBU, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000023, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void subu(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) - context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.XOR, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000026, mask = 0xfc0007ff)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RD)
    private static void xor(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) ^ context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.XORI, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x38000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    private static void xori(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) ^ MathHelper.zeroExtend(BitField.INTIMM.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ABS_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000005, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void abs_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), Math.abs(context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ABS_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000005, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void abs_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), Math.abs(context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADD_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000000, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void add_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)) + context.getRegisterFile().getFpr().getDouble(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADD_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000000, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void add_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)) + context.getRegisterFile().getFpr().getFloat(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.C_COND_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000030, mask = 0xfc0000f0)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies({Dependency.FS, Dependency.FT, Dependency.REGISTER_FCSR})
    @OutputDependencies(Dependency.REGISTER_FCSR)
    private static void c_cond_d(Context context, int machineInstruction) {
        double fs = context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction));
        double ft = context.getRegisterFile().getFpr().getDouble(BitField.FT.valueOf(machineInstruction));

        boolean unordered = Double.isNaN(fs) || Double.isNaN(ft);

        c_cond(context, machineInstruction, unordered, !unordered && fs < ft, !unordered && fs == ft);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.C_COND_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000030, mask = 0xfc0000f0)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies({Dependency.FS, Dependency.FT, Dependency.REGISTER_FCSR})
    @OutputDependencies(Dependency.REGISTER_FCSR)
    private static void c_cond_s(Context context, int machineInstruction) {
        float fs = context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction));
        float ft = context.getRegisterFile().getFpr().getFloat(BitField.FT.valueOf(machineInstruction));

        boolean unordered = Float.isNaN(fs) || Float.isNaN(ft);

        c_cond(context, machineInstruction, unordered, !unordered && fs < ft, !unordered && fs == ft);
    }

    private static void c_cond(Context context, int machineInstruction, boolean unordered, boolean less, boolean equal) {
        int cond = BitField.COND.valueOf(machineInstruction);

        Reference<Integer> fcsrRef = new Reference<Integer>(context.getRegisterFile().getFcsr());

        if ((((cond & 0x4) != 0) && less) || (((cond & 0x2) != 0) && equal) || (((cond & 0x1) != 0) && unordered)) {
            setFCC(fcsrRef, BitField.CC.valueOf(machineInstruction));
        } else {
            clearFCC(fcsrRef, BitField.CC.valueOf(machineInstruction));
        }

        context.getRegisterFile().setFcsr(fcsrRef.get());
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_D_L, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000021, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_LONG)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_d_l(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getLong(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_D_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000021, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_d_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_D_W, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000021, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_WORD)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_d_w(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getInt(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_L_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_l_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setLong(BitField.FD.valueOf(machineInstruction), (long) context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_L_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_l_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setLong(BitField.FD.valueOf(machineInstruction), (long) context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_S_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000020, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_s_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), (float) context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_S_L, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000020, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_LONG)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_s_l(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getLong(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_S_W, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000020, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_WORD)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_s_w(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getInt(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_W_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000024, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_w_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setInt(BitField.FD.valueOf(machineInstruction), (int) context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_W_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000024, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void cvt_w_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setInt(BitField.FD.valueOf(machineInstruction), (int) context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIV_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000003, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void div_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)) / context.getRegisterFile().getFpr().getDouble(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIV_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000003, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void div_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)) / context.getRegisterFile().getFpr().getFloat(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOV_D, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.FLOAT_COMPUTATION})
    @DecodeMethod(bits = 0x44000006, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void mov_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOV_S, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.FLOAT_COMPUTATION})
    @DecodeMethod(bits = 0x44000006, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void mov_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOVF, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x00000001, mask = 0xfc0307ff)
    private static void movf(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: movf
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVF, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44000011, mask = 0xfc03003f)
    private static void _movf(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: _movf
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOVN, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x0000000b, mask = 0xfc0007ff)
    private static void movn(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: movn
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVN, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44000013, mask = 0xfc00003f)
    private static void _movn(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: _movn
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVT, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44010011, mask = 0xfc03003f)
    private static void _movt(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: _movt
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOVZ, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x0000000a, mask = 0xfc0007ff)
    private static void movz(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: movz
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVZ, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44000012, mask = 0xfc00003f)
    private static void _movz(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: _movz
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MUL, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x70000002, mask = 0xfc0007ff)
    private static void mul(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: mul
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.TRUNC_W, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x4400000d, mask = 0xfc1f003f)
    private static void trunc_w(Context context, int machineInstruction) {
        throw new UnsupportedOperationException(); //TODO: trunc_w
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MUL_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000002, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void mul_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)) * context.getRegisterFile().getFpr().getDouble(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MUL_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000002, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void mul_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)) * context.getRegisterFile().getFpr().getFloat(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NEG_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000007, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void neg_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), -context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NEG_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000007, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void neg_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), -context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SQRT_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_SQRT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000004, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void sqrt_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), Math.sqrt(context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SQRT_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_SQRT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000004, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.FD)
    private static void sqrt_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), (float) Math.sqrt(context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUB_D, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000001, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void sub_d(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setDouble(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getDouble(BitField.FS.valueOf(machineInstruction)) - context.getRegisterFile().getFpr().getDouble(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUB_S, functionalUnitOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000001, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @InputDependencies({Dependency.FS, Dependency.FT})
    @OutputDependencies(Dependency.FD)
    private static void sub_s(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setFloat(BitField.FD.valueOf(machineInstruction), context.getRegisterFile().getFpr().getFloat(BitField.FS.valueOf(machineInstruction)) - context.getRegisterFile().getFpr().getFloat(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.J, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x08000000, mask = 0xfc000000)
    @InputDependencies({})
    @OutputDependencies({})
    private static void j(Context context, int machineInstruction) {
        doJump(context, getTargetPcForJ(context.getRegisterFile().getNpc(), machineInstruction));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.JAL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0c000000, mask = 0xfc000000)
    @InputDependencies({})
    @OutputDependencies(Dependency.REGISTER_RA)
    private static void jal(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, context.getRegisterFile().getNnpc());
        doJump(context, getTargetPcForJal(context.getRegisterFile().getNpc(), machineInstruction));

        if (!context.isSpeculative()) {
            onFunctionCall(context, false);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.JALR, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.INDIRECT_JUMP})
    @DecodeMethod(bits = 0x00000009, mask = 0xfc00003f)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RD)
    private static void jalr(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RD.valueOf(machineInstruction), context.getRegisterFile().getNnpc());
        doJump(context, getTargetPcForJalr(context, machineInstruction));

        if (!context.isSpeculative()) {
            onFunctionCall(context, true);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.JR, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.FUNCTION_RETURN, StaticInstructionFlag.INDIRECT_JUMP})
    @DecodeMethod(bits = 0x00000008, mask = 0xfc00003f)
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void jr(Context context, int machineInstruction) {
        doJump(context, getTargetPcForJr(context, machineInstruction));

        if (!context.isSpeculative() && BitField.RS.valueOf(machineInstruction) == ArchitecturalRegisterFile.REGISTER_RA) {
            onFunctionReturn(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.B, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x10000000, mask = 0xffff0000)
    @InputDependencies({})
    @OutputDependencies({})
    private static void b(Context context, int machineInstruction) {
        doBranch(context, machineInstruction);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BAL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04110000, mask = 0xffff0000)
    @InputDependencies({})
    @OutputDependencies(Dependency.REGISTER_RA)
    private static void bal(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, context.getRegisterFile().getNnpc());
        doBranch(context, machineInstruction);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1F, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x45000000, mask = 0xffe30000)
    @InputDependencies(Dependency.REGISTER_FCSR)
    @OutputDependencies({})
    private static void bc1f(Context context, int machineInstruction) {
        if (!getFCC(context.getRegisterFile().getFcsr(), BitField.BRANCH_CC.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1FL, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.REGISTER_FCSR)
    @OutputDependencies({})
    private static void bc1fl(Context context, int machineInstruction) {
        if (!getFCC(context.getRegisterFile().getFcsr(), BitField.BRANCH_CC.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1T, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x45010000, mask = 0xffe30000)
    @InputDependencies(Dependency.REGISTER_FCSR)
    @OutputDependencies({})
    private static void bc1t(Context context, int machineInstruction) {
        if (getFCC(context.getRegisterFile().getFcsr(), BitField.BRANCH_CC.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1TL, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.REGISTER_FCSR)
    @OutputDependencies({})
    private static void bc1tl(Context context, int machineInstruction) {
        if (getFCC(context.getRegisterFile().getFcsr(), BitField.BRANCH_CC.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BEQ, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x10000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    private static void beq(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) == context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BEQL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    private static void beql(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) == context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZ, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04010000, mask = 0xfc1f0000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void bgez(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) >= 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZAL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04110000, mask = 0xfc1f0000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.REGISTER_RA)
    private static void bgezal(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, context.getRegisterFile().getNnpc());

        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) >= 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZALL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.REGISTER_RA)
    private static void bgezall(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, context.getRegisterFile().getNnpc());

        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) >= 0) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void bgezl(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) >= 0) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGTZ, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x1c000000, mask = 0xfc1f0000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void bgtz(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) > 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGTZL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void bgtzl(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) > 0) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLEZ, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x18000000, mask = 0xfc1f0000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void blez(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) <= 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLEZL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void blezl(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) <= 0) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZ, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04000000, mask = 0xfc1f0000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void bltz(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) < 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZAL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.REGISTER_RA)
    private static void bltzal(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, context.getRegisterFile().getNnpc());

        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) < 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZALL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.REGISTER_RA)
    private static void bltzall(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, context.getRegisterFile().getNnpc());

        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) < 0) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies(Dependency.RS)
    @OutputDependencies({})
    private static void bltzl(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) < 0) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BNE, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x14000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    private static void bne(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) != context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BNEL, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    private static void bnel(Context context, int machineInstruction) {
        if (context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction)) != context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction))) {
            doBranch(context, machineInstruction);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LB, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x80000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lb(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getProcess().getMemory().readByte(getEffectiveAddress(context, machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LBU, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x90000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lbu(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getProcess().getMemory().readByte(getEffectiveAddress(context, machineInstruction)) & 0xff);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LDC1, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xd4000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.FT)
    @NonEffectiveAddressBaseDependency(Dependency.FT)
    private static void ldc1(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setLong(BitField.FT.valueOf(machineInstruction), context.getProcess().getMemory().readDoubleWord(getEffectiveAddress(context, machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LH, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x84000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lh(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getProcess().getMemory().readHalfWord(getEffectiveAddress(context, machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LHU, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x94000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lhu(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getProcess().getMemory().readHalfWord(getEffectiveAddress(context, machineInstruction)) & 0xffff);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LL, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xc0000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void ll(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getProcess().getMemory().readWord(getEffectiveAddress(context, machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LW, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x8c000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lw(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getProcess().getMemory().readWord(getEffectiveAddress(context, machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LWC1, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xc4000000, mask = 0xfc000000)
    @InputDependencies(Dependency.RS)
    @OutputDependencies(Dependency.FT)
    @NonEffectiveAddressBaseDependency(Dependency.FT)
    private static void lwc1(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setInt(BitField.FT.valueOf(machineInstruction), context.getProcess().getMemory().readWord(getEffectiveAddress(context, machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LWL, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x88000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lwl(Context context, int machineInstruction) {
        int addr = getEffectiveAddress(context, machineInstruction);
        int size = 4 - (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).put(context.getProcess().getMemory().readBlock(addr, size));

        byte[] dst = new byte[4];
        ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));

        for (int i = 0; i < size; i++) {
            dst[3 - i] = src[i];
        }

        int rt = ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).getInt();
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), rt);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LWR, functionalUnitOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x98000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void lwr(Context context, int machineInstruction) {
        int addr = getEffectiveAddress(context, machineInstruction);
        int size = 1 + (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).put(context.getProcess().getMemory().readBlock(addr - size + 1, size));

        byte[] dst = new byte[4];
        ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));

        for (int i = 0; i < size; i++) {
            dst[size - i - 1] = src[i];
        }

        int rt = ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).getInt();
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), rt);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SB, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xa0000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void sb(Context context, int machineInstruction) {
        context.getProcess().getMemory().writeByte(getEffectiveAddress(context, machineInstruction), (byte) MathHelper.bits(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)), 7, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SC, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xe0000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies(Dependency.RT)
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void sc(Context context, int machineInstruction) {
        context.getProcess().getMemory().writeWord(getEffectiveAddress(context, machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), 1);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SDC1, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xf4000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.FT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.FT)
    private static void sdc1(Context context, int machineInstruction) {
        context.getProcess().getMemory().writeDoubleWord(getEffectiveAddress(context, machineInstruction), context.getRegisterFile().getFpr().getLong(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SH, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xa4000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void sh(Context context, int machineInstruction) {
        context.getProcess().getMemory().writeHalfWord(getEffectiveAddress(context, machineInstruction), (short) MathHelper.bits(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)), 15, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SW, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xac000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void sw(Context context, int machineInstruction) {
        context.getProcess().getMemory().writeWord(getEffectiveAddress(context, machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SWC1, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xe4000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.FT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.FT)
    private static void swc1(Context context, int machineInstruction) {
        context.getProcess().getMemory().writeWord(getEffectiveAddress(context, machineInstruction), context.getRegisterFile().getFpr().getInt(BitField.FT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SWL, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xa8000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void swl(Context context, int machineInstruction) {
        int addr = getEffectiveAddress(context, machineInstruction);
        int size = 4 - (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));

        byte[] dst = new byte[4];
        for (int i = 0; i < size; i++) {
            dst[i] = src[3 - i];
        }

        context.getProcess().getMemory().writeBlock(addr, size, dst);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SWR, functionalUnitOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xb8000000, mask = 0xfc000000)
    @InputDependencies({Dependency.RS, Dependency.RT})
    @OutputDependencies({})
    @NonEffectiveAddressBaseDependency(Dependency.RT)
    private static void swr(Context context, int machineInstruction) {
        int addr = getEffectiveAddress(context, machineInstruction);
        int size = 1 + (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));

        byte[] dst = new byte[4];
        for (int i = 0; i < size; i++) {
            dst[i] = src[size - i - 1];
        }

        context.getProcess().getMemory().writeBlock(addr - size + 1, size, dst);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CFC1, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44400000, mask = 0xffe007ff)
    @InputDependencies(Dependency.REGISTER_FCSR)
    @OutputDependencies(Dependency.RT)
    private static void cfc1(Context context, int machineInstruction) {
        if (BitField.FS.valueOf(machineInstruction) == 31) {
            context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getFcsr());
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CTC1, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44c00000, mask = 0xffe007ff)
    @InputDependencies(Dependency.RT)
    @OutputDependencies(Dependency.REGISTER_FCSR)
    private static void ctc1(Context context, int machineInstruction) {
        if (BitField.FS.valueOf(machineInstruction) != 0) {
            context.getRegisterFile().setFcsr(context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MFC1, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44000000, mask = 0xffe007ff)
    @InputDependencies(Dependency.FS)
    @OutputDependencies(Dependency.RT)
    private static void mfc1(Context context, int machineInstruction) {
        context.getRegisterFile().setGpr(BitField.RT.valueOf(machineInstruction), context.getRegisterFile().getFpr().getInt(BitField.FS.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MTC1, functionalUnitOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44800000, mask = 0xffe007ff)
    @InputDependencies(Dependency.RT)
    @OutputDependencies(Dependency.FS)
    private static void mtc1(Context context, int machineInstruction) {
        context.getRegisterFile().getFpr().setInt(BitField.FS.valueOf(machineInstruction), context.getRegisterFile().getGpr(BitField.RT.valueOf(machineInstruction)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BREAK, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.TRAP})
    @DecodeMethod(bits = 0x0000000d, mask = 0xfc00003f)
    @InputDependencies({})
    @OutputDependencies({})
    private static void _break(Context context, int machineInstruction) {
        if (!context.isSpeculative()) {
            context.finish();
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SYSTEM_CALL, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.TRAP})
    @DecodeMethod(bits = 0x0000000c, mask = 0xfc00003f)
    @InputDependencies(Dependency.REGISTER_V0)
    @OutputDependencies({})
    private static void system_call(Context context, int machineInstruction) {
        if (!context.isSpeculative()) {
            context.getKernel().getSystemCallEmulation().doSystemCall(context.getRegisterFile().getGpr(ArchitecturalRegisterFile.REGISTER_V0), context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NOP, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.NOP})
    @DecodeMethod(bits = 0x00000000, mask = 0xffffffff)
    @InputDependencies({})
    @OutputDependencies({})
    private static void nop(Context context, int machineInstruction) {
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.UNKNOWN, functionalUnitOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNKNOWN})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: special support for unknown instruction
    @InputDependencies({})
    @OutputDependencies({})
    private static void unknown(Context context, int machineInstruction) {
        if (!context.isSpeculative()) {
            Logger.panicf(Logger.INSTRUCTION, "{ctx-%d} 0x%08x: Unknown instruction 0x%08x", context.getCycleAccurateEventQueue().getCurrentCycle(), context.getId(), context.getRegisterFile().getPc(), machineInstruction);
        }
    }

    private static void onFunctionCall(Context context, boolean jalr) {
        StaticInstruction staticInst = context.getProcess().getStaticInstruction(context.getRegisterFile().getPc());
        int targetPc = jalr ? getTargetPcForJalr(context, staticInst.getMachineInstruction()) : getTargetPcForJr(context, staticInst.getMachineInstruction());
        context.getFunctionCallContextStack().push(new FunctionCallContext(context.getRegisterFile().getPc(), targetPc));
        context.getBlockingEventDispatcher().dispatch(new FunctionalCallEvent(context));
    }

    private static void onFunctionReturn(Context context) {
        context.getFunctionCallContextStack().pop();
        context.getBlockingEventDispatcher().dispatch(new FunctionReturnEvent(context));
    }

    public boolean useStackPointerAsEffectiveAddressBase() {
        return useStackPointerAsEffectiveAddressBase(this.machineInstruction);
    }

    public int getEffectiveAddressBase(Context context) {
        return getEffectiveAddress(context, this.machineInstruction);
    }

    public int getEffectiveAddressDisplacement() {
        return getEffectiveAddressDisplacement(this.machineInstruction);
    }

    public int getEffectiveAddress(Context context) {
        return getEffectiveAddress(context, this.machineInstruction);
    }

    public static boolean useStackPointerAsEffectiveAddressBase(int machineInstruction) {
        return BitField.RS.valueOf(machineInstruction) == ArchitecturalRegisterFile.REGISTER_SP;
    }

    public static int getEffectiveAddressBase(Context context, int machineInstruction) {
        return context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction));
    }

    public static int getEffectiveAddressDisplacement(int machineInstruction) {
        return MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction));
    }

    public static int getEffectiveAddress(Context context, int machineInstruction) {
        return getEffectiveAddressBase(context, machineInstruction) + getEffectiveAddressDisplacement(machineInstruction);
    }

    private static boolean getFCC(int fcsr, int ccIdx) {
        return ((fcsr >> ((ccIdx == 0) ? 23 : ccIdx + 24)) & 0x00000001) != 0;
    }

    private static void setFCC(Reference<Integer> fcsr, int cc) {
        fcsr.set(fcsr.get() | (cc == 0 ? 0x800000 : 0x1000000 << cc));
    }

    private static void clearFCC(Reference<Integer> fcsr, int cc) {
        fcsr.set(fcsr.get() & (cc == 0 ? 0xFF7FFFFF : 0xFEFFFFFF << cc));
    }

    private static void doJump(Context context, int targetPc) {
        context.getRegisterFile().setNnpc(targetPc);
    }

    private static void doBranch(Context context, int machineInstruction) {
        context.getRegisterFile().setNnpc(getTargetPcForBranch(context.getRegisterFile().getNpc(), machineInstruction));
    }

    private static void skipDelaySlot(Context context) {
        context.getRegisterFile().setNpc(context.getRegisterFile().getNnpc());
        context.getRegisterFile().setNnpc(context.getRegisterFile().getNnpc() + 4);
    }

    public static int getTargetPcForControl(int pc, int machineInstruction, Mnemonic mnemonic) {
        switch (mnemonic) {
            case J:
                return getTargetPcForJ(pc, machineInstruction);
            case JAL:
                return getTargetPcForJal(pc, machineInstruction);
            case JALR:
                throw new IllegalArgumentException();
            case JR:
                throw new IllegalArgumentException();
            default:
                return getTargetPcForBranch(pc, machineInstruction);
        }
    }

    private static int getTargetPcForBranch(int pc, int machineInstruction) {
        return pc + MathHelper.signExtend(BitField.INTIMM.valueOf(machineInstruction) << 2);
    }

    private static int getTargetPcForJ(int pc, int machineInstruction) {
        return MathHelper.mbits(pc, 32, 28) | (BitField.TARGET.valueOf(machineInstruction) << 2);
    }

    private static int getTargetPcForJal(int pc, int machineInstruction) {
        return MathHelper.mbits(pc, 32, 28) | (BitField.TARGET.valueOf(machineInstruction) << 2);
    }

    public static int getTargetPcForJalr(Context context, int machineInstruction) {
        return context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction));
    }

    public static int getTargetPcForJr(Context context, int machineInstruction) {
        return context.getRegisterFile().getGpr(BitField.RS.valueOf(machineInstruction));
    }

    public static void execute(StaticInstruction staticInst, Context context) {
        int oldPc = context.getRegisterFile().getPc();
        try {
            staticInst.mnemonic.getMethod().invoke(null, context, staticInst.machineInstruction);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        context.getBlockingEventDispatcher().dispatch(new InstructionFunctionallyExecutedEvent(context, oldPc, staticInst));
    }

    public static final StaticInstruction NOP = new StaticInstruction(Mnemonic.NOP, 0x0);
}
