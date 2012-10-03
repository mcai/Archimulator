/*
 * This file is subject to the terms and conditions of the GNU General Public
 * License.  See the file "COPYING" in the main directory of this archive
 * for more details.
 *
 * Copyright (C) 1994 - 1999, 2000, 03 Ralf Baechle
 * Copyright (C) 1999, 2000 Silicon Graphics, Inc.
 */
#ifndef _ASM_PAGE_H
#define _ASM_PAGE_H

#include <unistd.h>

#define PAGE_SIZE (getpagesize())
static __inline__ int getpageshift()
{
    int pagesize = getpagesize();
#if (__GNUC__ >= 4) || ((__GNUC__ == 3) && (__GNUC_MINOR__ >= 4))
    return (__builtin_clz(pagesize) ^ 31);
#else
    register int pageshift = -1;
    while (pagesize) { pagesize >>= 1; pageshift++; }
    return pageshift;
#endif
}
#define PAGE_SHIFT (getpageshift())

#endif /* _ASM_PAGE_H */
#ifdef CONFIG_LIMITED_DMA
#define WANT_PAGE_VIRTUAL
#endif

