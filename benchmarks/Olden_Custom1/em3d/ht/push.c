#include "em3d.h"

#include "push.h"

#define PUSHDATA(ADDR) __builtin_prefetch((ADDR))

int volatile i_sem = 0;

node_t** volatile curr_pointer_addr;

extern int d_nodes;

int volatile push_flag = -1;

inline void push_thread_func()
{
  volatile int i = 0,degree = d_nodes;
  node_t* volatile curr_p;
  node_t** volatile  to_curr_p;

  //////////////////////////

  volatile int tmp_i, tmp_j, tmp_count;
  volatile node_t * volatile tmp_other_node;

  double ** volatile tmp_otherlist;
  double * volatile tmp_value;

  volatile double tmp;
  volatile int tmp_degree;

  volatile node_t * volatile node_index;
  volatile node_p * volatile tmp_cur_node;

  //////////////////////////

  while (1) {
    while (i_sem<=0);
    i_sem--;

    switch (push_flag) {
    case 1:
      to_curr_p = (node_t**)curr_pointer_addr;
      curr_p = (node_t*)(*to_curr_p);
      while (curr_p) {
        while (curr_p && i++<STEP) {
          int j;
          for (j = 0;j<degree;j++) {
            volatile int count,thecount;
            void* volatile pushaddr;
            node_t * volatile temp_other = curr_p->to_nodes[j];
            double volatile **otherlist;
            volatile double *value = curr_p->value;
            if (temp_other) {
              count = temp_other->from_length;
              otherlist = temp_other->from_values;
            }
            pushaddr = &(otherlist[count]);
            PUSHDATA(pushaddr);
          }
          curr_p = curr_p->next;
        }
        i = 0;
        curr_p = (node_t*)(*to_curr_p);
        if (curr_p) curr_p = curr_p->next;
      }
    case 10:

      //////////////////////////

      tmp_degree = d_nodes;
      tmp_cur_node = (node_p *)curr_pointer_addr;
      node_index = (node_t *)(*tmp_cur_node);

      while (node_index) {
        for (tmp_i=0; tmp_i<15 && node_index; tmp_i++, node_index = node_index->next) ;
        for (tmp_i = 0; node_index && tmp_i <10; tmp_i++, node_index = node_index->next) {
          for (tmp_j=0; tmp_j < tmp_degree; tmp_j++) {
            tmp_count = 0;
            tmp_other_node = node_index->to_nodes[tmp_j];
            tmp_value = node_index->value;
            tmp_count = tmp_other_node->from_length;
            tmp_otherlist = tmp_other_node->from_values;
            if (!tmp_otherlist) {
              tmp_otherlist = tmp_other_node->from_values;
            }
            tmp_value = tmp_otherlist[tmp_count];
            tmp = tmp_other_node->coeffs[tmp_count];
          }
        }
        node_index = (node_t *)(*tmp_cur_node);
      }

      //////////////////////////

    case 2:
      to_curr_p = (node_t**)curr_pointer_addr;
      curr_p = (node_t*)(*to_curr_p);
      while (curr_p) {
        while (curr_p&&i++<STEP) {
          volatile double tmp_cur_value;
          volatile int tmp_from_count;
          double volatile *tmp_other_value;
          volatile double tmp_coeff;
          volatile double tmp_value;
          volatile int tmp_i;
          tmp_cur_value = *curr_p->value;
          tmp_from_count = curr_p->from_count-1;
          for (tmp_i = 0 ; tmp_i<tmp_from_count; tmp_i+=2) {
            tmp_other_value = curr_p->from_values[tmp_i];
            PUSHDATA(&(curr_p->coeffs[tmp_i]));
            if (tmp_other_value) PUSHDATA(tmp_other_value);
            tmp_other_value = curr_p->from_values[tmp_i+1];
            if (tmp_other_value) PUSHDATA(tmp_other_value);
            PUSHDATA(&(curr_p->from_values[tmp_i+1]));
          }
          curr_p = curr_p->next;
        }
        i = 0;
        curr_p = (node_t*)(*to_curr_p);
        if (curr_p) curr_p = curr_p->next;
      }
    default:
      continue;
    }

  }
}

inline void push_start(void* curr_addr, int p_flag)
{
  curr_pointer_addr  = (node_t**)curr_addr;
  push_flag = p_flag;
  i_sem++;
}
