/*
 *  Registers for the SGS-Thomson M48T35 Timekeeper RAM chip
 */
#ifndef _ASM_M48T35_H
#define _ASM_M48T35_H


extern spinlock_t rtc_lock;

struct m48t35_rtc {
	volatile __u8	pad[0x7ff8];    /* starts at 0x7ff8 */
	volatile __u8	control;
	volatile __u8	sec;
	volatile __u8	min;
	volatile __u8	hour;
	volatile __u8	day;
	volatile __u8	date;
	volatile __u8	month;
	volatile __u8	year;
};

#define M48T35_RTC_SET		0x80
#define M48T35_RTC_STOPPED	0x80
#define M48T35_RTC_READ		0x40

#endif /* _ASM_M48T35_H */
