#include <stdlib.h>

#include "list.h"
#include "buffer.h"
#include "mhandle.h"
#include "private.h"


/* System Event */

struct sysevent_t *sysevent_free_list = NULL;

struct sysevent_t *sysevent_create(int ctx, int kind)
{
	struct sysevent_t *e;
	if (!sysevent_free_list)
		sysevent_free_list = calloc(1, sizeof(struct sysevent_t));
	e = sysevent_free_list;
	sysevent_free_list = sysevent_free_list->next;
	bzero(e, sizeof(struct sysevent_t));
	e->ctx = ctx;
	e->kind = kind;
	return e;
}


void sysevent_free(struct sysevent_t *e)
{
	e->next = sysevent_free_list;
	sysevent_free_list = e;
}


void sysevent_free_all()
{
	struct sysevent_t *next;
	while (sysevent_free_list) {
		next = sysevent_free_list->next;
		free(sysevent_free_list);
		sysevent_free_list = next;
	}
}


static void sysevent_process(struct sysevent_t *e, struct kernel_t *ke)
{
	struct regs_t *regs = ke_regs(ke, e->ctx);
	struct mem_t *mem = ke_mem(ke, e->ctx);

	switch (e->kind) {
	
	case SYSEVENT_READ:
	{
		int nread;
		void *buf;
		
		ke_resume_ctx(ke, e->ctx);
		
		buf = calloc(1, e->for_waitfd.size);
		nread = buffer_read(e->for_waitfd.buffer, buf, e->for_waitfd.size);
		SET_GPR(REGS_V0, nread);
		SET_GPR(REGS_A3, 0);
		WRITE_BLK(e->for_waitfd.addr, nread, buf);
		free(buf);
		break;
	}
	
	case SYSEVENT_RESUME:
		ke_resume_ctx(ke, e->ctx);
		break;
	
	case SYSEVENT_WAIT:
	{
		int pid;
		ke_resume_ctx(ke, e->ctx);
		pid = ke_kill_zombie(ke, e->for_waitpid.pid);
		SET_GPR(REGS_A3, 0);
		SET_GPR(REGS_V0, pid);
		break;
	}
	
	case SYSEVENT_POLL:
	{
		if (buffer_count(e->for_waitfd.buffer)) {
		
			/* POLLIN occurred */
			WRITE_HALF(e->for_waitfd.pufds + 6, 1);
			SET_GPR(REGS_V0, 1);
		} else {
			/* timeout occurred */
			SET_GPR(REGS_V0, 0);
		}
		SET_GPR(REGS_A3, 0);
		ke_resume_ctx(ke, e->ctx);
		break;
	}
	
	case SYSEVENT_SIGSUSPEND:
	{
		/* process pending signals before restoring original signal mask */
		ke_resume_ctx(ke, e->ctx);
		signaldb_process(ke);
		ke->signaldb->context[e->ctx].blocked = ke->signaldb->context[e->ctx].backup;
		debug("th%d: context resumed", e->ctx);
		break;
	}
		
	default:
		fatal("wrong event kind");
	}
}




/* System Event Queue */

struct syseventq_t {
	struct list_t *list;
};


void syseventq_init(struct kernel_t *ke)
{
	ke->syseventq = calloc(1, sizeof(struct syseventq_t));
	ke->syseventq->list = list_create(10);
}


void syseventq_done(struct kernel_t *ke)
{
	while (list_count(ke->syseventq->list))
		sysevent_free(list_dequeue(ke->syseventq->list));
	list_free(ke->syseventq->list);
	free(ke->syseventq);
}


void syseventq_sched(struct kernel_t *ke, struct sysevent_t *e)
{
	list_add(ke->syseventq->list, e);
}


void syseventq_process(struct kernel_t *ke)
{
	clock_t now = clock();
	struct sysevent_t *e;
	int i, process;
	
	/* process events in System Events Queue */
	for (i = 0; i < list_count(ke->syseventq->list); i++) {
		process = 0;
		e = list_get(ke->syseventq->list, i);
		
		/* activation after writing to an fd */
		if (e->for_waitfd.active && buffer_count(e->for_waitfd.buffer))
			process = e->for_waitfd.occurred = 1;
		
		/* time activation */
		if (e->for_time.active && e->for_time.when <= now)
			process = e->for_time.occurred = 1;
		
		/* pid wait activation */
		if (e->for_waitpid.active) {
			if (e->for_waitpid.pid == -1 && ke_zombie_count(ke))
				process = e->for_waitpid.occurred = 1;
			if (e->for_waitpid.pid > 0 && ke_is_zombie(ke, e->for_waitpid.pid))
				process = e->for_waitpid.occurred = 1;
		}
		
		/* signal activation */
		if (e->for_signal.active) {
			int sig;
			for (sig = 1; sig <= MAXSIGNAL; sig++)
				if (signaldb_must_process(ke, e->ctx, sig))
					process = e->for_signal.occurred = 1;
		}
		
		/* process event */
		if (process) {
			list_remove_at(ke->syseventq->list, i);
			sysevent_process(e, ke);
			sysevent_free(e);
			i--;
		}
	}
}




/* Pipe Data Base */

#define PIPEFD_START	100
#define PIPEFD_END	200

struct pipedb_t {
	int free_fd;
	struct list_t *list;
};

struct pipe_t {
	int fd[2];
	struct buffer_t *buffer;
};


static struct pipe_t *pipe_create()
{
	struct pipe_t *pipe;
	pipe = calloc(1, sizeof(struct pipe_t));
	pipe->buffer = buffer_create(200);
	return pipe;
}


static void pipe_free(struct pipe_t *pipe)
{
	buffer_free(pipe->buffer);
	free(pipe);
}


void pipedb_init(struct kernel_t *ke)
{
	ke->pipedb = calloc(1, sizeof(struct pipedb_t));
	ke->pipedb->list = list_create(10);
	ke->pipedb->free_fd = PIPEFD_START;
}


void pipedb_done(struct kernel_t *ke)
{
	while (list_count(ke->pipedb->list))
		pipe_free(list_pop(ke->pipedb->list));
	list_free(ke->pipedb->list);
	free(ke->pipedb);
}


void pipedb_pipe(struct kernel_t *ke, int *fd)
{
	struct pipe_t *pipe;
	pipe = pipe_create();
	fd[0] = pipe->fd[0] = ke->pipedb->free_fd++;
	fd[1] = pipe->fd[1] = ke->pipedb->free_fd++;
	if (ke->pipedb->free_fd > PIPEFD_END + 1)
		fatal("too much pipe descriptors");
	list_add(ke->pipedb->list, pipe);
}


void pipedb_close_fd(struct kernel_t *ke, int fd)
{
	int i;
	struct pipe_t *pipe;
	for (i = 0; i < list_count(ke->pipedb->list); i++) {
	
		/* close fd in this pipe */
		pipe = list_get(ke->pipedb->list, i);
		if (pipe->fd[0] == fd)
			pipe->fd[0] = -1;
		if (pipe->fd[1] == fd)
			pipe->fd[1] = -1;
		
		/* free pipe */
		if (pipe->fd[0] == -1 && pipe->fd[1] == -1) {
			pipe_free(pipe);
			list_remove_at(ke->pipedb->list, i);
			i--;
		}
	}
}


int pipedb_pipe_fd(int fd)
{
	return fd >= PIPEFD_START && fd <= PIPEFD_END;
}


static struct buffer_t *pipedb_buffer(struct kernel_t *ke, int fd, int idx)
{
	int i;
	struct pipe_t *pipe;
	for (i = 0; i < list_count(ke->pipedb->list); i++) {
		pipe = list_get(ke->pipedb->list, i);
		if (pipe->fd[idx] == fd)
			return pipe->buffer;
	}
	return NULL;
}


struct buffer_t *pipedb_write_buffer(struct kernel_t *ke, int fd)
{
	return pipedb_buffer(ke, fd, 1);
}


struct buffer_t *pipedb_read_buffer(struct kernel_t *ke, int fd)
{
	return pipedb_buffer(ke, fd, 0);
}




/* Signals */

string_map_t signal_map = {
	31, {
		{ "SIGHUP",	1 },
		{ "SIGINT",	2 },
		{ "SIGQUIT",	3 },
		{ "SIGILL",	4 },
		{ "SIGTRAP",	5 },
		{ "SIGABRT",	6 },
		{ "SIGEMT",	7 },
		{ "SIGFPE",	8 },
		{ "SIGKILL",	9 },
		{ "SIGBUS",	10 },
		{ "SIGSEGV",	11 },
		{ "SIGSYS",	12 },
		{ "SIGPIPE",	13 },
		{ "SIGALRM",	14 },
		{ "SIGTERM",	15 },
		{ "SIGUSR1",	16 },
		{ "SIGUSR2",	17 },
		{ "SIGCHLD",	18 },
		{ "SIGPWR",	19 },
		{ "SIGWINCH",	20 },
		{ "SIGURG",	21 },
		{ "SIGIO",	22 },
		{ "SIGSTOP",	23 },
		{ "SIGTSTP",	24 },
		{ "SIGCONT",	25 },
		{ "SIGTTIN",	26 },
		{ "SIGTTOU",	27 },
		{ "SIGVTALRM",	28 },
		{ "SIGPROF",	29 },
		{ "SIGXCPU",	30 },
		{ "SIGXFSZ",	31 }
	}
};

string_map_t sigactionflags_map = {
	5, {
		{ "SA_NOCLDSTOP",	0x00000001 },
		{ "SA_ONESHOT",		0x80000000 },
		{ "SA_RESTART",		0x10000000 },
		{ "SA_NOMASK",		0x40000000 },
		{ "SA_SIGINFO",		0x00000008 }
	}
};

string_map_t sigprocmaskhow_map = {
	3, {
		{ "SIG_BLOCK",		1 },
		{ "SIG_UNBLOCK",	2 },
		{ "SIG_SETMASK",	3 }
	}
};

void sim_sigset_add(struct sim_sigset_t *sim_sigset, int sig)
{
	if (sig < 1 || sig > MAXSIGNAL)
		return;
	sig--;
	sim_sigset->sig[sig / 32] |= (1 << (sig % 32));
}

void sim_sigset_del(struct sim_sigset_t *sim_sigset, int sig)
{
	if (sig < 1 || sig > MAXSIGNAL)
		return;
	sig--;
	sim_sigset->sig[sig / 32] &= ~(1 << (sig % 32));
}

int sim_sigset_member(struct sim_sigset_t *sim_sigset, int sig)
{
	if (sig < 1 || sig > MAXSIGNAL)
		return 0;
	sig--;
	return sim_sigset->sig[sig / 32] & (1 << (sig % 32));
}

void sim_sigset_dump(struct sim_sigset_t *sim_sigset, FILE *f)
{
	int i;
	char *comma = "", value[MAX_STRING_SIZE];
	fprintf(f, "{");
	for (i = 1; i <= MAXSIGNAL; i++) {
		if (sim_sigset_member(sim_sigset, i)) {
			map_value_string(&signal_map, i, value);
			fprintf(f, "%s%s", comma, value);
			comma = ", ";
		}
	}
	fprintf(f, "}");
}

void sim_sigset_read(struct mem_t *mem, word addr, struct sim_sigset_t *sim_sigset)
{
	int i;
	for (i = 0; i < MAXSIGNAL / 32; i++)
		READ_WORD(addr + i * 4, &sim_sigset->sig[i]);
}

void sim_sigset_write(struct mem_t *mem, word addr, struct sim_sigset_t *sim_sigset)
{
	int i;
	for (i = 0; i < MAXSIGNAL / 32; i++)
		WRITE_WORD(addr + i * 4, sim_sigset->sig[i]);
}

/*       sa.sa_flags    0    4
       sa.sa_handler    4    4
     sa.sa_sigaction    4    4
          sa.sa_mask    8  128
      sa.sa_restorer  136    4  */

void sim_sigaction_read(struct mem_t *mem, word addr, struct sim_sigaction_t *sim_sigaction)
{
	READ_WORD(addr, &sim_sigaction->flags);
	READ_WORD(addr + 4, &sim_sigaction->handler);
	READ_WORD(addr + 136, &sim_sigaction->restorer);
	sim_sigset_read(mem, addr + 8, &sim_sigaction->mask);
}

void sim_sigaction_write(struct mem_t *mem, word addr, struct sim_sigaction_t *sim_sigaction)
{
	WRITE_WORD(addr, sim_sigaction->flags);
	WRITE_WORD(addr + 4, sim_sigaction->handler);
	WRITE_WORD(addr + 136, sim_sigaction->restorer);
	sim_sigset_write(mem, addr + 8, &sim_sigaction->mask);
}

void sim_sigaction_dump(struct sim_sigaction_t *sim_sigaction, FILE *f)
{
	char flags[100];
	map_flags(&sigactionflags_map, sim_sigaction->flags, flags);
	fprintf(f, "sigaction={\n\tflags=%s\n", flags);
	fprintf(f, "\thandler=0x%x\n", sim_sigaction->handler);
	fprintf(f, "\trestorer=0x%x\n", sim_sigaction->restorer);
	fprintf(f, "\tmask=");
	sim_sigset_dump(&sim_sigaction->mask, f);
	fprintf(f, "\n}\n");
}

void signaldb_init(struct kernel_t *ke)
{
	ke->signaldb = calloc(1, sizeof(struct signaldb_t));
}

void signaldb_done(struct kernel_t *ke)
{
	free(ke->signaldb);
}

static void signaldb_run(struct kernel_t *ke, int ctx, int sig)
{
	struct regs_t oregs, *regs;
	char signame[100];
	
	/* is there an installed handler? */
	if (!ke->signaldb->sim_sigaction[sig - 1].handler) {
		map_value_string(&signal_map, sig, signame);
		ke_backtrace(ke, ctx, stderr);
		fatal("ctx%d: no handler installed for received signal %s",
			ctx, signame);
	}
	debug("ctx%d 0x%x: executing signal %d handler",
		ctx, ke->signaldb->sim_sigaction[sig - 1].handler, sig);
		
	/* initialize execution of signal handler */
	sim_sigset_del(&ke->signaldb->context[ctx].pending, sig);
	regs = KECTX.regs;
	oregs = *KECTX.regs;
	SET_GPR(REGS_A0, sig);
	SET_GPR(REGS_T9, ke->signaldb->sim_sigaction[sig - 1].handler);
	SET_GPR(REGS_RA, 0xffffffff);
	regs->regs_npc = ke->signaldb->sim_sigaction[sig - 1].handler;
	regs->regs_nnpc = regs->regs_npc + 4;
	
	/* run signal handler */
	while (ke_running(ke, ctx) && regs->regs_npc != 0xffffffff)
		ke_execute_inst(ke, ctx);
	
	/* restore old status */
	*KECTX.regs = oregs;
	debug("ctx%d 0x%x: return from signal %d handler",
		ctx, regs->regs_npc, sig);
}

int signaldb_must_process(struct kernel_t *ke, int ctx, int sig)
{
	return sim_sigset_member(&ke->signaldb->context[ctx].pending, sig) &&
		!sim_sigset_member(&ke->signaldb->context[ctx].blocked, sig);
}

void signaldb_process(struct kernel_t *ke)
{
	int ctx, sig;
	FOREACH_CTX(ke) {
		for (sig = 1; sig <= MAXSIGNAL; sig++)
			if (signaldb_must_process(ke, ctx, sig))
				signaldb_run(ke, ctx, sig);
	}
}
