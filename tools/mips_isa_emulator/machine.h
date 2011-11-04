#ifndef MACHINE_H
#define MACHINE_H

#include "misc.h"
#include "memory.h"
#include "regs.h"

#define	MD_TEXT_BASE		0x00400000
#define MD_DATA_BASE		0x10000000
#define	MD_STACK_BASE		0x7fffc000
#define MD_MAX_ENVIRON		16384


/* instruction flags */
#define F_ICOMP		0x00000001	/* integer computation */
#define F_FCOMP		0x00000002	/* floating-point computation */
#define F_CTRL		0x00000004	/* control instruction */
#define F_UNCOND	0x00000008	/*   unconditional */
#define F_COND		0x00000010	/*   conditional */
#define F_MEM		0x00000020	/* memory access */
#define F_LOAD		0x00000040	/*   load instr */
#define F_STORE		0x00000080	/*   store instr */
#define F_DISP		0x00000100	/*   mode direct+offs */
#define F_RR		0x00000200	/*   mode reg+reg */
#define F_DIRECT	0x00000400	/*   direct mode */
#define F_TRAP		0x00000800	/* instruction produces trap */
#define F_LONGLAT	0x00001000	/* high latency instr */
#define F_CALL		0x00002000	/* function call */
#define F_RET		0x00004000	/* function return */
#define F_FPCOND	0x00008000	/* floating point conditional branch */
#define F_IMM		0x00010000	/* instruction has immediate operand */


/* possible functional units */
enum fu_class {
	fu_NONE = 0,
	IntALU,
	IntMULT,
	IntDIV,
	FloatADD,
	FloatCMP,	/* fp comparer */
	FloatCVT,	/* fp-int conversion */
	FloatMULT,
	FloatDIV,
	FloatSQRT,
	RdPort,
	WrPort,
	NUM_FU_CLASSES
};


/* an identifier for each instructions */
enum md_opcode {
	OP_ERR = 0,
#define DEFINST(NAME,BITS,MASK,FORMAT,FUCLASS,FLAGS,O1,O2,I1,I2,I3) OP_##NAME,
#include "machine.def"
#undef DEFINST
	OP_MAX
};


/* instruction fields */
struct md_instfld_t {
	enum		md_opcode op;
	word		inst;
	
	char		*name;
	char		*format;
	int		fu_class;
	int		flags;
	int		in[3];
	int		out[2];

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


/* functions */
void md_print_inst(FILE *fd, word inst, word text_offset);
void md_dump_inst(FILE *fd, struct md_instfld_t *instfld);
void md_decode_inst(word inst, struct md_instfld_t *instfld);
int md_execute_inst(struct mem_t *mem, struct regs_t *regs,
	struct md_instfld_t *instfld, int ctx);


#endif

