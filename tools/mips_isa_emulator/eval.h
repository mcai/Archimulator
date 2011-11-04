#ifndef EVAL_H
#define EVAL_H

#include <stdio.h>



/* LEXICAL ANALYZER */

/* creation & destruction of a lex analyzer */
struct lex_t *lex_create();
void lex_free(struct lex_t *lex);

/* Add a finite state machine to a lexical analyzer.
 * When extracting a token, the analyzer will return the one which
 * corresponds to the fsm that extracted the longest token.
 * Example of 'fsm_def' for a fsm that recognizes real numbers:
 *    "0*: 0 [0-9] 1 [.]"
 *    "1:  2 [0-9]"
 *    "2*: 2 [0-9] 3 [eE]"
 *    "3:  4 [+\\-] 5 [0-9]"
 *    "4:  5 [0-9]"
 *    "5*: 5 [0-9]"
 */
void lex_add_fsm(struct lex_t *lex, char *fsm_def, int tok_id);

/* Dumps the finite state machines of a lexical analyzer */
void lex_dump(struct lex_t *lex, FILE *stream);

/* Extract a token from a string using the lex analyzer.
 * The returned value is the position of the next token.
 * The token is copied to 'token' and its kind is stored in 'tok_id'.
 * If no token is found, 'tok_id' is set to 0, 'token' is set to ""
 * and a pointer to 'text' is returned.
 */
char *lex_get_token(struct lex_t *lex, char *text, char *token, int *tok_id);




/* EXPRESION EVALUATOR */

/* function to resolve identifiers' value */
typedef double (*id_value_fn)(char *id);

/* creation & destruction of an expresion evaluator */
struct eval_t *eval_create(id_value_fn id_value);
void eval_free(struct eval_t *eval);

/* Evaluates an expresion returning the result value.
 * If the expresion is syntactically incorrect, 0.0 is returned */
double eval_expr(struct eval_t *eval, char *expr);

#endif
