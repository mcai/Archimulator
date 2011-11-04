#ifndef STAT_H
#define STAT_H

#include <stdio.h>
#include <stdint.h>

#include "eval.h"

/* Type of a stat */
struct stat_t;

/* Initialization & destruction */
void stat_init();
void stat_done();

/* Functions to register stats */
struct stat_t *stat_reg_int32(char *name, char *desc, int32_t *var);
struct stat_t *stat_reg_uint32(char *name, char *desc, uint32_t *var);
struct stat_t *stat_reg_int64(char *name, char *desc, int64_t *var);
struct stat_t *stat_reg_uint64(char *name, char *desc, uint64_t *var);
struct stat_t *stat_reg_float(char *name, char *desc, float *var);
struct stat_t *stat_reg_double(char *name, char *desc, double *var);
struct stat_t *stat_reg_string(char *name, char *desc, char *var);
struct stat_t *stat_reg_formula(char *name, char *desc, char *expr);
struct stat_t *stat_reg_note(char *note);

/* Distribution of an integer variable */
struct stat_t *stat_reg_dist(char *name, char *desc,
	int min, int max);
void stat_add_sample(struct stat_t *stat, int sample);

/* Double to compute average and samples */
struct stat_t *stat_reg_ddist(char *name, char *desc);
void stat_ddist_sample(struct stat_t *stat, double sample);

/* Register a function to print other stats */
typedef void (*print_stats_fn_t) (FILE *f);
struct stat_t *stat_reg_func(print_stats_fn_t func);

/* Specify output format of a stat */
void stat_set_format(struct stat_t *stat, char *fmt);

/* Print all stats */
void stat_print_stats(FILE *f);


#endif
