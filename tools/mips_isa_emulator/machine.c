#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <math.h>

#include "mhandle.h"
#include "private.h"
#include "machine.h"


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

#define OPCODE		(instfld->opcode)
#define RS		(instfld->rs)
#define RT		(instfld->rt)
#define RD		(instfld->rd)
#define SHIFT		(instfld->shift)
#define FUNC		(instfld->func)
#define IMM		(instfld->imm)
#define UIMM		(instfld->uimm)
#define TARGET		(instfld->target)
#define FR		(instfld->fr)
#define FS		(instfld->fs)
#define FT		(instfld->ft)
#define FD		(instfld->fd)
#define FMT		(instfld->fmt)
#define FMT3		(instfld->fmt3)
#define COND		(instfld->cond)
#define INST		(instfld->inst)
#define CC		(instfld->cc)

static int rmw_lock = 0;
#define SETRMW		(rmw_lock = ctx)
#define GETRMW		(rmw_lock)
#define CURRENT		(ctx) 

 
void md_print_inst(FILE *fd, word inst, word text_offset)
{
	struct	md_instfld_t fld;
	char	fullname[128];
	word	cc;
	int	i;
	int	add_fmt = FALSE, add_fmt3 = FALSE;
	
	/* first decode instruction */
	md_decode_inst(inst, &fld);
	
	/* name */
	while (*fld.name == '_')
		fld.name++;
	strcpy(fullname, fld.name);
	for (i = 0; i < strlen(fullname); i++)
		fullname[i] = fullname[i] == '_' ? '.' :
			tolower(fullname[i]);
	
	/* add '.cond' for COP1 condition instr */
	cc = fld.cc;
	if (fld.op == OP_C) {
		cc = BITS32(fld.inst, 10, 8);
		switch (fld.cond) {
			case 0: strcat(fullname, ".f"); break;
			case 1: strcat(fullname, ".un"); break;
			case 2: strcat(fullname, ".eq"); break;
			case 3: strcat(fullname, ".ueq"); break;
			case 4: strcat(fullname, ".olt"); break;
			case 5: strcat(fullname, ".ult"); break;
			case 6: strcat(fullname, ".ole"); break;
			case 7: strcat(fullname, ".ule"); break;
			case 8: strcat(fullname, ".sf"); break;
			case 9: strcat(fullname, ".ngle"); break;
			case 10: strcat(fullname, ".seq"); break;
			case 11: strcat(fullname, ".ngl"); break;
			case 12: strcat(fullname, ".lt"); break;
			case 13: strcat(fullname, ".nge"); break;
			case 14: strcat(fullname, ".le"); break;
			case 15: strcat(fullname, ".ngt"); break;
			default: strcat(fullname, ".err");
		}
	}
		
	/* add fmt or fmt3 */
	add_fmt = fld.opcode == MD_OPCODE_COP1 &&
			fld.op != OP_MFC1 && fld.op != OP_MTC1 &&
			fld.op != OP_CFC1 && fld.op != OP_CTC1 &&
			fld.op != OP_BC1F && fld.op != OP_BC1T;
	if (add_fmt) {
		switch (fld.fmt) {
			case MD_FMT_SINGLE: strcat(fullname, ".s"); break;
			case MD_FMT_DOUBLE: strcat(fullname, ".d"); break;
			case MD_FMT_WORD:   strcat(fullname, ".w"); break;
			case MD_FMT_LONG:   strcat(fullname, ".l"); break;
			default: strcat(fullname, ".err");
		}
	} else if (add_fmt3) {
		switch (fld.fmt3) {
			case MD_FMT3_SINGLE: strcat(fullname, ".s"); break;
			case MD_FMT3_DOUBLE: strcat(fullname, ".d"); break;
			case MD_FMT3_WORD:   strcat(fullname, ".w"); break;
			case MD_FMT3_LONG:   strcat(fullname, ".l"); break;
			default: strcat(fullname, ".err");
		}
	}
	
	/* print name */
	fprintf(fd, "%-7s ", fullname);
		
	/* fields */
	while (*fld.format) {
		switch (*fld.format) {
			case 's':
			case 'b': fprintf(fd, "%s", gpr_name[fld.rs]); break;
			case 't': fprintf(fd, "%s", gpr_name[fld.rt]); break;
			case 'd': fprintf(fd, "%s", gpr_name[fld.rd]); break;
			case 'R': fprintf(fd, "%s", fpr_name[fld.fr]); break;
			case 'S': fprintf(fd, "%s", fpr_name[fld.fs]); break;
			case 'T': fprintf(fd, "%s", fpr_name[fld.ft]); break;
			case 'D': fprintf(fd, "%s", fpr_name[fld.fd]); break;
			case 'J': fprintf(fd, "0x%x", fld.target << 2); break;
			case 'j':
				fprintf(fd, "0x%x", text_offset + (fld.imm << 2) + 4);
				break;
			case 'o':
			case 'i': fprintf(fd, "%i", fld.imm); break;
			case 'u': fprintf(fd, "%u", fld.uimm); break;
			case 'H': fprintf(fd, "%d", fld.shift); break;
			case 'c': fprintf(fd, "%d", cc); break;
			case ',': fprintf(fd, ", "); break;
			default: fprintf(fd, "%c", *fld.format);
		}
		fld.format++;
	}
}


void md_dump_inst(FILE *fd, struct md_instfld_t *instfld)
{
	fprintf(fd,
		"name=%s\n"
		"inst=0x%08x\n"
		"fu_class=%d\n"
		"flags=0x%x\n"
		"in={%d,%d,%d}\n"
		"out={%d,%d}\n"
		"opcode=0x%x, func=0x%x\n"
		"rs=%d, rt=%d, rd=%d\n",
		instfld->name,
		instfld->inst,
		instfld->fu_class,
		instfld->flags,
		instfld->in[0], instfld->in[1], instfld->in[2],
		instfld->out[0], instfld->out[1],
		instfld->opcode, instfld->func,
		instfld->rs, instfld->rt, instfld->rd);
}


/* decode */
void md_decode_inst(word inst, struct md_instfld_t *instfld)
{
	/* general use fields */
	instfld->inst		= inst;
	instfld->opcode		= BITS32(inst, 31, 26);
	instfld->rs		= BITS32(inst, 25, 21);
	instfld->rt		= BITS32(inst, 20, 16);
	instfld->rd		= BITS32(inst, 15, 11);
	instfld->shift		= BITS32(inst, 10, 6);
	instfld->func		= BITS32(inst, 5, 0);
	instfld->cond		= BITS32(inst, 3, 0);
	
	/* immediate values */
	instfld->uimm		= BITS32(inst, 15, 0);
	instfld->imm		= SEXT32(instfld->uimm, 16);
	instfld->target		= BITS32(inst, 25, 0);
	
	/* floating point fields */
	instfld->fmt		= BITS32(inst, 25, 21);
	instfld->fmt3		= BITS32(inst, 2, 0);
	instfld->ft		= BITS32(inst, 20, 16);
	instfld->fr		= BITS32(inst, 25, 21);
	instfld->fs		= BITS32(inst, 15, 11);
	instfld->fd		= BITS32(inst, 10, 6);
	instfld->cc		= BITS32(inst, 20, 18);

	/* 'op' field */
#define DEFINST(NAME, BITS, MASK, FORMAT, FUCLASS, FLAGS, O0, O1, I0, I1, I2) \
	if ((inst & MASK) == BITS) { \
		instfld->op = OP_##NAME; \
		instfld->format = FORMAT; \
		instfld->name = #NAME; \
		instfld->flags = FLAGS; \
		instfld->fu_class = FUCLASS; \
		instfld->in[0] = I0; \
		instfld->in[1] = I1; \
		instfld->in[2] = I2; \
		instfld->out[0] = O0; \
		instfld->out[1] = O1; \
	} else
#include "machine.def"
#undef DEFINST
	{
		instfld->op = OP_ERR;
		instfld->format = "";
		instfld->name = "-err-";
		instfld->flags = instfld->fu_class = 0;
		instfld->in[0] = instfld->in[1] = instfld->in[2] =
			instfld->out[0] = instfld->out[1] = DNA;
	}
}


/* instruction execution;
 * we need 'ctx' (a unique identifier per context) for RMW instructions */
int md_execute_inst(struct mem_t *mem, struct regs_t *regs,
	struct md_instfld_t *instfld, int ctx)
{
	int result = MD_MSG_OK;
	SET_GPR(0, 0);
	switch (instfld->op) {
#define DEFINST(NAME, BITS, MASK, FORMAT, FUCLASS, FLAGS, O1, O2, I1, I2, I3) \
	case OP_##NAME: \
	NAME##_IMPL; \
	break;
#include "machine.def"
#undef DEFINST
	default:
		return MD_MSG_NOTIMPL;
	}
	return result;
}
