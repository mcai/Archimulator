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

    int lookahead = 0;
    int tempValue = 0;

    lookahead = LOOKAHEAD;

    while(1)
    {
        while(!global_tmp);

        tmp = *global_tmp;
        key = (unsigned int)global_inserted;

#ifdef MIPS_1
        asm ("addiu $0,$0,3724");
#endif

        while(tmp)
        {
#ifdef MIPS_1
            asm ("addiu %0,$2,0" : "=r" (tempValue));
            printf("tempValue: %d\n",tempValue);

            asm ("addiu $0,$0,3820");
//            asm ("addiu %0,$2,0" : "=r" (lookahead));
            asm ("addiu %0,$0,0" : "=r" (lookahead));

            printf("lookahead: %d\n",lookahead);

            asm ("addiu $2,%0,0" : :"r"(tempValue));
#endif

            for(i = 0; tmp && i < LOOKAHEAD; i++, tmp = tmp->next);

            for(i = 0; tmp && i < STRIDE; i++, tmp = tmp->next)
            {
                if(tmp && tmp != global_inserted)
                {
                    hash = tmp->edgehash;

#ifdef MIPS_1
                    asm ("addiu $0,$0,3725");
#endif

                    j = (hash->mapfunc)(key);

#ifdef MIPS_1
                    asm ("addiu $0,$0,3726");
#endif

                    for(ent = hash->array[j];
						ent &&
							ent->key != key;
						ent = ent->next);

#ifdef MIPS_1
                    asm ("addiu $0,$0,3727");
#endif
                }
            }

            tmp = *global_tmp;
        }

#ifdef MIPS_1
        asm ("addiu $0,$0,3728");
#endif
    }
}

