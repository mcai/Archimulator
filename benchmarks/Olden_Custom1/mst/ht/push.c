#include "mst.h"
#include "hash.h"

#include "push.h"

Vertex global_inserted;
Vertex* volatile global_tmp;

//volatile int global_flag = 0; //TODO: to be removed

inline void push_thread_func()
{
    Hash hash;
    int i, j;
    unsigned int key;
    Vertex volatile tmp;
    HashEntry volatile ent;

    while(1)
    {
        while(!global_tmp);

        tmp = *global_tmp;
        key = (unsigned int)global_inserted;

        asm ("addiu $0,$0,3724");

        while(tmp)
        {
            for(i = 0; tmp && i < LOOKAHEAD; i++, tmp = tmp->next);

            for(i = 0; tmp && i < STRIDE; i++, tmp = tmp->next)
            {
                if(tmp && tmp != global_inserted)
                {
                    hash = tmp->edgehash;

                    asm ("addiu $0,$0,3725");

                    j = (hash->mapfunc)(key);

                    asm ("addiu $0,$0,3726");

                    for(ent = hash->array[j];
						ent &&
							ent->key != key;
						ent = ent->next);

                    asm ("addiu $0,$0,3727");
                }
            }

            tmp = *global_tmp;
        }

        asm ("addiu $0,$0,3728");
    }
}

//inline void push_thread_func1()
//{
//    Hash hash;
//    int i, j;
//    unsigned int key;
//    Vertex volatile tmp;
//    HashEntry volatile ent;
//
//    while(1)
//    {
//        while(!global_tmp);
//
//        tmp = *global_tmp;
//
//        asm ("addiu $0,$0,3724");
//
//        while(global_flag && tmp)
//        {
//            for(i = 0; global_flag && tmp && i < LOOKAHEAD; i++, tmp = tmp->next);
//
//            for(i = 0; global_flag && tmp && i < STRIDE; i++, tmp = tmp->next)
//            {
//                if(global_flag && tmp && tmp != global_inserted)
//                {
//                    hash = tmp->edgehash;
//                    key = (unsigned int)global_inserted;
//
//                    asm ("addiu $0,$0,3725");
//
//                    j = (hash->mapfunc)(key);
//
//                    asm ("addiu $0,$0,3726");
//
//                    for(ent = hash->array[j];
//						ent &&
//							ent->key != key;
//						ent = ent->next);
//
//                    asm ("addiu $0,$0,3727");
//                }
//            }
//
//            tmp = *global_tmp;
//        }
//
//        asm ("addiu $0,$0,3728");
//    }
//}

