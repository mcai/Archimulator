#include "defines.h"

#include "push.h"

#define PUSHDATA(ADDR) __builtin_prefetch((ADDR))

int volatile inter_push_flag = 0;

arc_t* volatile g_arc;
arc_t* volatile g_stop_arcs;
long volatile g_nr_group;

node_t* volatile g_root;

inline void push_thread_func()
{
  int i = 0;

  node_t* p_root;
  node_t* p_push;
  node_t* p_tmp, *tmp;
  arc_t* p_arc;
  arc_t* p_stop_arcs;
  long p_nr_group;

  while (1) {
    while (!inter_push_flag);
    if (inter_push_flag==1) {
      p_arc = g_arc;
      p_stop_arcs = g_stop_arcs;
      p_nr_group = g_nr_group;

      for (i = 0 ; i < 5  && p_arc < p_stop_arcs; i++ , p_arc += p_nr_group);
      for (; p_arc < p_stop_arcs; p_arc += p_nr_group) {
        PUSHDATA(p_arc->head);
        PUSHDATA(p_arc->tail);
      }

    } else {
      if (inter_push_flag==2) {
        p_root = g_root;
        p_tmp = p_push = p_root->child;
        while ( p_push!=p_root) {
          for (i=0; i<LOOKAHEAD && (p_push != p_root); i++) {
            while (p_push) {
              p_tmp = p_push;
              p_push = p_push->child;
            }
            p_push = p_tmp;
            while (p_push->pred) {
              p_tmp = p_push->sibling;
              if (p_tmp) {
                p_push = p_tmp;
                break;
              } else {
                p_push = p_push->pred;
              }
            }
          }
          for (i=0; i<STRIDE && (p_push != p_root); i++) {
            while (p_push) {
              PUSHDATA(&(p_push->basic_arc->cost));
              p_tmp = p_push;
              p_push = p_push->child;
            }
            p_push = p_tmp;
            while (p_push->pred) {
              p_tmp = p_push->sibling;
              if (p_tmp) {
                p_push = p_tmp;
                break;
              } else {
                p_push = p_push->pred;
              }
            }
          }
        }
      }
    }
    inter_push_flag = 0;
  }
}
