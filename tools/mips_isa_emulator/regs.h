#ifndef REGS_H
#define REGS_H

#include <stdio.h>

#include "misc.h"

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
#define GPR_NAME(X)	gpr_name[X]
#define FPR_NAME(X)	fpr_name[X]

		
			

extern char *gpr_name[];
extern char *fpr_name[];

struct regs_t {
	
	word	regs_R[GPR_COUNT];		/* general purpouse regs */
	
	union {
		float	s[FPR_COUNT];		/* single precision fp regs */
		double	d[FPR_COUNT / 2];	/* double precision fp regs */
	} regs_F;
	
	struct {
		word	FIR;			/* fp implementation register */
		word	FCSR;			/* fp control & status register */
	} regs_C;
	
	word		regs_HI;
	word		regs_LO;
	word		regs_pc;
	word		regs_npc;
	word		regs_nnpc;
};



/* data dependencies */
#define DNA		(0)
#define DGPR(N)		(N)
#define DFPR(N)		((N)+32)
#define DREGHI		(64)
#define DREGLO		(65)
#define DFPC		(66)
#define DTMP		(67)

#define DFIRST		(1)
#define DLAST		(DTMP)
#define DCOUNT		(DLAST - DFIRST + 1)
#define DVALID(X)	((X) >= DFIRST && (X) <= DLAST)


/* functions */
struct regs_t *regs_create();
void regs_copy(struct regs_t *dst, struct regs_t *src);
void regs_free(struct regs_t *regs);
void regs_dump(struct regs_t *regs, FILE *f);
void regs_reg_name(word reg, char *buffer);


/* register file access macros */
#define GPR(X)		(regs->regs_R[X])
#define SGPR(X)		((sword) regs->regs_R[X])
#define SET_GPR(X,V)	(regs->regs_R[X] = (V))
#define REGHI		(regs->regs_HI)
#define SET_REGHI(V)	(regs->regs_HI = (V))
#define REGLO		(regs->regs_LO)
#define SET_REGLO(V)	(regs->regs_LO = (V))
#define FPRS(X)		(regs->regs_F.s[X])
#define SET_FPRS(X,V)	(regs->regs_F.s[X] = (V))
#define FPRD(X)		(regs->regs_F.d[(X)/2])
#define SET_FPRD(X,V)	(regs->regs_F.d[(X)/2] = (V))
#define PC		(regs->regs_pc)
#define FPC_FIR		(regs->regs_C.FIR)
#define SET_FPC_FIR(V)	(regs->regs_C.FIR = (V))
#define FPC_FCSR	(regs->regs_C.FCSR)
#define SET_FPC_FCSR(V)	(regs->regs_C.FCSR = (V))
#define FPCC(C)		((C) ? GETBIT32(FPC_FCSR, 24 + (C)) : GETBIT32(FPC_FCSR, 23))
#define SET_FPCC(C,V)	((C) ? SET_FPC_FCSR(SETBITVALUE32(FPC_FCSR, 24 + (C), (V))) : \
			SET_FPC_FCSR(SETBITVALUE32(FPC_FCSR, 23, (V))))
#define BRANCH(V)	(regs->regs_nnpc = (V))
#define RELBRANCH(V)	(regs->regs_nnpc = PC + (V) + 4)


#endif
