#include <string.h>

#include "health.h"
#include "push.h"

int volatile tmp_cur_addr;
int volatile tmp_curr_addr;
volatile int __attribute__((aligned(64))) push_flag=0;
extern struct list *volatile __attribute__((aligned(64))) g_list;
extern struct list *volatile __attribute__((aligned(64))) g_valiage;

inline void push_start(int cur_addr, int curr_addr){
	tmp_cur_addr=cur_addr;
	tmp_curr_addr=curr_addr;
	push_flag=1;
}

inline void push_end(){
	push_flag=0;
}

inline void push_thread_func()
{
	struct Village volatile *curr_t;
	struct Village volatile  **to_curr_t;
	int k;
	int j;
	struct List volatile *curr_list;
	struct List volatile **to_curr_list;
  while(1){	
	while(!push_flag);

	curr_list=g_list;
     	curr_t=g_valiage;
	  int volatile i;
	  int volatile t,f;
	  while ( push_flag && curr_list!=NULL)
         { 
	 	for(k=1; ( k<LOOKAHEAD && curr_list); k++,curr_list=curr_list->forward);
		for(j=1; ( j<STRIDE && curr_list);j++) {
		if(curr_t)
		i = curr_t->hosp.free_personnel;
	   
		if (i > 0)
		 {
		  f = curr_list->patient->time;
		}
		curr_list = curr_list->forward;
		}
	      if(g_list)
	         curr_list=g_list;
	  } 
	}
}
