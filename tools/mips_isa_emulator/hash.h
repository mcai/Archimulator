#ifndef HASH_H
#define HASH_H

struct hashtable_t;

/* creation and destruction */
struct hashtable_t *hashtable_create(int size, int casesen);
void hashtable_free(struct hashtable_t *ht);

/* insert a new element; the key is strdup'ped, so it can be modified in
 * the calling program;
 * return value: 0=success, non-0=key already exists/outofmem/data=NULL */
int hashtable_insert(struct hashtable_t *ht, char *key, void *data);

/* change element data;
 * return value: 0=success, non-0=key does not exist/data=NULL */
int hashtable_set(struct hashtable_t *ht, char *key, void *data);

/* return number of elements in hashtable */
int hashtable_count(struct hashtable_t *ht);

/* get data associated to a key;
 * return value: NULL=key does not exist, ptr=data */
void *hashtable_get(struct hashtable_t *ht, char *key);

/* remove data associated to a key; the key is freed;
 * return value: NULL=key does not exist, ptr=data removed */
void *hashtable_remove(struct hashtable_t *ht, char *key);

/* find elements in hash table sequentially;
 * return value: NULL=no more elements,
 * non-NULL=key (data returned in 'data' if not NULL) */
char *hashtable_find_first(struct hashtable_t *ht, void **data);
char *hashtable_find_next(struct hashtable_t *ht, void **data);

#endif
