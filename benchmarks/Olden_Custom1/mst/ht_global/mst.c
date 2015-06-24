/* For copyright information, see olden_v1.0/COPYRIGHT */

#include "mst.h"

typedef struct blue_return {
  Vertex vert;
  int dist;
} BlueReturn;


typedef struct fc_br {
  BlueReturn value;
} future_cell_BlueReturn;

Vertex volatile g_tmp;
Vertex volatile g_vlist;
Vertex volatile g_inserted;
extern volatile int inter_push_flag;

static BlueReturn BlueRule(Vertex inserted, Vertex vlist) 
{
  BlueReturn retval;
  Vertex tmp,prev;
  Hash hash;
  int dist,dist2;
  
  if (!vlist) {
    retval.dist = 999999;
    return retval;
  }

  prev = vlist;
  retval.vert = vlist;
  retval.dist = vlist->mindist;
  hash = vlist->edgehash;
  dist = (int) HashLookup((unsigned int) inserted, hash);
  /*printf("Found %d at 0x%x for 0x%x\n",dist,inserted,vlist);*/
  if (dist) 
    {
      if (dist<retval.dist) 
        {
          vlist->mindist = dist;
          retval.dist = dist;
        }
    }
  else printf("Not found\n");
  
  /* We are guaranteed that inserted is not first in list */
//////////////////////
	g_tmp = tmp;
	g_vlist = vlist;
	g_inserted = inserted;
	inter_push_flag = 1;
//////////////////////
  for (tmp=vlist->next; tmp; prev=tmp,tmp=tmp->next) 
    {
		g_tmp = tmp;
      if (tmp==inserted) 
        {
          Vertex next;

          next = tmp->next;
          prev->next = next;
        }
      else 
        {
          hash = tmp->edgehash; /* <------  6% miss in tmp->edgehash */ 
          dist2 = tmp->mindist;
          dist = (int) HashLookup((unsigned int) inserted, hash);
          /*printf("Found %d at 0x%x for 0x%x\n",dist,inserted,tmp);*/
          if (dist) 
            {
              if (dist<dist2) 
                {
                  tmp->mindist = dist;
                  dist2 = dist;
                }
            }
          else printf("Not found\n");
          if (dist2<retval.dist) 
            {
              retval.vert = tmp;
              retval.dist = dist2;
            }
        } /* else */
    } /* for */
	inter_push_flag = 0;
  return retval;
}/* BlueRule() */

          

static Vertex MyVertexList = NULL;

static BlueReturn Do_all_BlueRule(Vertex inserted, int nproc, int pn) {
  future_cell_BlueReturn fcleft;
  BlueReturn retright;

  if (nproc > 1) {
     fcleft.value = Do_all_BlueRule(inserted,nproc/2,pn+nproc/2);
     retright = Do_all_BlueRule(inserted,nproc/2,pn);

     if (fcleft.value.dist < retright.dist) {
       retright.dist = fcleft.value.dist;
       retright.vert = fcleft.value.vert;
       }
     return retright;
  }
  else {
     if (inserted == MyVertexList)
       MyVertexList = MyVertexList->next;
     return BlueRule(inserted,MyVertexList);
  }
}

static int ComputeMst(Graph graph,int numproc,int numvert) 
{
  Vertex inserted,tmp;
  int cost=0,dist;
	
  /* make copy of graph */
  printf("Compute phase 1\n");

  /* Insert first node */
  inserted = graph->vlist[0];
  tmp = inserted->next;
  graph->vlist[0] = tmp;
  MyVertexList = tmp;
  numvert--;
  /* Announce insertion and find next one */
  printf("Compute phase 2\n");
  while (numvert) 
    {
      BlueReturn br;
      
      br = Do_all_BlueRule(inserted,numproc,0);
      inserted = br.vert;    
      dist = br.dist;
      numvert--;
      cost = cost+dist;
    }
  return cost;
}

extern void init_pushthread();
extern void destroy_pushthread();

int main(int argc, char *argv[]) 
{
  Graph graph;
  int dist;
  int size;

  init_pushthread();

  size = dealwithargs(argc,argv);
  printf("Making graph of size %d\n",size);
  graph = MakeGraph(size,NumNodes);

  dist = ComputeMst(graph,NumNodes,size);
  printf("MST has cost %d\n",dist);

  destroy_pushthread();

  exit(0);
}

