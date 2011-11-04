#ifndef MEMORY_H
#define MEMORY_H

#include <stdio.h>
#include "misc.h"


/* size constants */
#define MEM_PAGESIZE	(1<<12)
#define MEM_LOGPAGESIZE	(12)


/* main struct */
struct mem_t;


/* creation and destruction;
 * the creation can get another memory object (or NULL) if
 * one wants to share memory pages among memories */
struct mem_t *mem_create(struct mem_t *share);
void mem_free(struct mem_t *mem);


/* toggle spec mode status */
void mem_start_spec(struct mem_t *mem);
void mem_recover(struct mem_t *mem);




/* memory protection control */
#define MEM_PROT_READ	0x01
#define MEM_PROT_WRITE	0x02

/* set protection to a memory range; the associated pages will
 * be allocated if they do not already exist */
void mem_protect(struct mem_t *mem, word addr, word size, int prot);

/* try to map memory pages that cover 'size' bytes starting at
 * address 'addr'; if it is not possible, try other higher addresses;
 * return: 0=error; addr=address of allocated space */
word mem_map(struct mem_t *mem, word addr, word size, int prot);

/* remap a region of memory */
word mem_remap(struct mem_t *mem, word oldaddr, word oldsize, word newsize);

/* unmap region */
void mem_unmap(struct mem_t *mem, word addr, word size);




/* memory access functions */
#define MEM_READ	0x01
#define MEM_WRITE	0x02

void mem_bcopy(struct mem_t *mem, word addr, word size, void *buf, int cmd);
void mem_access(struct mem_t *mem, word addr, word size, void *buf, int cmd);

void mem_write_byte(struct mem_t *mem, word addr, byte data);
void mem_write_half(struct mem_t *mem, word addr, half data);
void mem_write_word(struct mem_t *mem, word addr, word data);
void mem_write_dword(struct mem_t *mem, word addr, dword data);
void mem_write_str(struct mem_t *mem, word addr, char *str);
void mem_write_blk(struct mem_t *mem, word addr, word size, void *p);

void mem_read_byte(struct mem_t *mem, word addr, byte *data);
void mem_read_half(struct mem_t *mem, word addr, half *data);
void mem_read_word(struct mem_t *mem, word addr, word *data);
void mem_read_dword(struct mem_t *mem, word addr, dword *data);
void mem_read_str(struct mem_t *mem, word addr, int max_size, char *str);
void mem_read_blk(struct mem_t *mem, word addr, word size, void *p);
	

/* memory access macros */
#define WRITE_BYTE(ADDR, DATA) mem_write_byte(mem, (ADDR), (DATA))
#define WRITE_HALF(ADDR, DATA) mem_write_half(mem, (ADDR), (DATA))
#define WRITE_WORD(ADDR, DATA) mem_write_word(mem, (ADDR), (DATA))
#define WRITE_DWORD(ADDR, DATA) mem_write_dword(mem, (ADDR), (DATA))
#define WRITE_STR(ADDR, STR) mem_write_str(mem, (ADDR), (STR))
#define WRITE_BLK(ADDR, SIZE, BLK) mem_write_blk(mem, (ADDR), (SIZE), (BLK))

#define READ_BYTE(ADDR, DATA) mem_read_byte(mem, (ADDR), (DATA))
#define READ_HALF(ADDR, DATA) mem_read_half(mem, (ADDR), (DATA))
#define READ_WORD(ADDR, DATA) mem_read_word(mem, (ADDR), (DATA))
#define READ_DWORD(ADDR, DATA) mem_read_dword(mem, (ADDR), (DATA))
#define READ_STR(ADDR, SIZE, STR) mem_read_str(mem, (ADDR), (SIZE), (STR))
#define READ_BLK(ADDR, SIZE, BLK) mem_read_blk(mem, (ADDR), (SIZE), (BLK))


#endif
