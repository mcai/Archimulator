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
package archimulator.isa;

import com.sun.jna.Callback;
import com.sun.jna.Library;

public interface NativeMipsIsaEmulatorInterface extends Library {
    public interface GetGprCallback extends Callback {
        int invoke(int ctx, int index);
    }

    public interface SetGprCallback extends Callback {
        void invoke(int ctx, int index, int value);
    }

    public interface GetFprsCallback extends Callback {
        float invoke(int ctx, int index);
    }

    public interface SetFprsCallback extends Callback {
        void invoke(int ctx, int index, float value);
    }

    public interface GetFprdCallback extends Callback {
        double invoke(int ctx, int index);
    }

    public interface SetFprdCallback extends Callback {
        void invoke(int ctx, int index, double value);
    }

    public interface GetFirCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetFirCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface GetFcsrCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetFcsrCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface GetHiCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetHiCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface GetLoCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetLoCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface GetPcCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetPcCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface GetNpcCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetNpcCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface GetNnpcCallback extends Callback {
        int invoke(int ctx);
    }

    public interface SetNnpcCallback extends Callback {
        void invoke(int ctx, int value);
    }

    public interface MemReadByteCallback extends Callback {
        byte invoke(int processId, int addr);
    }

    public interface MemReadHalfWordCallback extends Callback {
        short invoke(int processId, int addr);
    }

    public interface MemReadWordCallback extends Callback {
        int invoke(int processId, int addr);
    }

    public interface MemReadDoubleWordCallback extends Callback {
        long invoke(int processId, int addr);
    }

    public interface MemWriteByteCallback extends Callback {
        void invoke(int processId, int addr, byte data);
    }

    public interface MemWriteHalfWordCallback extends Callback {
        void invoke(int processId, int addr, short data);
    }

    public interface MemWriteWordCallback extends Callback {
        void invoke(int processId, int addr, int data);
    }

    public interface MemWriteDoubleWordCallback extends Callback {
        void invoke(int processId, int addr, long data);
    }

    public interface GetStackBaseCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetStackBaseCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetStackSizeCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetStackSizeCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetTextSizeCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetTextSizeCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetEnvironBaseCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetEnvironBaseCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetHeapTopCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetHeapTopCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetDataTopCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetDataTopCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetProgramEntryCallback extends Callback {
        int invoke(int processId);
    }

    public interface SetProgramEntryCallback extends Callback {
        void invoke(int processId, int value);
    }

    public interface GetProcessIdFromContextIdCallback extends Callback {
        int invoke(int ctx);
    }

    void initGetGprCallback(GetGprCallback callback);

    void initSetGprCallback(SetGprCallback callback);

    void initGetFprsCallback(GetFprsCallback callback);

    void initSetFprsCallback(SetFprsCallback callback);

    void initGetFprdCallback(GetFprdCallback callback);

    void initSetFprdCallback(SetFprdCallback callback);

    void initGetFirCallback(GetFirCallback callback);

    void initSetFirCallback(SetFirCallback callback);

    void initGetFcsrCallback(GetFcsrCallback callback);

    void initSetFcsrCallback(SetFcsrCallback callback);

    void initGetHiCallback(GetHiCallback callback);

    void initSetHiCallback(SetHiCallback callback);

    void initGetLoCallback(GetLoCallback callback);

    void initSetLoCallback(SetLoCallback callback);

    void initGetPcCallback(GetPcCallback callback);

    void initSetPcCallback(SetPcCallback callback);

    void initGetNpcCallback(GetNpcCallback callback);

    void initSetNpcCallback(SetNpcCallback callback);

    void initGetNnpcCallback(GetNnpcCallback callback);

    void initSetNnpcCallback(SetNnpcCallback callback);

    void initMemReadByteCallback(MemReadByteCallback callback);

    void initMemReadHalfWordCallback(MemReadHalfWordCallback callback);

    void initMemReadWordCallback(MemReadWordCallback callback);

    void initMemReadDoubleWordCallback(MemReadDoubleWordCallback callback);

    void initMemWriteByteCallback(MemWriteByteCallback callback);

    void initMemWriteHalfWordCallback(MemWriteHalfWordCallback callback);

    void initMemWriteWordCallback(MemWriteWordCallback callback);

    void initMemWriteDoubleWordCallback(MemWriteDoubleWordCallback callback);

    void initGetStackBaseCallback(GetStackBaseCallback callback);

    void initSetStackBaseCallback(SetStackBaseCallback callback);

    void initGetStackSizeCallback(GetStackSizeCallback callback);

    void initSetStackSizeCallback(SetStackSizeCallback callback);

    void initGetTextSizeCallback(GetTextSizeCallback callback);

    void initSetTextSizeCallback(SetTextSizeCallback callback);

    void initGetEnvironBaseCallback(GetEnvironBaseCallback callback);

    void initSetEnvironBaseCallback(SetEnvironBaseCallback callback);

    void initGetHeapTopCallback(GetHeapTopCallback callback);

    void initSetHeapTopCallback(SetHeapTopCallback callback);

    void initGetDataTopCallback(GetDataTopCallback callback);

    void initSetDataTopCallback(SetDataTopCallback callback);

    void initGetProgramEntryCallback(GetProgramEntryCallback callback);

    void initSetProgramEntryCallback(SetProgramEntryCallback callback);

    void initGetProcessIdFromContextIdCallback(GetProcessIdFromContextIdCallback callback);

    void single_thread_program_init(int argc, String[] argv);

    boolean single_thread_program_execute_next_instruction();

    void single_thread_program_quit();

    void single_thread_program_dump_regs();

    int single_thread_program_get_gpr(int index);

    float single_thread_program_get_fprs(int index);

    double single_thread_program_get_fprd(int index);

    int single_thread_program_get_hi();

    int single_thread_program_get_lo();

    int single_thread_program_get_fir();

    int single_thread_program_get_fcsr();

    int single_thread_program_get_pc();

    int single_thread_program_get_npc();

    int single_thread_program_get_nnpc();

    void run_program(int argc, String[] argv);

    void load_prog(int processId, int argc, String[] argv);

    void decode_inst(int inst);

    void nop_impl(int ctx, int inst);

    void bc1f_impl(int ctx, int inst);

    void bc1t_impl(int ctx, int inst);

    void mfc1_impl(int ctx, int inst);

    void mtc1_impl(int ctx, int inst);

    void cfc1_impl(int ctx, int inst);

    void ctc1_impl(int ctx, int inst);

    void abs_impl(int ctx, int inst);

    void add_impl(int ctx, int inst);

    void _add_impl(int ctx, int inst);

    void addi_impl(int ctx, int inst);

    void addiu_impl(int ctx, int inst);

    void addu_impl(int ctx, int inst);

    void and_impl(int ctx, int inst);

    void andi_impl(int ctx, int inst);

    void b_impl(int ctx, int inst);

    void bal_impl(int ctx, int inst);

    void beq_impl(int ctx, int inst);

    void bgez_impl(int ctx, int inst);

    void bgezal_impl(int ctx, int inst);

    void bgtz_impl(int ctx, int inst);

    void blez_impl(int ctx, int inst);

    void bltz_impl(int ctx, int inst);

    void bne_impl(int ctx, int inst);

    void break_impl(int ctx, int inst);

    void c_impl(int ctx, int inst);

    void cvt_d_impl(int ctx, int inst);

    void cvt_s_impl(int ctx, int inst);

    void cvt_w_impl(int ctx, int inst);

    void div_impl(int ctx, int inst);

    void _div_impl(int ctx, int inst);

    void divu_impl(int ctx, int inst);

    void j_impl(int ctx, int inst);

    void jal_impl(int ctx, int inst);

    void jalr_impl(int ctx, int inst);

    void jr_impl(int ctx, int inst);

    void lb_impl(int ctx, int inst);

    void lbu_impl(int ctx, int inst);

    void ldc1_impl(int ctx, int inst);

    void lh_impl(int ctx, int inst);

    void lhu_impl(int ctx, int inst);

    void ll_impl(int ctx, int inst);

    void lui_impl(int ctx, int inst);

    void lw_impl(int ctx, int inst);

    void lwc1_impl(int ctx, int inst);

    void lwl_impl(int ctx, int inst);

    void lwr_impl(int ctx, int inst);

    void madd_impl(int ctx, int inst);

    void mfhi_impl(int ctx, int inst);

    void mflo_impl(int ctx, int inst);

    void mov_impl(int ctx, int inst);

    void movf_impl(int ctx, int inst);

    void _movf_impl(int ctx, int inst);

    void movn_impl(int ctx, int inst);

    void _movn_impl(int ctx, int inst);

    void _movt_impl(int ctx, int inst);

    void movz_impl(int ctx, int inst);

    void _movz_impl(int ctx, int inst);

    void msub_impl(int ctx, int inst);

    void mtlo_impl(int ctx, int inst);

    void mul_impl(int ctx, int inst);

    void _mul_impl(int ctx, int inst);

    void mult_impl(int ctx, int inst);

    void multu_impl(int ctx, int inst);

    void neg_impl(int ctx, int inst);

    void nor_impl(int ctx, int inst);

    void or_impl(int ctx, int inst);

    void ori_impl(int ctx, int inst);

    void sb_impl(int ctx, int inst);

    void sc_impl(int ctx, int inst);

    void sdc1_impl(int ctx, int inst);

    void sh_impl(int ctx, int inst);

    void sll_impl(int ctx, int inst);

    void sllv_impl(int ctx, int inst);

    void slt_impl(int ctx, int inst);

    void slti_impl(int ctx, int inst);

    void sltiu_impl(int ctx, int inst);

    void sltu_impl(int ctx, int inst);

    void sqrt_impl(int ctx, int inst);

    void sra_impl(int ctx, int inst);

    void srav_impl(int ctx, int inst);

    void srl_impl(int ctx, int inst);

    void srlv_impl(int ctx, int inst);

    void _sub_impl(int ctx, int inst);

    void subu_impl(int ctx, int inst);

    void sw_impl(int ctx, int inst);

    void swc1_impl(int ctx, int inst);

    void swl_impl(int ctx, int inst);

    void swr_impl(int ctx, int inst);

    void syscall_impl(int ctx, int inst);

    void trunc_w_impl(int ctx, int inst);

    void xor_impl(int ctx, int inst);

    void xori_impl(int ctx, int inst);

    void syscall_fstat64_impl(int ctx, int fd);
}
