#include <stdio.h>

#define __USE_GNU

#include <sched.h>
#include <pthread.h>
    
#if defined(SIMICS)
#include <simics/magic-instruction.h>
#endif

#include "push_thread.h"

inline void cpu_set(int i)
{
    //TODO: the following code can not work under Simics, so commented.
    //~ cpu_set_t mask;
//~ 
    //~ CPU_ZERO(&mask);
    //~ CPU_SET(i, &mask);
//~ 
    //~ if (sched_setaffinity(0, sizeof(mask), &mask) == -1) {
        //~ perror("CPU set error.\n");
    //~ }
}

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
