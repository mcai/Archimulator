/*
 * Image header stuff
 */
#ifndef _HEAD_H
#define _HEAD_H

#define LASAT_K_MAGIC0_VAL	0xfedeabba
#define LASAT_K_MAGIC1_VAL	0x00bedead

#ifndef _LANGUAGE_ASSEMBLY
#include <linux/types.h>
struct bootloader_header {
	__u32 magic[2];
	__u32 version;
	__u32 image_start;
	__u32 image_size;
	__u32 kernel_start;
	__u32 kernel_entry;
};
#endif

#endif /* _HEAD_H */
