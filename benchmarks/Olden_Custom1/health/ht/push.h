#ifndef PUSH_H_
#define PUSH_H_

#if defined(SIMICS)
#include <simics/magic-instruction.h>

/*
static inline unsigned long MAGIC_READ_MISS()
{
  unsigned long val;
  MAGIC(9098); //notify GEMS to save value of g1 and put cache miss address in g1
  __asm__ __volatile__ ("mov %%g1, %0" : "=g" (val) : );
  MAGIC(9099); //notify GEMS to restore value of g1
  return val;
}
*/

#endif

#include "push_thread.h"
#include "push_params.h"

void push_thread_func();

inline void push_start(int cur_addr, int curr_addr);
inline void push_end();

#endif
