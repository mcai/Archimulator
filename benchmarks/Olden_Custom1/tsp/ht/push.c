#include <string.h>

#include "tsp.h"

#include "push.h"

Tree volatile tmp_a,case2_t;
Tree volatile __attribute__((aligned(64))) g_curr_pointer;
volatile int __attribute__((aligned(64))) push_flag=0;

inline void push_thread_func()
{
	int i,synInterval=0;
	Tree p_push,p_stop,p_push2;
	volatile double tmp_x,tmp_y;

	while(1)
	{
		while(!push_flag);

		switch(push_flag)
		{
			case 1:
			{	
				p_stop=tmp_a;
				p_push=tmp_a->next;

			    while(p_push && push_flag&& p_push!=p_stop)
			    {	
					for(i=0; push_flag && i< LOOKAHEAD && p_push != p_stop;i++)
					{	p_push=p_push->next;
						__builtin_prefetch(p_push->next);
					}

					for(i=0 ; push_flag && i< STRIDE && p_push != p_stop; i++)
					{
						__builtin_prefetch(&(p_push->x));
						p_push=p_push->next;
					}

					p_push=g_curr_pointer;
			    }

				break;	
			}
		}
	}
}
