#ifndef LIST_H
#define LIST_H

/* Error constants */
#define LIST_ENOMEM	1
#define LIST_EBOUNDS	2
#define LIST_EELEM	3
#define LIST_EEMPTY	4


struct list_t;

/* Creation and destruction */
struct list_t *list_create(int size);
void list_free(struct list_t *list);

/* Return error occurred in last list operation;
 * 0 means success */
int list_error(struct list_t *list);
char *list_error_msg(struct list_t *list);

/* List operations */
int list_count(struct list_t *list);
void list_add(struct list_t *list, void *elem);
void *list_get(struct list_t *list, int index);
void list_set(struct list_t *list, int index, void *elem);
void list_insert(struct list_t *list, int index, void *elem);
int list_index_of(struct list_t *list, void *elem);
void *list_remove_at(struct list_t *list, int index);
void *list_remove(struct list_t *list, void *elem);
void list_clear(struct list_t *list);

/* Stack operations */
void list_push(struct list_t *list, void *elem);
void *list_pop(struct list_t *list);
void *list_top(struct list_t *list);
void *list_bottom(struct list_t *list);

/* Queue operations */
void list_enqueue(struct list_t *list, void *elem);
void *list_dequeue(struct list_t *list);
void *list_tail(struct list_t *list);
void *list_head(struct list_t *list);

/* Sort */
void list_sort(struct list_t *list,
	int (*comp)(const void *, const void *));


#endif
