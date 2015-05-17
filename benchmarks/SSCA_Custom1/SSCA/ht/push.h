#ifndef PUSH_H
#define PUSH_H

#define _GNU_SOURCE

#include <sched.h>
#include <pthread.h> 
#include <stdlib.h>

pthread_t push_thread_id;

void *push_thread_func(void *pvoid);
void init_thread();

void destroy_thread();

#endif
