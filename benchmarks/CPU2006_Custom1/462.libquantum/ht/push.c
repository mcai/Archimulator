#include <stdio.h>

#include "qureg.h"

#include "push.h"

extern volatile int mt_status;

extern volatile quantum_reg* cur_reg;

extern volatile int i_quantum_toffoli;

inline void push_thread_func()
{
    const int cur_reg_size = cur_reg->size;
    volatile long tmp_state;
    
    int j = 0;

    while(1)
    {
        while(mt_status != 1);

        int step = STEP;

        while(step < cur_reg->size)
        {
        #if defined(SIMICS)
            MAGIC(9017);
        #endif
    
            for (j = step; j < cur_reg->size && j <= step + BLOCKSIZE; j += STRIDE)
            {
                 tmp_state = cur_reg->node[j].state;
            }

            for(;j > i_quantum_toffoli && j < cur_reg->size;j+=STRIDE)
            {
                 tmp_state = cur_reg->node[j].state;
            }

            step = i_quantum_toffoli + STEP;
        }
        
        mt_status = 0;
    }
}
