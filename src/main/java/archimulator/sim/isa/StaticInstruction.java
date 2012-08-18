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
    private int machInst;
    private List<Integer> ideps;
    private List<Integer> odeps;

    private int nonEffectiveAddressBaseDep;

    private Map<RegisterDependencyType, Integer> numFreePhysRegsToAllocate = new EnumMap<RegisterDependencyType, Integer>(RegisterDependencyType.class);

    public StaticInstruction(Mnemonic mnemonic, int machInst) {
        this.mnemonic = mnemonic;
        this.machInst = machInst;
        this.ideps = mnemonic.getIdeps(machInst);
        this.odeps = mnemonic.getOdeps(machInst);

        if (this.mnemonic.getType() == StaticInstructionType.LOAD || this.mnemonic.getType() == StaticInstructionType.STORE) {
            this.nonEffectiveAddressBaseDep = mnemonic.getNonEffectiveAddressBaseDep(machInst);
        }

        for (int odep : this.odeps) {
            if (odep != 0) {
                RegisterDependencyType odepType = RegisterDependencyType.getType(odep);

                if (!this.numFreePhysRegsToAllocate.containsKey(odepType)) {
                    this.numFreePhysRegsToAllocate.put(odepType, 0);
                }

                this.numFreePhysRegsToAllocate.put(odepType, this.numFreePhysRegsToAllocate.get(odepType) + 1);
            }
        }
    }

    public Mnemonic getMnemonic() {
        return mnemonic;
    }

    public int getMachInst() {
        return machInst;
    }

    public List<Integer> getIdeps() {
        return ideps;
    }

    public List<Integer> getOdeps() {
        return odeps;
    }

    public int getNonEffectiveAddressBaseDep() {
        return nonEffectiveAddressBaseDep;
    }

    public Map<RegisterDependencyType, Integer> getNumFreePhysRegsToAllocate() {
        return numFreePhysRegsToAllocate;
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

    public static enum Dep {
        RS, RT, RD, FS, FT, FD, REG_RA, REG_V0, REG_HI, REG_LO, REG_FCSR
    }

    public static List<Mnemonic> machInstDecoderInfos = new ArrayList<Mnemonic>();

    private static void registerInstruction(Mnemonic mnemonic) {
        machInstDecoderInfos.add(mnemonic);
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

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADD, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000020, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void add(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADDI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x20000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void addi(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + MathHelper.signExtend(BitField.INTIMM.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADDIU, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x24000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void addiu(Context context, int machInst) {
        if (BitField.RT.valueOf(machInst) == 0 && MathHelper.signExtend(BitField.INTIMM.valueOf(machInst)) != 0 && !context.isSpeculative()) {
            context.setPseudoCallEncounteredInLastInstructionExecution(true);
            context.getBlockingEventDispatcher().dispatch(new PseudoCallEncounteredEvent(context, BitField.RS.valueOf(machInst), MathHelper.signExtend(BitField.INTIMM.valueOf(machInst))));
        } else {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + MathHelper.signExtend(BitField.INTIMM.valueOf(machInst)));
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADDU, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000021, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void addu(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.AND, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000024, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void and(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) & context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ANDI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x30000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void andi(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) & MathHelper.zeroExtend(BitField.INTIMM.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIV, fuOperationType = FunctionalUnitOperationType.INT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0000001a, mask = 0xfc00ffff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({Dep.REG_HI, Dep.REG_LO})
    private static void div(Context context, int machInst) {
        int rs = context.getRegs().getGpr(BitField.RS.valueOf(machInst));
        int rt = context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        context.getRegs().setHi(rt != 0 ? rs % rt : 0);
        context.getRegs().setLo(rt != 0 ? rs / rt : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIVU, fuOperationType = FunctionalUnitOperationType.INT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0000001b, mask = 0xfc00003f)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({Dep.REG_HI, Dep.REG_LO})
    private static void divu(Context context, int machInst) {
        long rs = ((long) context.getRegs().getGpr(BitField.RS.valueOf(machInst))) << 32 >>> 32;
        long rt = ((long) context.getRegs().getGpr(BitField.RT.valueOf(machInst))) << 32 >>> 32;

        context.getRegs().setHi(rt != 0 ? (int) (((rs % rt) << 32) >> 32) : 0);
        context.getRegs().setLo(rt != 0 ? (int) (((rs / rt) << 32) >> 32) : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LUI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x3c000000, mask = 0xffe00000)
    @Ideps({})
    @Odeps(Dep.RT)
    private static void lui(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), BitField.INTIMM.valueOf(machInst) << 16);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MADD, fuOperationType = FunctionalUnitOperationType.INT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x70000000, mask = 0xfc00ffff)
    @Ideps({Dep.RS, Dep.RT, Dep.REG_HI, Dep.REG_LO})
    @Odeps({Dep.REG_HI, Dep.REG_LO})
    private static void madd(Context context, int machInst) {
        long temp = (long) context.getRegs().getHi() << 32 | (long) context.getRegs().getLo() + context.getRegs().getGpr(BitField.RS.valueOf(machInst)) * context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        context.getRegs().setHi((int) (temp >> 32));
        context.getRegs().setLo((int) ((temp << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MFHI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000010, mask = 0xffff07ff)
    @Ideps(Dep.REG_HI)
    @Odeps(Dep.RD)
    private static void mfhi(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getHi());
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MFLO, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000012, mask = 0xffff07ff)
    @Ideps(Dep.REG_LO)
    @Odeps(Dep.RD)
    private static void mflo(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getLo());
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MSUB, fuOperationType = FunctionalUnitOperationType.INT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x70000004, mask = 0xfc00ffff)
    @Ideps({Dep.RS, Dep.RT, Dep.REG_HI, Dep.REG_LO})
    @Odeps({Dep.REG_HI, Dep.REG_LO})
    private static void msub(Context context, int machInst) {
        long temp = (long) context.getRegs().getHi() << 32 | (long) context.getRegs().getLo() - context.getRegs().getGpr(BitField.RS.valueOf(machInst)) * context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        context.getRegs().setHi((int) (temp >> 32));
        context.getRegs().setLo((int) ((temp << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MTHI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RD)
    @Odeps(Dep.REG_HI)
    private static void mthi(Context context, int machInst) {
        context.getRegs().setHi(context.getRegs().getGpr(BitField.RD.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MTLO, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000013, mask = 0xfc1fffff)
    @Ideps(Dep.RD)
    @Odeps(Dep.REG_LO)
    private static void mtlo(Context context, int machInst) {
        context.getRegs().setLo(context.getRegs().getGpr(BitField.RD.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MULT, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000018, mask = 0xfc00003f)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({Dep.REG_LO, Dep.REG_HI})
    private static void mult(Context context, int machInst) {
        long product = (long) (context.getRegs().getGpr(BitField.RS.valueOf(machInst))) * (long) (context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        context.getRegs().setHi((int) (product >> 32));
        context.getRegs().setLo((int) ((product << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MULTU, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000019, mask = 0xfc00003f)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({Dep.REG_LO, Dep.REG_HI})
    private static void multu(Context context, int machInst) {
        long product = (((long) context.getRegs().getGpr(BitField.RS.valueOf(machInst))) << 32 >>> 32) * (((long) context.getRegs().getGpr(BitField.RT.valueOf(machInst))) << 32 >>> 32);

        context.getRegs().setHi((int) (product >> 32));
        context.getRegs().setLo((int) ((product << 32) >> 32));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NOR, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000027, mask = 0xfc00003f)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void nor(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), ~(context.getRegs().getGpr(BitField.RS.valueOf(machInst)) | context.getRegs().getGpr(BitField.RT.valueOf(machInst))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.OR, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000025, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void or(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) | context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ORI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x34000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void ori(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) | MathHelper.zeroExtend(BitField.INTIMM.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000000, mask = 0xffe0003f)
    @Ideps(Dep.RT)
    @Odeps(Dep.RD)
    private static void sll(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) << BitField.SHIFT.valueOf(machInst));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLLV, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000004, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void sllv(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) << MathHelper.bits(context.getRegs().getGpr(BitField.RS.valueOf(machInst)), 4, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLT, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0000002a, mask = 0xfc00003f)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void slt(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < context.getRegs().getGpr(BitField.RT.valueOf(machInst))) ? 1 : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLTI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x28000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void slti(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < MathHelper.signExtend(BitField.INTIMM.valueOf(machInst))) ? 1 : 0);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLTIU, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x2c000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void sltiu(Context context, int machInst) {
        int rs = context.getRegs().getGpr(BitField.RS.valueOf(machInst));
        int imm = MathHelper.signExtend(BitField.INTIMM.valueOf(machInst));

        if (rs >= 0 && imm >= 0 || rs < 0 && imm < 0) {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), (rs < imm) ? 1 : 0);
        } else {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), (rs >= 0) ? 1 : 0);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SLTU, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x0000002b, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void sltu(Context context, int machInst) {
        int rs = context.getRegs().getGpr(BitField.RS.valueOf(machInst));
        int rt = context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        if (rs >= 0 && rt >= 0 || rs < 0 && rt < 0) {
            context.getRegs().setGpr(BitField.RD.valueOf(machInst), (rs < rt) ? 1 : 0);
        } else {
            context.getRegs().setGpr(BitField.RD.valueOf(machInst), (rs >= 0) ? 1 : 0);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRA, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000003, mask = 0xffe0003f)
    @Ideps(Dep.RT)
    @Odeps(Dep.RD)
    private static void sra(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >> BitField.SHIFT.valueOf(machInst));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRAV, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000007, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void srav(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >> MathHelper.bits(context.getRegs().getGpr(BitField.RS.valueOf(machInst)), 4, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000002, mask = 0xffe0003f)
    @Ideps(Dep.RT)
    @Odeps(Dep.RD)
    private static void srl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >>> BitField.SHIFT.valueOf(machInst));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SRLV, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000006, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void srlv(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >>> MathHelper.bits(context.getRegs().getGpr(BitField.RS.valueOf(machInst)), 4, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUB, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) // TODO: missing decoding information
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void sub(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) - context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUBU, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000023, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void subu(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) - context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.XOR, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags(StaticInstructionFlag.INTEGER_COMPUTATION)
    @DecodeMethod(bits = 0x00000026, mask = 0xfc0007ff)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RD)
    private static void xor(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) ^ context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.XORI, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION, StaticInstructionFlag.IMMEDIATE})
    @DecodeMethod(bits = 0x38000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    private static void xori(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) ^ MathHelper.zeroExtend(BitField.INTIMM.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ABS_D, fuOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000005, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void abs_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), Math.abs(context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ABS_S, fuOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000005, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void abs_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), Math.abs(context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADD_D, fuOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000000, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void add_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) + context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.ADD_S, fuOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000000, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void add_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) + context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.C_COND_D, fuOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000030, mask = 0xfc0000f0)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps({Dep.FS, Dep.FT, Dep.REG_FCSR})
    @Odeps(Dep.REG_FCSR)
    private static void c_cond_d(Context context, int machInst) {
        double fs = context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst));
        double ft = context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst));

        boolean unordered = Double.isNaN(fs) || Double.isNaN(ft);

        c_cond(context, machInst, unordered, !unordered && fs < ft, !unordered && fs == ft);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.C_COND_S, fuOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000030, mask = 0xfc0000f0)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps({Dep.FS, Dep.FT, Dep.REG_FCSR})
    @Odeps(Dep.REG_FCSR)
    private static void c_cond_s(Context context, int machInst) {
        float fs = context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst));
        float ft = context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst));

        boolean unordered = Float.isNaN(fs) || Float.isNaN(ft);

        c_cond(context, machInst, unordered, !unordered && fs < ft, !unordered && fs == ft);
    }

    private static void c_cond(Context context, int machInst, boolean unordered, boolean less, boolean equal) {
        int cond = BitField.COND.valueOf(machInst);

        Reference<Integer> fcsrRef = new Reference<Integer>(context.getRegs().getFcsr());

        if ((((cond & 0x4) != 0) && less) || (((cond & 0x2) != 0) && equal) || (((cond & 0x1) != 0) && unordered)) {
            setFCC(fcsrRef, BitField.CC.valueOf(machInst));
        } else {
            clearFCC(fcsrRef, BitField.CC.valueOf(machInst));
        }

        context.getRegs().setFcsr(fcsrRef.get());
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_D_L, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000021, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_LONG)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_d_l(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getLong(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_D_S, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000021, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_d_s(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_D_W, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000021, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_WORD)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_d_w(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getInt(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_L_D, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_l_d(Context context, int machInst) {
        context.getRegs().getFpr().setLong(BitField.FD.valueOf(machInst), (long) context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_L_S, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_l_s(Context context, int machInst) {
        context.getRegs().getFpr().setLong(BitField.FD.valueOf(machInst), (long) context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_S_D, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000020, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_s_d(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), (float) context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_S_L, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000020, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_LONG)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_s_l(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getLong(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_S_W, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000020, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_WORD)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_s_w(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getInt(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_W_D, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000024, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_w_d(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FD.valueOf(machInst), (int) context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CVT_W_S, fuOperationType = FunctionalUnitOperationType.FLOAT_CONVERT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000024, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void cvt_w_s(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FD.valueOf(machInst), (int) context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIV_D, fuOperationType = FunctionalUnitOperationType.FLOAT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000003, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void div_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) / context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.DIV_S, fuOperationType = FunctionalUnitOperationType.FLOAT_DIVIDE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000003, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void div_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) / context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOV_D, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.FLOAT_COMPUTATION})
    @DecodeMethod(bits = 0x44000006, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void mov_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOV_S, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.FLOAT_COMPUTATION})
    @DecodeMethod(bits = 0x44000006, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void mov_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOVF, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x00000001, mask = 0xfc0307ff)
    private static void movf(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: movf
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVF, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44000011, mask = 0xfc03003f)
    private static void _movf(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movf
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOVN, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x0000000b, mask = 0xfc0007ff)
    private static void movn(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: movn
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVN, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44000013, mask = 0xfc00003f)
    private static void _movn(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movn
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVT, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44010011, mask = 0xfc03003f)
    private static void _movt(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movt
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MOVZ, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x0000000a, mask = 0xfc0007ff)
    private static void movz(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: movz
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic._MOVZ, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x44000012, mask = 0xfc00003f)
    private static void _movz(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movz
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MUL, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x70000002, mask = 0xfc0007ff)
    private static void mul(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: mul
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.TRUNC_W, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNIMPLEMENTED})
    @DecodeMethod(bits = 0x4400000d, mask = 0xfc1f003f)
    private static void trunc_w(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: trunc_w
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MUL_D, fuOperationType = FunctionalUnitOperationType.FLOAT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000002, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void mul_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) * context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MUL_S, fuOperationType = FunctionalUnitOperationType.FLOAT_MULTIPLY)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000002, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void mul_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) * context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NEG_D, fuOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000007, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void neg_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), -context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NEG_S, fuOperationType = FunctionalUnitOperationType.FLOAT_COMPARE)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000007, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void neg_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), -context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SQRT_D, fuOperationType = FunctionalUnitOperationType.FLOAT_SQRT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000004, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void sqrt_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), Math.sqrt(context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SQRT_S, fuOperationType = FunctionalUnitOperationType.FLOAT_SQRT)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000004, mask = 0xfc1f003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps(Dep.FS)
    @Odeps(Dep.FD)
    private static void sqrt_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), (float) Math.sqrt(context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst))));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUB_D, fuOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000001, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_DOUBLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void sub_d(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) - context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SUB_S, fuOperationType = FunctionalUnitOperationType.FLOAT_ADD)
    @StaticInstructionFlags(StaticInstructionFlag.FLOAT_COMPUTATION)
    @DecodeMethod(bits = 0x44000001, mask = 0xfc00003f)
    @DecodeCondition(bitField = "fmt", value = FMT_SINGLE)
    @Ideps({Dep.FS, Dep.FT})
    @Odeps(Dep.FD)
    private static void sub_s(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) - context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.J, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x08000000, mask = 0xfc000000)
    @Ideps({})
    @Odeps({})
    private static void j(Context context, int machInst) {
        doJump(context, getTargetPcForJ(context.getRegs().getNpc(), machInst));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.JAL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0c000000, mask = 0xfc000000)
    @Ideps({})
    @Odeps(Dep.REG_RA)
    private static void jal(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());
        doJump(context, getTargetPcForJal(context.getRegs().getNpc(), machInst));

        if (!context.isSpeculative()) {
            onFunctionCall(context, false);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.JALR, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.INDIRECT_JUMP})
    @DecodeMethod(bits = 0x00000009, mask = 0xfc00003f)
    @Ideps(Dep.RS)
    @Odeps(Dep.RD)
    private static void jalr(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getNnpc());
        doJump(context, getTargetPcForJalr(context, machInst));

        if (!context.isSpeculative()) {
            onFunctionCall(context, true);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.JR, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.FUNCTION_RETURN, StaticInstructionFlag.INDIRECT_JUMP})
    @DecodeMethod(bits = 0x00000008, mask = 0xfc00003f)
    @Ideps(Dep.RS)
    @Odeps({})
    private static void jr(Context context, int machInst) {
        doJump(context, getTargetPcForJr(context, machInst));

        if (!context.isSpeculative() && BitField.RS.valueOf(machInst) == ArchitecturalRegisterFile.REG_RA) {
            onFunctionReturn(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.B, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x10000000, mask = 0xffff0000)
    @Ideps({})
    @Odeps({})
    private static void b(Context context, int machInst) {
        doBranch(context, machInst);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BAL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.UNCONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04110000, mask = 0xffff0000)
    @Ideps({})
    @Odeps(Dep.REG_RA)
    private static void bal(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());
        doBranch(context, machInst);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1F, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x45000000, mask = 0xffe30000)
    @Ideps(Dep.REG_FCSR)
    @Odeps({})
    private static void bc1f(Context context, int machInst) {
        if (!getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1FL, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.REG_FCSR)
    @Odeps({})
    private static void bc1fl(Context context, int machInst) {
        if (!getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1T, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x45010000, mask = 0xffe30000)
    @Ideps(Dep.REG_FCSR)
    @Odeps({})
    private static void bc1t(Context context, int machInst) {
        if (getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BC1TL, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.REG_FCSR)
    @Odeps({})
    private static void bc1tl(Context context, int machInst) {
        if (getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BEQ, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x10000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    private static void beq(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) == context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BEQL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    private static void beql(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) == context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZ, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04010000, mask = 0xfc1f0000)
    @Ideps(Dep.RS)
    @Odeps({})
    private static void bgez(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZAL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04110000, mask = 0xfc1f0000)
    @Ideps(Dep.RS)
    @Odeps(Dep.REG_RA)
    private static void bgezal(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZALL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps(Dep.REG_RA)
    private static void bgezall(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGEZL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps({})
    private static void bgezl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGTZ, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x1c000000, mask = 0xfc1f0000)
    @Ideps(Dep.RS)
    @Odeps({})
    private static void bgtz(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) > 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BGTZL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps({})
    private static void bgtzl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) > 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLEZ, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x18000000, mask = 0xfc1f0000)
    @Ideps(Dep.RS)
    @Odeps({})
    private static void blez(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) <= 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLEZL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps({})
    private static void blezl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) <= 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZ, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x04000000, mask = 0xfc1f0000)
    @Ideps(Dep.RS)
    @Odeps({})
    private static void bltz(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZAL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps(Dep.REG_RA)
    private static void bltzal(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZALL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.FUNCTION_CALL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps(Dep.REG_RA)
    private static void bltzall(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BLTZL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps(Dep.RS)
    @Odeps({})
    private static void bltzl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BNE, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x14000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    private static void bne(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) != context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BNEL, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.CONDITIONAL, StaticInstructionFlag.DIRECT_JUMP})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: missing decoding information
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    private static void bnel(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) != context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LB, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x80000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lb(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readByte(getEffectiveAddress(context, machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LBU, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x90000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lbu(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readByte(getEffectiveAddress(context, machInst)) & 0xff);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LDC1, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xd4000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.FT)
    @NonEffectiveAddressBaseDep(Dep.FT)
    private static void ldc1(Context context, int machInst) {
        context.getRegs().getFpr().setLong(BitField.FT.valueOf(machInst), context.getProcess().getMemory().readDoubleWord(getEffectiveAddress(context, machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LH, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x84000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lh(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readHalfWord(getEffectiveAddress(context, machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LHU, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x94000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lhu(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readHalfWord(getEffectiveAddress(context, machInst)) & 0xffff);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LL, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xc0000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void ll(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readWord(getEffectiveAddress(context, machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LW, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x8c000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lw(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readWord(getEffectiveAddress(context, machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LWC1, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xc4000000, mask = 0xfc000000)
    @Ideps(Dep.RS)
    @Odeps(Dep.FT)
    @NonEffectiveAddressBaseDep(Dep.FT)
    private static void lwc1(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FT.valueOf(machInst), context.getProcess().getMemory().readWord(getEffectiveAddress(context, machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LWL, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x88000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lwl(Context context, int machInst) {
        int addr = getEffectiveAddress(context, machInst);
        int size = 4 - (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).put(context.getProcess().getMemory().readBlock(addr, size));

        byte[] dst = new byte[4];
        ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        for (int i = 0; i < size; i++) {
            dst[3 - i] = src[i];
        }

        int rt = ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).getInt();
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), rt);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.LWR, fuOperationType = FunctionalUnitOperationType.READ_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.LOAD, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0x98000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void lwr(Context context, int machInst) {
        int addr = getEffectiveAddress(context, machInst);
        int size = 1 + (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).put(context.getProcess().getMemory().readBlock(addr - size + 1, size));

        byte[] dst = new byte[4];
        ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        for (int i = 0; i < size; i++) {
            dst[size - i - 1] = src[i];
        }

        int rt = ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).getInt();
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), rt);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SB, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xa0000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void sb(Context context, int machInst) {
        context.getProcess().getMemory().writeByte(getEffectiveAddress(context, machInst), (byte) MathHelper.bits(context.getRegs().getGpr(BitField.RT.valueOf(machInst)), 7, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SC, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xe0000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps(Dep.RT)
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void sc(Context context, int machInst) {
        context.getProcess().getMemory().writeWord(getEffectiveAddress(context, machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), 1);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SDC1, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xf4000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.FT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.FT)
    private static void sdc1(Context context, int machInst) {
        context.getProcess().getMemory().writeDoubleWord(getEffectiveAddress(context, machInst), context.getRegs().getFpr().getLong(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SH, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xa4000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void sh(Context context, int machInst) {
        context.getProcess().getMemory().writeHalfWord(getEffectiveAddress(context, machInst), (short) MathHelper.bits(context.getRegs().getGpr(BitField.RT.valueOf(machInst)), 15, 0));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SW, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xac000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void sw(Context context, int machInst) {
        context.getProcess().getMemory().writeWord(getEffectiveAddress(context, machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SWC1, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xe4000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.FT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.FT)
    private static void swc1(Context context, int machInst) {
        context.getProcess().getMemory().writeWord(getEffectiveAddress(context, machInst), context.getRegs().getFpr().getInt(BitField.FT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SWL, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xa8000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void swl(Context context, int machInst) {
        int addr = getEffectiveAddress(context, machInst);
        int size = 4 - (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        byte[] dst = new byte[4];
        for (int i = 0; i < size; i++) {
            dst[i] = src[3 - i];
        }

        context.getProcess().getMemory().writeBlock(addr, size, dst);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SWR, fuOperationType = FunctionalUnitOperationType.WRITE_PORT)
    @StaticInstructionFlags({StaticInstructionFlag.STORE, StaticInstructionFlag.DISPLACED_ADDRESSING})
    @DecodeMethod(bits = 0xb8000000, mask = 0xfc000000)
    @Ideps({Dep.RS, Dep.RT})
    @Odeps({})
    @NonEffectiveAddressBaseDep(Dep.RT)
    private static void swr(Context context, int machInst) {
        int addr = getEffectiveAddress(context, machInst);
        int size = 1 + (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        byte[] dst = new byte[4];
        for (int i = 0; i < size; i++) {
            dst[i] = src[size - i - 1];
        }

        context.getProcess().getMemory().writeBlock(addr - size + 1, size, dst);
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CFC1, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44400000, mask = 0xffe007ff)
    @Ideps(Dep.REG_FCSR)
    @Odeps(Dep.RT)
    private static void cfc1(Context context, int machInst) {
        if (BitField.FS.valueOf(machInst) == 31) {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getFcsr());
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.CTC1, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44c00000, mask = 0xffe007ff)
    @Ideps(Dep.RT)
    @Odeps(Dep.REG_FCSR)
    private static void ctc1(Context context, int machInst) {
        if (BitField.FS.valueOf(machInst) != 0) {
            context.getRegs().setFcsr(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MFC1, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44000000, mask = 0xffe007ff)
    @Ideps(Dep.FS)
    @Odeps(Dep.RT)
    private static void mfc1(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getFpr().getInt(BitField.FS.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.MTC1, fuOperationType = FunctionalUnitOperationType.INT_ALU)
    @StaticInstructionFlags({StaticInstructionFlag.INTEGER_COMPUTATION})
    @DecodeMethod(bits = 0x44800000, mask = 0xffe007ff)
    @Ideps(Dep.RT)
    @Odeps(Dep.FS)
    private static void mtc1(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FS.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.BREAK, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.TRAP})
    @DecodeMethod(bits = 0x0000000d, mask = 0xfc00003f)
    @Ideps({})
    @Odeps({})
    private static void _break(Context context, int machInst) {
        if (!context.isSpeculative()) {
            context.finish();
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.SYSTEM_CALL, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.TRAP})
    @DecodeMethod(bits = 0x0000000c, mask = 0xfc00003f)
    @Ideps(Dep.REG_V0)
    @Odeps({})
    private static void system_call(Context context, int machInst) {
        if (!context.isSpeculative()) {
            context.getKernel().getSystemCallEmulation().doSystemCall(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_V0), context);
        }
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.NOP, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.NOP})
    @DecodeMethod(bits = 0x00000000, mask = 0xffffffff)
    @Ideps({})
    @Odeps({})
    private static void nop(Context context, int machInst) {
    }

    @StaticInstructionIntrinsic(mnemonic = Mnemonic.UNKNOWN, fuOperationType = FunctionalUnitOperationType.NONE)
    @StaticInstructionFlags({StaticInstructionFlag.UNKNOWN})
    @DecodeMethod(bits = 0x0, mask = 0x0) //TODO: special support for unknown instruction
    @Ideps({})
    @Odeps({})
    private static void unknown(Context context, int machInst) {
        if (!context.isSpeculative()) {
            Logger.panicf(Logger.INSTRUCTION, "{ctx-%d} 0x%08x: Unknown instruction 0x%08x", context.getCycleAccurateEventQueue().getCurrentCycle(), context.getId(), context.getRegs().getPc(), machInst);
        }
    }

    private static void onFunctionCall(Context context, boolean jalr) {
        StaticInstruction staticInst = context.getProcess().getStaticInst(context.getRegs().getPc());
        int targetPc = jalr ? getTargetPcForJalr(context, staticInst.getMachInst()) : getTargetPcForJr(context, staticInst.getMachInst());
        context.getFunctionCallContextStack().push(new FunctionCallContext(context.getRegs().getPc(), targetPc));
        context.getBlockingEventDispatcher().dispatch(new FunctionalCallEvent(context));
    }

    private static void onFunctionReturn(Context context) {
        context.getFunctionCallContextStack().pop();
        context.getBlockingEventDispatcher().dispatch(new FunctionReturnEvent(context));
    }

    public boolean useStackPointerAsEffectiveAddressBase() {
        return useStackPointerAsEffectiveAddressBase(this.machInst);
    }

    public int getEffectiveAddressBase(Context context) {
        return getEffectiveAddress(context, this.machInst);
    }

    public int getEffectiveAddressDisplacement() {
        return getEffectiveAddressDisplacement(this.machInst);
    }

    public int getEffectiveAddress(Context context) {
        return getEffectiveAddress(context, this.machInst);
    }

    public static boolean useStackPointerAsEffectiveAddressBase(int machInst) {
        return BitField.RS.valueOf(machInst) == ArchitecturalRegisterFile.REG_SP;
    }

    public static int getEffectiveAddressBase(Context context, int machInst) {
        return context.getRegs().getGpr(BitField.RS.valueOf(machInst));
    }

    public static int getEffectiveAddressDisplacement(int machInst) {
        return MathHelper.signExtend(BitField.INTIMM.valueOf(machInst));
    }

    public static int getEffectiveAddress(Context context, int machInst) {
        return getEffectiveAddressBase(context, machInst) + getEffectiveAddressDisplacement(machInst);
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
        context.getRegs().setNnpc(targetPc);
    }

    private static void doBranch(Context context, int machInst) {
        context.getRegs().setNnpc(getTargetPcForBranch(context.getRegs().getNpc(), machInst));
    }

    private static void skipDelaySlot(Context context) {
        context.getRegs().setNpc(context.getRegs().getNnpc());
        context.getRegs().setNnpc(context.getRegs().getNnpc() + 4);
    }

    public static int getTargetPcForControl(int pc, int machInst, Mnemonic mnemonic) {
        switch (mnemonic) {
            case J:
                return getTargetPcForJ(pc, machInst);
            case JAL:
                return getTargetPcForJal(pc, machInst);
            case JALR:
                throw new IllegalArgumentException();
            case JR:
                throw new IllegalArgumentException();
            default:
                return getTargetPcForBranch(pc, machInst);
        }
    }

    private static int getTargetPcForBranch(int pc, int machInst) {
        return pc + MathHelper.signExtend(BitField.INTIMM.valueOf(machInst) << 2);
    }

    private static int getTargetPcForJ(int pc, int machInst) {
        return MathHelper.mbits(pc, 32, 28) | (BitField.TARGET.valueOf(machInst) << 2);
    }

    private static int getTargetPcForJal(int pc, int machInst) {
        return MathHelper.mbits(pc, 32, 28) | (BitField.TARGET.valueOf(machInst) << 2);
    }

    public static int getTargetPcForJalr(Context context, int machInst) {
        return context.getRegs().getGpr(BitField.RS.valueOf(machInst));
    }

    public static int getTargetPcForJr(Context context, int machInst) {
        return context.getRegs().getGpr(BitField.RS.valueOf(machInst));
    }

    public static void execute(StaticInstruction staticInst, Context context) {
        int oldPc = context.getRegs().getPc();
        try {
            staticInst.mnemonic.getMethod().invoke(null, context, staticInst.machInst);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        context.getBlockingEventDispatcher().dispatch(new InstructionFunctionallyExecutedEvent(context, oldPc, staticInst));
    }

    public static final StaticInstruction NOP = new StaticInstruction(Mnemonic.NOP, 0x0);
}
