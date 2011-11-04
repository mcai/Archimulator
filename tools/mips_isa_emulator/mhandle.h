#ifndef MHANDLE_H
#define MHANDLE_H

#include <stdlib.h>
#include <string.h>

#define MHANDLE_STRINGIFY(x) #x
#define MHANDLE_TOSTRING(x) MHANDLE_STRINGIFY(x)
#define MHANDLE_AT __FILE__ ":" MHANDLE_TOSTRING(__LINE__)

#ifdef MHANDLE

#undef strdup
#define free(X) (mhandle_free(X, MHANDLE_AT), (X)=NULL)
#define malloc(X) (mhandle_malloc(X, MHANDLE_AT))
#define calloc(X, Y) (mhandle_calloc(X, Y, MHANDLE_AT))
#define realloc(X, Y) (mhandle_realloc(X, Y, MHANDLE_AT))
#define strdup(X) (mhandle_strdup(X, MHANDLE_AT))
#define mhandle_check() __mhandle_check(MHANDLE_AT)
#define mhandle_done() __mhandle_done()

#else

#define mhandle_check()
#define mhandle_done()

#endif // MHANDLE


void *mhandle_malloc(unsigned long size, char *at);
void *mhandle_calloc(unsigned long nmemb, unsigned long size, char *at);
void *mhandle_realloc(void *ptr, unsigned long size, char *at);
char *mhandle_strdup(const char *s, char *at);
void mhandle_free(void *ptr, char *at);

void __mhandle_check(char *at);
void __mhandle_done();

#endif
