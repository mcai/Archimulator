#include <string.h>

#include "mhandle.h"
#include "regs.h"

char *gpr_name[32] = {
	"zero",	"at",	"v0",	"v1",	"a0",	"a1",	"a2",	"a3",
	"t0",	"t1",	"t2",	"t3",	"t4",	"t5",	"t6",	"t6",
	"s0",	"s1",	"s2",	"s3",	"s4",	"s5",	"s6",	"s7",
	"t8",	"t9",	"k0",	"k1",	"gp",	"sp",	"fp",	"ra"
};

char *fpr_name[32] = {
	"$f0",	"$f1",	"$f2",	"$f3",	"$f4",	"$f5",	"$f6",	"$f7",
	"$f8",	"$f9",	"$f10",	"$f11",	"$f12",	"$f13",	"$f14",	"$f15",
	"$f16",	"$f17",	"$f18",	"$f19",	"$f20",	"$f21",	"$f22",	"$f23",
	"$f24",	"$f25",	"$f26",	"$f27",	"$f28",	"$f29",	"$f30",	"$f31"
};


struct regs_t *regs_create()
{
	struct regs_t *regs;
	regs = calloc(1, sizeof(struct regs_t));
	return regs;
}


void regs_copy(struct regs_t *dst, struct regs_t *src)
{
	*dst = *src;
}


void regs_free(struct regs_t *regs)
{
	free(regs);
}


void regs_dump(struct regs_t *regs, FILE *f)
{
	int i;
	for (i = 0; i < 32; i++)
		fprintf(f, "%s = 0x%08x, \n", gpr_name[i], regs->regs_R[i]);
	fprintf(f, "pc = 0x%08x, npc = 0x%08x, nnpc = 0x%08x\n", regs->regs_pc, regs->regs_npc, regs->regs_nnpc);
	fflush(f);
}

void regs_reg_name(word reg, char * buffer)
{
	if (!reg)
		sprintf(buffer, "DNA");
	else if (reg < NUM_IREGS)
		sprintf(buffer, "%s", gpr_name[reg]);
	else if (reg < NUM_IREGS + NUM_FREGS)
		sprintf(buffer, "%s", fpr_name[reg]);
	else if (reg == (0+32+32))
		sprintf(buffer, "DREGHI");
	else if (reg == (1+32+32))
		sprintf(buffer, "DREGLO");
	else if (reg == (2+32+32))
		sprintf(buffer, "DFPC");
	else if (reg == (3+32+32))
		sprintf(buffer, "DTMP");
}
