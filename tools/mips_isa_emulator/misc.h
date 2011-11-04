#ifndef MISC_H
#define MISC_H

#include <stdint.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>


/* boolean values */
#define TRUE				1
#define FALSE				0

/* max string size */
#define MAX_STRING_SIZE			200

/* min max bool */
#define MIN(X, Y) ((X)<(Y)?(X):(Y))
#define MAX(X, Y) ((X)>(Y)?(X):(Y))
#define BOOL(X) ((X) ? 't' : 'f')

/* round */
#define ROUND_UP(N,ALIGN)	(((N) + ((ALIGN)-1)) & ~((ALIGN)-1))
#define ROUND_DOWN(N,ALIGN)	((N) & ~((ALIGN)-1))

/* alignment */
#define DWORD_ALIGN(N) ROUND_DOWN((N),8)
#define WORD_ALIGN(N) ROUND_DOWN((N), 4)
#define HALF_ALIGN(N) ROUND_DOWN((N), 2)

/* modulo */
#define MOD(X, Y)		(((X) + (Y)) % (Y))

/* endian control */
#define SWAPH(X)	(((((half)(X)) & 0xff) << 8) | \
			((((half)(X)) & 0xff00) >> 8))
#define SWAPW(X)	((((word)(X)) << 24) |			\
			((((word)(X)) << 8)  & 0x00ff0000) |		\
			((((word)(X)) >> 8)  & 0x0000ff00) |		\
			((((word)(X)) >> 24) & 0x000000ff))
#define SWAPDW(X)	((((dword)(X)) << 56) |				\
			((((dword)(X)) << 40) & 0x00ff000000000000ULL) |	\
			((((dword)(X)) << 24) & 0x0000ff0000000000ULL) |	\
			((((dword)(X)) << 8)  & 0x000000ff00000000ULL) |	\
			((((dword)(X)) >> 8)  & 0x00000000ff000000ULL) |	\
			((((dword)(X)) >> 24) & 0x0000000000ff0000ULL) |	\
			((((dword)(X)) >> 40) & 0x000000000000ff00ULL) |	\
			((((dword)(X)) >> 56) & 0x00000000000000ffULL))


/* sign extend */
#define SEXT32(X, B)		(((word)(X))&(1U<<(B-1))?((word)(X))|~((1U<<B)-1):(X))
#define SEXT64(X, B)		(((dword)(X))&(1ULL<<(B-1))?((dword)(X))|~((1ULL<<B)-1):(X))

/* extract bits from HI to LO from X */
#define BITS32(X, HI, LO)	((((word)(X))>>(LO))&((1U<<((HI)-(LO)+1))-1))
#define BITS64(X, HI, LO)	((((dword)(X))>>(LO))&((1ULL<<((HI)-(LO)+1ULL))-1ULL))

/* bits */
#define GETBIT32(X, B)		((word)(X)&(1U<<(B)))
#define GETBIT64(X, B)		((dword)(X)&(1ULL<<(B)))
#define SETBIT32(X, B)		((word)(X)|(1U<<(B)))
#define SETBIT64(X, B)		((dword)(X)|(1ULL<<(B)))
#define CLEARBIT32(X, B)	((word)(X)&(~(1U<<(B))))
#define CLEARBIT64(X, B)	((dword)(X)&(~(1ULL<<(B))))
#define SETBITVALUE32(X, B, V)	((V) ? SETBIT32((X),(B)) : CLEARBIT32((X),(B)))
#define SETBITVALUE64(X, B, V)	((V) ? SETBIT64((X),(B)) : CLEARBIT64((X),(B)))

/* bitmaps */
#define BITMAP_TYPE(NAME, SIZE) \
	byte NAME[((SIZE)+7)>>3]
#define BITMAP_INIT(NAME, SIZE) { \
	int irg; \
	for (irg = 0; irg < (((SIZE)+7)>>3); irg++) \
	NAME[irg] = 0; }
#define BITMAP_SET(NAME, BIT) \
	(NAME[(BIT)>>3]|=1<<((BIT)&7))
#define BITMAP_CLEAR(NAME, BIT) \
	(NAME[(BIT)>>3]&=~(1<<((BIT)&7)))
#define BITMAP_IS_SET(NAME, BIT) \
	(NAME[(BIT)>>3]&(1<<((BIT)&7)))
#define BITMAP_SET_RANGE(NAME, LO, HI) { \
	int irg; \
	for (irg = (LO); irg <= (HI); irg++) \
	BITMAP_SET((NAME), irg); }
#define BITMAP_CLEAR_RANGE(NAME, LO, HI) { \
	int irg; \
	for (irg = (LO); irg <= (HI); irg++) \
	BITMAP_CLEAR((NAME), irg); }

/* types */
typedef uint8_t		byte;
typedef uint16_t	half;
typedef uint32_t	word;
typedef uint64_t	dword;

typedef int8_t		sbyte;
typedef int16_t		shalf;
typedef int32_t		sword;
typedef int64_t		sdword;


/* string maps;
 * output string must be declared as:
 *   string: char[MAX_STRING_SIZE]; */
typedef struct {
	int count;
	struct {
		char *string;
		int value;
	} map[];
} string_map_t;
int map_string(string_map_t *map, char *string);
char *map_value(string_map_t *map, int value);
void map_value_string(string_map_t *map, int value, char *out);
void map_flags(string_map_t *map, word flags, char *out);

/* error management */
void fatal(char *fmt, ...);
void panic(char *fmt, ...);
void warning(char *fmt, ...);

/* strings */
void strccpy(char *dest, char *src, int size);
void strccat(char *dest, char *src);
void strdump(char *dest, char *src, int size);

/* debug */
void debug_open(char *fname);
void debug_close();
void debug(char *fmt, ...);
void debug_threads(char *fname);
void debug_thread(int ctxid, char *fmt, ...);
int debugging();

/* open/close file ("stdout", "stderr" or <name>) */
FILE *open_read(char *fname);
FILE *open_write(char *fname);
int read_line(FILE *f, char *line, int size);
void close_file(FILE *f);

/* time */
void tstart();
dword tbarrier();

/* other */
void dump_ptr(byte *ptr, int size, FILE *stream);
int log_base2(dword x);

#endif

