#ifndef PUSH_THRAD_H_
#define PUSH_THRAD_H_

#define __USE_GNU

#include <pthread.h>

pthread_t thread_spawn(void* thread_func_p);

void thread_destroy(pthread_t thread_id);

#endif
