#include <stdint.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <bfd.h>
#include <time.h>
#include <signal.h>
#include <assert.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/utsname.h>
#include <sys/uio.h>
#include <sys/mman.h>
#include <sys/resource.h>
#include <sys/times.h>
#include <sys/ioctl.h>
#include <sys/sysctl.h>
#include <sys/wait.h>

#include "misc.h"

#define MD_FMT_SINGLE		16
#define MD_FMT_DOUBLE		17
#define MD_FMT_WORD		20
#define MD_FMT_LONG		21
#define MD_FMT_PS		22

#define MD_FMT3_SINGLE		0
#define MD_FMT3_DOUBLE		1
#define MD_FMT3_WORD		4
#define MD_FMT3_LONG		5
#define MD_FMT3_PS		6

#define OPCODE		(instfld.opcode)
#define RS		(instfld.rs)
#define RT		(instfld.rt)
#define RD		(instfld.rd)
#define SHIFT		(instfld.shift)
#define FUNC		(instfld.func)
#define IMM		(instfld.imm)
#define UIMM		(instfld.uimm)
#define TARGET		(instfld.target)
#define FR		(instfld.fr)
#define FS		(instfld.fs)
#define FT		(instfld.ft)
#define FD		(instfld.fd)
#define FMT		(instfld.fmt)
#define FMT3		(instfld.fmt3)
#define COND		(instfld.cond)
#define INST		(instfld.inst)
#define CC		(instfld.cc)

static int rmw_lock = 0;
#define SETRMW		(rmw_lock = ctx)
#define GETRMW		(rmw_lock)
#define CURRENT		(ctx) 

/* opcodes */
#define MD_OPCODE_SPECIAL	0x00
#define MD_OPCODE_REGIMM	0x01
#define MD_OPCODE_COP1		0x11
#define MD_OPCODE_COP1X		0x13


/* return values for md_execute_inst */
#define MD_MSG_OK		0
#define MD_MSG_NOTIMPL		1
#define MD_MSG_FINISH		2
#define MD_MSG_SYSCALL		3
#define MD_MSG_FMTERROR		4
#define MD_MSG_FMT3ERROR	5
#define MD_MSG_STARTRMW		6
#define MD_MSG_ENDRMW		7

typedef word (*GetGprCallback)(int ctx, int index);
typedef void (*SetGprCallback)(int ctx, int index, word value);

typedef float (*GetFprsCallback)(int ctx, int index);
typedef void (*SetFprsCallback)(int ctx, int index, float value);

typedef double (*GetFprdCallback)(int ctx, int index);
typedef void (*SetFprdCallback)(int ctx, int index, double value);

typedef word (*GetFirCallback)(int ctx);
typedef void (*SetFirCallback)(int ctx, word value);

typedef word (*GetFcsrCallback)(int ctx);
typedef void (*SetFcsrCallback)(int ctx, word value);

typedef word (*GetHiCallback)(int ctx);
typedef void (*SetHiCallback)(int ctx, word value);

typedef word (*GetLoCallback)(int ctx);
typedef void (*SetLoCallback)(int ctx, word value);

typedef word (*GetPcCallback)(int ctx);
typedef void (*SetPcCallback)(int ctx, word value);

typedef word (*GetNpcCallback)(int ctx);
typedef void (*SetNpcCallback)(int ctx, word value);

typedef word (*GetNnpcCallback)(int ctx);
typedef void (*SetNnpcCallback)(int ctx, word value);

typedef byte (*MemReadByteCallback)(int processId, word addr);
typedef half (*MemReadHalfWordCallback)(int processId, word addr);
typedef word (*MemReadWordCallback)(int processId, word addr);
typedef dword (*MemReadDoubleWordCallback)(int processId, word addr);

typedef void (*MemWriteByteCallback)(int processId, word addr, byte data);
typedef void (*MemWriteHalfWordCallback)(int processId, word addr, half data);
typedef void (*MemWriteWordCallback)(int processId, word addr, word data);
typedef void (*MemWriteDoubleWordCallback)(int processId, word addr, dword data);

typedef word (*GetStackBaseCallback)(int processId);
typedef void (*SetStackBaseCallback)(int processId, word value);

typedef word (*GetStackSizeCallback)(int processId);
typedef void (*SetStackSizeCallback)(int processId, word value);

typedef word (*GetTextSizeCallback)(int processId);
typedef void (*SetTextSizeCallback)(int processId, word value);

typedef word (*GetEnvironBaseCallback)(int processId);
typedef void (*SetEnvironBaseCallback)(int processId, word value);

typedef word (*GetHeapTopCallback)(int processId);
typedef void (*SetHeapTopCallback)(int processId, word value);

typedef word (*GetDataTopCallback)(int processId);
typedef void (*SetDataTopCallback)(int processId, word value);

typedef word (*GetProgramEntryCallback)(int processId);
typedef void (*SetProgramEntryCallback)(int processId, word value);

typedef int (*GetProcessIdFromContextIdCallback)(int ctx);

GetGprCallback getGpr;
SetGprCallback setGpr;

GetFprsCallback getFprs;
SetFprsCallback setFprs;

GetFprdCallback getFprd;
SetFprdCallback setFprd;

GetFirCallback getFir;
SetFirCallback setFir;

GetFcsrCallback getFcsr;
SetFcsrCallback setFcsr;

GetHiCallback getHi;
SetHiCallback setHi;

GetLoCallback getLo;
SetLoCallback setLo;

GetPcCallback getPc;
SetPcCallback setPc;

GetNpcCallback getNpc;
SetNpcCallback setNpc;

GetNnpcCallback getNnpc;
SetNnpcCallback setNnpc;

MemReadByteCallback memReadByte;
MemReadHalfWordCallback memReadHalfWord;
MemReadWordCallback memReadWord;
MemReadDoubleWordCallback memReadDoubleWord;

MemWriteByteCallback memWriteByte;
MemWriteHalfWordCallback memWriteHalfWord;
MemWriteWordCallback memWriteWord;
MemWriteDoubleWordCallback memWriteDoubleWord;

GetStackBaseCallback getStackBase;
SetStackBaseCallback setStackBase;

GetStackSizeCallback getStackSize;
SetStackSizeCallback setStackSize;

GetTextSizeCallback getTextSize;
SetTextSizeCallback setTextSize;

GetEnvironBaseCallback getEnvironBase;
SetEnvironBaseCallback setEnvironBase;

GetHeapTopCallback getHeapTop;
SetHeapTopCallback setHeapTop;

GetDataTopCallback getDataTop;
SetDataTopCallback setDataTop;

GetProgramEntryCallback getProgramEntry;
SetProgramEntryCallback setProgramEntry;

GetProcessIdFromContextIdCallback getProcessIdFromContextId;

void memReadBlock(int processId, word addr, word size, byte *p)
{
	word i;
	for (i = 0; i < size; i++)
		* (p + i) = memReadByte(processId, addr + i);
}

void memReadString(int processId, word addr, int max_size, char *str)
{
	int i;
	for (i = 0; i <= max_size; i++) {
		* (str + i) = memReadByte(processId, addr + i);
		if (!str[i])
			break;
	}
}

void memWriteBlock(int processId, word addr, word size, byte *p)
{
	word i;
	for (i = 0; i < size; i++)
		memWriteByte(processId, addr + i, * (p + i));
}

void memWriteString(int processId, word addr, char *str)
{
	int i;
	for (i = 0; i <= strlen(str); i++)
		memWriteByte(processId, addr + i, str[i]);
}

void initGetGprCallback(GetGprCallback callback)
{
	getGpr = callback;
}

void initSetGprCallback(SetGprCallback callback)
{
	setGpr = callback;
}

void initGetFprsCallback(GetFprsCallback callback)
{
	getFprs = callback;
}

void initSetFprsCallback(SetFprsCallback callback)
{
	setFprs = callback;
}

void initGetFprdCallback(GetFprdCallback callback)
{
	getFprd = callback;
}

void initSetFprdCallback(SetFprdCallback callback)
{
	setFprd = callback;
}

void initGetFirCallback(GetFirCallback callback)
{
	getFir = callback;
}

void initSetFirCallback(SetFirCallback callback)
{
	setFir = callback;
}

void initGetFcsrCallback(GetFcsrCallback callback)
{
	getFcsr = callback;
}

void initSetFcsrCallback(SetFcsrCallback callback)
{
	setFcsr = callback;
}

void initGetHiCallback(GetHiCallback callback)
{
	getHi = callback;
}

void initSetHiCallback(SetHiCallback callback)
{
	setHi = callback;
}

void initGetLoCallback(GetLoCallback callback)
{
	getLo = callback;
}

void initSetLoCallback(SetLoCallback callback)
{
	setLo = callback;
}

void initGetPcCallback(GetPcCallback callback)
{
	getPc = callback;
}

void initSetPcCallback(SetPcCallback callback)
{
	setPc = callback;
}

void initGetNpcCallback(GetNpcCallback callback)
{
	getNpc = callback;
}

void initSetNpcCallback(SetNpcCallback callback)
{
	setNpc = callback;
}

void initGetNnpcCallback(GetNnpcCallback callback)
{
	getNnpc = callback;
}

void initSetNnpcCallback(SetNnpcCallback callback)
{
	setNnpc = callback;
}

void initMemReadByteCallback(MemReadByteCallback callback)
{
	memReadByte = callback;
}

void initMemReadHalfWordCallback(MemReadHalfWordCallback callback)
{
	memReadHalfWord = callback;
}

void initMemReadWordCallback(MemReadWordCallback callback)
{
	memReadWord = callback;
}

void initMemReadDoubleWordCallback(MemReadDoubleWordCallback callback)
{
	memReadDoubleWord = callback;
}

void initMemWriteByteCallback(MemWriteByteCallback callback)
{
	memWriteByte = callback;
}

void initMemWriteHalfWordCallback(MemWriteHalfWordCallback callback)
{
	memWriteHalfWord = callback;
}

void initMemWriteWordCallback(MemWriteWordCallback callback)
{
	memWriteWord = callback;
}

void initMemWriteDoubleWordCallback(MemWriteDoubleWordCallback callback)
{
	memWriteDoubleWord = callback;
}

void initGetStackBaseCallback(GetStackBaseCallback callback)
{
	getStackBase = callback;
}

void initSetStackBaseCallback(SetStackBaseCallback callback)
{
	setStackBase = callback;
}

void initGetStackSizeCallback(GetStackSizeCallback callback)
{
	getStackSize = callback;
}

void initSetStackSizeCallback(SetStackSizeCallback callback)
{
	setStackSize = callback;
}

void initGetTextSizeCallback(GetTextSizeCallback callback)
{
	getTextSize = callback;
}

void initSetTextSizeCallback(SetTextSizeCallback callback)
{
	setTextSize = callback;
}

void initGetEnvironBaseCallback(GetEnvironBaseCallback callback)
{
	getEnvironBase = callback;
}

void initSetEnvironBaseCallback(SetEnvironBaseCallback callback)
{
	setEnvironBase = callback;
}

void initGetHeapTopCallback(GetHeapTopCallback callback)
{
	getHeapTop = callback;
}

void initSetHeapTopCallback(SetHeapTopCallback callback)
{
	setHeapTop = callback;
}

void initGetDataTopCallback(GetDataTopCallback callback)
{
	getDataTop = callback;
}

void initSetDataTopCallback(SetDataTopCallback callback)
{
	setDataTop = callback;
}

void initGetProgramEntryCallback(GetProgramEntryCallback callback)
{
	getProgramEntry = callback;
}

void initSetProgramEntryCallback(SetProgramEntryCallback callback)
{
	setProgramEntry = callback;
}

void initGetProcessIdFromContextIdCallback(GetProcessIdFromContextIdCallback callback)
{
	getProcessIdFromContextId = callback;
}

#define EMUL_GPR(X)		(getGpr((ctx), (X)))
#define EMUL_SGPR(X)		((sword) getGpr((ctx), (X)))
#define EMUL_SET_GPR(X,V)	(setGpr((ctx), (X), (V)))
#define EMUL_REGHI		(getHi((ctx)))
#define EMUL_SET_REGHI(V)	(setHi((ctx), (V)))
#define EMUL_REGLO		(getLo((ctx)))
#define EMUL_SET_REGLO(V)	(setLo((ctx), (V)))
#define EMUL_FPRS(X)		(getFprs((ctx), (X)))
#define EMUL_SET_FPRS(X,V)	(setFprs((ctx), (X), (V)))
#define EMUL_FPRD(X)		(getFprd((ctx), (X)))
#define EMUL_SET_FPRD(X,V)	(setFprd((ctx), (X), (V)))
#define EMUL_PC		(getPc((ctx)))
#define EMUL_SET_PC(V) (setPc((ctx), (V)))
#define EMUL_NPC     (getNpc((ctx)))
#define EMUL_SET_NPC(V) (setNpc((ctx), (V)))
#define EMUL_NNPC    (getNnpc((ctx)))
#define EMUL_SET_NNPC(V)  (setNnpc((ctx), (V)))
#define EMUL_FPC_FIR		(getFir((ctx)))
#define EMUL_SET_FPC_FIR(V)	(setFir((ctx), (V)))
#define EMUL_FPC_FCSR	(getFcsr((ctx)))
#define EMUL_SET_FPC_FCSR(V)	(setFcsr((ctx), (V)))
#define EMUL_FPCC(C)		((C) ? GETBIT32(EMUL_FPC_FCSR, 24 + (C)) : GETBIT32(EMUL_FPC_FCSR, 23))
#define EMUL_SET_FPCC(C,V)	((C) ? EMUL_SET_FPC_FCSR(SETBITVALUE32(EMUL_FPC_FCSR, 24 + (C), (V))) : \
			EMUL_SET_FPC_FCSR(SETBITVALUE32(EMUL_FPC_FCSR, 23, (V))))
#define EMUL_BRANCH(V)	(setNnpc((ctx), (V)))
#define EMUL_RELBRANCH(V)	(setNnpc((ctx), EMUL_PC + (V) + 4))

#define EMUL_READ_BYTE(PROCESS_ID, ADDR, DATA) (DATA) = memReadByte((PROCESS_ID), (ADDR))
#define EMUL_READ_HALF(PROCESS_ID, ADDR, DATA) (DATA) = memReadHalfWord((PROCESS_ID), (ADDR))
#define EMUL_READ_WORD(PROCESS_ID, ADDR, DATA) (DATA) = memReadWord((PROCESS_ID), (ADDR))
#define EMUL_READ_DWORD(PROCESS_ID, ADDR, DATA) (DATA) = memReadDoubleWord((PROCESS_ID), (ADDR))
#define EMUL_READ_STRING(PROCESS_ID, ADDR, SIZE, STR) memReadString((PROCESS_ID), (ADDR), (SIZE), (STR))
#define EMUL_READ_BLK(PROCESS_ID, ADDR, SIZE, BLK) memReadBlock((PROCESS_ID), (ADDR), (SIZE), (BLK))

#define EMUL_WRITE_BYTE(PROCESS_ID, ADDR, DATA) memWriteByte((PROCESS_ID), (ADDR), (DATA))
#define EMUL_WRITE_HALF(PROCESS_ID, ADDR, DATA) memWriteHalfWord((PROCESS_ID), (ADDR), (DATA))
#define EMUL_WRITE_WORD(PROCESS_ID, ADDR, DATA) memWriteWord((PROCESS_ID), (ADDR), (DATA))
#define EMUL_WRITE_DWORD(PROCESS_ID, ADDR, DATA) memWriteDoubleWord((PROCESS_ID), (ADDR), (DATA))
#define EMUL_WRITE_STRING(PROCESS_ID, ADDR, STR) memWriteString((PROCESS_ID), (ADDR), (STR))
#define EMUL_WRITE_BLK(PROCESS_ID, ADDR, SIZE, BLK) memWriteBlock((PROCESS_ID), (ADDR), (SIZE), (BLK))

#define PROCESS_ID getProcessIdFromContextId((ctx))

#define STACK_BASE getStackBase((processId))
#define SET_STACK_BASE(V) setStackBase((processId), (V))

#define STACK_SIZE getStackBase((processId))
#define SET_STACK_SIZE(V) setStackSize((processId), (V))

#define TEXT_SIZE getTextSize((processId))
#define SET_TEXT_SIZE(V) setTextSize((processId), (V))

#define ENVIRON_BASE getEnvironBase((processId))
#define SET_ENVIRON_BASE(V) setEnvironBase((processId), (V))

#define HEAP_TOP getHeapTop((processId))
#define SET_HEAP_TOP(V) setHeapTop((processId), (V))

#define DATA_TOP getDataTop((processId))
#define SET_DATA_TOP(V) setDataTop((processId), (V))

#define PROGRAM_ENTRY getProgramEntry((processId))
#define SET_PROGRAM_ENTRY(V) setProgramEntry((processId), (V))

/* instruction fields */
struct instfld_t {
	word		inst;

	word		opcode;
	word		rs;
	word		rt;
	word		rd;
	word		shift;
	word		func;
	word		cond;
	word		cc;

	sword		imm;
	word		uimm;
	word		target;

	word		fmt;
	word		fmt3;
	word		fr;
	word		fs;
	word		ft;
	word		fd;
};

struct	instfld_t instfld;

void decode_inst(word inst)
{
	/* general use fields */
	instfld.inst		= inst;
	instfld.opcode		= BITS32(inst, 31, 26);
	instfld.rs		= BITS32(inst, 25, 21);
	instfld.rt		= BITS32(inst, 20, 16);
	instfld.rd		= BITS32(inst, 15, 11);
	instfld.shift		= BITS32(inst, 10, 6);
	instfld.func		= BITS32(inst, 5, 0);
	instfld.cond		= BITS32(inst, 3, 0);

	/* immediate values */
	instfld.uimm		= BITS32(inst, 15, 0);
	instfld.imm		= SEXT32(instfld.uimm, 16);
	instfld.target		= BITS32(inst, 25, 0);

	/* floating point fields */
	instfld.fmt		= BITS32(inst, 25, 21);
	instfld.fmt3		= BITS32(inst, 2, 0);
	instfld.ft		= BITS32(inst, 20, 16);
	instfld.fr		= BITS32(inst, 25, 21);
	instfld.fs		= BITS32(inst, 15, 11);
	instfld.fd		= BITS32(inst, 10, 6);
	instfld.cc		= BITS32(inst, 20, 18);
}

int result;

int getResult() {
    return result;
}

void nop_impl(int ctx, word inst)
{
}

void bc1f_impl(int ctx, word inst)
{
	if (!EMUL_FPCC(CC)) EMUL_RELBRANCH(IMM << 2);
}

void bc1t_impl(int ctx, word inst)
{
	if (EMUL_FPCC(CC)) EMUL_RELBRANCH(IMM << 2);
}

void mfc1_impl(int ctx, word inst)
{
	float temp = EMUL_FPRS(FS);
	word wrd = * (word *) &temp;
	EMUL_SET_GPR(RT, wrd);
}

void mtc1_impl(int ctx, word inst)
{
	word temp = EMUL_GPR(RT);
	float f = * (float *) &temp;
	EMUL_SET_FPRS(FS, f);
}

void cfc1_impl(int ctx, word inst)
{
	word temp = 0;
	switch (FS) {
		case 0:
			temp = EMUL_FPC_FIR;
			break;
		case 25:
			temp = (BITS32(EMUL_FPC_FCSR, 31, 25) << 1) |
			BITS32(EMUL_FPC_FCSR, 23, 23);
			break;
		case 26:
			temp = (BITS32(EMUL_FPC_FCSR, 17, 12) << 12) |
			(BITS32(EMUL_FPC_FCSR, 6, 2) << 2);
			break;
		case 28:
			temp = (BITS32(EMUL_FPC_FCSR, 11, 7) << 7) |
			(BITS32(EMUL_FPC_FCSR, 24, 24) << 2)|
			BITS32(EMUL_FPC_FCSR, 1, 0);
			break;
		case 31:
			temp = EMUL_FPC_FCSR;
			break;
		default: fatal("machine.def: cfc1: unknown value for fs");
	}
	EMUL_SET_GPR(RT, temp);
}

void ctc1_impl(int ctx, word inst)
{
	word temp = EMUL_GPR(RT);
	switch (FS) {
		case 25:
			EMUL_SET_FPC_FCSR((BITS32(temp, 7, 1) << 25) |
			(BITS32(EMUL_FPC_FCSR, 24, 24) << 24) |
			(BITS32(temp, 0, 0) << 23) |
			BITS32(EMUL_FPC_FCSR, 22, 0));
			break;
		case 26:
			EMUL_SET_FPC_FCSR((BITS32(EMUL_FPC_FCSR, 31, 18) << 18) |
			(BITS32(temp, 17, 12) << 12) |
			(BITS32(EMUL_FPC_FCSR, 11, 7) << 7) |
			(BITS32(temp, 6, 2) << 2) |
			BITS32(EMUL_FPC_FCSR, 1, 0));
			break;
		case 28:
			EMUL_SET_FPC_FCSR((BITS32(EMUL_FPC_FCSR, 31, 25) << 25) |
			(BITS32(temp, 2, 2) << 24) |
			(BITS32(EMUL_FPC_FCSR, 23, 12) << 12) |
			(BITS32(temp, 11, 7) << 7) |
			(BITS32(EMUL_FPC_FCSR, 6, 2) << 2) |
			BITS32(temp, 1, 0));
			break;
		case 31:
			EMUL_SET_FPC_FCSR(temp);
			break;
		default: fatal("machine.def: ctc1: unknown value for fs");
	}
}

void abs_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		float temp = EMUL_FPRS(FS) < 0.0 ? -EMUL_FPRS(FS) : EMUL_FPRS(FS);
		EMUL_SET_FPRS(FD, temp);
	} else if (FMT == MD_FMT_DOUBLE) {
		double temp = EMUL_FPRD(FS) < 0.0 ? -EMUL_FPRD(FS) : EMUL_FPRD(FS);
		EMUL_SET_FPRD(FD, temp);
	} else result = MD_MSG_FMTERROR;
}

void add_impl(int ctx, word inst)
{
	sword temp = EMUL_SGPR(RS) + EMUL_SGPR(RT);
	EMUL_SET_GPR(RD, temp);
}

void _add_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) EMUL_SET_FPRS(FD, EMUL_FPRS(FS) + EMUL_FPRS(FT));
	else if (FMT == MD_FMT_DOUBLE) EMUL_SET_FPRD(FD, EMUL_FPRD(FS) + EMUL_FPRD(FT));
	else result = MD_MSG_FMTERROR;
}

void addi_impl(int ctx, word inst)
{
	sword temp = EMUL_SGPR(RS) + IMM;
	EMUL_SET_GPR(RT, temp);
}

void addiu_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RT, EMUL_SGPR(RS) + IMM);
}

void addu_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RS) + EMUL_GPR(RT));
}

void and_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RS) & EMUL_GPR(RT));
}

void andi_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RT, EMUL_GPR(RS) & UIMM);
}

void b_impl(int ctx, word inst)
{
	EMUL_RELBRANCH(IMM << 2);
}

void bal_impl(int ctx, word inst)
{
    printf("EMUL_PC: 0x%08x", EMUL_PC);
	EMUL_SET_GPR(31, EMUL_PC + 8);
    printf("EMUL_GPR(31): 0x%08x", EMUL_GPR(31));
	EMUL_RELBRANCH(IMM << 2);
}

void beq_impl(int ctx, word inst)
{
	if (EMUL_GPR(RS) == EMUL_GPR(RT)) EMUL_RELBRANCH(IMM << 2);
}

void bgez_impl(int ctx, word inst)
{
	if (EMUL_SGPR(RS) >= 0) EMUL_RELBRANCH(IMM << 2);
}

void bgezal_impl(int ctx, word inst)
{
	EMUL_SET_GPR(31, EMUL_PC + 8);
	if (EMUL_SGPR(RS) >= 0) EMUL_RELBRANCH(IMM << 2);
}

void bgtz_impl(int ctx, word inst)
{
	if (EMUL_SGPR(RS) > 0) EMUL_RELBRANCH(IMM << 2);
}

void blez_impl(int ctx, word inst)
{
	if (EMUL_SGPR(RS) <= 0) EMUL_RELBRANCH(IMM << 2);
}

void bltz_impl(int ctx, word inst)
{
	if (EMUL_SGPR(RS) < 0) EMUL_RELBRANCH(IMM << 2);
}

void bne_impl(int ctx, word inst)
{
	if (EMUL_GPR(RS) != EMUL_GPR(RT)) EMUL_RELBRANCH(IMM << 2);
}

void break_impl(int ctx, word inst)
{
	result = MD_MSG_FINISH;
}

void c_impl(int ctx, word inst)
{
	int less, equal, unordered, condition, cc;
	if (FMT == MD_FMT_SINGLE) {
		float op1 = EMUL_FPRS(FS);
		float op2 = EMUL_FPRS(FT);
		less = op1 < op2;
		equal = op1 == op2;
		unordered = FALSE;
		cc = BITS32(INST, 10, 8);
	} else if (FMT == MD_FMT_DOUBLE) {
		double op1 = EMUL_FPRD(FS);
		double op2 = EMUL_FPRD(FT);
		less = op1 < op2;
		equal = op1 == op2;
		unordered = FALSE;
		cc = BITS32(INST, 10, 8);
	} else result = MD_MSG_FMTERROR;
	if (result != MD_MSG_FMTERROR) {
		condition = ((GETBIT32(COND, 2) && less) ||
			(GETBIT32(COND, 1) && equal) ||
			(GETBIT32(COND, 0) && unordered));
		EMUL_SET_FPCC(cc, condition);
	}
}

void cvt_d_impl(int ctx, word inst)
{	
	if (FMT == MD_FMT_SINGLE) {
		double temp = (double) EMUL_FPRS(FS);
		EMUL_SET_FPRD(FD, temp);
	} else if (FMT == MD_FMT_WORD) {
		float temp = EMUL_FPRS(FS);
		sword wrd = * (sword *) &temp;
		EMUL_SET_FPRD(FD, (double) wrd);
	} else if (FMT == MD_FMT_LONG) {
		double temp = EMUL_FPRD(FS);
		sdword dwrd = * (sdword *) &temp;
		EMUL_SET_FPRD(FD, (double) dwrd);
	} else result = MD_MSG_FMTERROR;
}

void cvt_s_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_DOUBLE) {
		float temp = (float) EMUL_FPRD(FS);
		EMUL_SET_FPRS(FD, temp);
	} else if (FMT == MD_FMT_WORD) {
		float temp = EMUL_FPRS(FS);
		sword wrd = * (sword *) &temp;
		EMUL_SET_FPRS(FD, (float) wrd);
	} else if (FMT == MD_FMT_LONG) {
		double temp = EMUL_FPRD(FS);
		sdword dwrd = * (sdword *) &temp;
		EMUL_SET_FPRS(FD, (float) dwrd);
	} else result = MD_MSG_FMTERROR;
}

void cvt_w_impl(int ctx, word inst)
{
	sword wrd;
	float temp;
	if (FMT == MD_FMT_SINGLE) {
		wrd = (sword) EMUL_FPRS(FS);
		temp = * (float *) &wrd;
		EMUL_SET_FPRS(FD, temp);
	} else if (FMT == MD_FMT_DOUBLE) {
		wrd = (sword) EMUL_FPRD(FS);
		temp = * (float *) &wrd;
		EMUL_SET_FPRS(FD, temp);
	} else result = MD_MSG_FMTERROR;
}

void div_impl(int ctx, word inst)
{
	if (EMUL_GPR(RT)) {
		EMUL_SET_REGLO(EMUL_SGPR(RS) / EMUL_SGPR(RT));
		EMUL_SET_REGHI(EMUL_SGPR(RS) % EMUL_SGPR(RT));
	}
}

void _div_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) EMUL_SET_FPRS(FD, EMUL_FPRS(FS) / EMUL_FPRS(FT));
	else if (FMT == MD_FMT_DOUBLE) EMUL_SET_FPRD(FD, EMUL_FPRD(FS) / EMUL_FPRD(FT));
	else result = MD_MSG_FMTERROR;
}

void divu_impl(int ctx, word inst)
{
	if (EMUL_GPR(RT)) {
		EMUL_SET_REGLO(EMUL_GPR(RS) / EMUL_GPR(RT));
		EMUL_SET_REGHI(EMUL_GPR(RS) % EMUL_GPR(RT));
	}
}

void j_impl(int ctx, word inst)
{
	word dest = (BITS32(EMUL_PC + 4, 32, 28) << 28) | (TARGET << 2);
	EMUL_BRANCH(dest);
}

void jal_impl(int ctx, word inst)
{
	word dest = (BITS32(EMUL_PC + 4, 32, 28) << 28) | (TARGET << 2);
	EMUL_SET_GPR(31, EMUL_PC + 8);
	EMUL_BRANCH(dest);
}

void jalr_impl(int ctx, word inst)
{
	EMUL_BRANCH(EMUL_GPR(RS));
	EMUL_SET_GPR(RD, EMUL_PC + 8);
}

void jr_impl(int ctx, word inst)
{
	EMUL_BRANCH(EMUL_GPR(RS));
}

void lb_impl(int ctx, word inst)
{
	byte temp;
	word addr = EMUL_GPR(RS) + IMM;
	EMUL_READ_BYTE(PROCESS_ID, addr, temp);
	EMUL_SET_GPR(RT, SEXT32(temp, 8));
}

void lbu_impl(int ctx, word inst)
{
	byte temp;
	word addr = EMUL_GPR(RS) + IMM;
	EMUL_READ_BYTE(PROCESS_ID, addr, temp);
	EMUL_SET_GPR(RT, temp);
}

void ldc1_impl(int ctx, word inst)
{
	dword temp;
	EMUL_READ_DWORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
	EMUL_SET_FPRD(FT, * (double *) &temp);
}

void lh_impl(int ctx, word inst)
{
	half temp;
	word addr = EMUL_GPR(RS) + IMM;
	EMUL_READ_HALF(PROCESS_ID, addr, temp);
	EMUL_SET_GPR(RT, SEXT32(temp, 16));
}

void lhu_impl(int ctx, word inst)
{
	half temp;
	word addr = EMUL_GPR(RS) + IMM;
	EMUL_READ_HALF(PROCESS_ID, addr, temp);
	EMUL_SET_GPR(RT, temp);
}

void ll_impl(int ctx, word inst)
{
	word temp;
	SETRMW;
	EMUL_READ_WORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
	EMUL_SET_GPR(RT, temp);
	result = MD_MSG_STARTRMW;
}

void lui_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RT, UIMM << 16);
}

void lw_impl(int ctx, word inst)
{
	word temp;
	EMUL_READ_WORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
	EMUL_SET_GPR(RT, temp);
}

void lwc1_impl(int ctx, word inst)
{
	word temp;
	float f;
	EMUL_READ_WORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
	f = * (float *) &temp;
	EMUL_SET_FPRS(FT, f);
}

void lwl_impl(int ctx, word inst)
{
	byte	src[4];
	word rt = EMUL_GPR(RT);
	byte	*dst = (byte *) &rt;
	word	addr = EMUL_GPR(RS) + IMM;
	int	i, size = 4 - (addr & 3);
	EMUL_READ_BLK(PROCESS_ID, addr, size, src);
	for (i = 0; i < size; i++)
		dst[3 - i] = src[i];
	EMUL_SET_GPR(RT, rt);
}

void lwr_impl(int ctx, word inst)
{
	byte	src[4];
	word rt = EMUL_GPR(RT);
	byte	*dst = (byte *) &rt;
	word	addr = EMUL_GPR(RS) + IMM;
	int	i, size = 1 + (addr & 3);
	EMUL_READ_BLK(PROCESS_ID, addr - size + 1, size, src);
	for (i = 0; i < size; i++)
		dst[size - i - 1] = src[i];
	EMUL_SET_GPR(RT, rt);
}

void madd_impl(int ctx, word inst)
{
	sdword temp, temp1, temp2, temp3;
	temp1 = EMUL_SGPR(RS);
	temp2 = EMUL_SGPR(RT);
	temp3 = ((dword) EMUL_REGHI << 32) | ((dword) EMUL_REGLO);
	temp = temp1 * temp2 + temp3;
	EMUL_SET_REGHI(BITS64(temp, 63, 32));
	EMUL_SET_REGLO(BITS64(temp, 31, 0));
}

void mfhi_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_REGHI);
}

void mflo_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_REGLO);
}

void mov_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) EMUL_SET_FPRS(FD, EMUL_FPRS(FS));
	else if (FMT == MD_FMT_DOUBLE) EMUL_SET_FPRD(FD, EMUL_FPRD(FS));
	else result = MD_MSG_FMTERROR;
}

void movf_impl(int ctx, word inst)
{
	if (!EMUL_FPCC(CC)) EMUL_SET_GPR(RD, EMUL_GPR(RS));
}

void _movf_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		if (!EMUL_FPCC(CC)) EMUL_SET_FPRS(FD, EMUL_FPRS(FS));
	} else if (FMT == MD_FMT_DOUBLE) {
		if (!EMUL_FPCC(CC)) EMUL_SET_FPRD(FD, EMUL_FPRD(FS));
	} else result = MD_MSG_FMTERROR;
}

void movn_impl(int ctx, word inst)
{
	if (EMUL_GPR(RT)) EMUL_SET_GPR(RD, EMUL_GPR(RS));
}

void _movn_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		if (EMUL_GPR(RT)) EMUL_SET_FPRS(FD, EMUL_FPRS(FS));
	} else if (FMT == MD_FMT_DOUBLE) {
		if (EMUL_GPR(RT)) EMUL_SET_FPRD(FD, EMUL_FPRD(FS));
	} else result = MD_MSG_FMTERROR;
}

void _movt_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		if (EMUL_FPCC(CC)) EMUL_SET_FPRS(FD, EMUL_FPRS(FS));
	} else if (FMT == MD_FMT_DOUBLE) {
		if (EMUL_FPCC(CC)) EMUL_SET_FPRD(FD, EMUL_FPRD(FS));
	} else result = MD_MSG_FMTERROR;
}

void movz_impl(int ctx, word inst)
{
	if (!EMUL_GPR(RT)) EMUL_SET_GPR(RD, EMUL_GPR(RS));
}

void _movz_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		if (!EMUL_GPR(RT)) EMUL_SET_FPRS(FD, EMUL_FPRS(FS));
	} else if (FMT == MD_FMT_DOUBLE) {
		if (!EMUL_GPR(RT)) EMUL_SET_FPRD(FD, EMUL_FPRD(FS));
	} else result = MD_MSG_FMTERROR;
}

void msub_impl(int ctx, word inst)
{
	sdword temp, temp1, temp2, temp3;
	temp1 = EMUL_SGPR(RS);
	temp2 = EMUL_SGPR(RT);
	temp3 = ((dword) EMUL_REGHI << 32) | ((dword) EMUL_REGLO);
	temp = temp3 - temp1 * temp2 + temp3;
	EMUL_SET_REGHI(BITS64(temp, 63, 32));
	EMUL_SET_REGLO(BITS64(temp, 31, 0));
}

void mtlo_impl(int ctx, word inst)
{
	EMUL_SET_REGLO(EMUL_GPR(RS));
}

void mul_impl(int ctx, word inst)
{
	sdword temp = (sdword) EMUL_SGPR(RS) * (sdword) EMUL_SGPR(RT);
	EMUL_SET_GPR(RD, temp);
}

void _mul_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) EMUL_SET_FPRS(FD, EMUL_FPRS(FS) * EMUL_FPRS(FT));
	else if (FMT == MD_FMT_DOUBLE) EMUL_SET_FPRD(FD, EMUL_FPRD(FS) * EMUL_FPRD(FT));
	else result = MD_MSG_FMTERROR;
}

void mult_impl(int ctx, word inst)
{
	dword temp = (sdword) EMUL_SGPR(RS) * (sdword) EMUL_SGPR(RT);
	EMUL_SET_REGLO(BITS64(temp, 31, 0));
	EMUL_SET_REGHI(BITS64(temp, 63, 32));
}

void multu_impl(int ctx, word inst)
{
	dword temp = (dword) EMUL_GPR(RS) * (dword) EMUL_GPR(RT);
	EMUL_SET_REGLO(BITS64(temp, 31, 0));
	EMUL_SET_REGHI(BITS64(temp, 63, 32));
}

void neg_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) EMUL_SET_FPRS(FD, -EMUL_FPRS(FS));
	else if (FMT == MD_FMT_DOUBLE) EMUL_SET_FPRD(FD, -EMUL_FPRD(FS));
	else result = MD_MSG_FMTERROR;
}

void nor_impl(int ctx, word inst)
{
	word temp = EMUL_GPR(RS) | EMUL_GPR(RT);
	EMUL_SET_GPR(RD, ~temp);
}

void or_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RS) | EMUL_GPR(RT));
}

void ori_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RT, EMUL_GPR(RS) | UIMM);
}

void sb_impl(int ctx, word inst)
{
	byte temp = EMUL_GPR(RT);
	word addr = EMUL_GPR(RS) + IMM;
	EMUL_WRITE_BYTE(PROCESS_ID, addr, temp);
}

void sc_impl(int ctx, word inst)
{
	word temp = EMUL_GPR(RT);
	if (GETRMW == CURRENT) {
		EMUL_WRITE_WORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
		EMUL_SET_GPR(RT, 1);
	} else {
		EMUL_SET_GPR(RT, 0);
	}
	result = MD_MSG_ENDRMW;
}

void sdc1_impl(int ctx, word inst)
{
	double dbl = EMUL_FPRD(FT);
	dword temp = * (dword *) &dbl;
	EMUL_WRITE_DWORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
}

void sh_impl(int ctx, word inst)
{
	half temp = EMUL_GPR(RT);
	word addr = EMUL_GPR(RS) + IMM;
	EMUL_WRITE_HALF(PROCESS_ID, addr, temp);
}

void sll_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RT) << SHIFT);
}

void sllv_impl(int ctx, word inst)
{
	word s;
	s = BITS32(EMUL_GPR(RS), 4, 0);
	EMUL_SET_GPR(RD, EMUL_GPR(RT) << s);
}

void slt_impl(int ctx, word inst)
{
	if (EMUL_SGPR(RS) < EMUL_SGPR(RT)) EMUL_SET_GPR(RD, 1);
	else EMUL_SET_GPR(RD, 0);
}

void slti_impl(int ctx, word inst)
{
	if (EMUL_SGPR(RS) < IMM) EMUL_SET_GPR(RT, 1);
	else EMUL_SET_GPR(RT, 0);
}

void sltiu_impl(int ctx, word inst)
{
	if (EMUL_GPR(RS) < (word) IMM) EMUL_SET_GPR(RT, 1);
	else EMUL_SET_GPR(RT, 0);
}

void sltu_impl(int ctx, word inst)
{
	if (EMUL_GPR(RS) < EMUL_GPR(RT)) EMUL_SET_GPR(RD, 1);
	else EMUL_SET_GPR(RD, 0);
}

void sqrt_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		float temp = sqrt(EMUL_FPRS(FS));
		EMUL_SET_FPRS(FD, temp);
	} else if (FMT == MD_FMT_DOUBLE) {
		double temp = sqrt(EMUL_FPRD(FS));
		EMUL_SET_FPRD(FD, temp);
	} else result = MD_MSG_FMTERROR;
}

void sra_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_SGPR(RT) >> SHIFT);
}

void srav_impl(int ctx, word inst)
{
	sword s = BITS32(EMUL_GPR(RS), 4, 0);
	EMUL_SET_GPR(RD, EMUL_SGPR(RT) >> s);
}

void srl_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RT) >> SHIFT);
}

void srlv_impl(int ctx, word inst)
{
	word s = BITS32(EMUL_GPR(RS), 4, 0);
	EMUL_SET_GPR(RD, EMUL_GPR(RT) >> s);
}

void _sub_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) EMUL_SET_FPRS(FD, EMUL_FPRS(FS) - EMUL_FPRS(FT));
	else if (FMT == MD_FMT_DOUBLE) EMUL_SET_FPRD(FD, EMUL_FPRD(FS) - EMUL_FPRD(FT));
	else result = MD_MSG_FMTERROR;
}

void subu_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RS) - EMUL_GPR(RT));
}

void sw_impl(int ctx, word inst)
{
	word temp = EMUL_GPR(RT);
	EMUL_WRITE_WORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
}

void swc1_impl(int ctx, word inst)
{
	float f = EMUL_FPRS(FT);
	word temp = * (word *) &f;
	EMUL_WRITE_WORD(PROCESS_ID, EMUL_GPR(RS) + IMM, temp);
}

void swl_impl(int ctx, word inst)
{
    word rt = EMUL_GPR(RT);
	byte *src = (byte *) &rt;
	byte dst[4];
	word addr = EMUL_GPR(RS) + IMM;
	int size = 4 - (addr & 3);
	int i;
	for (i = 0; i < size; i++)
		dst[i] = src[3 - i];
	EMUL_WRITE_BLK(PROCESS_ID, addr, size, dst);
}

void swr_impl(int ctx, word inst)
{
    word rt = EMUL_GPR(RT);
	byte *src = (byte *) &rt;
	byte dst[4];
	word addr = EMUL_GPR(RS) + IMM;
	int size = 1 + (addr & 3);
	int i;
	for (i = 0; i < size; i++)
		dst[i] = src[size - i - 1];
	EMUL_WRITE_BLK(PROCESS_ID, addr - size + 1, size, dst);
}

void syscall_impl(int ctx, word inst)
{
	result = MD_MSG_SYSCALL;
}

void trunc_w_impl(int ctx, word inst)
{
	if (FMT == MD_FMT_SINGLE) {
		float temp = EMUL_FPRS(FS);
		sword itmp = (sword) temp;
		EMUL_SET_FPRS(FD, * (float *) &itmp);
	} else if (FMT == MD_FMT_DOUBLE) {
		double temp = EMUL_FPRD(FS);
		sword itmp = (sword) temp;
		EMUL_SET_FPRS(FD, * (float *) &itmp);
	} else result = MD_MSG_FMTERROR;
}

void xor_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RD, EMUL_GPR(RS) ^ EMUL_GPR(RT));
}

void xori_impl(int ctx, word inst)
{
	EMUL_SET_GPR(RT, EMUL_GPR(RS) ^ UIMM);
}

#define MAX_ARGC	50

#define	MD_TEXT_BASE		0x00400000
#define MD_DATA_BASE		0x10000000
#define	MD_STACK_BASE		0x7fffc000
#define MD_MAX_ENVIRON		16384

#define SIM_O_RDONLY		0
#define SIM_O_WRONLY		1
#define SIM_O_RDWR		2
#define SIM_O_CREAT		0x100
#define SIM_O_EXCL		0x400
#define SIM_O_NOCTTY		0x800
#define SIM_O_TRUNC		0x200
#define SIM_O_APPEND		8
#define SIM_O_NONBLOCK		0x80
#define SIM_O_SYNC		0x10

#define MEM_PAGESIZE	(1<<12)
#define MEM_LOGPAGESIZE	(12)

#define NUM_IREGS	32
#define NUM_FREGS	32
#define NUM_CREGS	2
#define TOTAL_REGS	(32 + 32 + 2 + 1 + 1 + 1)

#define REGS_ZERO	0
#define REGS_AT		1
#define REGS_V0		2
#define REGS_V1		3
#define REGS_A0		4
#define REGS_A1		5
#define REGS_A2		6
#define REGS_A3		7
#define REGS_T0		8
#define REGS_T1		9
#define REGS_T2		10
#define REGS_T3		11
#define REGS_T4		12
#define REGS_T5		13
#define REGS_T6		14
#define REGS_T7		15
#define REGS_S0		16
#define REGS_S1		17
#define REGS_S2		18
#define REGS_S3		19
#define REGS_S4		20
#define REGS_S5		21
#define REGS_S6		22
#define REGS_S7		23
#define REGS_T8		24
#define REGS_T9		25
#define REGS_K0		26
#define REGS_K1		27
#define REGS_GP		28
#define REGS_SP		29
#define REGS_FP		30
#define REGS_RA		31

/* register file */
#define GPR_COUNT	32
#define FPR_COUNT	32

extern char **environ;

struct loader_t {
	int	argc;
	char	*argv[MAX_ARGC];
	char	exe[MAX_STRING_SIZE];
	char	cwd[MAX_STRING_SIZE];
	char	stdin_file[MAX_STRING_SIZE];
	char	stdout_file[MAX_STRING_SIZE];
	
	int	stdin_fd, stdout_fd;
	word	stack_base, stack_size, text_size;
	word	environ_base, heap_top, data_top;
	word	prog_entry;
	
	bfd	*abfd;
};

void load_prog(int processId, int argc, char **argv)
{	
	bfd *abfd;
	asection *sect;
	word sp;
	long storage;
	int i;
	word argv_addr, envp_addr;
	
	/* context */
	SET_STACK_BASE(MD_STACK_BASE);
	sp = (MD_STACK_BASE - MD_MAX_ENVIRON) & ~7;
	SET_STACK_SIZE(STACK_BASE - sp);

	/* initial stack ptr */
	SET_ENVIRON_BASE(sp);

	/* load program into memory */
	abfd = bfd_openr(argv[0], "default");
	if (!abfd)
		fatal("%s: cannot open file", argv[0]);
	if (!bfd_check_format(abfd, bfd_object)) {
		bfd_close(abfd);
		fatal("%s: not a valid elf file", argv[0]);
	}

	/* read sections */
	for (sect = abfd->sections; sect; sect = sect->next)
	{		
		/* check if the section is dynamic; in this case, fatal: we do not
		 * support dynamic linking */
		if (!strcmp(bfd_section_name(abfd, sect), ".dynamic"))
			fatal("dynamic linking not supported; compile with '-static' option");
		
		if (bfd_get_section_flags(abfd, sect) & (SEC_ALLOC | SEC_RELOC))
		{
			byte *p;
			
			p = calloc(bfd_section_size(abfd, sect), sizeof(byte));
			if (!bfd_get_section_contents(abfd, sect, p, (file_ptr) 0,
				bfd_section_size(abfd, sect)))
				fatal("cannot read section '%s'",
					bfd_section_name(abfd, sect));

			/* copy program to memory */
			EMUL_WRITE_BLK(processId, bfd_section_vma(abfd, sect),
				bfd_section_size(abfd, sect), p);
			free(p);
			
			/* if data segment, increase data segment size */
			if (bfd_section_vma(abfd, sect) >= MD_DATA_BASE) {
				SET_DATA_TOP(MAX(DATA_TOP, bfd_section_vma(abfd, sect) + bfd_section_size(abfd, sect) - 1));
			}
		}
		else if (bfd_get_section_flags(abfd, sect) & SEC_LOAD)
		{
			byte *p = calloc(bfd_section_size(abfd, sect), sizeof(byte));
			EMUL_WRITE_BLK(processId, bfd_section_vma(abfd, sect),
				bfd_section_size(abfd, sect), p);
			free(p);
		}
		
		/* code section */
		if (!strcmp(bfd_section_name(abfd, sect), ".text"))
			SET_TEXT_SIZE(bfd_section_vma(abfd, sect) + bfd_section_size(abfd, sect) - MD_TEXT_BASE);
	}
	
	/* calculate data segment */
	SET_PROGRAM_ENTRY(bfd_get_start_address(abfd));
	SET_HEAP_TOP(ROUND_UP(DATA_TOP, MEM_PAGESIZE));
	
	/* local stack ptr */
	SET_STACK_BASE(MD_STACK_BASE);
	SET_STACK_SIZE(MD_MAX_ENVIRON);
	SET_ENVIRON_BASE(MD_STACK_BASE - MD_MAX_ENVIRON);

	/* load arguments and environment vars */
	sp = ENVIRON_BASE;
	EMUL_WRITE_WORD(processId, sp, argc);
	sp += 4;
	argv_addr = sp;
	sp = sp + (argc + 1) * 4;

	/* save space for environ and null */
	envp_addr = sp;
//	for (i = 0; environ[i]; i++)
//		sp += 4;
	sp += 4;

	/* argv ptr */
	for (i = 0; i < argc; i++) {
		EMUL_WRITE_WORD(processId, argv_addr + i * 4, sp);
		EMUL_WRITE_STRING(processId, sp, argv[i]);
		sp += strlen(argv[i]) + 1;
	}
	EMUL_WRITE_WORD(processId, argv_addr + i * 4, 0);

	/* envp ptr and stack data */
	//~ for (i = 0; environ[i]; i++) {
		//~ EMUL_WRITE_WORD(processId, envp_addr + i * 4, sp);
		//~ EMUL_WRITE_STRING(processId, sp, environ[i]);
		//~ sp += strlen(environ[i]) + 1;
	//~ }
	i = 0;
	EMUL_WRITE_WORD(processId, envp_addr + i * 4, 0);
	if (sp > STACK_BASE)
		fatal("'environ' overflow, increment MD_MAX_ENVIRON");
}

int emu_check_syscall_error(int ctx)
{
	int error;
	if (EMUL_SGPR(REGS_V0) != -1) {
		EMUL_SET_GPR(REGS_A3, 0);
		error = FALSE;
	} else {
		EMUL_SET_GPR(REGS_V0, errno);
		EMUL_SET_GPR(REGS_A3, 1);
		error = TRUE;
	}
	return error;
}

void syscall_fstat64_impl(int ctx, int fd)
{
	int	error = 0;

    struct sim_stat {
        word	sim_st_dev;
        word	pad0[3];
        dword	sim_st_ino;
        word	sim_st_mode;
        word	sim_st_nlink;
        word	sim_st_uid;
        word	sim_st_gid;
        word	sim_st_rdev;
        word	pad1[3];
        dword	sim_st_size;
        word	sim_st_atime;
        word	pad2;
        word	sim_st_mtime;
        word	pad3;
        word	sim_st_ctime;
        word	pad4;
        word	sim_st_blksize;
        word	pad5;
        dword	sim_st_blocks;
    };

    struct stat buf;
    struct sim_stat sim_buf;

    EMUL_SET_GPR(2, fstat(fd, &buf));
    error = emu_check_syscall_error(ctx);

    if (!error) {
        /*if (fd == 1) {
            buf.st_dev = 0xb;
            buf.st_ino = 0x2;
            buf.st_mode = 0x2190;
            buf.st_rdev = 0x8800;
            buf.st_blksize = 0x2190;
            fprintf(stderr, "*****************\n");
            fprintf(stderr, "st_dev=0x%x\n", (word) buf.st_dev);
            fprintf(stderr, "st_ino=0x%llx\n", (long long) buf.st_ino);
            fprintf(stderr, "st_mode=0x%x\n", buf.st_mode);
            fprintf(stderr, "st_rdev=0x%x\n", (word) buf.st_rdev);
            fprintf(stderr, "st_blksize=0x%x\n", buf.st_mode);
            fflush(stderr);
        }*/

        sim_buf.sim_st_dev	= SWAPW(buf.st_dev);
        sim_buf.sim_st_ino	= SWAPDW(buf.st_ino);
        sim_buf.sim_st_mode	= SWAPW(buf.st_mode);
        sim_buf.sim_st_nlink	= SWAPW(buf.st_nlink);
        sim_buf.sim_st_uid	= SWAPW(buf.st_uid);
        sim_buf.sim_st_gid	= SWAPW(buf.st_gid);
        sim_buf.sim_st_rdev	= SWAPW(buf.st_rdev);
        sim_buf.sim_st_size	= SWAPDW(buf.st_size);
        sim_buf.sim_st_atime	= SWAPW(buf.st_atime);
        sim_buf.sim_st_mtime	= SWAPW(buf.st_mtime);
        sim_buf.sim_st_ctime	= SWAPW(buf.st_ctime);
        sim_buf.sim_st_blksize	= SWAPW(buf.st_blksize);
        sim_buf.sim_st_blocks	= SWAPDW(buf.st_blocks);

        EMUL_WRITE_BLK(PROCESS_ID, EMUL_GPR(5), sizeof(sim_buf), (byte *) &sim_buf);
    }
}
