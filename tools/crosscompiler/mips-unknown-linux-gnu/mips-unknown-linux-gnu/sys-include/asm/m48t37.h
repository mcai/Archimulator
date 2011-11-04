/*
 *  Registers for the SGS-Thomson M48T37 Timekeeper RAM chip
 */
#ifndef _ASM_M48T37_H
#define _ASM_M48T37_H

#include <linux/spinlock.h>

extern spinlock_t rtc_lock;

struct m48t37_rtc {
	volatile __u8	pad[0x7ff0];    /* NVRAM */
	volatile __u8	flags;
	volatile __u8	century;
	volatile __u8	alarm_sec;
	volatile __u8	alarm_min;
	volatile __u8	alarm_hour;
	volatile __u8	alarm_data;
	volatile __u8	interrupts;
	volatile __u8	watchdog;
	volatile __u8	control;
	volatile __u8	sec;
	volatile __u8	min;
	volatile __u8	hour;
	volatile __u8	day;
	volatile __u8	date;
	volatile __u8	month;
	volatile __u8	year;
};

#define M48T37_RTC_SET		0x80
#define M48T37_RTC_STOPPED	0x80
#define M48T37_RTC_READ		0x40

#endif /* _ASM_M48T37_H */
