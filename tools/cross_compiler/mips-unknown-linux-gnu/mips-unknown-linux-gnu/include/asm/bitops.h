/*
 * This file is subject to the terms and conditions of the GNU General Public
 * License.  See the file "COPYING" in the main directory of this archive
 * for more details.
 *
 * Copyright (c) 1994 - 1997, 1999, 2000  Ralf Baechle (ralf@gnu.org)
 * Copyright (c) 1999, 2000  Silicon Graphics, Inc.
 */

/* non-atomic version which could be used in userspace (but shouldn't anyway) */

#ifndef _ASM_BITOPS_H
#define _ASM_BITOPS_H

#include <linux/types.h>
#include <asm/byteorder.h>		/* sigh ... */

#if (_MIPS_SZLONG == 32)
#define SZLONG_LOG 5
#define SZLONG_MASK 31UL
#define __LL	"ll"
#define __SC	"sc"
#define cpu_to_lelongp(x) cpu_to_le32p((__u32 *) (x)) 
#elif (_MIPS_SZLONG == 64)
#define SZLONG_LOG 6
#define SZLONG_MASK 63UL
#define __LL	"lld"
#define __SC	"scd"
#define cpu_to_lelongp(x) cpu_to_le64p((__u64 *) (x)) 
#endif

/*
 * set_bit - Atomically set a bit in memory
 * @nr: the bit to set
 * @addr: the address to start counting from
 *
 * This function is atomic and may not be reordered.  See __set_bit()
 * if you do not require the atomic guarantees.
 * Note that @nr may be almost arbitrarily large; this function is not
 * restricted to acting on a single-word quantity.
 */
static inline void set_bit(unsigned long nr, volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	*a |= mask;
}

/*
 * __set_bit - Set a bit in memory
 * @nr: the bit to set
 * @addr: the address to start counting from
 *
 * Unlike set_bit(), this function is non-atomic and may be reordered.
 * If it's called on the same region of memory simultaneously, the effect
 * may be that only one operation succeeds.
 */
static inline void __set_bit(unsigned long nr, volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	*a |= mask;
}

/*
 * clear_bit - Clears a bit in memory
 * @nr: Bit to clear
 * @addr: Address to start counting from
 *
 * clear_bit() is atomic and may not be reordered.  However, it does
 * not contain a memory barrier, so if it is used for locking purposes,
 * you should call smp_mb__before_clear_bit() and/or smp_mb__after_clear_bit()
 * in order to ensure changes are visible on other processors.
 */
static inline void clear_bit(unsigned long nr, volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	*a &= ~mask;
}

static inline void __clear_bit(unsigned long nr, volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	*a &= ~mask;
}

/*
 * change_bit - Toggle a bit in memory
 * @nr: Bit to change
 * @addr: Address to start counting from
 *
 * change_bit() is atomic and may not be reordered.
 * Note that @nr may be almost arbitrarily large; this function is not
 * restricted to acting on a single-word quantity.
 */
static inline void change_bit(unsigned long nr, volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	*a ^= mask;
}

/*
 * __change_bit - Toggle a bit in memory
 * @nr: the bit to change
 * @addr: the address to start counting from
 *
 * Unlike change_bit(), this function is non-atomic and may be reordered.
 * If it's called on the same region of memory simultaneously, the effect
 * may be that only one operation succeeds.
 */
static inline void __change_bit(unsigned long nr, volatile unsigned long * addr)
{
	unsigned long * m = ((unsigned long *) addr) + (nr >> SZLONG_LOG);

	*m ^= 1UL << (nr & SZLONG_MASK);
}

/*
 * test_and_set_bit - Set a bit and return its old value
 * @nr: Bit to set
 * @addr: Address to count from
 *
 * This operation is atomic and cannot be reordered.
 * It also implies a memory barrier.
 */
static inline int test_and_set_bit(unsigned long nr,
	volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;
	int retval;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	retval = (mask & *a) != 0;
	*a |= mask;

	return retval;
}

/*
 * __test_and_set_bit - Set a bit and return its old value
 * @nr: Bit to set
 * @addr: Address to count from
 *
 * This operation is non-atomic and can be reordered.
 * If two examples of this operation race, one can appear to succeed
 * but actually fail.  You must protect multiple accesses with a lock.
 */
static inline int __test_and_set_bit(unsigned long nr,
	volatile unsigned long *addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;
	int retval;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	retval = (mask & *a) != 0;
	*a |= mask;

	return retval;
}

/*
 * test_and_clear_bit - Clear a bit and return its old value
 * @nr: Bit to clear
 * @addr: Address to count from
 *
 * This operation is atomic and cannot be reordered.
 * It also implies a memory barrier.
 */
static inline int test_and_clear_bit(unsigned long nr,
	volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;
	int retval;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	retval = (mask & *a) != 0;
	*a &= ~mask;

	return retval;
}

/*
 * __test_and_clear_bit - Clear a bit and return its old value
 * @nr: Bit to clear
 * @addr: Address to count from
 *
 * This operation is non-atomic and can be reordered.
 * If two examples of this operation race, one can appear to succeed
 * but actually fail.  You must protect multiple accesses with a lock.
 */
static inline int __test_and_clear_bit(unsigned long nr,
	volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;
	int retval;

	a += (nr >> SZLONG_LOG);
	mask = 1UL << (nr & SZLONG_MASK);
	retval = ((mask & *a) != 0);
	*a &= ~mask;

	return retval;
}

/*
 * test_and_change_bit - Change a bit and return its old value
 * @nr: Bit to change
 * @addr: Address to count from
 *
 * This operation is atomic and cannot be reordered.
 * It also implies a memory barrier.
 */
static inline int test_and_change_bit(unsigned long nr,
	volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask, retval;

	a += nr >> SZLONG_LOG;
	mask = 1 << (nr & SZLONG_MASK);
	retval = (mask & *a) != 0;
	*a ^= mask;

	return retval;
}

/*
 * __test_and_change_bit - Change a bit and return its old value
 * @nr: Bit to change
 * @addr: Address to count from
 *
 * This operation is non-atomic and can be reordered.
 * If two examples of this operation race, one can appear to succeed
 * but actually fail.  You must protect multiple accesses with a lock.
 */
static inline int __test_and_change_bit(unsigned long nr,
	volatile unsigned long * addr)
{
	volatile unsigned long *a = addr;
	unsigned long mask;
	int retval;

	a += (nr >> SZLONG_LOG);
	mask = 1 << (nr & SZLONG_MASK);
	retval = (mask & *a) != 0;
	*a ^= mask;

	return retval;
}

/*
 * test_bit - Determine whether a bit is set
 * @nr: bit number to test
 * @addr: Address to start counting from
 */
static inline int test_bit(unsigned long nr, const volatile unsigned long *addr)
{
	return 1UL & (addr[nr >> SZLONG_LOG] >> (nr & SZLONG_MASK));
}

/*
 * ffz - find first zero in word.
 * @word: The word to search
 *
 * Undefined if no zero exists, so code should check against ~0UL first.
 */
static inline unsigned long ffz(unsigned long word)
{
	int b = 0, s;

	word = ~word;
#if (_MIPS_SZLONG == 32)
	s = 16; if (word << 16 != 0) s = 0; b += s; word >>= s;
	s =  8; if (word << 24 != 0) s = 0; b += s; word >>= s;
	s =  4; if (word << 28 != 0) s = 0; b += s; word >>= s;
	s =  2; if (word << 30 != 0) s = 0; b += s; word >>= s;
	s =  1; if (word << 31 != 0) s = 0; b += s;
#endif
#if (_MIPS_SZLONG == 64)
	s = 32; if (word << 32 != 0) s = 0; b += s; word >>= s;
	s = 16; if (word << 48 != 0) s = 0; b += s; word >>= s;
	s =  8; if (word << 56 != 0) s = 0; b += s; word >>= s;
	s =  4; if (word << 60 != 0) s = 0; b += s; word >>= s;
	s =  2; if (word << 62 != 0) s = 0; b += s; word >>= s;
	s =  1; if (word << 63 != 0) s = 0; b += s;
#endif

	return b;
}

/*
 * __ffs - find first bit in word.
 * @word: The word to search
 *
 * Undefined if no bit exists, so code should check against 0 first.
 */
static inline unsigned long __ffs(unsigned long word)
{
	return ffz(~word);
}

/*
 * fls: find last bit set.
 */

#define fls(x) generic_fls(x)

/*
 * find_next_zero_bit - find the first zero bit in a memory region
 * @addr: The address to base the search on
 * @offset: The bitnumber to start searching at
 * @size: The maximum size to search
 */
static inline unsigned long find_next_zero_bit(const unsigned long *addr,
	unsigned long size, unsigned long offset)
{
	const unsigned long *p = addr + (offset >> SZLONG_LOG);
	unsigned long result = offset & ~SZLONG_MASK;
	unsigned long tmp;

	if (offset >= size)
		return size;
	size -= result;
	offset &= SZLONG_MASK;
	if (offset) {
		tmp = *(p++);
		tmp |= ~0UL >> (_MIPS_SZLONG-offset);
		if (size < _MIPS_SZLONG)
			goto found_first;
		if (~tmp)
			goto found_middle;
		size -= _MIPS_SZLONG;
		result += _MIPS_SZLONG;
	}
	while (size & ~SZLONG_MASK) {
		if (~(tmp = *(p++)))
			goto found_middle;
		result += _MIPS_SZLONG;
		size -= _MIPS_SZLONG;
	}
	if (!size)
		return result;
	tmp = *p;

found_first:
	tmp |= ~0UL << size;
	if (tmp == ~0UL)		/* Are any bits zero? */
		return result + size;	/* Nope. */
found_middle:
	return result + ffz(tmp);
}

#define find_first_zero_bit(addr, size) \
	find_next_zero_bit((addr), (size), 0)

/*
 * find_next_bit - find the next set bit in a memory region
 * @addr: The address to base the search on
 * @offset: The bitnumber to start searching at
 * @size: The maximum size to search
 */
static inline unsigned long find_next_bit(const unsigned long *addr,
	unsigned long size, unsigned long offset)
{
	const unsigned long *p = addr + (offset >> SZLONG_LOG);
	unsigned long result = offset & ~SZLONG_MASK;
	unsigned long tmp;

	if (offset >= size)
		return size;
	size -= result;
	offset &= SZLONG_MASK;
	if (offset) {
		tmp = *(p++);
		tmp &= ~0UL << offset;
		if (size < _MIPS_SZLONG)
			goto found_first;
		if (tmp)
			goto found_middle;
		size -= _MIPS_SZLONG;
		result += _MIPS_SZLONG;
	}
	while (size & ~SZLONG_MASK) {
		if ((tmp = *(p++)))
			goto found_middle;
		result += _MIPS_SZLONG;
		size -= _MIPS_SZLONG;
	}
	if (!size)
		return result;
	tmp = *p;

found_first:
	tmp &= ~0UL >> (_MIPS_SZLONG - size);
	if (tmp == 0UL)			/* Are any bits set? */
		return result + size;	/* Nope. */
found_middle:
	return result + __ffs(tmp);
}

/*
 * find_first_bit - find the first set bit in a memory region
 * @addr: The address to start the search at
 * @size: The maximum size to search
 *
 * Returns the bit-number of the first set bit, not the number of the byte
 * containing a bit.
 */
#define find_first_bit(addr, size) \
	find_next_bit((addr), (size), 0)

#endif /* _ASM_BITOPS_H */
