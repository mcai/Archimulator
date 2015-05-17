#include <stdio.h>

#define __USE_GNU

#include <pthread.h>
    
#if defined(SIMICS)
#include <simics/magic-instruction.h>
#endif

#include "push_thread.h"

inline pthread_t thread_spawn(void* thread_func_p)
{
    pthread_t thread_id;

    pthread_create(&thread_id, NULL, thread_func_p, NULL);

    return thread_id;
}

inline void thread_destroy(pthread_t thread_id)
{
    pthread_cancel(thread_id);
}
