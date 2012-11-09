#define _GNU_SOURCE

#include <sched.h>
#include <pthread.h>

#include "push.h"
#include "defs.h"

#define PUSHDATA(ADDR) __builtin_prefetch((ADDR));

#define K_F 50
#define P_F 21
#define K_S 70
#define P_S 105

LONG_T   *volatile tmp_start;
LONG_T   *volatile tmp_end;
int volatile g_phase_num;
int volatile g_hot2_j;
int volatile g_vertex;
VERT_T  *volatile tmp_S;
DOUBLE_T  *volatile tmp_sig;
LONG_T    *volatile tmp_d;
DOUBLE_T  *volatile tmp_del;
plist  *volatile tmp_P;
graph  *volatile tmp_G;
DOUBLE_T *volatile tmp_BC;
int volatile  push_flag;

void *push_thread_func(void *pvoid)
{
	LONG_T ph_num;
	int volatile tmp_vert,tmp_v;
	int j,tmp_w,i,k;
		
	while(1)
	{
		while(!push_flag);

		if(1 == push_flag)
		{	
		      ph_num=g_phase_num;

		      tmp_vert =tmp_start[ph_num]; 
	      
		      while(push_flag&&(tmp_vert < tmp_end[ph_num]))
		      {
		      //	skip the K_F vetexes
		      if ((tmp_vert+K_F)<tmp_end[ph_num])
			      tmp_vert=tmp_vert+K_F;
			  i=0;
		      while ( push_flag&& (tmp_vert < (tmp_end[ph_num]))&& i<P_F)
			  {
			      i++;
			      tmp_v = tmp_S[tmp_vert];
			      for (j=tmp_G->numEdges[tmp_v];push_flag&&( j<tmp_G->numEdges[tmp_v+1]); j++)
			      {

				  PUSHDATA (&(tmp_G->weight[j] ));
				  tmp_w=(tmp_G->endV[j]);
				  PUSHDATA(&(tmp_d[tmp_w]));
				  PUSHDATA(&(tmp_sig[tmp_w]));
				  PUSHDATA(&(tmp_sig[tmp_v]));
				  PUSHDATA(&(tmp_P[tmp_w].list[tmp_P[tmp_w].count]));

			      }
				tmp_vert+=1;
			  }
		      tmp_vert=g_vertex;
		      }
		}
		else if (push_flag==2)
	        {
			ph_num=g_phase_num;
			j=tmp_start[ph_num];
			
			while(push_flag&& j< tmp_end[ph_num]){

			        if ((j+K_S)<tmp_end[ph_num])
				        j=j+K_S;
				for(i=0;push_flag&&j<tmp_end[ph_num]&& i<P_S;i++,j++){
					tmp_w=tmp_S[j];
					for(k=0;push_flag&&k<tmp_P[tmp_w].count;k++){
						tmp_v=tmp_P[tmp_w].list[k];
						PUSHDATA(&(tmp_del[tmp_v]));
						PUSHDATA(&(tmp_del[tmp_w]));
						PUSHDATA(&(tmp_sig[tmp_v]));
						PUSHDATA(&(tmp_sig[tmp_w]));
					}
						PUSHDATA(&(tmp_BC[tmp_w]));
					}
				j=g_hot2_j;
			}
		}
	}
	return NULL;
}

inline void init_thread()
{
	pthread_create(&push_thread_id, NULL, push_thread_func, NULL);
	return;
}

inline void destroy_thread()
{
	pthread_cancel(push_thread_id);
}

