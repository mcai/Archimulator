#include <signal.h>

#include "mhandle.h"
#include "private.h"
#include "public.h"

struct kernel_t *ke;
int sigint = 0;

void handler(int signum)
{
	switch (signum) {
		case SIGUSR1:
			ke_dump_stats(ke, 1);
			break;
		case SIGUSR2:
			ke_dump_stats(ke, 2);
			break;
		case SIGINT:
			sigint = 1;
			signal(SIGINT, SIG_DFL);
			break;
	}
}


int double_is_nan(double val)
{
	dword temp = * (dword *) &val;
	return (temp & 0x7ff0000000000000ULL) == 0x7ff0000000000000ULL &&
		(temp & 0x000fffffffffffffULL) != 0x0000000000000000;
}


int float_is_nan(float val)
{
	word temp = * (word *) &val;
	return (temp & 0x7f800000) == 0x7f800000 &&
		(temp & 0x007fffff) != 0x00000000;
}


int double_is_inf(double val)
{
	dword temp = * (dword *) &val;
	return (temp & 0x7ff0000000000000ULL) == 0x7ff0000000000000ULL &&
		(temp & 0x000fffffffffffffULL) == 0x0000000000000000;
}


int float_is_inf(float val)
{
	word temp = * (word *) &val;
	return (temp & 0x7f800000) == 0x7f800000 &&
		(temp & 0x007fffff) == 0x00000000;
}

int get_active_context_count() {
    return ke_active_count(ke);
}

void execute_inst() {
    if (ke_running(ke, 0)) {
        ke_execute_inst(ke, 0);
    }
}

void advance_one_cycle() {
    ke_new_cycle(ke);
}

void single_thread_program_init(int argc, char **argv)
{
	/* create kernel */
	ke = ke_create();

	ke_new_ctx(ke);
	ld_add_args(ke, 0, argc - 1, argv + 1);
	ld_load_prog(ke, 0, argv[1]);

	/* signal handlers */
	signal(SIGUSR1, handler);
	signal(SIGUSR2, handler);
	signal(SIGINT, handler);
}

void single_thread_program_dump_regs() {
    regs_dump(KECTXI(0).regs, stdout);
}

int single_thread_program_get_gpr(int index) {
    return KECTXI(0).regs->regs_R[index];
}

float single_thread_program_get_fprs(int index) {
    return KECTXI(0).regs->regs_F.s[index];
}

double single_thread_program_get_fprd(int index) {
    return KECTXI(0).regs->regs_F.d[index/2];
}

int single_thread_program_get_hi() {
    return KECTXI(0).regs->regs_HI;
}

int single_thread_program_get_lo() {
    return KECTXI(0).regs->regs_LO;
}

int single_thread_program_get_fir() {
    return KECTXI(0).regs->regs_C.FIR;
}

int single_thread_program_get_fcsr() {
    return KECTXI(0).regs->regs_C.FCSR;
}

int single_thread_program_get_pc()
{
    return KECTXI(0).regs->regs_pc;
}

int single_thread_program_get_npc()
{
    return KECTXI(0).regs->regs_npc;
}

int single_thread_program_get_nnpc()
{
    return KECTXI(0).regs->regs_nnpc;
}

int single_thread_program_execute_next_instruction()
{
	/* run */
	if (get_active_context_count() > 0) {
        execute_inst();
		advance_one_cycle();

		return 1;
	}
	else {
	    return 0;
	}
}

void single_thread_program_quit()
{
	/* finalization */
	ke_free(ke);
	mhandle_done();
}

extern int num_mem_pages;

void run_program(int argc, char **argv)
{
	long long total_insts = 0;
	
	int ctx;
	
	/* create kernel */
	ke = ke_create();
	
	ctx = ke_new_ctx(ke);
	ld_add_args(ke, ctx, argc - 1, argv + 1);
	ld_load_prog(ke, ctx, argv[1]);
	
	/* signal handlers */
	signal(SIGUSR1, handler);
	signal(SIGUSR2, handler);
	signal(SIGINT, handler);
	
	/* run */
	while (ke_active_count(ke)) {
		FOREACH_RUNNING_CTX(ke) {
			ke_execute_inst(ke, ctx);
			total_insts++;
			
			if(total_insts % 1000000000 == 0) {
				printf("<mips_isa_emulator> insts executed: %lld, num_mem_pages: %d\n", total_insts, num_mem_pages);
			}
		}
		ke_new_cycle(ke);
	}

	/* finalization */
	ke_free(ke);
	mhandle_done();
	
	printf("<mips_isa_emulator> total insts: %lld\n", total_insts);
}

void main(int argc, char **argv)
{
	run_program(argc, argv);
}

