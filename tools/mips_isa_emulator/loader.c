#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

#include "config.h"
#include "mhandle.h"
#include "private.h"

extern char **environ;


/* Private (to Loader) Functions */

static string_map_t sectionflags_map = {
	8, {
		{ "SEC_ALLOC",	SEC_ALLOC },
		{ "SEC_LOAD",	SEC_LOAD },
		{ "SEC_RELOC",	SEC_RELOC },
		{ "SEC_READONLY", SEC_READONLY },
		{ "SEC_CODE", SEC_CODE },
		{ "SEC_DATA", SEC_DATA },
		{ "SEC_ROM", SEC_ROM },
		{ "SEC_HAS_CONTENTS", SEC_HAS_CONTENTS }
	}
};

/* compare two symbols and indicate which one must be located
 * first in symbol table */
static int compare_symbols(const void *ap, const void *bp)
{
	const asymbol *a = * (const asymbol **) ap;
	const asymbol *b = * (const asymbol **) bp;
	const char *an, *bn;
	size_t anl, bnl;
	bfd_boolean af, bf;
	flagword aflags, bflags;

 	if (bfd_asymbol_value(a) > bfd_asymbol_value(b))
		return 1;
	else if (bfd_asymbol_value(a) < bfd_asymbol_value(b))
		return -1;

	if (a->section > b->section)
		return 1;
	else if (a->section < b->section)
		return -1;

	an = bfd_asymbol_name(a);
	bn = bfd_asymbol_name(b);
	anl = strlen(an);
	bnl = strlen(bn);

	af = (strstr(an, "gnu_compiled") != NULL
		|| strstr(an, "gcc2_compiled") != NULL);
	bf = (strstr (bn,"gnu_compiled") != NULL
		|| strstr(bn, "gcc2_compiled") != NULL);

	if (af && !bf)
		return 1;
	if (!af && bf)
		return -1;

#define file_symbol(s, sn, snl)			\
  (((s)->flags & BSF_FILE) != 0			\
   || ((sn)[(snl) - 2] == '.'			\
       && ((sn)[(snl) - 1] == 'o'		\
	   || (sn)[(snl) - 1] == 'a')))

	af = file_symbol(a, an, anl);
	bf = file_symbol(b, bn, bnl);

	if (af && !bf)
		return 1;
	if (! af && bf)
		return -1;

	aflags = a->flags;
	bflags = b->flags;

	if ((aflags & BSF_DEBUGGING) != (bflags & BSF_DEBUGGING)) {
		if ((aflags & BSF_DEBUGGING) != 0)
			return 1;
		else
			return -1;
	}
	if ((aflags & BSF_FUNCTION) != (bflags & BSF_FUNCTION)) {
		if ((aflags & BSF_FUNCTION) != 0)
			return -1;
		else
			return 1;
	}
	if ((aflags & BSF_LOCAL) != (bflags & BSF_LOCAL)) {
		if ((aflags & BSF_LOCAL) != 0)
			return 1;
		else
			return -1;
	}
	if ((aflags & BSF_GLOBAL) != (bflags & BSF_GLOBAL)) {
		if ((aflags & BSF_GLOBAL) != 0)
			return -1;
		else
			return 1;
	}

	if (an[0] == '.' && bn[0] != '.')
		return 1;
	if (an[0] != '.' && bn[0] == '.')
		return -1;

	return strcmp (an, bn);
}


/* Private (to Functional Simulator) Functions */

void ld_init(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	KECTX.ld = calloc(1, sizeof(struct loader_t));
}

void ld_done(struct kernel_t *ke, int ctx)
{
	ke_assert_ctx(ke, ctx);
	free(KECTX.ld);
}

void ld_convert_filename(struct kernel_t *ke, int ctx, char *filename)
{
	char temp[MAX_STRING_SIZE];
	ke_assert_ctx(ke, ctx);
	if (*filename != '/') {
		if (strlen(KECTX.ld->cwd) + strlen(filename) + 2 > MAX_STRING_SIZE)
			fatal("ld_convert_filename: buffer too small");
		strccpy(temp, KECTX.ld->cwd, MAX_STRING_SIZE);
		strccat(temp, "/");
		strccat(temp, filename);
		strcpy(filename, temp);
	}
}

int ld_translate_fd(struct kernel_t *ke, int ctx, int fd)
{
	ke_assert_ctx(ke, ctx);
	if (fd == 1 || fd == 2)
		return KECTX.ld->stdout_fd;
	else if (!fd)
		return KECTX.ld->stdin_fd;
	return fd;
}

void ld_close_prog(struct kernel_t *ke, int ctx)
{
	struct loader_t *ld;
	int i;
	
	ke_assert_ctx(ke, ctx);
	ld = KECTX.ld;
	
	/* free structure */
	if (!bfd_close(ld->abfd))
		fatal("%s: cannot close file", bfd_get_filename(ld->abfd));
	if (ld->stdin_fd)
		close(ld->stdin_fd);
	if (ld->stdout_fd > 2)
		close(ld->stdout_fd);
	for (i = 0; i < ld->argc; i++)
		free(ld->argv[i]);
	free(ld->symtable);
}


/* Public Functions */

char *ld_get_symbol(struct loader_t *ld, word addr, word *offs)
{
	int min, max, mid;
	asymbol	**symtable = ld->symtable;
	int symcount = ld->symcount;
	
	if (!offs)
		return NULL;
	*offs = 0;
	if (!symcount)
		return NULL;
	
	/* binary search */
	min = 0;
	max = symcount;
	while (min + 1 < max) {
		mid = (max + min) / 2;
		
		if (bfd_asymbol_value(symtable[mid]) > addr)
			max = mid;
		else if (bfd_asymbol_value(symtable[mid]) < addr)
			min = mid;
		else {
			min = mid;
			break;
		}
	}
	
	/* go backwards to find appropriate symbol */
	while (min > 0 && bfd_asymbol_value(symtable[min])
		== bfd_asymbol_value(symtable[min - 1]))
		--min;
	*offs = addr - bfd_asymbol_value(ld->symtable[min]);
	return (char *) bfd_asymbol_name(ld->symtable[min]);
}

void ld_debug_call(struct loader_t *ld, struct md_instfld_t *fld, word pc, word npc)
{
	char *symsrc, *symdst;
	word offssrc, offsdst;
	
	if ((fld->flags & F_RET) && fld->rs != REGS_RA)
		return;
	
	symdst = ld_get_symbol(ld, npc, &offsdst);
	symsrc = ld_get_symbol(ld, pc, &offssrc);
		
	if (fld->flags & F_CALL)
		debug("0x%x <%s+0x%x>: calling 0x%x <%s>",
		     pc, symsrc, offssrc, npc, symdst);
	else
		debug("0x%x <%s+0x%x>: return to 0x%x <%s+0x%x>",
		      pc, symsrc, offssrc, npc, symdst, offsdst);
}

void ld_add_arg(struct kernel_t *ke, int ctx, char *arg)
{
	ke_assert_ctx(ke, ctx);
	if (KECTX.ld->argc == MAX_ARGC)
		fatal("too much args");
	KECTX.ld->argv[KECTX.ld->argc++] = strdup(arg);
}

void ld_add_args(struct kernel_t *ke, int ctx, int argc, char **argv)
{
	int i;
	ke_assert_ctx(ke, ctx);
	if (KECTX.ld->argc + argc > MAX_ARGC)
		fatal("too much args");
	for (i = 0; i < argc; i++)
		KECTX.ld->argv[KECTX.ld->argc++] = strdup(argv[i]);
}

void ld_add_cmdline(struct kernel_t *ke, int ctx, char *cmdline)
{
	struct loader_t *ld;
	int wordlen = 0;
	
	ke_assert_ctx(ke, ctx);
	ld = KECTX.ld;
	while (*cmdline) {
		if (cmdline[wordlen] != 32 && cmdline[wordlen]) {
			wordlen++;
			continue;
		}
		if (!wordlen) {
			cmdline++;
			continue;
		}
		
		/* new argument */
		if (ld->argc == MAX_ARGC)
			fatal("too much args");
		ld->argv[ld->argc] = calloc(1, wordlen + 1);
		memcpy(ld->argv[ld->argc], cmdline, wordlen);
		ld->argc++;
		cmdline += wordlen;
		wordlen = 0;
	}
}

void ld_set_cwd(struct kernel_t *ke, int ctx, char *cwd)
{
	ke_assert_ctx(ke, ctx);
	strcpy(KECTX.ld->cwd, cwd);
}

void ld_set_redir(struct kernel_t *ke, int ctx, char *stdin, char *stdout)
{
	ke_assert_ctx(ke, ctx);
	strcpy(KECTX.ld->stdin_file, stdin);
	strcpy(KECTX.ld->stdout_file, stdout);
}

void ld_load_prog(struct kernel_t *ke, int ctx, char *exe)
{
	struct loader_t *ld;
	struct mem_t *mem;
	struct regs_t *regs;
	bfd *abfd;
	asection *sect;
	word sp;
	long storage;
	int i;
	word argv_addr, envp_addr;
	
	/* context */
	ke_assert_ctx(ke, ctx);
	mem = KECTX.mem;
	regs = KECTX.regs;
	ld = KECTX.ld;
	
	ld->stack_base = MD_STACK_BASE;
	sp = (MD_STACK_BASE - MD_MAX_ENVIRON) & ~7;
	ld->stack_size = ld->stack_base - sp;
	
	/* open stdin & stdout, and get cwd */
	ld->stdin_fd = ld->stdin_file[0] ? open(ld->stdin_file, O_RDONLY) : 0;
	ld->stdout_fd = ld->stdout_file[0] ?
		open(ld->stdout_file, O_CREAT | O_APPEND |
		O_TRUNC | O_WRONLY, 0660) : 1;
	if (ld->stdin_fd < 0 || ld->stdout_fd < 0)
		fatal("%s: cannot open stdin or stdout", exe);
	if (!ld->cwd[0])
		getcwd(ld->cwd, MAX_STRING_SIZE);

	/* initial stack ptr */
	ld->environ_base = sp;

	/* load program into memory */
	ld->abfd = abfd = bfd_openr(exe, "default");
	if (!ld->abfd)
		fatal("%s: cannot open file", exe);
	if (!bfd_check_format(ld->abfd, bfd_object)) {
		bfd_close(ld->abfd);
		fatal("%s: not a valid elf file", exe);
	}

	/* file name & endianness */
	strcpy(ld->exe, exe);
	if (!bfd_big_endian(ld->abfd))
		fatal("%s: not big endian", exe);

	/* symbol table */
	if (!(bfd_get_file_flags(ld->abfd) & HAS_SYMS))
	{
		ld->symcount = 0;
		ld->symtable = NULL;
	}
	else
	{
		storage = bfd_get_symtab_upper_bound(ld->abfd);
		ld->symtable = (asymbol **) calloc(storage, 1);
		ld->symcount = bfd_canonicalize_symtab(ld->abfd, ld->symtable);
		if (ld->symcount < 0)
			fatal("loader.c: no symbol table information");
		qsort(ld->symtable, ld->symcount, sizeof(asymbol *), &compare_symbols);
		debug("%s: %d symbols read from symbol table",
		      ld->exe, ld->symcount);
	}

	/* read sections */
	debug("processing %d sections in '%s'...",
		bfd_count_sections(ld->abfd), ld->exe);
	for (sect = ld->abfd->sections; sect; sect = sect->next)
	{
		char flags[200];
		map_flags(&sectionflags_map, bfd_get_section_flags(ld->abfd, sect), flags);
		debug("section '%s'; offs=%08x; size=%u; flags=%s",
			bfd_section_name(ld->abfd, sect),
			(word) bfd_section_vma(ld->abfd, sect),
			(word) bfd_section_size(ld->abfd, sect),
			flags);
		
		/* check if the section is dynamic; in this case, fatal: we do not
		 * support dynamic linking */
		if (!strcmp(bfd_section_name(ld->abfd, sect), ".dynamic"))
			fatal("dynamic linking not supported; compile with '-static' option");
		
		if (bfd_get_section_flags(ld->abfd, sect) & (SEC_ALLOC | SEC_RELOC))
		{
			byte *p;
			
			debug("\tloading section...");
			p = calloc(bfd_section_size(ld->abfd, sect), sizeof(byte));
			if (!bfd_get_section_contents(ld->abfd, sect, p, (file_ptr) 0,
				bfd_section_size(ld->abfd, sect)))
				fatal("cannot read section '%s'",
					bfd_section_name(ld->abfd, sect));

			/* copy program to memory */
			WRITE_BLK(bfd_section_vma(ld->abfd, sect),
				bfd_section_size(ld->abfd, sect), p);
			free(p);
			
			/* if data segment, increase data segment size */
			if (bfd_section_vma(ld->abfd, sect) >= MD_DATA_BASE)
				ld->data_top = MAX(ld->data_top,
					bfd_section_vma(ld->abfd, sect) +
					bfd_section_size(ld->abfd, sect) - 1);
		}
		else if (bfd_get_section_flags(abfd, sect) & SEC_LOAD)
		{
			byte *p = calloc(bfd_section_size(ld->abfd, sect), sizeof(byte));
			WRITE_BLK(bfd_section_vma(ld->abfd, sect),
				bfd_section_size(ld->abfd, sect), p);
			free(p);
		}

		
		/* code section */
		if (!strcmp(bfd_section_name(ld->abfd, sect), ".text"))
			ld->text_size = ((bfd_section_vma(ld->abfd, sect) +
				bfd_section_size(ld->abfd, sect)) - MD_TEXT_BASE);
	}
	
	/* calculate data segment */
	ld->prog_entry = bfd_get_start_address(ld->abfd);
	ld->heap_top = ROUND_UP(ld->data_top, MEM_PAGESIZE);
	
	/* local stack ptr */
	ld->stack_base = MD_STACK_BASE;
	ld->stack_size = MD_MAX_ENVIRON;
	ld->environ_base = MD_STACK_BASE - MD_MAX_ENVIRON;

	/* load arguments and environment vars */
	sp = ld->environ_base;
	WRITE_WORD(sp, ld->argc);
	sp += 4;
	argv_addr = sp;
	sp = sp + (ld->argc + 1) * 4;

	/* save space for environ and null */
	envp_addr = sp;
	//~ for (i = 0; environ[i]; i++)
		//~ sp += 4;
	sp += 4;

	/* argv ptr */
	for (i = 0; i < ld->argc; i++) {
		WRITE_WORD(argv_addr + i * 4, sp);
		WRITE_STR(sp, ld->argv[i]);
		sp += strlen(ld->argv[i]) + 1;
	}
	WRITE_WORD(argv_addr + i * 4, 0);

	/* envp ptr and stack data */
	//~ for (i = 0; environ[i]; i++) {
		//~ mem_write_word(mem, envp_addr + i * 4, sp);
		//~ mem_write_str(mem, sp, environ[i]);
		//~ sp += strlen(environ[i]) + 1;
	//~ }
	i = 0;
	mem_write_word(mem, envp_addr + i * 4, 0);
	if (sp > ld->stack_base)
		fatal("'environ' overflow, increment MD_MAX_ENVIRON");

	/* register initialization */
	KECTX.regs->regs_npc = ld->prog_entry;
	KECTX.regs->regs_nnpc = KECTX.regs->regs_npc + 4;
	KECTX.regs->regs_R[REGS_SP] = ld->environ_base;
}

void ld_load_progs(struct kernel_t *ke, char *ctxconfig)
{
	struct config_t *config;
	int ctx, ctxnum;
	char *exe, *cwd;
	char *in, *out;
	char section[MAX_STRING_SIZE];
	
	/* open context config file */
	config = config_create(ctxconfig);
	if (!config_load(config))
		fatal("%s: cannot open context configuration file",
			ctxconfig);
	
	/* create contexts */
	for (ctxnum = 0; ; ctxnum++) {
	
		/* create new context */
		sprintf(section, "Context %d", ctxnum);
		if (!config_section_exists(config, section))
			break;
		ctx = ke_new_ctx(ke);
		
		/* arguments */
		exe = config_read_string(config, section, "exe", "");
		ld_add_arg(ke, ctx, exe);
		ld_add_cmdline(ke, ctx,
			config_read_string(config, section, "args", ""));
			
		/* current working directory */
		cwd = config_read_string(config, section, "cwd", NULL);
		if (cwd)
			ld_set_cwd(ke, ctx, cwd);
		
		/* stdin & stdout */
		in = config_read_string(config, section, "stdin", "");
		out = config_read_string(config, section, "stdout", "");
		ld_set_redir(ke, ctx, in, out);
			
		/* load program */
		ld_load_prog(ke, ctx, exe);
	}
	config_free(config);
}
