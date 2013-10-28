/* For copyright information, see olden_v1.0/COPYRIGHT */

/*****************************************************************
 * args.c:  Handles arguments to command line.                   *
 *          To be used with health.c.                            *
 *****************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include "health.h"

void dealwithargs(int argc, char *argv[]) { 
  if (argc < 4) {
    max_level = 5;
    max_time = 5000;
    seed = 40;
  } else {
    max_level = atoi(argv[1]);
    max_time = atol(argv[2]);
    seed = atol(argv[3]);
  }
}




