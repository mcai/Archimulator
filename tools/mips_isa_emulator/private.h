#ifndef PRIVATE_H
#define PRIVATE_H

#include <bfd.h>
#include <time.h>

#include "misc.h"
#include "public.h"


/* System Events */

#define SYSEVENT_READ		0
#define SYSEVEYT_WRITE		1
#define SYSEVENT_RESUME		2
#define SYSEVENT_WAIT		3
#define SYSEVENT_POLL		4
#define SYSEVENT_SIGSUSPEND	5

struct sysevent_t {

	struct	sysevent_t *next;
	
	int	ctx;
	int	kind;
	
	struct {
		int active, occurred;
		clock_t when;
	} for_time;
	
	struct {
		int active, occurred;
		int pid;
	} for_waitpid;
	
	struct {
		int active, occurred;
		struct buffer_t *buffer;
		word addr, size;	/* for read */
		word pufds;		/* for poll */
	} for_waitfd;
	
	struct {
		int active, occurred;
	} for_signal;
};

struct sysevent_t *sysevent_create(int ctx, int kind);
void sysevent_free(struct sysevent_t *e);
void sysevent_free_all();


/* System Events Queue */

struct syseventq_t;
void syseventq_init(struct kernel_t *ke);
void syseventq_done(struct kernel_t *ke);
void syseventq_sched(struct kernel_t *ke, struct sysevent_t *e);
void syseventq_process(struct kernel_t *ke);


/* Pipe Data Base */

struct pipedb_t;
void pipedb_init(struct kernel_t *ke);
void pipedb_done(struct kernel_t *ke);
void pipedb_pipe(struct kernel_t *ke, int *fd);
void pipedb_close_fd(struct kernel_t *ke, int fd);
int pipedb_pipe_fd(int fd); /* true if fd in pipe range (although not valid) */
struct buffer_t *pipedb_write_buffer(struct kernel_t *ke, int fd);
struct buffer_t *pipedb_read_buffer(struct kernel_t *ke, int fd);


/* Signal Data Base */

#define MAXSIGNAL 64
struct sim_sigset_t {
	word sig[MAXSIGNAL / 32];
};

void sim_sigset_add(struct sim_sigset_t *sim_sigset, int sig);
void sim_sigset_del(struct sim_sigset_t *sim_sigset, int sig);
int sim_sigset_member(struct sim_sigset_t *sim_sigset, int sig);
void sim_sigset_dump(struct sim_sigset_t *sim_sigset, FILE *f);
void sim_sigset_read(struct mem_t *mem, word addr, struct sim_sigset_t *sim_sigset);
void sim_sigset_write(struct mem_t *mem, word addr, struct sim_sigset_t *sim_sigset);

struct sim_sigaction_t {
	word flags;
	word handler;
	word restorer;
	struct sim_sigset_t mask;
};

void sim_sigaction_read(struct mem_t *mem, word addr, struct sim_sigaction_t *sim_sigaction);
void sim_sigaction_write(struct mem_t *mem, word addr, struct sim_sigaction_t *sim_sigaction);
void sim_sigaction_dump(struct sim_sigaction_t *sim_sigaction, FILE *f);

struct signaldb_t {
	struct sim_sigaction_t sim_sigaction[MAXSIGNAL];
	struct {
		struct sim_sigset_t blocked;
		struct sim_sigset_t backup;
		struct sim_sigset_t pending;
	} context[KE_MAXCTX];
};

void signaldb_init(struct kernel_t *ke);
void signaldb_done(struct kernel_t *ke);
int signaldb_must_process(struct kernel_t *ke, int ctx, int sig);
void signaldb_process(struct kernel_t *ke);


/* System Calls */

void sim_syscall(struct kernel_t *ke, int ctx);
char *get_syscall_name(int syscode);


/* Loader */

#define MAX_ARGC	50

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
	asymbol	**symtable;
	int	symcount;
};

void ld_init(struct kernel_t *ke, int ctx);
void ld_done(struct kernel_t *ke, int ctx);
void ld_close_prog(struct kernel_t *ke, int ctx);
void ld_convert_filename(struct kernel_t *ke, int ctx, char *filename);
int ld_translate_fd(struct kernel_t *ke, int ctx, int fd);
char *ld_get_symbol(struct loader_t *ld, word addr, word *offs);


/* Functional Simulator Kernel */

/* status flags */
#define KE_ALIVE		0x01
#define KE_RUNNING		0x02
#define KE_SPECMODE		0x04
#define KE_SUSPENDED		0x08
#define KE_FINISHED		0x10
#define KE_EXCL			0x20
#define KE_LOCKED		0x40

#define KECTXI(I)		(ke->context[I])
#define KECTX			(KECTXI(ctx))
#define REGSI(I)		(*KECTXI(I).regs)
#define REGS			(REGSI(ctx))
#define MEM			(*KECTX.mem)

#define STATUS(X)		(KECTX.status & (X))
#define STATUSI(I, X)		(KECTXI(I).status & (X))
#define SETSTATUS(X)		(KECTX.status |= (X))
#define SETSTATUSI(I, X)	(KECTXI(I).status |= (X))
#define CLRSTATUS(X)		(KECTX.status &= ~(X))
#define CLRSTATUSI(I, X)	(KECTXI(I).status &= ~(X))

#define MAX_FNDEPTH		100

struct kernel_t {

	sdword	cycle;
	dword	notnops;
	int	alive_count;
	int	active_count;
	int	rmw_sequence;
	int	current_memid;
	
	/* structs */
	struct	syseventq_t *syseventq;
	struct	pipedb_t *pipedb;
	struct	signaldb_t *signaldb;
	
	/* for context enumeration */
	int	first_ctx;
	int	current_ctx;
	int	ring_first, ring_current;
	
	/* pid assignment */
	int	current_pid;
	int	zombie[KE_MAXCTX];
	int	zombie_count;
	
	/* per context data */
	struct {
	
		/* loader info */
		struct	loader_t *ld;
	
		/* context status */
		int	status;
		int	fndepth;
		word	backtrace[MAX_FNDEPTH];
		int	pid;
		int	memid;			/* unique identifier of memory */
		int	child_count;
		struct	regs_t *regs;		/* register file */
		struct	regs_t *rec_regs;	/* recover register file */
		struct	mem_t *mem;		/* memory */
		struct	md_instfld_t instfld;	/* last decoded inst */
		word	effaddr;		/* last memory effective address */
		
		/* signal to send to parent when finished */
		int	sigfinish;
		
		/* pointers to alive contexts */
		int	parent;
		int	prev;
		int	next;
		
	} context[KE_MAXCTX];
};

/* context clonation */
int ke_clone_ctx(struct kernel_t *ke, int ctx, int sigfinish);

/* pids */
int ke_get_pid(struct kernel_t *ke, int ctx);
int ke_get_ppid(struct kernel_t *ke, int ctx);
int ke_pid_ctx(struct kernel_t *ke, int pid);		/* return ctx of a pid or -1 */
int ke_child_count(struct kernel_t *ke, int ctx);
int ke_is_zombie(struct kernel_t *ke, int pid);
int ke_zombie_count(struct kernel_t *ke);
int ke_kill_zombie(struct kernel_t *ke, int pid);	/* pid can be -1; return zombie pid */

/* change context status */
void ke_suspend_ctx(struct kernel_t *ke, int ctx);
void ke_resume_ctx(struct kernel_t *ke, int ctx);
void ke_set_running(struct kernel_t *ke, int ctx);	/* update KE_RUNNING status */

#endif
