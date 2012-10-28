#ifndef PUSH_H_
#define PUSH_H_
    
#if defined(SIMICS)
#include <simics/magic-instruction.h>
#endif

#include "push_thread.h"
#include "push_params.h"

void push_thread_func();

void push_start(void*);

#endif
