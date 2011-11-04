#ifndef BUFFER_H
#define BUFFER_H

struct buffer_t;

/* creation and destruction */
struct buffer_t *buffer_create(int size);
void buffer_free(struct buffer_t *buffer);

/* read/write */
int buffer_read(struct buffer_t *buffer, void *dest, int size);
int buffer_write(struct buffer_t *buffer, void *src, int size);
int buffer_count(struct buffer_t *buffer);

#endif
