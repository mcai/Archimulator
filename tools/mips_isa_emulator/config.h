#ifndef CONFIG_H
#define CONFIG_H

struct config_t;

/* creation and destruction */
struct config_t *config_create(char *filename);
void config_free(struct config_t *cfg);

/* load and save configuration;
 * return value: non-0=ok, 0=file access failure */
int config_load(struct config_t *cfg);
int config_save(struct config_t *cfg);

/* ask for section of key existence */
int config_section_exists(struct config_t *cfg, char *section);
int config_key_exists(struct config_t *cfg, char *section, char *key);

/* remove a key/section;
 * return value: non-0=ok, 0=key/section does not exist */
int config_section_remove(struct config_t *cfg, char *section);
int config_key_remove(struct config_t *cfg, char *section, char *key);

/* add keys in a section; if section does not exists, it is created;
 * if key already exists, replace old value;
 * string values are strdup'ped, so they can be modified in user program */
void config_write_string(struct config_t *cfg, char *section, char *key, char *value);
void config_write_int(struct config_t *cfg, char *section, char *key, int value);
void config_write_bool(struct config_t *cfg, char *section, char *key, int value);
void config_write_double(struct config_t *cfg, char *section, char *key, double value);

/* read keys from a section; if section or key do not exist,
 * the default value if returned */
char *config_read_string(struct config_t *cfg, char *section, char *key, char *def);
int config_read_int(struct config_t *cfg, char *section, char *key, int def);
int config_read_bool(struct config_t *cfg, char *section, char *key, int def);
double config_read_double(struct config_t *cfg, char *section, char *key, double def);

#endif

