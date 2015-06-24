#define _GNU_SOURCE
#include <sched.h>
#include <sys/time.h>
#include <pthread.h>
#include <stdio.h>

#include "mst.h"

#define STEP_PRE  100
#define STEP_PUSH 40

volatile int inter_push_flag;
extern Vertex volatile g_tmp;
extern Vertex volatile g_vlist;
extern Vertex volatile g_inserted;



#define PUSHDATA(ADDR) asm volatile  ("prefetcht1 (%0)"::"r"(ADDR)) 


struct timeval t_start, t_end;
pthread_t push_thread_id;


float timeused(const struct timeval ts,const struct timeval te)
{
	return (float)(1000000*(te.tv_sec-ts.tv_sec)+te.tv_usec-ts.tv_usec)/1000000.0f;
}


void cpu_set()
{
    cpu_set_t mask;
    CPU_ZERO(&mask);
    CPU_SET(0,&mask);
    CPU_SET(1,&mask);
    if(sched_setaffinity( 0, sizeof(mask), &mask )==-1)
	{
        printf("failed to set cpu affinity\n");
		return ;
	}
	return ;
}


void* push_thread_func(void* arg)
{
	int i, j;
	unsigned int key;
	Vertex tmp, vlist, inserted;
	Hash hash;
	HashEntry volatile ent;

	while(1)
	{
		while(!inter_push_flag)
			asm("pause");
		if(1 == inter_push_flag)
		{
			vlist = g_vlist;
			inserted = g_inserted;
			tmp = vlist->next;
			while(tmp)
			{
				for(i=0; i<STEP_PRE && tmp; i++, tmp=tmp->next);
				for(i=0; i<STEP_PUSH && tmp; i++, tmp=tmp->next)
				{
					if(tmp != inserted)
					{
						hash = tmp->edgehash;
						key = (unsigned int)inserted;
						j = (hash->mapfunc)(key);
						for(ent=hash->array[j]; ent && ent->key != key; ent=ent->next);
					}
				}
				if(tmp)
				{
					tmp = g_tmp;
				}
			}
		}
		inter_push_flag = 0;
	}
	return NULL;
}


void init_pushthread()
{
	cpu_set();
	gettimeofday(&t_start, NULL);
	pthread_create(&push_thread_id, NULL, push_thread_func, NULL);
}


void destroy_pushthread()
{
	pthread_cancel(push_thread_id);
	gettimeofday(&t_end, NULL);
	printf("total time: %.3f\n", timeused(t_start, t_end));
}

