#ifndef PUBLIC_H
#define PUBLIC_H

#include "stats.h"
#include "options.h"
#include "machine.h"


#define KE_MAXCTX  256

#define FOREACH_CTX(KE)		for (ctx = ke_first_ctx(KE); \
				ctx >= 0; ctx = ke_next_ctx(KE))
#define FOREACH_RUNNING_CTX(KE) for (ctx = ke_first_ctx(KE); \
				ctx >= 0; ctx = ke_next_ctx(KE)) \
				if (ke_running((KE), ctx))

struct kernel_t;

/* creation/destruction */
struct kernel_t *ke_create();
void ke_free(struct kernel_t *ke);

/* simulator cycle */
sdword ke_cycle(struct kernel_t *ke);
void ke_new_cycle(struct kernel_t *ke);
void ke_reset_cycle(struct kernel_t *ke);

/* context creation */
int ke_new_ctx(struct kernel_t *ke);

/* context enumeration; return value=-1 if no more context available */
int ke_first_ctx(struct kernel_t *ke);
int ke_next_ctx(struct kernel_t *ke);

/* round robin enumeration; return -1 only if no context alive */
int ke_next_rr(struct kernel_t *ke, int ctx);
int ke_prev_rr(struct kernel_t *ke, int ctx);

/* visit each context once in ring fashion starting at the
 * next alive context from 'ctx'; return -1 when all contexts visited */
int ke_ring_first(struct kernel_t *ke, int ctx);
int ke_ring_next(struct kernel_t *ke);

/* check for valid (in range) and alive context */
int ke_valid_ctx(struct kernel_t *ke, int ctx);
void ke_assert_ctx(struct kernel_t *ke, int ctx);
void ke_dump_ctx(struct kernel_t *ke, int ctx, FILE *f);

/* consult context status */
int ke_specmode(struct kernel_t *ke, int ctx);
int ke_running(struct kernel_t *ke, int ctx);
int ke_suspended(struct kernel_t *ke, int ctx);
int ke_finished(struct kernel_t *ke, int ctx);

/* return memory space identifier */
int ke_memid(struct kernel_t *ke, int ctx);

/* change context status */
void ke_finish_ctx(struct kernel_t *ke, int ctx);
void ke_kill_ctx(struct kernel_t *ke, int ctx);

/* kernel context architected status */
struct mem_t *ke_mem(struct kernel_t *ke, int ctx);
struct regs_t *ke_regs(struct kernel_t *ke, int ctx);

/* return number of alive conexts */
int ke_alive_count(struct kernel_t *ke);
int ke_active_count(struct kernel_t *ke); /* =not finished */

/* force value of pc for the next by 'ke_execute_inst' executed inst;
 * return true if specmode execution is starting */
int ke_set_npc(struct kernel_t *ke, int ctx, word npc);
void ke_recover(struct kernel_t *ke, int ctx);


/* Instruction Execution */

/* execute instruction in a context; return value is 0 if instruction cannot be
 * executed (for example, system call when specmode=TRUE) */
int ke_execute_inst(struct kernel_t *ke, int ctx);

/* return the last decoded instruction/effective addr in a ke_execute_inst call */
void ke_instfld(struct kernel_t *ke, int ctx, struct md_instfld_t *instfld);
word ke_effaddr(struct kernel_t *ke, int ctx);

/* print stack backtrace */
void ke_backtrace(struct kernel_t *ke, int ctx, FILE *f);
void ke_dump_stats(struct kernel_t *ke, int level);


/* Program Loading */

void ld_add_arg(struct kernel_t *ke, int ctx, char *arg);
void ld_add_args(struct kernel_t *ke, int ctx, int argc, char **argv);
void ld_add_cmdline(struct kernel_t *ke, int ctx, char *cmdline);
void ld_set_cwd(struct kernel_t *ke, int ctx, char *cwd);
void ld_set_redir(struct kernel_t *ke, int ctx, char *stdin, char *stdout);
void ld_load_prog(struct kernel_t *ke, int ctx, char *exe);
void ld_load_progs(struct kernel_t *ke, char *ctxconfig);


#endif
