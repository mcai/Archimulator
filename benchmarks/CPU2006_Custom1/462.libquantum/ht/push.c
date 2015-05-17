#include <stdio.h>

#include "qureg.h"

#include "push.h"

extern volatile int push_flag;

extern  quantum_reg *volatile cur_reg;

extern volatile int global_i;

inline void push_thread_func()
{
    volatile long tmp_state;
    
    int i=0;
    int j = 0;

    while(1)
    {
        while(!push_flag);
                
        i = global_i;

        while(push_flag && i < cur_reg->size)
		{
        #if defined(SIMICS)
            MAGIC(9017);
        #endif

	 		if(i + LOOKAHEAD < cur_reg->size)
	        	i += LOOKAHEAD;
	    	j=0;
	    	while(push_flag && i < cur_reg->size && j < STRIDE)
	    	{
				j++;       
	            tmp_state = cur_reg->node[i].state;
				i+=1;
	     	}
	   		i = global_i;
        }
    }
}
