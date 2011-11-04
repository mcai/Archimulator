#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "mhandle.h"
#include "eval.h"


/* BITMAP HANDLING */

#define BITMAP_TYPE(NAME, SIZE) \
	unsigned char NAME[((SIZE)+7)>>3]
#define BITMAP_INIT(NAME, SIZE) { \
	int irg; \
	for (irg = 0; irg < (((SIZE)+7)>>3); irg++) \
	NAME[irg] = 0; }
#define BITMAP_SET(NAME, BIT) \
	(NAME[(BIT)>>3]|=1<<((BIT)&7))
#define BITMAP_CLEAR(NAME, BIT) \
	(NAME[(BIT)>>3]&=~(1<<((BIT)&7)))
#define BITMAP_IS_SET(NAME, BIT) \
	(NAME[(BIT)>>3]&(1<<((BIT)&7)))
#define BITMAP_SET_RANGE(NAME, LO, HI) { \
	int irg; \
	for (irg = (LO); irg <= (HI); irg++) \
	BITMAP_SET((NAME), irg); }




/* FINITE STATE MACHINE */

struct fsm_trans_t {
	BITMAP_TYPE(sym, 256);
	int dest;
};

struct fsm_state_t {
	int final;
	int trans_count;
	struct fsm_trans_t *trans;
};

struct fsm_t {
	int tok_id;
	int state_count;
	struct fsm_state_t *state;
};




/* LEXICAL ANALYZER */

#define ITEM_BLOCK		10

struct lex_t {
	int fsm_count;
	struct fsm_t *fsm;
};


/* aborts program with message if 'condition' is not satisfied */
void lex_assert(int condition, char *msg)
{
	if (condition)
		return;
	fprintf(stderr, "lexical analyzer panic: %s\n", msg);
	exit(-1);
}


struct lex_t *lex_create()
{
	struct lex_t *lex;
	lex = (struct lex_t *) calloc(1, sizeof(struct lex_t));
	assert(lex);
	return lex;
}


void lex_free(struct lex_t *lex)
{
	int i, j;
	for (i = 0; i < lex->fsm_count; i++) {
		for (j = 0; j < lex->fsm[i].state_count; j++)
			free(lex->fsm[i].state[j].trans);
		free(lex->fsm[i].state);
	}
	free(lex->fsm);
	free(lex);
}


/* skips spaces in text buffer */
void skip_spc(char **text)
{
	while (**text == ' ' || **text == '\n' || **text == '\t')
		(*text)++;
}


/* reads a number from a text buffer */
int read_number(char **text)
{
	int result = 0;
	skip_spc(text);
	lex_assert(**text >= '0' && **text <= '9', "number expected");
	while (**text >= '0' && **text <= '9') {
		result = result * 10 + (int) (**text - '0');
		(*text)++;
	}
	return result;
}


void lex_add_fsm(struct lex_t *lex, char *fsm_def, int tok_id)
{
	struct fsm_t *fsm;
	
	/* assigns memory for a new fsm */
	if (!(lex->fsm_count % ITEM_BLOCK))
		lex->fsm = (struct fsm_t *) realloc(lex->fsm, (lex->fsm_count + ITEM_BLOCK)
				* sizeof(struct fsm_t));
	lex_assert(lex->fsm != NULL, "realloc lex failed");
	lex->fsm_count++;
	fsm = &lex->fsm[lex->fsm_count - 1];
	fsm->state_count = 0;
	fsm->tok_id = tok_id;
	fsm->state = NULL;
	
	/* reads states in fsm */
	lex_assert(!read_number(&fsm_def), "first state not zero");
	do {
		struct fsm_state_t *state;
		
		/* creates new state */
		if (!(fsm->state_count % ITEM_BLOCK))
			fsm->state = realloc(fsm->state, (fsm->state_count + ITEM_BLOCK)
					* sizeof(struct fsm_state_t));
		lex_assert(fsm->state != NULL, "realloc fsm failed");
		fsm->state_count++;
		state = &fsm->state[fsm->state_count - 1];
		state->trans_count = 0;
		state->trans = NULL;
		
		/* final state? */
		if (*fsm_def == ':') {
			fsm_def++;
			state->final = 0;
		} else if (*fsm_def == '*' && fsm_def[1] == ':') {
			fsm_def += 2;
			state->final = 1;
		} else {
			lex_assert(0, "':' or '*:' expected");
		}
		
		/* transitions */
		skip_spc(&fsm_def);
		while (*fsm_def) {
			int dest;
			struct fsm_trans_t *trans;
			
			/* reads dest of transition (can be new state) */
			dest = read_number(&fsm_def);
			if (*fsm_def == ':' || *fsm_def == '*') {	/* new state */
				lex_assert(dest == fsm->state_count, "state order");
				break;
			}
			
			/* creates new transition */
			if (!(state->trans_count % ITEM_BLOCK))
				state->trans = realloc(state->trans, (state->trans_count +
						ITEM_BLOCK) * sizeof(struct fsm_trans_t));
			lex_assert(state->trans != NULL, "realloc trans failed");
			state->trans_count++;
			trans = &state->trans[state->trans_count - 1];
			trans->dest = dest;
			BITMAP_INIT(trans->sym, 256);
			
			/* symbols in the transition */
			skip_spc(&fsm_def);
			lex_assert(*(fsm_def++) == '[', "'[' expected");
			while (*fsm_def != ']') {
				
				/* unexpected end */
				lex_assert(*fsm_def, "unterminated char class");
				
				/* range */
				if (*fsm_def == '-') {
					lex_assert(fsm_def[1], "ussage of '-' character");
					BITMAP_SET_RANGE(trans->sym, (int) *(fsm_def - 1),
							(int) *(fsm_def + 1));
					fsm_def += 2;
					continue;
				}
				
				/* escape character */
				if (*fsm_def == '\\') {
					fsm_def++;
					lex_assert(*fsm_def, "ussage of escape character");
					BITMAP_SET(trans->sym, (int) *fsm_def);
					fsm_def++;
					continue;
				}
				
				/* normal character */
				BITMAP_SET(trans->sym, (int) *fsm_def);
				fsm_def++;
			}
			
			/* skips closing bracket and spaces */
			fsm_def++;
			skip_spc(&fsm_def);
			
		};
		
	} while (*fsm_def);
}


void lex_dump(struct lex_t *lex, FILE *stream)
{
	int i, j, k, l;
	char syms[257];
	struct fsm_t *fsm;
	struct fsm_state_t *state;
	struct fsm_trans_t *trans;
	
	fprintf(stream, "Lexical analyzer contents:\n");
	for (i = 0; i < lex->fsm_count; i++) {
		
		fsm = &lex->fsm[i];
		fprintf(stream, "FSM #%d (%d states):\n", i, fsm->state_count);
		
		/* states */
		for (j = 0; j < fsm->state_count; j++) {
			state = &fsm->state[j];
			fprintf(stream, "\t%sstate %d (%d transitions):\n",
				state->final ? "final " : "", j, state->trans_count);
			
			/* transitions */
			for (k = 0; k < state->trans_count; k++) {
				trans = &state->trans[k];
				
				/* symbols */
				for (i = 0; i <= 257; i++)
					syms[i] = 0;
				for (l = 0; l < 256; l++)
					if (BITMAP_IS_SET(trans->sym, l))
						syms[strlen(syms)] = (char) l;
				fprintf(stream, "\t\ttrans[%02d]: dest=%02d, syms={%s}\n",
					k, trans->dest, syms);
			}
		}
	}
}


#define MAX_TOKEN_SIZE 128
char *lex_get_token(struct lex_t *lex, char *text, char *token, int *tok_id)
{
	int *curr_state;	/* current states for each fsm */
	int *final_pos;		/* array of positions when final states were reached */
	int fsm_final;		/* last fsm which reached a final state */
	int fsm_left;		/* fsm still active */
	int length;		/* length of the token */
	int i, j;
	char *s;
	struct fsm_state_t *state;
	struct fsm_trans_t *trans;
	
	/* initialization */
	fsm_final = -1;
	fsm_left = lex->fsm_count;
	curr_state = calloc(lex->fsm_count, sizeof(int));
	final_pos = calloc(lex->fsm_count, sizeof(int));
	lex_assert(curr_state && final_pos, "lex_get_token: out of memory");
	for (i = 0; i < lex->fsm_count; i++)
		final_pos[i] = -1;
	
	/* analysis */
	s = text;
	while (*s && fsm_left) {
		
		/* consumes char in all fsms */
		for (i = 0; i < lex->fsm_count; i++) {
			
			/* if this fsm was invalidated, go to next one */
			if (curr_state[i] == -1)
				continue;
			
			/* examine transitions */
			state = &lex->fsm[i].state[curr_state[i]];
			for (j = 0; j < state->trans_count; j++) {
				trans = &state->trans[j];
				
				/* transition occurs */
				if (BITMAP_IS_SET(trans->sym, (int) *s)) {
					curr_state[i] = trans->dest;
					
					/* reached a final state */
					if (lex->fsm[i].state[trans->dest].final) {
						final_pos[i] = s - text;
						fsm_final = i;
					}
					break;
				}
			}
			
			/* if no transition for this symbol, invalidate fsm */
			if (j == state->trans_count) {
				curr_state[i] = -1;
				fsm_left--;
			}
		}
		
		/* next char */
		s++;
	}
	
	/* no token found? */
	if (fsm_final == -1) {
		*tok_id = 0;
		*token = '\0';
		free(curr_state);
		free(final_pos);
		return text;
	}
	
	/* token too large? */
	length = final_pos[fsm_final] + 1;
	lex_assert(length < MAX_TOKEN_SIZE, "token too large; increase MAX_TOKEN_SIZE");
	
	/* return obtained token */
	*tok_id = lex->fsm[fsm_final].tok_id;
	strncpy(token, text, length);
	token[length] = '\0';
	free(curr_state);
	free(final_pos);
	return text + length;
}




/* EXPRESION EVALUATOR */

struct eval_t {
	struct lex_t *lex;
	id_value_fn id_value;
};


enum token_kind_t {
	tok_none = 0,
	tok_num,
	tok_id,
	tok_add,
	tok_sub,
	tok_mult,
	tok_div,
	tok_par_open,
	tok_par_close
};


struct token_t {
	int kind;
	char str[MAX_TOKEN_SIZE];
	double value;
};


/* aborts program with message if 'condition' is not satisfied */
void eval_assert(int condition, char *msg)
{
	if (condition)
		return;
	fprintf(stderr, "expresion evaluator panic: %s\n", msg);
	exit(-1);
}


struct eval_t *eval_create(id_value_fn id_value)
{
	struct eval_t *eval;
	eval = calloc(1, sizeof(struct eval_t));
	eval_assert(eval != NULL, "out of memory");
	eval->id_value = id_value;
	
	/* creates lexical analyzer */
	eval->lex = lex_create();
	lex_add_fsm(eval->lex, "0*: 0 [ \n\t]", tok_none);
	lex_add_fsm(eval->lex,
		"0*: 0 [0-9] 1 [.]"
		"1:  2 [0-9]"
		"2*: 2 [0-9] 3 [eE]"
		"3:  4 [+\\-] 5 [0-9]"
		"4:  5 [0-9]"
		"5*: 5 [0-9]", tok_num);
	lex_add_fsm(eval->lex,
		"0:  1 [a-zA-Z_]"
		"1*: 1 [a-zA-Z0-9_.:]", tok_id);
	lex_add_fsm(eval->lex, "0: 1 [+] 1*:", tok_add);
	lex_add_fsm(eval->lex, "0: 1 [\\-] 1*:", tok_sub);
	lex_add_fsm(eval->lex, "0: 1 [*] 1*:", tok_mult);
	lex_add_fsm(eval->lex, "0: 1 [/] 1*:", tok_div);
	lex_add_fsm(eval->lex, "0: 1 [(] 1*:", tok_par_open);
	lex_add_fsm(eval->lex, "0: 1 [)] 1*:", tok_par_close);
	return eval;
}


void eval_free(struct eval_t *eval)
{
	lex_free(eval->lex);
	free(eval);
}


/* functions used by 'eval_expr' */

/* create a new token with a value */
void new_token(struct token_t *t, double value)
{
	t->kind = tok_num;
	t->value = value;
	t->str[0] = 0;
}


/* read a new token from 'expr' */
void get_token(struct eval_t *eval, char **expr, struct token_t *t)
{
	new_token(t, 0.0);
	do {
		*expr = lex_get_token(eval->lex, *expr, t->str, &t->kind);
	} while (t->str[0] && !t->kind);
}


#define PUSH(TOK) do { \
	if (stack_count == stack_size) { \
		stack_size = stack_size * 2 + ITEM_BLOCK; \
		stack = realloc(stack, stack_size * sizeof(struct token_t)); \
		eval_assert(stack != NULL, "realloc failed"); \
	} \
	stack[stack_count++] = (TOK); \
} while (0)
#define POP(N) do { \
	assert(stack_count >= (N)); \
	stack_count -= (N); \
} while (0)
#define TOP(N) (stack[stack_count - 1 - (N)])
#define EXPR3(KIND1, KIND2, KIND3) \
	(stack_count >= 3 && \
	TOP(2).kind == (KIND1) && \
	TOP(1).kind == (KIND2) && \
	TOP(0).kind == (KIND3))
#define EXPR2(KIND1, KIND2) \
	(stack_count >= 2 && \
	TOP(1).kind == (KIND1) && \
	TOP(0).kind == (KIND2))


double eval_expr(struct eval_t *eval, char *expr)
{
	struct token_t *stack = NULL;	/* stack of tokens */
	int stack_size = 0;		/* size of the stack */
	int stack_count = 0;		/* num of tokens in the stack */
	
	struct token_t t, curr, next;
	double result = 0.0;
	
	get_token(eval, &expr, &next);
	while (next.kind) {
		
		/* extracts new token */
		curr = next;
		get_token(eval, &expr, &next);
		
		/* calculates double value of the token */
		switch (curr.kind) {
				
			case tok_num:
				sscanf(curr.str, "%lf", &curr.value);
				break;
				
			case tok_id:
				curr.value = eval->id_value(curr.str);
				curr.kind = tok_num;
				break;
				
			default:
				break;
		}
		PUSH(curr);
		
		/* reduces as many times as possible */
		do {
			
			/* multiplication/division */
			if (EXPR3(tok_num, tok_mult, tok_num) || 
				EXPR3(tok_num, tok_div, tok_num))
			{
				new_token(&t, TOP(1).kind == tok_mult ?
					TOP(2).value * TOP(0).value :
					TOP(2).value / TOP(0).value);
				POP(3);
				PUSH(t);
				continue;
			}
			
			/* addition/subtraction */
			if ((EXPR3(tok_num, tok_add, tok_num) || 
				EXPR3(tok_num, tok_sub, tok_num)) &&
				next.kind != tok_mult && next.kind != tok_div)
			{
				new_token(&t, TOP(1).kind == tok_add ?
					TOP(2).value + TOP(0).value :
					TOP(2).value - TOP(0).value);
				POP(3);
				PUSH(t);
				continue;
			}
			
			/* brackets */
			if (EXPR3(tok_par_open, tok_num, tok_par_close))
			{
				new_token(&t, TOP(1).value);
				POP(3);
				PUSH(t);
				continue;
			}
			
			/* sign */
			if (EXPR2(tok_sub, tok_num) &&
				!EXPR3(tok_num, tok_sub, tok_num))
			{
				new_token(&t, -TOP(0).value);
				POP(2);
				PUSH(t);
				continue;
			}
			
			/* reduce result */
			if (stack_count == 1 && !next.kind && TOP(0).kind == tok_num) {
				result = TOP(0).value;
				POP(1);
				break;
			}
			
			/* exit 'while' if arrived here */
			break;
		
		} while (1);
		
	};
	
	free(stack);
	return result;
}
