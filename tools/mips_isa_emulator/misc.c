#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <string.h>
#include <time.h>

#include "mhandle.h"
#include "misc.h"



/* error management */
void fatal(char *fmt, ...) {
	va_list va;
	va_start(va, fmt);
	fprintf(stderr, "fatal: ");
	vfprintf(stderr, fmt, va);
	fprintf(stderr, "\n");
	abort();
}

void panic(char *fmt, ...) {
	va_list va;
	va_start(va, fmt);
	fprintf(stderr, "panic: ");
	vfprintf(stderr, fmt, va);
	fprintf(stderr, "\n");
	abort();
}

void warning(char *fmt, ...) {
	va_list va;
	va_start(va, fmt);
	fprintf(stderr, "warning: ");
	vfprintf(stderr, fmt, va);
	fprintf(stderr, "\n");
}


/* debug */
#define MAX_DEBUG_THREADS	20
static FILE *debug_fd;
static int debug_thread_on;
static FILE *debug_thread_fd[MAX_DEBUG_THREADS];
static char *debug_thread_fname;

void debug_open(char *fname) {
	debug_fd = open_write(fname);
	if (!debug_fd && *fname)
		fatal("cannot open debug file '%s'", fname);
}

void debug_threads(char *fname) {
	debug_thread_on = TRUE;
	debug_thread_fname = fname;
}

void debug_thread(int ctxid, char *fmt, ...)
{
	va_list va;
	char name[100];
	if (!debug_thread_on)
		return;
	if (ctxid < 0 || ctxid >= MAX_DEBUG_THREADS)
		fatal("debug_thread: increase MAX_DEBUG_THREADS");
	va_start(va, fmt);
	if (!debug_thread_fd[ctxid]) {
		sprintf(name, "%s%02d.log", debug_thread_fname, ctxid);
		debug_thread_fd[ctxid] = open_write(name);
		if (!debug_thread_fd[ctxid])
			fatal("%s: cannot open file for debugging", name);
	}
	vfprintf(debug_thread_fd[ctxid], fmt, va);
	fprintf(debug_thread_fd[ctxid], "\n");
}

void debug_close()
{
	int i;
	if (debug_fd && debug_fd != stdout && debug_fd != stderr)
		fclose(debug_fd);
	for (i = 0; i < MAX_DEBUG_THREADS; i++)
		if (debug_thread_fd[i] && debug_thread_fd[i] != stdout
		&& debug_thread_fd[i] != stderr)
			fclose(debug_thread_fd[i]);
	debug_thread_on = 0;
	debug_fd = NULL;
}


void debug(char *fmt, ...)
{
	va_list va;
	int i;
	if (!debug_fd)
		return;
	va_start(va, fmt);
	vfprintf(debug_fd, fmt, va);
	fprintf(debug_fd, "\n");
	if (debug_thread_on) {
		for (i = 0; i < MAX_DEBUG_THREADS; i++) {
			if (debug_thread_fd[i]) {
				vfprintf(debug_thread_fd[i], fmt, va);
				fprintf(debug_thread_fd[i], "\n");
			}
		}
	}
}


int debugging() {
	return debug_fd != NULL;
}



/* numeric functions */
int log_base2(dword x) {
	int res = 0, value = x;
	if (!value)
		fatal("base 2 logarithm of 0");
	while (!(value & 1)) {
		value >>= 1;
		res++;
	}
	if (value != 1)
		fatal("base 2 logarithm of something that is not power of 2 (%llx)", x);
	return res;
}



/* open file, choosing from "stdout", "stderr" or <name> */
FILE *open_read(char *fname)
{
	if (!fname[0])
		return NULL;
	if (!strcmp(fname, "stdout"))
		return stdout;
	else if (!strcmp(fname, "stderr"))
		return stderr;
	else
		return fopen(fname, "rt");
}


FILE *open_write(char *fname)
{
	if (!fname[0])
		return NULL;
	if (!strcmp(fname, "stdout"))
		return stdout;
	else if (!strcmp(fname, "stderr"))
		return stderr;
	else
		return fopen(fname, "wt");
}


/* read a line from a text file, deleting final '\n';
 * if eof, return -1; else return length of string */
int read_line(FILE *f, char *line, int size)
{
	if (!f)
		return -1;
	fgets(line, size, f);
	if (feof(f))
		return -1;
	while (strlen(line) && (line[strlen(line) - 1] == 13 ||
		line[strlen(line) - 1] == 10))
		line[strlen(line) - 1] = 0;
	return strlen(line);
}


void close_file(FILE *f)
{
	if (f && f != stdout && f != stderr)
		fclose(f);
}


/* dump memory contents, printing a dot for unprintable chars */
void dump_ptr(byte *ptr, int size, FILE *stream)
{
	int i, j, val;
	for (i = 0; i < size; i++, ptr++) {
		for (j = 0; j < 2; j++) {
			val = j ? *ptr & 0xf : *ptr >> 4;
			if (val < 10)
				fprintf(stream, "%d", val);
			else
				fprintf(stream, "%c", val - 10 + 'a');
		}
		fprintf(stream, " ");
	}
}


static dword tcurrent;
void tstart()
{
	tcurrent = clock();
}

dword tbarrier()
{
	dword now = clock();
	dword ellapsed = now - tcurrent;
	tcurrent = now;
	return ellapsed;
}


/* string mapping functions */
int map_string(string_map_t *map, char *string)
{
	int i;
	for (i = 0; i < map->count; i++)
		if (!strcmp(string, map->map[i].string))
			return map->map[i].value;
	return 0;
}


static char *unknown = "<unknown>";
char *map_value(string_map_t *map, int value)
{
	int i;
	for (i = 0; i < map->count; i++)
		if (map->map[i].value == value)
			return map->map[i].string;
	return unknown;
}


void map_value_string(string_map_t *map, int value, char *out)
{
	char *s = map_value(map, value);
	strncpy(out, s, MAX_STRING_SIZE - 1);
	if (!strcmp(s, unknown))
		sprintf(out, "%d", value);
}


void map_flags(string_map_t *map, word flags, char *out)
{
	int i;
	char *comma = "", temp[MAX_STRING_SIZE];

	strccpy(out, "{", MAX_STRING_SIZE);
	for (i = 0; i < 32; i++) {
		if (flags & (1U << i)) {
			strccat(out, comma);
			map_value_string(map, 1U << i, temp);
			strccat(out, temp);
			comma = "|";
		}
	}
	strccat(out, "}");
}



/* strings */
static int memoryleft;

void strccat(char *dest, char *src)
{
	int destlen = strlen(dest);
	int srclen = strlen(src);
	if (memoryleft <= 1)
		return;
	srclen = MIN(srclen, memoryleft - 1);
	memcpy(dest + destlen, src, srclen);
	dest[destlen + srclen] = 0;
	memoryleft -= srclen;
}


void strccpy(char *dest, char *src, int size)
{
	int srclen = strlen(src);
	memoryleft = size;
	if (memoryleft <= 1)
		return;
	srclen = MIN(srclen, memoryleft - 1);
	memcpy(dest, src, srclen);
	dest[srclen] = 0;
	memoryleft -= srclen;
}


void strdump(char *dest, char *src, int size)
{
	int i;
	for (i = 0; i < size - 1 && *src; i++) {
		*dest = *src > 31 ? *src : '.';
		src++, dest++;
	}
	*dest = 0;
}
