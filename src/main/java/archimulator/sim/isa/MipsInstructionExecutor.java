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

public interface MipsInstructionExecutor {
    void add_impl(Context context, int machInst);

    void addi_impl(Context context, int machInst);

    void addiu_impl(Context context, int machInst);

    void addu_impl(Context context, int machInst);

    void and_impl(Context context, int machInst);

    void andi_impl(Context context, int machInst);

    void div_impl(Context context, int machInst);

    void divu_impl(Context context, int machInst);

    void lui_impl(Context context, int machInst);

    void madd_impl(Context context, int machInst);

    void mfhi_impl(Context context, int machInst);

    void mflo_impl(Context context, int machInst);

    void msub_impl(Context context, int machInst);

    void mthi_impl(Context context, int machInst);

    void mtlo_impl(Context context, int machInst);

    void mult_impl(Context context, int machInst);

    void multu_impl(Context context, int machInst);

    void nor_impl(Context context, int machInst);

    void or_impl(Context context, int machInst);

    void ori_impl(Context context, int machInst);

    void sll_impl(Context context, int machInst);

    void sllv_impl(Context context, int machInst);

    void slt_impl(Context context, int machInst);

    void slti_impl(Context context, int machInst);

    void sltiu_impl(Context context, int machInst);

    void sltu_impl(Context context, int machInst);

    void sra_impl(Context context, int machInst);

    void srav_impl(Context context, int machInst);

    void srl_impl(Context context, int machInst);

    void srlv_impl(Context context, int machInst);

    void sub_impl(Context context, int machInst);

    void subu_impl(Context context, int machInst);

    void xor_impl(Context context, int machInst);

    void xori_impl(Context context, int machInst);

    void abs_d_impl(Context context, int machInst);

    void abs_s_impl(Context context, int machInst);

    void add_d_impl(Context context, int machInst);

    void add_s_impl(Context context, int machInst);

    void c_cond_d_impl(Context context, int machInst);

    void c_cond_s_impl(Context context, int machInst);

    void cvt_d_l_impl(Context context, int machInst);

    void cvt_d_s_impl(Context context, int machInst);

    void cvt_d_w_impl(Context context, int machInst);

    void cvt_l_d_impl(Context context, int machInst);

    void cvt_l_s_impl(Context context, int machInst);

    void cvt_s_d_impl(Context context, int machInst);

    void cvt_s_l_impl(Context context, int machInst);

    void cvt_s_w_impl(Context context, int machInst);

    void cvt_w_d_impl(Context context, int machInst);

    void cvt_w_s_impl(Context context, int machInst);

    void div_d_impl(Context context, int machInst);

    void div_s_impl(Context context, int machInst);

    void mov_d_impl(Context context, int machInst);

    void mov_s_impl(Context context, int machInst);

    void movf_impl(Context context, int machInst);

    void _movf_impl(Context context, int machInst);

    void movn_impl(Context context, int machInst);

    void _movn_impl(Context context, int machInst);

    void _movt_impl(Context context, int machInst);

    void movz_impl(Context context, int machInst);

    void _movz_impl(Context context, int machInst);

    void mul_impl(Context context, int machInst);

    void trunc_w_impl(Context context, int machInst);

    void mul_d_impl(Context context, int machInst);

    void mul_s_impl(Context context, int machInst);

    void neg_d_impl(Context context, int machInst);

    void neg_s_impl(Context context, int machInst);

    void sqrt_d_impl(Context context, int machInst);

    void sqrt_s_impl(Context context, int machInst);

    void sub_d_impl(Context context, int machInst);

    void sub_s_impl(Context context, int machInst);

    void j_impl(Context context, int machInst);

    void jal_impl(Context context, int machInst);

    void jalr_impl(Context context, int machInst);

    void jr_impl(Context context, int machInst);

    void b_impl(Context context, int machInst);

    void bal_impl(Context context, int machInst);

    void bc1f_impl(Context context, int machInst);

    void bc1fl_impl(Context context, int machInst);

    void bc1t_impl(Context context, int machInst);

    void bc1tl_impl(Context context, int machInst);

    void beq_impl(Context context, int machInst);

    void beql_impl(Context context, int machInst);

    void bgez_impl(Context context, int machInst);

    void bgezal_impl(Context context, int machInst);

    void bgezall_impl(Context context, int machInst);

    void bgezl_impl(Context context, int machInst);

    void bgtz_impl(Context context, int machInst);

    void bgtzl_impl(Context context, int machInst);

    void blez_impl(Context context, int machInst);

    void blezl_impl(Context context, int machInst);

    void bltz_impl(Context context, int machInst);

    void bltzal_impl(Context context, int machInst);

    void bltzall_impl(Context context, int machInst);

    void bltzl_impl(Context context, int machInst);

    void bne_impl(Context context, int machInst);

    void bnel_impl(Context context, int machInst);

    void lb_impl(Context context, int machInst);

    void lbu_impl(Context context, int machInst);

    void ldc1_impl(Context context, int machInst);

    void lh_impl(Context context, int machInst);

    void lhu_impl(Context context, int machInst);

    void ll_impl(Context context, int machInst);

    void lw_impl(Context context, int machInst);

    void lwc1_impl(Context context, int machInst);

    void lwl_impl(Context context, int machInst);

    void lwr_impl(Context context, int machInst);

    void sb_impl(Context context, int machInst);

    void sc_impl(Context context, int machInst);

    void sdc1_impl(Context context, int machInst);

    void sh_impl(Context context, int machInst);

    void sw_impl(Context context, int machInst);

    void swc1_impl(Context context, int machInst);

    void swl_impl(Context context, int machInst);

    void swr_impl(Context context, int machInst);

    void cfc1_impl(Context context, int machInst);

    void ctc1_impl(Context context, int machInst);

    void mfc1_impl(Context context, int machInst);

    void mtc1_impl(Context context, int machInst);
}
