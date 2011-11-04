#include <assert.h>
#include <string.h>
#include <unistd.h>

#include "mhandle.h"
#include "private.h"


/* Private Functions */
 
static int ke_next_ctx_incr(struct kernel_t *ke, int ctx, int incr)
{
	do {
		ctx = MOD(ctx + incr, KE_MAXCTX);
	} while (!STATUS(KE_ALIVE));
	return ctx;
}


static int ke_create_ctx(struct kernel_t *ke)
{
	int ctx;
	
	/* search free ctx */
	for (ctx = 0; ctx < KE_MAXCTX; ctx++)
		if (!STATUS(KE_ALIVE))
			break;
	if (ctx == KE_MAXCTX)
		fatal("too much contexts; increase KE_MAXCTX");
	
	/* initialize context */
	bzero(&KECTX, sizeof(KECTX));
	SETSTATUS(KE_ALIVE);
	SETSTATUS(KE_RUNNING);
	KECTX.parent = -1;
	KECTX.prev = ke_next_ctx_incr(ke, ctx, -1);
	KECTX.next = ke_next_ctx_incr(ke, ctx, 1);
	KECTXI(KECTX.prev).next = ctx;
	KECTXI(KECTX.next).prev = ctx;
	ke->alive_count++;
	ke->active_count++;
	
	/* context enumeration */
	if (ke->first_ctx == -1)
		ke->first_ctx = ctx;
	else if (ke->first_ctx > ctx)
		ke->first_ctx = ctx;
	
	/* return context */
	return ctx;
}


static void ke_add_zombie(struct kernel_t *ke, int pid)
{
	if (ke->zombie_count == KE_MAXCTX)
		fatal("too much zombie processes");
	ke->zombie[ke->zombie_count++] = pid;
}


/* Public Functions */

struct kernel_t *ke_create()
{
	struct kernel_t *ke;
	ke = calloc(1, sizeof(struct kernel_t));
	syseventq_init(ke);
	pipedb_init(ke);
	signaldb_init(ke);
	ke->first_ctx = -1;
	ke->current_pid = 1000;
	return ke;
}


void ke_free(struct kernel_t *ke)
{
	int ctx;
	for (ctx = 0; ctx < KE_MAXCTX; ctx++) {
		if (!STATUS(KE_ALIVE))
			continue;
		if (!STATUS(KE_FINISHED))
			ke_finish_ctx(ke, ctx);
		ke_kill_ctx(ke, ctx);
	}
	syseventq_done(ke);
	pipedb_done(ke);
	signaldb_done(ke);
	sysevent_free_all();
	free(ke);
}


sdword ke_cycle(struct kernel_t *ke)
{
	return ke->cycle;
}


void ke_new_cycle(struct kernel_t *ke)
{
	ke->cycle++;
	if (ke->cycle % 1000 == 0) {
		syseventq_process(ke);
		signaldb_process(ke);
	}
}


void ke_reset_cycle(struct kernel_t *ke)
{
	ke->cycle = 0;
}


int ke_new_ctx(struct kernel_t *ke)
{
	int ctx = ke_create_ctx(ke);
	KECTX.regs = regs_create();
	KECTX.rec_regs = regs_create();
	KECTX.mem = mem_create(NULL);
	KECTX.memid = ke->current_memid++;
	KECTX.pid = ke->current_pid++;
	KECTX.parent = -1;
	
	/* private structs initialization */
	ld_init(ke, ctx);
	
	return ctx;
}


int ke_clone_ctx(struct kernel_t *ke, int ctx, int sigfinish)
{
	int new, i;
	ke_assert_ctx(ke, ctx);
	new = ke_create_ctx(ke);
	KECTXI(new).regs = regs_create();
	regs_copy(KECTXI(new).regs, KECTX.regs);
	KECTXI(new).rec_regs = regs_create();
	KECTXI(new).mem = mem_create(KECTX.mem);
	KECTXI(new).memid = KECTX.memid;
	KECTXI(new).ld = KECTX.ld;
	KECTXI(new).pid = ke->current_pid++;
	KECTXI(new).parent = ctx;
	KECTXI(new).sigfinish = sigfinish;
	for (i = 0; i < MAX_FNDEPTH; i++)
		KECTXI(new).backtrace[i] = KECTX.backtrace[i];
	KECTXI(new).fndepth = KECTX.fndepth;
	KECTX.child_count++;
	return new;
}


int ke_first_ctx(struct kernel_t *ke)
{
	ke->current_ctx = ke->first_ctx;
	return ke->first_ctx;
}

int ke_next_ctx(struct kernel_t *ke)
{
	int ctx;
	if (!STATUSI(ke->current_ctx, KE_ALIVE))
		return -1;
	ctx = KECTXI(ke->current_ctx).next;
	if (ctx <= ke->current_ctx)
		return -1;
	ke->current_ctx = ctx;
	return ctx;
}

int ke_next_rr(struct kernel_t *ke, int ctx)
{
	if (ke->first_ctx == -1)
		return -1;
	if (!ke_valid_ctx(ke, ctx))
		return ke->first_ctx;
	return KECTX.next;
}

int ke_prev_rr(struct kernel_t *ke, int ctx)
{
	if (ke->first_ctx == -1)
		return -1;
	if (!ke_valid_ctx(ke, ctx))
		return ke->first_ctx;
	return KECTX.prev;
}

int ke_ring_first(struct kernel_t *ke, int ctx)
{
	ke->ring_first = ke->ring_current = ke_next_rr(ke, ctx);
	return ke->ring_first;
}

int ke_ring_next(struct kernel_t *ke)
{
	ke->ring_current = ke_next_rr(ke, ke->ring_current);
	if (ke->ring_current == ke->ring_first)
		return -1;
	return ke->ring_current;
}

/* check that 'ctx' is a correct and alive context */
int ke_valid_ctx(struct kernel_t *ke, int ctx)
{
	return ctx >= 0 && ctx < KE_MAXCTX && STATUS(KE_ALIVE);
}

void ke_assert_ctx(struct kernel_t *ke, int ctx)
{
	if (!ke_valid_ctx(ke, ctx))
		fatal("th%d: invalid or not alive context", ctx);
}

static string_map_t ke_status_map = {
	7, {
		{ "KE_ALIVE", KE_ALIVE },
		{ "KE_RUNNING", KE_RUNNING },
		{ "KE_SPECMODE", KE_SPECMODE },
		{ "KE_SUSPENDED", KE_SUSPENDED },
		{ "KE_FINISHED", KE_FINISHED },
		{ "KE_EXCL", KE_EXCL },
		{ "KE_LOCKED", KE_LOCKED }
	}
};

void ke_dump_ctx(struct kernel_t *ke, int ctx, FILE *f)
{
	char flags[100];
	ke_assert_ctx(ke, ctx);
	map_flags(&ke_status_map, KECTX.status, flags);
	fprintf(f, "Ctx %d:\n", ctx);
	fprintf(f, "\tstatus=%s\n", flags);
}

int ke_running(struct kernel_t *ke, int ctx)
{
	if (!ke_valid_ctx(ke, ctx))
		return 0;
	return STATUS(KE_RUNNING);
}

int ke_specmode(struct kernel_t *ke, int ctx)
{
	if (!ke_valid_ctx(ke, ctx))
		return 0;
	return STATUS(KE_SPECMODE);
}

int ke_suspended(struct kernel_t *ke, int ctx)
{
	if (!ke_valid_ctx(ke, ctx))
		return 0;
	return STATUS(KE_SUSPENDED);
}

int ke_finished(struct kernel_t *ke, int ctx)
{
	if (!ke_valid_ctx(ke, ctx))
		return 0;
	return STATUS(KE_FINISHED);
}

void ke_set_running(struct kernel_t *ke, int ctx)
{
	int running;
	
	ke_assert_ctx(ke, ctx);
	running = STATUS(KE_ALIVE) && !STATUS(KE_FINISHED) &&
		!STATUS(KE_SUSPENDED) && !STATUS(KE_LOCKED);
	if (running)
		SETSTATUS(KE_RUNNING);
	else
		CLRSTATUS(KE_RUNNING);
}


int ke_memid(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	return KECTX.memid;
}


int ke_get_pid(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	return KECTX.pid;
}

int ke_get_ppid(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	if (KECTX.parent < 0)
		return 1;
	else
		return KECTXI(KECTX.parent).pid;
}

int ke_child_count(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	return KECTX.child_count;
}

int ke_is_zombie(struct kernel_t *ke, int pid)
{
	int i;
	for (i = 0; i < ke->zombie_count; i++)
		if (ke->zombie[i] == pid)
			return 1;
	return 0;
}

int ke_zombie_count(struct kernel_t *ke)
{
	return ke->zombie_count;
}

int ke_kill_zombie(struct kernel_t *ke, int pid)
{
	int i, pos;
	for (pos = 0; pos < ke->zombie_count; pos++)
		if (ke->zombie[pos] == pid || pid == -1)
			break;
	if (pos == ke->zombie_count)
		return -1;
	pid = ke->zombie[pos];
	for (i = pos + 1; i < ke->zombie_count; i++)
		ke->zombie[i - 1] = ke->zombie[i];
	ke->zombie_count--;
	return pid;
}

int ke_pid_ctx(struct kernel_t *ke, int pid)
{
	int ctx;
	for (ctx = 0; ctx < KE_MAXCTX; ctx++)
		if (STATUS(KE_ALIVE) && !STATUS(KE_FINISHED)
			&& KECTX.pid == pid)
			return ctx;
	return -1;
}

/* mem, regs, ld, syseventq */
struct mem_t *ke_mem(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	return KECTX.mem;
}

struct regs_t *ke_regs(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	return KECTX.regs;
}

int ke_alive_count(struct kernel_t *ke)
{
	return ke->alive_count;
}

int ke_active_count(struct kernel_t *ke)
{
	return ke->active_count;
}

int ke_set_npc(struct kernel_t *ke, int ctx, word npc)
{
	/* if npc does not force current execution trace,
	 * do nothing and return false */
	if (KECTX.regs->regs_npc == npc)
		return FALSE;
	
	/* if we are forcing a new trace, but we are already
	 * in specmode, do it and return false */
	if (STATUS(KE_SPECMODE)) {
		KECTX.regs->regs_npc = npc;
		return FALSE;
	}
	
	/* we are starting specmode; force npc and return true */
	SETSTATUS(KE_SPECMODE);
	mem_start_spec(KECTX.mem);
	*KECTX.rec_regs = *KECTX.regs;
	KECTX.regs->regs_npc = npc;
	return TRUE;
}

void ke_recover(struct kernel_t *ke, int ctx)
{
	if (!STATUS(KE_SPECMODE))
		fatal("ke_recover: not in specmode");
	CLRSTATUS(KE_SPECMODE);
	mem_recover(KECTX.mem);
	*KECTX.regs = *KECTX.rec_regs;
}

/* request context halt */
void ke_finish_ctx(struct kernel_t *ke, int ctx)
{
	int i;
	
	/* finish ctx */
	ke_assert_ctx(ke, ctx);
	if (STATUS(KE_FINISHED))
		fatal("ke_finish_ctx requires a not finished context");
	SETSTATUS(KE_FINISHED);
	ke_set_running(ke, ctx);
	ke_add_zombie(ke, KECTX.pid);
	ke->active_count--;
	assert(ke->active_count >= 0);
	
	/* finish child ctxs */
	for (i = 0; i < KE_MAXCTX; i++) {
		if (STATUSI(i, KE_ALIVE) && !STATUSI(i, KE_FINISHED) &&
			KECTXI(i).parent == ctx)
			ke_finish_ctx(ke, i);
	}
	
	/* send finish signal to parent */
	if (KECTX.sigfinish && KECTX.parent >= 0)
		sim_sigset_add(&ke->signaldb->context[KECTX.parent].pending, KECTX.sigfinish);
}

/* halt completely a context (after external termination finished) */
void ke_kill_ctx(struct kernel_t *ke, int ctx)
{
	int i;
	ke_assert_ctx(ke, ctx);
	if (!STATUS(KE_FINISHED))
		fatal("ke_kill_ctx requires a not running & finished context");
	
	/* kill children first */
	for (i = 0; i < KE_MAXCTX; i++) {
		if (STATUSI(i, KE_ALIVE) && KECTXI(i).parent == ctx) {
			if (!STATUSI(i, KE_FINISHED))
				fatal("child ctx not finished");
			ke_kill_ctx(ke, i);
		}
	}
		
	/* free regs, memory & loader */
	regs_free(KECTX.regs);
	regs_free(KECTX.rec_regs);
	mem_free(KECTX.mem);
	if (KECTX.parent == -1) {
		ld_close_prog(ke, ctx);
		ld_done(ke, ctx);
	}
	
	/* decrease parent children counter */
	if (KECTX.parent >= 0)
		KECTXI(KECTX.parent).child_count--;
	
	/* context enumeration */
	if (ke->alive_count == 1)
		ke->first_ctx = -1;
	else if (ctx == ke->first_ctx)
		ke->first_ctx = KECTX.next;
		
	/* delete entry */
	KECTXI(KECTX.prev).next = KECTX.next;
	KECTXI(KECTX.next).prev = KECTX.prev;
	CLRSTATUS(KE_ALIVE);
	ke->alive_count--;
	assert(ke->alive_count >= 0);
	
	/* kill child ctxs */
	for (i = 0; i < KE_MAXCTX; i++) {
		if (STATUSI(i, KE_ALIVE) && KECTXI(i).parent == ctx) {
			assert(!STATUSI(i, KE_RUNNING));
			ke_kill_ctx(ke, i);
		}
	}
}

void ke_suspend_ctx(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	if (STATUS(KE_SUSPENDED))
		fatal("ke_suspend_ctx: wrong previous context status");
	SETSTATUS(KE_SUSPENDED);
	ke_set_running(ke, ctx);
}

void ke_resume_ctx(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	if (!STATUS(KE_SUSPENDED))
		fatal("ke_resume_ctx: wrong previous context status");
	CLRSTATUS(KE_SUSPENDED);
	ke_set_running(ke, ctx);
}


static void ke_excl_mode(struct kernel_t *ke, int ctx, int excl)
{
	int i;
	ke_assert_ctx(ke, ctx);
	if (excl) {
		SETSTATUS(KE_EXCL);
		for (i = 0; i < KE_MAXCTX; i++) {
			if (ke_valid_ctx(ke, i) && i != ctx) {
				SETSTATUSI(i, KE_LOCKED);
				ke_set_running(ke, i);
			}
		}
	} else {
		CLRSTATUS(KE_EXCL);
		for (i = 0; i < KE_MAXCTX; i++) {
			if (ke_valid_ctx(ke, i)) {
				CLRSTATUSI(i, KE_LOCKED);
				ke_set_running(ke, i);
			}
		}
	}
}


/*static struct { dword freq; char *name; } inststat[OP_MAX];
static struct { dword freq; char *name; } sysstat[269];*/
void ke_dump_stats(struct kernel_t *ke, int level)
{
	int ctx;
	
	switch (level) {
	
	case 1:
		printf("%lld cycles\n", (long long) ke->cycle);
		printf("%lld executed inst (not nops)\n", (long long) ke->notnops);
		ctx = ke_first_ctx(ke);
		while (ctx >= 0) {
			printf("ctx%d.pc = 0x%x\n", ctx, KECTX.regs->regs_pc);
			ctx = ke_next_ctx(ke);
		}
		fflush(stdout);
		break;
	}
}


/* execute an instruction in a context;
 * store decoded instructions in KECTX.instfld;
 * return FALSE if instruction in spec_mode cannot be executed */
int ke_execute_inst(struct kernel_t *ke, int ctx)
{
	struct mem_t *mem;
	struct regs_t *regs;
	struct loader_t *ld;
	int result;
	word inst;

	/* ctx info */
	ke_assert_ctx(ke, ctx);
	mem = KECTX.mem;
	regs = KECTX.regs;
	ld = KECTX.ld;
	if (!STATUS(KE_RUNNING))
		fatal("cannot execute instructions if ctx is not running");
	
	/* next pc */
	regs->regs_pc = regs->regs_npc;
	regs->regs_npc = regs->regs_nnpc;
	regs->regs_nnpc = regs->regs_nnpc + 4;
	
	/* read and decode instruction */
	READ_WORD(regs->regs_pc, &inst);
	md_decode_inst(inst, &KECTX.instfld);
	KECTX.effaddr = KECTX.instfld.imm + regs->regs_R[KECTX.instfld.rs];
	/*{ ///
		if (!STATUS(KE_SPECMODE)) {
			inststat[KECTX.instfld.op].freq++;
			inststat[KECTX.instfld.op].name = KECTX.instfld.name;
			if (KECTX.instfld.op == OP_SYSCALL) {
				assert(regs->regs_R[REGS_V0] >= 4000 &&
					regs->regs_R[REGS_V0] < 4269);
				sysstat[regs->regs_R[REGS_V0] - 4000].freq++;
			}
		}
	}*/
	result = md_execute_inst(mem, regs, &KECTX.instfld, ctx);
	
	/* actions fot non spec mode execution */
	if (!STATUS(KE_SPECMODE)) {
		if (KECTX.instfld.inst)
			ke->notnops++;
	}
	
	/*{ ///
		static int out;
		if (!(KECTX.instfld.flags & F_LOAD) && out) {
			if (KECTX.instfld.in[0] == out ||
				KECTX.instfld.in[1] == out ||
				KECTX.instfld.in[2] == out)
				fatal("risk situation");
		}

		if (KECTX.instfld.flags & F_LOAD)
			out = KECTX.instfld.out[0];
		else
			out = 0;
	}*/
	
	/* backtrace */
	if (!STATUS(KE_SPECMODE) && KECTX.instfld.flags & (F_CALL|F_RET)) {
		if (KECTX.instfld.flags & F_CALL) {
			if (KECTX.fndepth < MAX_FNDEPTH)
				KECTX.backtrace[KECTX.fndepth] = regs->regs_pc;
			KECTX.fndepth++;
		} else {
			KECTX.fndepth--;
			if (KECTX.fndepth < 0)
				KECTX.fndepth = 0;
		}
	}
	
	/* special result in specmode not handled */
	if (STATUS(KE_SPECMODE) && result)
		return FALSE;
	
	/* stop exclusive execution mode? */
	if (STATUS(KE_EXCL)) {
	
		/* if we are in specmode, do not stop excl mode */
		if (STATUS(KE_SPECMODE))
			return FALSE;
		
		/* stop excl mode if some event occurred (including MD_MSG_ENDRMW)
		 * or a memory operation appeared */
		/*fprintf(stderr, "ctx%d:0x%x: excl: %s", ctx, regs->regs_pc,
			STATUS(KE_SPECMODE) ? "*" : "");
		md_print_inst(stderr, inst, regs->regs_pc);
		fprintf(stderr, "\n");*/
		/*if (result != MD_MSG_OK && result != MD_MSG_ENDRMW)
			fatal("rmw non portable event");
		if (KECTX.instfld.flags & F_MEM && result != MD_MSG_ENDRMW)
			fatal("rmw non portable event 2");*/
		if (result != MD_MSG_OK || KECTX.instfld.flags & F_MEM)
			ke_excl_mode(ke, ctx, FALSE);
	}
	
	/* process result */
	switch (result) {
	
	case MD_MSG_NOTIMPL:
	case MD_MSG_FMTERROR:
	case MD_MSG_FMT3ERROR:
		fatal("ctx%d:0x%x: not implemented instruction "
			"(opcode=0x%02x, func=0x%02x)",
			ctx, regs->regs_pc, KECTX.instfld.opcode,
			KECTX.instfld.func);
		break;
		
	case MD_MSG_FINISH:
		ke_finish_ctx(ke, ctx);
		break;
		
	case MD_MSG_SYSCALL:
		sim_syscall(ke, ctx);
		break;
		
	/* start of exclusive mode;
	 * set all contexts' status to KE_LOCKED */
	case MD_MSG_STARTRMW:
		ke_excl_mode(ke, ctx, TRUE);
		break;
	
	}
	return TRUE;
}

/* return last executed instruction */
void ke_instfld(struct kernel_t *ke, int ctx, struct md_instfld_t *instfld)
{
	ke_assert_ctx(ke, ctx);
	*instfld = KECTX.instfld;
}

/* return effective address of last executed instruction */
word ke_effaddr(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	return KECTX.effaddr;
}

/* stack backtrace */
void ke_backtrace(struct kernel_t *ke, int ctx, FILE *f)
{
	int i;
	char *sym, *comma = "";
	word offs;
	
	ke_assert_ctx(ke, ctx);
	fprintf(f, "\nbacktrace: ");
	for (i = 0; i < KECTX.fndepth; i++) {
		sym = ld_get_symbol(KECTX.ld, KECTX.backtrace[i], &offs);
		fprintf(f, "%s%s", comma, sym);
		comma = " - ";
	}
	fprintf(f, "\n");
}
