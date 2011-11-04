#include <stdlib.h>
#include <string.h>

#include "mhandle.h"
#include "stats.h"
#include "options.h"


/* global private stat data base */
struct sdb_t {
	struct stat_t *head, *tail;
	struct eval_t *eval;
	int width;
};

static struct sdb_t *sdb = NULL;


enum stat_class_t {
	sc_int32 = 0,
	sc_uint32,
	sc_int64,
	sc_uint64,
	sc_float,
	sc_double,
	sc_string,
	sc_formula,
	sc_note,
	sc_func,
	sc_dist,
	sc_ddist
};


struct stat_t {
	struct stat_t *next;
	enum stat_class_t sc;
	char *name;
	char *desc;
	char *format;
	void *var;
	
	union {
		struct {
			int min, max, size;
			uint64_t overflows;
			uint64_t count; /* overflows included */
			int64_t acc; /* accumulative values to compute average */
			uint64_t *arr;
		} for_dist;

		struct {
			double acc;
			uint64_t count;
		} for_ddist;
	} fields;
};


/* Return the value of an identifier */
static double get_id_value(char *name)
{
	struct stat_t *stat;
	double res = 0;
	
	/* search identifier in data base */
	for (stat = sdb->head; stat; stat = stat->next) {
		if (!strcmp(stat->name, name)) {
			switch (stat->sc) {
			case sc_int32:
				res = * (int32_t *) stat->var;
				break;
			case sc_uint32:
				res = * (uint32_t *) stat->var;
				break;
			case sc_int64:
				res = * (int64_t *) stat->var;
				break;
			case sc_uint64:
				res = * (uint64_t *) stat->var;
				break;
			case sc_float:
				res = * (float *) stat->var;
				break;
			case sc_double:
				res = * (double *) stat->var;
				break;
			case sc_formula:
				res = eval_expr(sdb->eval, stat->var);
				break;
			default:
				fprintf(stderr, "error: cannot extract value of '%s' for a formula\n", stat->name);
				abort();
			}
			break;
		}
	}
	
	/* if not fount in stat data base, search in options data base */
	if (!stat)
		res = opt_get_option(name);
	
	return res;
}


static void stat_add_stat(struct stat_t *stat)
{
	if (!sdb)
		return;
	
	/* check max width */
	if (strlen(stat->name) > sdb->width)
		sdb->width = strlen(stat->name);
	
	/* add stat to stat list */
	if (!sdb->head)
		sdb->head = sdb->tail = stat;
	else {
		sdb->tail->next = stat;
		sdb->tail = stat;
	}
}


static void stat_print_dist(struct stat_t *stat, FILE *f)
{
	char fmt[20], name[100];
	int i;
	uint64_t val;
	
	sprintf(fmt, "%%-%ds ", sdb->width);
	fprintf(f, "\n");
	
	for (i = stat->fields.for_dist.min; i <= stat->fields.for_dist.max; i++) {
		sprintf(name, "%s[%d]", stat->name, i);
		fprintf(f, fmt, name);
		val = stat->fields.for_dist.arr[i - stat->fields.for_dist.min];
		fprintf(f, "%12llu %12.4f\n", (long long unsigned int) val,
			(double) val / stat->fields.for_dist.count);
	}
	
	sprintf(name, "%s.overflows", stat->name);
	fprintf(f, fmt, name);
	fprintf(f, "%12llu %12.4f\n", (long long unsigned int) stat->fields.for_dist.overflows,
		(double) stat->fields.for_dist.overflows / stat->fields.for_dist.count);
	
	sprintf(name, "%s.count", stat->name);
	fprintf(f, fmt, name);
	fprintf(f, "%12llu\n", (long long unsigned int) stat->fields.for_dist.count);
	
	sprintf(name, "%s.avg", stat->name);
	fprintf(f, fmt, name);
	fprintf(f, "%12.3f\n", (double) stat->fields.for_dist.acc /
		stat->fields.for_dist.count);
	
	fprintf(f, "\n");
}


static void stat_print_ddist(struct stat_t *stat, FILE *f)
{
	char fmt[20], name[100], buf[100];
	sprintf(fmt, "%%-%ds ", sdb->width);

	sprintf(name, "%s.count", stat->name);
	fprintf(f, fmt, name);
	fprintf(f, "%12lld # %s\n", (long long) stat->fields.for_ddist.count, stat->desc);
	
	sprintf(name, "%s.average", stat->name);
	fprintf(f, fmt, name);
	sprintf(buf, stat->format, stat->fields.for_ddist.acc / stat->fields.for_ddist.count);
	fprintf(f, "%12s\n", buf);
}


void stat_init()
{
	sdb = calloc(1, sizeof(struct sdb_t));
	sdb->eval = eval_create(get_id_value);
	sdb->width = 10;
}


void stat_done()
{
	struct stat_t *stat;
	
	if (!sdb)
		return;

	/* free stats */
	while (sdb->head) {
		stat = sdb->head;
		sdb->head = stat->next;
		free(stat->name);
		free(stat->desc);
		if (stat->sc == sc_formula)
			free(stat->var);
		if (stat->sc == sc_dist)
			free(stat->fields.for_dist.arr);
		free(stat);
	}
			
	/* free rest */
	eval_free(sdb->eval);
	free(sdb);
	sdb = NULL;
}


struct stat_t *stat_reg_int32(char *name, char *desc, int32_t *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12d";
	stat->sc = sc_int32;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_uint32(char *name, char *desc, uint32_t *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12u";
	stat->sc = sc_uint32;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_int64(char *name, char *desc, int64_t *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12lld";
	stat->sc = sc_int64;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_uint64(char *name, char *desc, uint64_t *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12llu";
	stat->sc = sc_uint64;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_float(char *name, char *desc, float *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12f";
	stat->sc = sc_float;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_double(char *name, char *desc, double *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12f";
	stat->sc = sc_double;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_string(char *name, char *desc, char *var)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12s";
	stat->sc = sc_string;
	stat->var = var;
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_formula(char *name, char *desc, char *expr)
{
	struct	stat_t *stat = calloc(1, sizeof(struct stat_t));
	
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%12f";
	stat->sc = sc_formula;
	stat->var = strdup(expr);
	stat_add_stat(stat);
	return stat;
}


struct stat_t *stat_reg_note(char *note)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup("");
	stat->desc = strdup(note);
	stat->sc = sc_note;
	stat_add_stat(stat);
	return stat;
}


/* Register a distribution */
struct stat_t *stat_reg_dist(char *name, char *desc, int min, int max)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	int width;
	
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "";
	stat->sc = sc_dist;
	
	stat->fields.for_dist.min = min;
	stat->fields.for_dist.max = max;
	stat->fields.for_dist.size = max - min + 1;
	stat->fields.for_dist.arr = calloc(stat->fields.for_dist.size, sizeof(uint64_t));
	
	width = strlen(name) + 10;
	if (width > sdb->width)
		sdb->width = width;
	
	stat_add_stat(stat);
	return stat;
}


/* Distribution of doubles */
struct stat_t *stat_reg_ddist(char *name, char *desc)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	int width;
	stat->name = strdup(name);
	stat->desc = strdup(desc);
	stat->format = "%.2f";
	stat->sc = sc_ddist;
	
	width = strlen(name) + 10;
	if (width > sdb->width)
		sdb->width = width;
	
	stat_add_stat(stat);
	return stat;
}


void stat_add_sample(struct stat_t *stat, int sample)
{
	if (stat->sc != sc_dist)
		return;
	
	/* Is this sample an overflow? */
	if (sample < stat->fields.for_dist.min || sample > stat->fields.for_dist.max)
		stat->fields.for_dist.overflows++;
	else
		stat->fields.for_dist.arr[sample - stat->fields.for_dist.min]++;
	
	stat->fields.for_dist.count++;
	stat->fields.for_dist.acc += sample;
}


void stat_ddist_sample(struct stat_t *stat, double sample)
{
	if (stat->sc != sc_ddist)
		return;
	stat->fields.for_ddist.count++;
	stat->fields.for_ddist.acc += sample;
}


/* register a function */
struct stat_t *stat_reg_func(print_stats_fn_t func)
{
	struct stat_t *stat = calloc(1, sizeof(struct stat_t));
	stat->name = strdup("");
	stat->desc = strdup("");
	stat->sc = sc_func;
	stat->var = func;
	stat_add_stat(stat);
	return stat;
}


void stat_set_format(struct stat_t *stat, char *fmt)
{
	stat->format = fmt;
}


void stat_print_stats(FILE *fd)
{
	struct stat_t *stat;
	char s[128];
	int i;
	
	if (!sdb)
		return;
	
	/* key */
	sprintf(s, "\n%%-%ds %%12s # %%s\n", sdb->width);
	fprintf(fd, s, "stat", "value", "description");
	for (i = 0; i < 60; i++)
		fprintf(fd, "-");
	fprintf(fd, "\n");
	
	/* stats */
	for (stat = sdb->head; stat; stat = stat->next) {
		
		if (stat->sc != sc_func && stat->sc != sc_note &&
			stat->sc != sc_dist &&
			stat->sc != sc_ddist)
		{
			sprintf(s, "%%-%ds ", sdb->width);
			fprintf(fd, s, stat->name);
		}
		
		/* action depending on stat */
		switch (stat->sc) {
		
		case sc_int32:
			sprintf(s, stat->format, * (int32_t *) stat->var);
			break;
		case sc_uint32:
			sprintf(s, stat->format, * (uint32_t *) stat->var);
			break;
		case sc_int64:
			sprintf(s, stat->format, * (int64_t *) stat->var);
			break;
		case sc_uint64:
			sprintf(s, stat->format, * (uint64_t *) stat->var);
			break;
		case sc_float:
			sprintf(s, stat->format, (double) * (float *) stat->var);
			break;
		case sc_double:
			sprintf(s, stat->format, * (double *) stat->var);
			break;
		case sc_string:
			sprintf(s, stat->format, stat->var);
			break;
		case sc_formula:
			sprintf(s, stat->format, eval_expr(sdb->eval, (char *) stat->var));
			break;
		case sc_dist:
			stat_print_dist(stat, fd);
			continue;
		case sc_ddist:
			stat_print_ddist(stat, fd);
			continue;
		case sc_func:
			((print_stats_fn_t) stat->var)(fd);
			continue;
		case sc_note:
			if (*stat->desc)
				fprintf(fd, "\n# %s\n", stat->desc);
			else
				fprintf(fd, "\n");
			continue;
		}
		
		/* more general actions */
		fprintf(fd, "%12s # %s\n", s, stat->desc);
	}
	fprintf(fd, "\n");
}
