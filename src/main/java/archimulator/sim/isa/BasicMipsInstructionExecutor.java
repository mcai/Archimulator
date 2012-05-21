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

import archimulator.sim.os.Context;
import net.pickapack.Reference;
import net.pickapack.math.MathHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BasicMipsInstructionExecutor implements MipsInstructionExecutor {
    public void add_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void addi_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + MathHelper.signExtend(BitField.INTIMM.valueOf(machInst)));
    }

    public void addiu_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + MathHelper.signExtend(BitField.INTIMM.valueOf(machInst)));
    }

    public void addu_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) + context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void and_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) & context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void andi_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) & MathHelper.zeroExtend(BitField.INTIMM.valueOf(machInst)));
    }

    public void div_impl(Context context, int machInst) {
        int rs = context.getRegs().getGpr(BitField.RS.valueOf(machInst));
        int rt = context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        context.getRegs().setHi(rt != 0 ? rs % rt : 0);
        context.getRegs().setLo(rt != 0 ? rs / rt : 0);
    }

    public void divu_impl(Context context, int machInst) {
        long rs = ((long) context.getRegs().getGpr(BitField.RS.valueOf(machInst))) << 32 >>> 32;
        long rt = ((long) context.getRegs().getGpr(BitField.RT.valueOf(machInst))) << 32 >>> 32;

        context.getRegs().setHi(rt != 0 ? (int) (((rs % rt) << 32) >> 32) : 0);
        context.getRegs().setLo(rt != 0 ? (int) (((rs / rt) << 32) >> 32) : 0);
    }

    public void lui_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), BitField.INTIMM.valueOf(machInst) << 16);
    }

    public void madd_impl(Context context, int machInst) {
        long temp = (long) context.getRegs().getHi() << 32 | (long) context.getRegs().getLo() + context.getRegs().getGpr(BitField.RS.valueOf(machInst)) * context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        context.getRegs().setHi((int) (temp >> 32));
        context.getRegs().setLo((int) ((temp << 32) >> 32));
    }

    public void mfhi_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getHi());
    }

    public void mflo_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getLo());
    }

    public void msub_impl(Context context, int machInst) {
        long temp = (long) context.getRegs().getHi() << 32 | (long) context.getRegs().getLo() - context.getRegs().getGpr(BitField.RS.valueOf(machInst)) * context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        context.getRegs().setHi((int) (temp >> 32));
        context.getRegs().setLo((int) ((temp << 32) >> 32));
    }

    public void mthi_impl(Context context, int machInst) {
        context.getRegs().setHi(context.getRegs().getGpr(BitField.RD.valueOf(machInst)));
    }

    public void mtlo_impl(Context context, int machInst) {
        context.getRegs().setLo(context.getRegs().getGpr(BitField.RD.valueOf(machInst)));
    }

    public void mult_impl(Context context, int machInst) {
        long product = (long) (context.getRegs().getGpr(BitField.RS.valueOf(machInst))) * (long) (context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        context.getRegs().setHi((int) (product >> 32));
        context.getRegs().setLo((int) ((product << 32) >> 32));
    }

    public void multu_impl(Context context, int machInst) {
        long product = (((long) context.getRegs().getGpr(BitField.RS.valueOf(machInst))) << 32 >>> 32) * (((long) context.getRegs().getGpr(BitField.RT.valueOf(machInst))) << 32 >>> 32);

        context.getRegs().setHi((int) (product >> 32));
        context.getRegs().setLo((int) ((product << 32) >> 32));
    }

    public void nor_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), ~(context.getRegs().getGpr(BitField.RS.valueOf(machInst)) | context.getRegs().getGpr(BitField.RT.valueOf(machInst))));
    }

    public void or_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) | context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void ori_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) | MathHelper.zeroExtend(BitField.INTIMM.valueOf(machInst)));
    }

    public void sll_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) << BitField.SHIFT.valueOf(machInst));
    }

    public void sllv_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) << MathHelper.bits(context.getRegs().getGpr(BitField.RS.valueOf(machInst)), 4, 0));
    }

    public void slt_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < context.getRegs().getGpr(BitField.RT.valueOf(machInst))) ? 1 : 0);
    }

    public void slti_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < MathHelper.signExtend(BitField.INTIMM.valueOf(machInst))) ? 1 : 0);
    }

    public void sltiu_impl(Context context, int machInst) {
        int rs = context.getRegs().getGpr(BitField.RS.valueOf(machInst));
        int imm = MathHelper.signExtend(BitField.INTIMM.valueOf(machInst));

        if (rs >= 0 && imm >= 0 || rs < 0 && imm < 0) {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), (rs < imm) ? 1 : 0);
        } else {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), (rs >= 0) ? 1 : 0);
        }
    }

    public void sltu_impl(Context context, int machInst) {
        int rs = context.getRegs().getGpr(BitField.RS.valueOf(machInst));
        int rt = context.getRegs().getGpr(BitField.RT.valueOf(machInst));

        if (rs >= 0 && rt >= 0 || rs < 0 && rt < 0) {
            context.getRegs().setGpr(BitField.RD.valueOf(machInst), (rs < rt) ? 1 : 0);
        } else {
            context.getRegs().setGpr(BitField.RD.valueOf(machInst), (rs >= 0) ? 1 : 0);
        }
    }

    public void sra_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >> BitField.SHIFT.valueOf(machInst));
    }

    public void srav_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >> MathHelper.bits(context.getRegs().getGpr(BitField.RS.valueOf(machInst)), 4, 0));
    }

    public void srl_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >>> BitField.SHIFT.valueOf(machInst));
    }

    public void srlv_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)) >>> MathHelper.bits(context.getRegs().getGpr(BitField.RS.valueOf(machInst)), 4, 0));
    }

    public void sub_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) - context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void subu_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) - context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void xor_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) ^ context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void xori_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getGpr(BitField.RS.valueOf(machInst)) ^ MathHelper.zeroExtend(BitField.INTIMM.valueOf(machInst)));
    }

    public void abs_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), Math.abs(context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst))));
    }

    public void abs_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), Math.abs(context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst))));
    }

    public void add_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) + context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    public void add_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) + context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    public void c_cond_d_impl(Context context, int machInst) {
        double fs = context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst));
        double ft = context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst));

        boolean unordered = Double.isNaN(fs) || Double.isNaN(ft);

        c_cond(context, machInst, unordered, !unordered && fs < ft, !unordered && fs == ft);
    }

    public void c_cond_s_impl(Context context, int machInst) {
        float fs = context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst));
        float ft = context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst));

        boolean unordered = Float.isNaN(fs) || Float.isNaN(ft);

        c_cond(context, machInst, unordered, !unordered && fs < ft, !unordered && fs == ft);
    }

    private void c_cond(Context context, int machInst, boolean unordered, boolean less, boolean equal) {
        int cond = BitField.COND.valueOf(machInst);

        Reference<Integer> fcsrRef = new Reference<Integer>(context.getRegs().getFcsr());

        if ((((cond & 0x4) != 0) && less) || (((cond & 0x2) != 0) && equal) || (((cond & 0x1) != 0) && unordered)) {
            setFCC(fcsrRef, BitField.CC.valueOf(machInst));
        } else {
            clearFCC(fcsrRef, BitField.CC.valueOf(machInst));
        }

        context.getRegs().setFcsr(fcsrRef.get());
    }

    public void cvt_d_l_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getLong(BitField.FS.valueOf(machInst)));
    }

    public void cvt_d_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    public void cvt_d_w_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getInt(BitField.FS.valueOf(machInst)));
    }

    public void cvt_l_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setLong(BitField.FD.valueOf(machInst), (long) context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    public void cvt_l_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setLong(BitField.FD.valueOf(machInst), (long) context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    public void cvt_s_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), (float) context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    public void cvt_s_l_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getLong(BitField.FS.valueOf(machInst)));
    }

    public void cvt_s_w_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getInt(BitField.FS.valueOf(machInst)));
    }

    public void cvt_w_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FD.valueOf(machInst), (int) context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    public void cvt_w_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FD.valueOf(machInst), (int) context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    public void div_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) / context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    public void div_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) / context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    public void mov_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    public void mov_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    public void movf_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: movf
    }

    public void _movf_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movf
    }

    public void movn_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: movn
    }

    public void _movn_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movn
    }

    public void _movt_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movt
    }

    public void movz_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: movz
    }

    public void _movz_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: _movz
    }

    public void mul_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: mul
    }

    public void trunc_w_impl(Context context, int machInst) {
        throw new UnsupportedOperationException(); //TODO: trunc_w
    }

    public void mul_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) * context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    public void mul_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) * context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    public void neg_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), -context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)));
    }

    public void neg_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), -context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)));
    }

    public void sqrt_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), Math.sqrt(context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst))));
    }

    public void sqrt_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), (float) Math.sqrt(context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst))));
    }

    public void sub_d_impl(Context context, int machInst) {
        context.getRegs().getFpr().setDouble(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getDouble(BitField.FS.valueOf(machInst)) - context.getRegs().getFpr().getDouble(BitField.FT.valueOf(machInst)));
    }

    public void sub_s_impl(Context context, int machInst) {
        context.getRegs().getFpr().setFloat(BitField.FD.valueOf(machInst), context.getRegs().getFpr().getFloat(BitField.FS.valueOf(machInst)) - context.getRegs().getFpr().getFloat(BitField.FT.valueOf(machInst)));
    }

    public void j_impl(Context context, int machInst) {
        doJump(context, getTargetPcForJ(context.getRegs().getNpc(), machInst));
    }

    public void jal_impl(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());
        doJump(context, getTargetPcForJal(context.getRegs().getNpc(), machInst));
    }

    public void jalr_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RD.valueOf(machInst), context.getRegs().getNnpc());
        doJump(context, getTargetPcForJalr(context, machInst));
    }

    public void jr_impl(Context context, int machInst) {
        doJump(context, getTargetPcForJr(context, machInst));
    }

    public void b_impl(Context context, int machInst) {
        doBranch(context, machInst);
    }

    public void bal_impl(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());
        doBranch(context, machInst);
    }

    public void bc1f_impl(Context context, int machInst) {
        if (!getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    public void bc1fl_impl(Context context, int machInst) {
        if (!getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void bc1t_impl(Context context, int machInst) {
        if (getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    public void bc1tl_impl(Context context, int machInst) {
        if (getFCC(context.getRegs().getFcsr(), BitField.BRANCH_CC.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void beq_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) == context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    public void beql_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) == context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void bgez_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        }
    }

    public void bgezal_impl(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        }
    }

    public void bgezall_impl(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void bgezl_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) >= 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void bgtz_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) > 0) {
            doBranch(context, machInst);
        }
    }

    public void bgtzl_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) > 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void blez_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) <= 0) {
            doBranch(context, machInst);
        }
    }

    public void blezl_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) <= 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void bltz_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        }
    }

    public void bltzal_impl(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        }
    }

    public void bltzall_impl(Context context, int machInst) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, context.getRegs().getNnpc());

        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void bltzl_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) < 0) {
            doBranch(context, machInst);
        }
    }

    public void bne_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) != context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        }
    }

    public void bnel_impl(Context context, int machInst) {
        if (context.getRegs().getGpr(BitField.RS.valueOf(machInst)) != context.getRegs().getGpr(BitField.RT.valueOf(machInst))) {
            doBranch(context, machInst);
        } else {
            skipDelaySlot(context);
        }
    }

    public void lb_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readByte(StaticInstruction.getEffectiveAddress(context, machInst)));
    }

    public void lbu_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readByte(StaticInstruction.getEffectiveAddress(context, machInst)) & 0xff);
    }

    public void ldc1_impl(Context context, int machInst) {
        context.getRegs().getFpr().setLong(BitField.FT.valueOf(machInst), context.getProcess().getMemory().readDoubleWord(StaticInstruction.getEffectiveAddress(context, machInst)));
    }

    public void lh_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readHalfWord(StaticInstruction.getEffectiveAddress(context, machInst)));
    }

    public void lhu_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readHalfWord(StaticInstruction.getEffectiveAddress(context, machInst)) & 0xffff);
    }

    public void ll_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readWord(StaticInstruction.getEffectiveAddress(context, machInst)));
    }

    public void lw_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getProcess().getMemory().readWord(StaticInstruction.getEffectiveAddress(context, machInst)));
    }

    public void lwc1_impl(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FT.valueOf(machInst), context.getProcess().getMemory().readWord(StaticInstruction.getEffectiveAddress(context, machInst)));
    }

    public void lwl_impl(Context context, int machInst) {
        int addr = StaticInstruction.getEffectiveAddress(context, machInst);
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

    public void lwr_impl(Context context, int machInst) {
        int addr = StaticInstruction.getEffectiveAddress(context, machInst);
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

    public void sb_impl(Context context, int machInst) {
        context.getProcess().getMemory().writeByte(StaticInstruction.getEffectiveAddress(context, machInst), (byte) MathHelper.bits(context.getRegs().getGpr(BitField.RT.valueOf(machInst)), 7, 0));
    }

    public void sc_impl(Context context, int machInst) {
        context.getProcess().getMemory().writeWord(StaticInstruction.getEffectiveAddress(context, machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), 1);
    }

    public void sdc1_impl(Context context, int machInst) {
        context.getProcess().getMemory().writeDoubleWord(StaticInstruction.getEffectiveAddress(context, machInst), context.getRegs().getFpr().getLong(BitField.FT.valueOf(machInst)));
    }

    public void sh_impl(Context context, int machInst) {
        context.getProcess().getMemory().writeHalfWord(StaticInstruction.getEffectiveAddress(context, machInst), (short) MathHelper.bits(context.getRegs().getGpr(BitField.RT.valueOf(machInst)), 15, 0));
    }

    public void sw_impl(Context context, int machInst) {
        context.getProcess().getMemory().writeWord(StaticInstruction.getEffectiveAddress(context, machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    public void swc1_impl(Context context, int machInst) {
        context.getProcess().getMemory().writeWord(StaticInstruction.getEffectiveAddress(context, machInst), context.getRegs().getFpr().getInt(BitField.FT.valueOf(machInst)));
    }

    public void swl_impl(Context context, int machInst) {
        int addr = StaticInstruction.getEffectiveAddress(context, machInst);
        int size = 4 - (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        byte[] dst = new byte[4];
        for (int i = 0; i < size; i++) {
            dst[i] = src[3 - i];
        }

        context.getProcess().getMemory().writeBlock(addr, size, dst);
    }

    public void swr_impl(Context context, int machInst) {
        int addr = StaticInstruction.getEffectiveAddress(context, machInst);
        int size = 1 + (addr & 3);

        byte[] src = new byte[4];
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).putInt(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));

        byte[] dst = new byte[4];
        for (int i = 0; i < size; i++) {
            dst[i] = src[size - i - 1];
        }

        context.getProcess().getMemory().writeBlock(addr - size + 1, size, dst);
    }

    public void cfc1_impl(Context context, int machInst) {
        if (BitField.FS.valueOf(machInst) == 31) {
            context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getFcsr());
        }
    }

    public void ctc1_impl(Context context, int machInst) {
        if (BitField.FS.valueOf(machInst) != 0) {
            context.getRegs().setFcsr(context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
        }
    }

    public void mfc1_impl(Context context, int machInst) {
        context.getRegs().setGpr(BitField.RT.valueOf(machInst), context.getRegs().getFpr().getInt(BitField.FS.valueOf(machInst)));
    }

    public void mtc1_impl(Context context, int machInst) {
        context.getRegs().getFpr().setInt(BitField.FS.valueOf(machInst), context.getRegs().getGpr(BitField.RT.valueOf(machInst)));
    }

    private boolean getFCC(int fcsr, int ccIdx) {
        return ((fcsr >> ((ccIdx == 0) ? 23 : ccIdx + 24)) & 0x00000001) != 0;
    }

    private void setFCC(Reference<Integer> fcsr, int cc) {
        fcsr.set(fcsr.get() | (cc == 0 ? 0x800000 : 0x1000000 << cc));
    }

    private void clearFCC(Reference<Integer> fcsr, int cc) {
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
}
