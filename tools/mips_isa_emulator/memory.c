#include <assert.h>

#include "mhandle.h"
#include "memory.h"
#include "misc.h"


#define MEM_PAGE_COUNT		1024
#define MEM_SPECBLK_COUNT	1024

#define FREE_SPECBLK_COUNT	10
#define MEM_SPECBLK_LOGSIZE	8
#define MEM_SPECBLK_SIZE	(1<<MEM_SPECBLK_LOGSIZE)


struct mem_specblk_t {
	word	tag;
	struct	mem_specblk_t *next;
	byte	data[MEM_SPECBLK_SIZE];
};


struct mem_page_t {
	word	tag;
	int	prot;
	struct	mem_page_t *next;
	byte	data[MEM_PAGESIZE];
};


struct mem_pages_t {
	struct	mem_page_t *page[MEM_PAGE_COUNT];
	int	links;		/* no of memories sharing pages */
};


struct mem_t {
	struct	mem_pages_t *pages;
	struct	mem_specblk_t *specblk[MEM_SPECBLK_COUNT];
	int	spec_mode;
};




/*
 * PRIVATE FUNCTIONS
 */
 
/* list of free spec blocks */
static struct mem_specblk_t *free_specblks = NULL;


/* return memory page corresponding to a page tag;
 * if page exists, place it in list head */
static struct mem_page_t *mem_page_get(struct mem_t *mem, word tag)
{
	word idx = tag % MEM_PAGE_COUNT;
	struct mem_page_t *page = mem->pages->page[idx], *prev = NULL;
	
	/* look for page */
	while (page && page->tag != tag) {
		prev = page;
		page = page->next;
	}
	
	/* place page into list head */
	if (prev && page) {
		prev->next = page->next;
		page->next = mem->pages->page[idx];
		mem->pages->page[idx] = page;
	}
	
	/* return found page */
	return page;
}

int num_mem_pages = 0;

/* create new memory page */
static struct mem_page_t *mem_page_create(struct mem_t *mem, word tag, int prot)
{
	word	idx = tag % MEM_PAGE_COUNT;
	struct	mem_page_t *page;
	
	/* create new page */
	page = calloc(1, sizeof(struct mem_page_t));
	page->tag = tag;
	page->prot = prot;
	
	/* insert in pages hash table */
	page->next = mem->pages->page[idx];
	mem->pages->page[idx] = page;
	
	num_mem_pages++;
	
	/* return created page */
	return page;
}


/* free memory pages */
static void mem_page_free(struct mem_t *mem, word tag)
{
	word	idx = tag % MEM_PAGE_COUNT;
	struct	mem_page_t *page = mem->pages->page[idx], *prev = NULL;
	
	/* find page */
	while (page && page->tag != tag) {
		prev = page;
		page = page->next;
	}
	if (!page)
		return;
	
	/* free page */
	if (prev)
		prev->next = page->next;
	else
		mem->pages->page[idx] = page->next;
	free(page);
	
	num_mem_pages--;
}


/* copy contents of pages */
static void mem_copy_pages(struct mem_t *mem, word tagdst, word tagsrc, int numpages)
{
	struct mem_page_t *pagedst, *pagesrc;
	int i;
	for (i = 0; i < numpages; i++) {
		pagedst = mem_page_get(mem, tagdst + i);
		pagesrc = mem_page_get(mem, tagsrc + i);
		if (!pagedst || !pagesrc)
			panic("mem_copy_pages: !pagedst || !pagesrc");
		memcpy(&pagedst->data, &pagesrc->data, MEM_PAGESIZE);
	}
}


#define ERR_ALIGN	1
#define ERR_ACCESS	2
#define ERR_READ	3
#define ERR_WRITE	4

static int mem_nonspec_access(struct mem_t *mem, word addr, word size, void *buf, int cmd)
{
	struct mem_page_t *page;
	word tag, offs;

	/* check alignment */
	tag = addr >> MEM_LOGPAGESIZE;
	offs = addr & (MEM_PAGESIZE - 1);
	if (size & (size - 1) || addr & (size - 1))
		return ERR_ALIGN;
		
	/* if page is not allocated, segment fault */
	page = mem_page_get(mem, tag);
	/*if (!page)
		return ERR_ACCESS;*/
	if (!page)
		page = mem_page_create(mem, tag, MEM_PROT_READ | MEM_PROT_WRITE);
	
	/* read/write into memory */
	if (cmd == MEM_READ) {
		if (!(page->prot & MEM_PROT_READ))
			return ERR_READ;
		memcpy(buf, page->data + offs, size);
	} else {
		if (!(page->prot & MEM_PROT_WRITE))
			return ERR_WRITE;
		memcpy(page->data + offs, buf, size);
	}
	
	/* success */
	return 0;
}


/* speculative memory access */
static void mem_spec_access(struct mem_t *mem, word addr, word size, void *buf, int cmd)
{
	word tag = addr >> MEM_SPECBLK_LOGSIZE;
	word offs = addr & (MEM_SPECBLK_SIZE - 1);
	word idx = tag % MEM_SPECBLK_COUNT;
	struct mem_specblk_t *blk, *prev = NULL;
	
	/* look for specblk */
	blk = mem->specblk[idx];
	while (blk && blk->tag != tag) {
		prev = blk;
		blk = blk->next;
	}
	
	/* put block into head of colision list */
	if (blk && prev) {
		prev->next = blk->next;
		blk->next = mem->specblk[idx];
		mem->specblk[idx] = blk;
	}
	
	/* if no blk associated, create a new one or get it from free_specblk list */
	if (!blk) {
		
		/* get/create blk */
		if (free_specblks) {
			blk = free_specblks;
			free_specblks = blk->next;
		} else
			blk = calloc(1, sizeof(struct mem_specblk_t));
		
		/* initialize blk */
		blk->tag = tag;
		blk->next = mem->specblk[idx];
		mem->specblk[idx] = blk;
		mem_nonspec_access(mem, addr & ~(MEM_SPECBLK_SIZE - 1),
			MEM_SPECBLK_SIZE, blk->data, MEM_READ);
	}
	
	/* check size & align */
	if (size & (size - 1) || size > MEM_SPECBLK_SIZE || addr & (size - 1))
		return;

	/* carry out memory operation */
	if (cmd == MEM_READ)
		memcpy(buf, blk->data + offs, size);
	else
		memcpy(blk->data + offs, buf, size);
}




/*
 * PUBLIC FUNCTIONS
 */

/* creation and destruction */
struct mem_t *mem_create(struct mem_t *share)
{
	struct mem_t *mem;
	mem = calloc(1, sizeof(struct mem_t));
	
	/* memory pages */
	mem->pages = share ? share->pages :
		calloc(1, sizeof(struct mem_pages_t));
	++mem->pages->links;
	
	return mem;
}


void mem_free(struct mem_t *mem)
{
	struct mem_page_t *page, *nextpage;
	struct mem_specblk_t *blk;
	int i;
	
	/* recover */
	mem_recover(mem);
	
	/* free pages */
	--mem->pages->links;
	if (!mem->pages->links) {
		for (i = 0; i < MEM_PAGE_COUNT; i++) {
			page = mem->pages->page[i];
			while (page) {
				nextpage = page->next;
				free(page);
				page = nextpage;
			}
		}
		free(mem->pages);
	}
	
	/* free blocks in free_specblks list */
	while (free_specblks) {
		blk = free_specblks;
		free_specblks = blk->next;
		free(blk);
	}
	
	/* free memory */
	mem_recover(mem);
	free(mem);
}


/* memory protection control */
void mem_protect(struct mem_t *mem, word addr, word size, int prot)
{
	word tag, tag_start, tag_end, page_count;
	struct mem_page_t *page;
	
	/* page align */
	if (addr & (MEM_PAGESIZE - 1) || size & (MEM_PAGESIZE - 1))
		fatal("wrong alignment in memory protection");
	
	/* calculate space bounds */
	tag_start = addr >> MEM_LOGPAGESIZE;
	tag_end = (addr + size - 1) >> MEM_LOGPAGESIZE;
	page_count = tag_end - tag_start + 1;
	
	/* set protection */
	for (tag = tag_start; tag <= tag_end; tag++) {
		page = mem_page_get(mem, tag);
		if (!page)
			page = mem_page_create(mem, tag, prot);
		page->prot = prot;
	}
}


word mem_map(struct mem_t *mem, word addr, word size, int prot)
{
	word tag, tag_start, tag_end, page_count;
	
	/* we cannot map in specmode */
	if (mem->spec_mode)
		fatal("mem_map: cannot map in spec mode");

	/* page align */
	if (addr & (MEM_PAGESIZE - 1) || size & (MEM_PAGESIZE - 1))
		fatal("wrong alignment in memory mapping");
	
	/* find pages */
	tag_start = tag_end = addr >> MEM_LOGPAGESIZE;
	page_count = ((addr + size - 1) >> MEM_LOGPAGESIZE) - tag_start + 1;
	for (;;) {
		/* address space overflow */
		if (!tag_end)
			return (word) -1;
		
		/* not enough free pages in current region */
		if (mem_page_get(mem, tag_end)) {
			tag_end++;
			tag_start = tag_end;
			continue;
		}
		
		/* enough free pages */
		if (tag_end - tag_start + 1 == page_count)
			break;
		
		/* we have a new free page */
		tag_end++;
	}
	
	/* allocate pages */
	for (tag = tag_start; tag <= tag_end; tag++) {
		assert(!mem_page_get(mem, tag));
		mem_page_create(mem, tag, prot);
	}
	
	/* return allocated region address */
	return tag_start << MEM_LOGPAGESIZE;
}


/* In order to remap, we first map the new memory pages, next we copy the contents of
 * the previous pages, and then we free the previous pages */
word mem_remap(struct mem_t *mem, word oldaddr, word oldsize, word newsize)
{
	struct mem_page_t *page;
	int prot, numpages;
	word start;

	/* we cannot remap in specmode */
	if (mem->spec_mode)
		fatal("mem_remap: cannot remap in spec mode");

	/* page align */
	if (oldaddr & (MEM_PAGESIZE - 1) || oldsize & (MEM_PAGESIZE - 1) ||
		newsize & (MEM_PAGESIZE - 1))
		fatal("mem_remap: wrong alignment");
	
	/* map new pages */
	page = mem_page_get(mem, oldaddr >> MEM_LOGPAGESIZE);
	prot = page ? page->prot : MEM_PROT_READ|MEM_PROT_WRITE;
	start = mem_map(mem, 0, newsize, prot);
	if (start == (word) -1)
		return start;
	
	/* copy contents to new pages */
	numpages = MIN(oldsize, newsize) >> MEM_LOGPAGESIZE;
	mem_copy_pages(mem, start >> MEM_LOGPAGESIZE,
		oldaddr >> MEM_LOGPAGESIZE, numpages);

	/* unmap old pages */
	mem_unmap(mem, oldaddr, oldsize);
	return start;
}


void mem_unmap(struct mem_t *mem, word addr, word size)
{
	word tag, tag_start, tag_end;
	
	/* we cannot unmap in specmode */
	if (mem->spec_mode)
		fatal("mem_unmap: cannot unmap in spec mode");

	/* page align */
	if (addr & (MEM_PAGESIZE - 1) || size & (MEM_PAGESIZE - 1))
		fatal("wrong alignment in memory unmapping");
	
	/* extract affected pages */
	tag_start = addr >> MEM_LOGPAGESIZE;
	tag_end = (addr + size - 1) >> MEM_LOGPAGESIZE;
	
	/* free pages */
	for (tag = tag_start; tag <= tag_end; tag++)
		mem_page_free(mem, tag);
}


void mem_start_spec(struct mem_t *mem)
{
	mem->spec_mode = TRUE;
}


void mem_recover(struct mem_t *mem)
{
	word i;
	struct mem_specblk_t *blk;
	
	/* send specblks to free_specblks list */
	for (i = 0; i < MEM_SPECBLK_COUNT; i++) {
		while (mem->specblk[i]) {
			blk = mem->specblk[i];
			mem->specblk[i] = blk->next;
			blk->next = free_specblks;
			free_specblks = blk;
		}
	}
	
	/* leave spec mode */
	mem->spec_mode = FALSE;
}


void mem_access(struct mem_t *mem, word addr, word size, void *buf, int cmd)
{
    //~ printf("mem_access[0x%08x - 0x%08x]\n", addr, addr + size);
    //~ fflush(stdout);

	int err;
	
	/* speculative access */
	if (mem->spec_mode) {
		mem_spec_access(mem, addr, size, buf, cmd);
		return;
	}
	
	/* non speculative access */
	err = mem_nonspec_access(mem, addr, size, buf, cmd);
	switch (err) {
		case 0: break;
		case ERR_ALIGN:
			fatal("memory access not aligned (%u bytes at 0x%x)", size, addr);
		case ERR_ACCESS:
			fatal("memory access violation at 0x%x", addr);
		case ERR_READ:
			fatal("memory protection fault (read) at 0x%x", addr);
		case ERR_WRITE:
			fatal("memory protection fault (write) at 0x%x", addr);
	}
}


/* read/write memory block, without endianness conversion */
void mem_bcopy(struct mem_t *mem, word addr, word size, void *buf, int cmd)
{
	while (addr & 7 && size) {
		mem_access(mem, addr, 1, buf, cmd);
		size--;
		addr++;
		buf++;
	}
	while (size >= 8) {
		mem_access(mem, addr, 8, buf, cmd);
		size -= 8;
		addr += 8;
		buf +=8;
	}
	while (size) {
		mem_access(mem, addr, 1, buf, cmd);
		size--;
		addr++;
		buf++;
	}
}




void mem_write_byte(struct mem_t *mem, word addr, byte data)
{
	mem_access(mem, addr, 1, &data, MEM_WRITE);
}

void mem_write_half(struct mem_t *mem, word addr, half data)
{
	data = SWAPH(data);
	mem_access(mem, addr, 2, (byte *) &data, MEM_WRITE);

//    printf("mem write half, addr: 0x%08x, data: 0x%08x\n", addr, SWAPH(data));
//    fflush(stdout);
}

void mem_write_word(struct mem_t *mem, word addr, word data)
{
	data = SWAPW(data);
	mem_access(mem, addr, 4, (byte *) &data, MEM_WRITE);

//    printf("mem write word, addr: 0x%08x, data: 0x%08x\n", addr, SWAPW(data));
//    fflush(stdout);
}

void mem_write_dword(struct mem_t *mem, word addr, dword data)
{
	data = SWAPDW(data);
	mem_access(mem, addr, 8, (byte *) &data, MEM_WRITE);

//    printf("mem write dword, addr: 0x%08x, data: 0x%08x\n", addr, SWAPDW(data));
//    fflush(stdout);
}

void mem_write_str(struct mem_t *mem, word addr, char *str)
{
	int i;
	for (i = 0; i <= strlen(str); i++)
		mem_write_byte(mem, addr + i, str[i]);
}

void mem_write_blk(struct mem_t *mem, word addr, word size, void *p)
{
	word i;
	for (i = 0; i < size; i++)
		mem_write_byte(mem, addr + i, * (byte *) (p + i));
}


void mem_read_byte(struct mem_t *mem, word addr, byte *data)
{
	mem_access(mem, addr, 1, data, MEM_READ);
}

void mem_read_half(struct mem_t *mem, word addr, half *data)
{
	mem_access(mem, addr, 2, (byte *) data, MEM_READ);
	*data = SWAPH(*data);
}

void mem_read_word(struct mem_t *mem, word addr, word *data)
{
	mem_access(mem, addr, 4, (byte *) data, MEM_READ);
	*data = SWAPW(*data);
}

void mem_read_dword(struct mem_t *mem, word addr, dword *data)
{
	mem_access(mem, addr, 8, (byte *) data, MEM_READ);
	*data = SWAPDW(*data);
}

void mem_read_str(struct mem_t *mem, word addr, int max_size, char *str)
{
	int i;
	for (i = 0; i <= max_size; i++) {
		mem_read_byte(mem, addr + i, (byte *) (str + i));
		if (!str[i])
			break;
	}
}

void mem_read_blk(struct mem_t *mem, word addr, word size, void *p)
{
	word i;
	for (i = 0; i < size; i++)
		mem_read_byte(mem, addr + i, p + i);
}
