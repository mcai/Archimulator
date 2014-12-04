/*----------------------------------------------------------------------
  File    : Scanner.java
  Contents: scanner (lexical analysis of a character stream)
  Author  : Christian Borgelt
  History : 2004.05.10 file created from file scan.c
            2004.05.11 function to put back token renamed to pushBack
            2004.05.20 line number reporting added
            2004.05.25 ungetToken added as a synonym for pushBack
            2005.02.21 some utility functions added
            2005.03.01 optional newline token added
            2006.02.02 token T_DASH (undirected edge '--') added
            2006.10.05 adapted to class StringReader, javadoc comments
            2007.02.05 function format and table scftab added
            2007.02.08 function close added
----------------------------------------------------------------------*/
package archimulator.util.ai.aco.v2;

import java.io.*;

/*--------------------------------------------------------------------*/

/**
 * Class for simple, fairly generic scanners (tokenizers).
 * An input stream or a string is broken into tokens, which
 * are returned one by one. The tokens that are recognized
 * a fairly common set that fits a variety of applications.
 *
 * @author Christian Borgelt
 * @since 2004.05.10
 */
/*--------------------------------------------------------------------*/
public class Scanner {

  /*------------------------------------------------------------------*/
  /*  constants                                                       */
  /*------------------------------------------------------------------*/
    /**
     * token: end of file
     */
    public static final int T_EOF = 256;
    /**
     * token: number (floating point)
     */
    public static final int T_NUM = 257;
    /**
     * token: identifier or string
     */
    public static final int T_ID = 258;
    /**
     * token: right arrow '-&gt;'
     */
    public static final int T_RGT = 259;
    /**
     * token: left  arrow '&lt;-'
     */
    public static final int T_LFT = 260;
    /**
     * token: dash '--'
     */
    public static final int T_DASH = 261;
    /**
     * token: two character comparison operator
     */
    public static final int T_CMP = 262;

    /**
     * character class: illegal character
     */
    private static final int C_ILLEGAL = 0;
    /**
     * character class: white space, e.g. ' '
     */
    private static final int C_SPACE = 1;
    /**
     * character class: letter or underscore '_'
     */
    private static final int C_LETTER = 2;
    /**
     * character class: digit
     */
    private static final int C_DIGIT = 3;
    /**
     * character class: point, that is, '.'
     */
    private static final int C_POINT = 4;
    /**
     * character class: sign, that is, '+' or '-'
     */
    private static final int C_SIGN = 5;
    /**
     * character class: slash, '/'
     */
    private static final int C_SLASH = 6;
    /**
     * character class: quote, e.g. '"' '`'
     */
    private static final int C_QUOTE = 7;
    /**
     * character class: comparison operator, e.g. '&lt;'
     */
    private static final int C_CMPOP = 8;
    /**
     * character class: active characters, e.g. ','
     */
    private static final int C_ACTIVE = 9;

    /**
     * scanner state: skipping white space
     */
    private static final int S_SPACE = 0;
    /**
     * scanner state: reading identifier
     */
    private static final int S_ID = 1;
    /**
     * scanner state: number, digit read
     */
    private static final int S_NUMDIG = 2;
    /**
     * scanner state: number, decimal point read
     */
    private static final int S_NUMPT = 3;
    /**
     * scanner state: number, digit and decimal point read
     */
    private static final int S_FRAC = 4;
    /**
     * scanner state: exponent, indicator read
     */
    private static final int S_EXPIND = 5;
    /**
     * scanner state: exponent, sign read
     */
    private static final int S_EXPSGN = 6;
    /**
     * scanner state: exponent, digit read
     */
    private static final int S_EXPDIG = 7;
    /**
     * scanner state: sign read
     */
    private static final int S_SIGN = 8;
    /**
     * scanner state: comparison operator
     */
    private static final int S_CMPOP = 9;
    /**
     * scanner state: quoted string
     */
    private static final int S_STRING = 10;
    /**
     * scanner state: escaped character
     */
    private static final int S_ESC = 11;
    /**
     * scanner state: octal  number, one digit read
     */
    private static final int S_OCT1 = 12;
    /**
     * scanner state: octal  number, two digits read
     */
    private static final int S_OCT2 = 13;
    /**
     * scanner state: hexadecimal number, one digit read
     */
    private static final int S_HEX1 = 14;
    /**
     * scanner state: hexadecimal number, two digits read
     */
    private static final int S_HEX2 = 15;
    /**
     * scanner state: slash read
     */
    private static final int S_SLASH = 16;
    /**
     * scanner state: C++ style comment
     */
    private static final int S_CPPCOM = 17;
    /**
     * scanner state: C style comment
     */
    private static final int S_CCOM1 = 18;
    /**
     * scanner state: C style comment, possible end
     */
    private static final int S_CCOM2 = 19;
    /**
     * scanner state: C style comment, possible start
     */
    private static final int S_CCOM3 = 20;

    private static final char scftab[] = {
          /* NUL  SOH  STX  ETX  EOT  ENQ  ACK  BEL */
  /* 00 */    2, 2, 2, 2, 2, 2, 2, 'a',
          /*  BS   HT   LF   VT   FF   CR   SO   SI */
            'b', 't', 'n', 'v', 'f', 'r', 2, 2,
          /* DLE  DC1  DC2  DC3  DC4  NAK  SYN  ETB */
  /* 10 */    2, 2, 2, 2, 2, 2, 2, 2,
          /* CAN   EM  SUB  ESC   FS   GS   RS   US */
            2, 2, 2, 2, 2, 2, 2, 2,
          /* ' '  '!'  '"'  '#'  '$'  '%'  '&'  ''' */
  /* 20 */    1, 1, '"', 1, 1, 1, 1, 1,
          /* '('  ')'  '*'  '+'  ','  '-'  '.'  '/' */
            1, 1, 1, 0, 1, 0, 0, 1,
          /* '0'  '1'  '2'  '3'  '4'  '5'  '6'  '7' */
  /* 30 */    0, 0, 0, 0, 0, 0, 0, 0,
          /* '8'  '9'  ':'  ';'  '<'  '='  '>'  '?' */
            0, 0, 1, 1, 1, 1, 1, 1,
          /* '@'  'A'  'B'  'C'  'D'  'E'  'F'  'G' */
  /* 40 */    1, 0, 0, 0, 0, 0, 0, 0,
          /* 'H'  'I'  'J'  'K'  'L'  'M'  'N'  'O' */
            0, 0, 0, 0, 0, 0, 0, 0,
          /* 'P'  'Q'  'R'  'S'  'T'  'U'  'V'  'W' */
  /* 50 */    0, 0, 0, 0, 0, 0, 0, 0,
          /* 'X'  'Y'  'Z'  '['  '\'  ']'  '^'  '_' */
            0, 0, 0, 1, '\\', 1, 1, 0,
          /* '`'  'a'  'b'  'c'  'd'  'e'  'f'  'g' */
  /* 60 */    1, 0, 0, 0, 0, 0, 0, 0,
          /* 'h'  'i'  'j'  'k'  'l'  'm'  'n'  'o' */
            0, 0, 0, 0, 0, 0, 0, 0,
          /* 'p'  'q'  'r'  's'  't'  'u'  'v'  'w' */
  /* 70 */    0, 0, 0, 0, 0, 0, 0, 0,
          /* 'x'  'y'  'z'  '{'  '|'  '}'  '~'  DEL */
            0, 0, 0, 1, 1, 1, 1, 2,
  /* 80 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* 90 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* a0 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* b0 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* c0 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* d0 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* e0 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
  /* f0 */    1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1};

    /**
     * table of character classes (for the 256 ASCII characters)
     */
    private static final char ccltab[] = {
          /* NUL  SOH  STX  ETX  EOT  ENQ  ACK  BEL */
  /* 00 */    0, 0, 0, 0, 0, 0, 0, 0,
          /*  BS   HT   LF   VT   FF   CR   SO   SI */
            0, 1, 1, 1, 1, 1, 0, 0,
          /* DLE  DC1  DC2  DC3  DC4  NAK  SYN  ETB */
  /* 10 */    0, 0, 0, 0, 0, 0, 0, 0,
          /* CAN   EM  SUB  ESC   FS   GS   RS   US */
            0, 0, 0, 0, 0, 0, 0, 0,
          /* ' '  '!'  '"'  '#'  '$'  '%'  '&'  ''' */
  /* 20 */    1, 8, 7, 9, 9, 9, 9, 7,
          /* '('  ')'  '*'  '+'  ','  '-'  '.'  '/' */
            9, 9, 9, 5, 9, 5, 4, 6,
          /* '0'  '1'  '2'  '3'  '4'  '5'  '6'  '7' */
  /* 30 */    3, 3, 3, 3, 3, 3, 3, 3,
          /* '8'  '9'  ':'  ';'  '<'  '='  '>'  '?' */
            3, 3, 9, 9, 8, 8, 8, 9,
          /* '@'  'A'  'B'  'C'  'D'  'E'  'F'  'G' */
  /* 40 */    0, 2, 2, 2, 2, 2, 2, 2,
          /* 'H'  'I'  'J'  'K'  'L'  'M'  'N'  'O' */
            2, 2, 2, 2, 2, 2, 2, 2,
          /* 'P'  'Q'  'R'  'S'  'T'  'U'  'V'  'W' */
  /* 50 */    2, 2, 2, 2, 2, 2, 2, 2,
          /* 'X'  'Y'  'Z'  '['  '\'  ']'  '^'  '_' */
            2, 2, 2, 9, 9, 9, 9, 2,
          /* '`'  'a'  'b'  'c'  'd'  'e'  'f'  'g' */
  /* 60 */    7, 2, 2, 2, 2, 2, 2, 2,
          /* 'h'  'i'  'j'  'k'  'l'  'm'  'n'  'o' */
            2, 2, 2, 2, 2, 2, 2, 2,
          /* 'p'  'q'  'r'  's'  't'  'u'  'v'  'w' */
  /* 70 */    2, 2, 2, 2, 2, 2, 2, 2,
          /* 'x'  'y'  'z'  '{'  '|'  '}'  '~'  DEL */
            2, 2, 2, 9, 9, 9, 9, 0,
  /* 80 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* 90 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* a0 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* b0 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* c0 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* d0 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* e0 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
  /* f0 */    0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0,
            C_ILLEGAL};

  /*------------------------------------------------------------------*/
  /*  instance variables                                              */
  /*------------------------------------------------------------------*/
    /**
     * reader for the input file
     */
    private Reader reader;
    /**
     * buffer for the current token value
     */
    private StringBuffer buf;
    /**
     * buffer for pushed back character
     */
    private int pbchar;
    /**
     * flag for pushed back token
     */
    private boolean pbtok;
    /**
     * flag for newline tokens (if true, the newline character
     * is treated as a token, otherwise as white space)
     */
    private boolean nltok;

    /**
     * current token type
     */
    public int ttype;
    /**
     * current token value
     */
    public String value;
    /**
     * current line number (determined by counting newline characters)
     */
    public int line;

  /*------------------------------------------------------------------*/

    /**
     * Create a scanner working on a reader.
     *
     * @param reader the reader to work on
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public Scanner(Reader reader) {                             /* --- create a scanner */
        this.reader = reader;       /* store the reader to work on */
        this.buf = new StringBuffer();
        this.pbchar = -1;           /* there is no buffered character */
        this.pbtok = false;        /* and no buffered token */
        this.nltok = false;        /* newline '\n' is not a token */
        this.line = 1;            /* initialize the line number */
    }  /* Scanner() */

  /*------------------------------------------------------------------*/

    /**
     * Create a scanner working on a string.
     *
     * @param string the string to scan
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public Scanner(String string) {
        this(new StringReader(string));
    }

  /*------------------------------------------------------------------*/

    /**
     * Create a scanner working on an input stream.
     *
     * @param stream the input stream to scan
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public Scanner(InputStream stream) {
        this(new InputStreamReader(stream));
    }

  /*------------------------------------------------------------------*/

    /**
     * Get a string stating the current line number.
     * Useful for error reporting.
     *
     * @return a string stating the current line number
     * in the format "(line xxx)"
     * @since 2004.05.20 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public String lno() {
        return " (line " + this.line + ")";
    }

  /*------------------------------------------------------------------*/

    /**
     * Set whether the newline character should be treated as a token
     * or not. By default it is treated as a whitespace character.
     *
     * @param flag if <code>true</code>, the newline character is
     *             treated as a token, otherwise as whitespace.
     * @since 2005.03.01 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void nlToken(boolean flag) {
        this.nltok = flag;
    }

  /*------------------------------------------------------------------*/

    /**
     * Get the next character from the reader, respecting a possibly
     * existing pushed back character and counting newline characters.
     *
     * @return the next character or <code>-1</code> if there is
     * no next character (end of file)
     * @throws IOException if an I/O error occurs
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    private int getc() throws IOException {                             /* --- get the next character */
        int c;                      /* the next character */

        if (this.pbchar >= 0) {     /* check for a buffered character */
            c = (char) this.pbchar;
            this.pbchar = -1;
            return c;
        }
        c = this.reader.read();     /* read the next character */
        if (c == '\n') this.line++; /* count a new line and */
        return c;                   /* return the next character */
    }  /* getc() */

  /*------------------------------------------------------------------*/

    /**
     * Unget the last character read.
     * Only one character can be pushed back into the input;
     * two consecutive calls have the same effect as a single call.
     *
     * @param c the character to return to the input
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    private void ungetc(int c) {
        this.pbchar = c;
    }

  /*------------------------------------------------------------------*/

    /**
     * Get the next token of the input stream or the string.
     *
     * @return the code of the next token
     * @throws IOException if an I/O error occurs or the scanner
     *                     detects an error in the format of the stream
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public int nextToken() throws IOException {                             /* --- get next token */
        int c, ccl;                 /* character and character class */
        int quote = 0;              /* quote at the start of a string */
        int ec = 0;              /* escaped character */
        int state = 0;              /* state of automaton */
        int level = 0;              /* comment nesting level */

        if (this.pbtok) {           /* check for a pushed back token */
            this.pbtok = false;
            return this.ttype;
        }

        this.buf.setLength(0);      /* clear the token buffer */
        while (true) {              /* read loop */
            c = this.getc();        /* get character and character class */
            ccl = (c < 0) ? -1 : ccltab[c];

            switch (state) {          /* evaluate state of automaton */

                case S_SPACE:           /* --- skip white space */
                    switch (ccl) {        /* evaluate character category */
                        case C_SPACE:
                            if ((c == '\n') && this.nltok) {
                                this.buf.append((char) c);
                                this.value = this.buf.toString();
                                return this.ttype = c;
                            }    /* if newline is a token, return it, */
                            break;            /* otherwise do nothing */
                        case C_LETTER:
                            this.buf.append((char) c);
                            state = S_ID;
                            break;
                        case C_DIGIT:
                            this.buf.append((char) c);
                            state = S_NUMDIG;
                            break;
                        case C_POINT:
                            this.buf.append((char) c);
                            state = S_NUMPT;
                            break;
                        case C_SIGN:
                            this.buf.append((char) c);
                            state = S_SIGN;
                            break;
                        case C_CMPOP:
                            this.buf.append((char) c);
                            state = S_CMPOP;
                            break;
                        case C_QUOTE:
                            quote = c;
                            state = S_STRING;
                            break;
                        case C_SLASH:
                            state = S_SLASH;
                            break;
                        case C_ACTIVE:
                            this.buf.append((char) c);
                            this.value = this.buf.toString();
                            return this.ttype = c;
                        case -1:
                            this.value = "<eof>";
                            return this.ttype = T_EOF;
                        default:
                            this.buf.append((char) c);
                            this.value = this.buf.toString();
                            throw new IOException("illegal character '"
                                    + (char) c + "'" + this.lno());
                    }
                    break;

                case S_ID:              /* --- identifier (letter read) */
                    if ((ccl == C_LETTER) /* if another letter */
                            || (ccl == C_DIGIT)  /* or a digit */
                            || (ccl == C_POINT)  /* or a decimal point */
                            || (ccl == C_SIGN)) { /* or a sign follows */
                        this.buf.append((char) c);
                        break;
                    }            /* buffer character */
                    this.ungetc(c);       /* put back last character */
                    this.value = this.buf.toString();
                    return this.ttype = T_ID;  /* return 'identifier' */

                case S_NUMDIG:          /* --- number (digit read) */
                    this.buf.append((char) c);  /* buffer character */
                    if (ccl == C_DIGIT)  /* if another digit follows, */
                        break;              /* do nothing */
                    if (ccl == C_POINT) { /* if a decimal point follows, */
                        state = S_FRAC;
                        break;
                    } /* go to 'fraction' state */
                    if ((c == 'e')        /* if an exponent indicator follows */
                            || (c == 'E')) {     /* (lower- or uppercase), */
                        state = S_EXPIND;
                        break;
                    } /* go to 'exponent' state */
                    if ((ccl == C_LETTER) /* if a letter */
                            || (ccl == C_SIGN)) { /* or a sign follows, */
                        state = S_ID;
                        break;/* go to 'identifier' state */
                    }                     /* otherwise */
                    this.ungetc(c);       /* put back last character */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_NUM;   /* return 'number' */

                case S_NUMPT:           /* --- number (point read) */
                    this.buf.append((char) c);    /* buffer character */
                    if (ccl == C_DIGIT) {       /* if a digit follows, */
                        state = S_FRAC;
                        break;
                    }   /* go to 'fraction' state */
                    if ((ccl == C_LETTER) /* if a letter */
                            || (ccl == C_POINT)  /* or a decimal point */
                            || (ccl == C_SIGN)) { /* or a sign follows */
                        state = S_ID;
                        break;/* go to 'identifier' state */
                    }                     /* otherwise */
                    this.ungetc(c);       /* put back last character, */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_ID;  /* return 'identifier' */

                case S_FRAC:            /* --- number (digit & point read) */
                    this.buf.append((char) c);    /* buffer character */
                    if (ccl == C_DIGIT)  /* if another digit follows, */
                        break;              /* do nothing else */
                    if ((c == 'e')        /* if an exponent indicator follows, */
                            || (c == 'E')) {     /* (lower- or uppercase), */
                        state = S_EXPIND;
                        break;
                    } /* go to exponent state */
                    if ((ccl == C_LETTER) /* if a letter */
                            || (ccl == C_POINT)  /* or a decimal point */
                            || (ccl == C_SIGN)) { /* or a sign follows, */
                        state = S_ID;
                        break;/* go to 'identifier' state */
                    }                     /* otherwise */
                    this.ungetc(c);       /* put back last character, */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_NUM; /* and return 'number' */

                case S_EXPIND:          /* --- exponent (indicator read) */
                    this.buf.append((char) c);    /* buffer character */
                    if (ccl == C_SIGN) {        /* if a sign follows, */
                        state = S_EXPSGN;
                        break;
                    } /* go to 2nd 'exponent' state */
                    if (ccl == C_DIGIT) {       /* if a digit follows, */
                        state = S_EXPDIG;
                        break;
                    } /* go to 3rd 'exponent' state */
                    if ((ccl == C_LETTER) /* if a letter */
                            || (ccl == C_POINT)) {/* or a decimal point follows */
                        state = S_ID;
                        break;/* go to 'identifier' state */
                    }                     /* otherwise */
                    this.ungetc(c);       /* put back last character, */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_ID;  /* return 'identifier' */

                case S_EXPSGN:          /* --- exponent (sign read) */
                    this.buf.append((char) c);    /* buffer character */
                    if (ccl == C_DIGIT) {       /* if a digit follows, */
                        state = S_EXPDIG;
                        break;
                    } /* do nothing else */
                    if ((ccl == C_LETTER) /* if a letter */
                            || (ccl == C_POINT)  /* or a decimal point */
                            || (ccl == C_SIGN)) { /* or a sign follows */
                        state = S_ID;
                        break;/* go to 'identifier' state */
                    }                     /* otherwise */
                    this.ungetc(c);       /* put back last character, */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_ID;  /* return 'identifier' */

                case S_EXPDIG:          /* --- exponent (digit read) */
                    this.buf.append((char) c);    /* buffer character */
                    if (ccl == C_DIGIT)  /* if another digit follows, */
                        break;              /* do nothing else */
                    if ((ccl == C_LETTER) /* if a letter */
                            || (ccl == C_POINT)  /* or a decimal point */
                            || (ccl == C_SIGN)) { /* or a sign follows, */
                        state = S_ID;
                        break;/* go to 'identifier' state */
                    }                     /* otherwise */
                    this.ungetc(c);       /* put back last character, */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_NUM; /* and return 'number' */

                case S_SIGN:            /* --- number (sign read) */
                    this.buf.append((char) c);    /* buffer character */
                    if (ccl == C_DIGIT) {       /* if a digit follows, */
                        state = S_NUMDIG;
                        break;
                    } /* go to 'number' state */
                    if (ccl == C_POINT) {       /* if a decimal point follows, */
                        state = S_NUMPT;
                        break;
                    }  /* go to fraction state */
                    if ((c == '-')        /* if a '-' follows and previous */
                            && (this.buf.charAt(0) == '-')) {     /* char was '-' */
                        this.value = this.buf.toString();
                        return this.ttype = T_DASH;
                    }                     /* token is '--' */
                    if ((c == '>')        /* if a '>' follows and previous */
                            && (this.buf.charAt(0) == '-')) {     /* char was '-' */
                        this.value = this.buf.toString();
                        return this.ttype = T_RGT;
                    }                     /* token is '->' */
                    if ((ccl == C_LETTER)        /* if a letter */
                            || (ccl == C_SIGN)) {       /* or a sign follows, */
                        state = S_ID;
                        break;
                    }     /* go to 'identifier' state */
                    this.ungetc(c);       /* put back last character, */
                    this.value = this.buf.substring(0, this.buf.length() - 1);
                    return this.ttype = T_ID;  /* return 'identifier' */

                case S_CMPOP:           /* --- comparison operator read */
                    if ((c == '-')        /* if minus sign follows and prev. */
                            && (this.buf.charAt(0) == '<')) {  /* character was '<' */
                        this.buf.append('-');
                        this.ttype = T_LFT;
                    } else if (c == '=') {  /* if an equal sign follows */
                        this.buf.append('=');
                        this.ttype = T_CMP;
                    } else {                /* if anything else follows */
                        this.ungetc(c);
                        this.ttype = this.buf.charAt(0);
                    }
                    this.value = this.buf.toString();
                    return this.ttype;    /* and return the token read */

                case S_STRING:          /* --- quoted string */
                    if ((c == '\n') || (c < 0)) /* if end of line or file */
                        throw new IOException("unterminated string" + this.lno());
                    if (c != quote) {     /* if not at end of string */
                        if (c == '\\') {    /* if escaped character follows, */
                            state = S_ESC;
                            break;
                        }/* go to escaped char state */
                        this.buf.append((char) c);
                        break;
                    }                     /* otherwise buffer character */
                    this.value = this.buf.toString();
                    return this.ttype = T_ID;    /* return 'identifier' */

                case S_ESC:             /* --- after '\' in quoted string */
                    if ((c >= '0') && (c <= '7')) {        /* if octal digit, */
                        ec = c - '0';
                        state = S_OCT1;
                        break;
                    }/* evaluate digit  */
                    if (c == 'x') {       /* if hexadecimal character code, */
                        state = S_HEX1;
                        break;
                    } /* go to hexadecimal evaluation */
                    switch (c) {          /* evaluate character after '\' */
                        case 'b':
                            c = '\b';
                            break;
                        case 'f':
                            c = '\f';
                            break;
                        case 'n':
                            c = '\n';
                            break;
                        case 'r':
                            c = '\r';
                            break;
                        case 't':
                            c = '\t';
                            break;
                        case '\n':
                            c = -1;
                            break;
                        default:
                            break;
                    }                     /* get escaped character and */
                    if (c >= 0) this.buf.append((char) c);
                    state = S_STRING;     /* store it and then */
                    break;                /* return to quoted string state */

                case S_OCT1:            /* --- escaped octal number 1 */
                    if ((c >= '0')        /* if an octal digit follows, */
                            && (c <= '7')) {     /* evaluate it */
                        ec = ec * 8 + c - '0';
                        state = S_OCT2;
                        break;
                    }
                    this.ungetc(c);       /* otherwise put back last character */
                    this.buf.append((char) ec);
                    state = S_STRING;     /* store escaped character and */
                    break;                /* return to quoted string state */

                case S_OCT2:            /* --- escaped octal number 2 */
                    if ((c >= '0') || (c <= '7'))
                        ec = ec * 8 + c - '0'; /* if octal digit, evaluate it */
                    else this.ungetc(c);  /* otherwise put back last character */
                    this.buf.append((char) ec);
                    state = S_STRING;     /* store escaped character and */
                    break;                /* return to quoted string state */

                case S_HEX1:            /* --- escaped hexadecimal number 1 */
                    if (ccl == C_DIGIT) { /* if hexadecimal digit, evaluate it */
                        ec = c - '0';
                        state = S_HEX2;
                        break;
                    }
                    if ((c >= 'a') && (c <= 'f')) {
                        ec = c - 'a' + 10;
                        state = S_HEX2;
                        break;
                    }
                    if ((c >= 'A') && (c <= 'F')) {
                        ec = c - 'A' + 10;
                        state = S_HEX2;
                        break;
                    }
                    this.ungetc(c);       /* otherwise put back last character */
                    this.buf.append('x'); /* store escaped character ('x') and */
                    state = S_STRING;     /* return to quoted string state */
                    break;

                case S_HEX2:            /* --- escaped hexadecimal number 2 */
                    if (ccl == C_DIGIT)   /* if hexadecimal digit, evaluate it */
                        ec = ec * 16 + c - '0';
                    else if ((c >= 'a') && (c <= 'f'))
                        ec = ec * 16 + c - 'a' + 10;
                    else if ((c >= 'A') && (c <= 'F'))
                        ec = ec * 16 + c - 'A' + 10;
                    else this.ungetc(c);  /* otherwise put back last character */
                    this.buf.append((char) ec);
                    state = S_STRING;     /* store escaped character and */
                    break;                /* return to quoted string state */

                case S_SLASH:           /* --- slash '/' */
                    if (c == '/') {       /* if C++ style comment, then */
                        state = S_CPPCOM;
                        break;
                    }   /* skip to end of line */
                    if (c == '*') {       /* if C style comment */
                        state = S_CCOM1;
                        break;
                    }    /* return to 1st state */
                    this.ungetc(c);       /* otherwise put back last character */
                    this.value = "/";     /* store character in buffer */
                    return this.ttype = '/';     /* return `character' */

                case S_CPPCOM:          /* --- C++ style comment */
                    if ((c == '\n')       /* if at end of line */
                            || (c < 0))         /* or at end of file */
                        state = S_SPACE;    /* return to white space skipping */
                    break;                /* (skip to end of line) */

                case S_CCOM1:           /* --- C style comment 1 */
                    if (c < 0)      /* if end of file, abort */
                        throw new IOException("unterminated comment" + this.lno());
                    if (c == '*')    /* if possibly 'end of comment', */
                        state = S_CCOM2;    /* go to 2nd 'comment' state */
                    else if (c == '/')    /* if possibly 'start of comment', */
                        state = S_CCOM3;    /* go to 3rd 'comment' state */
                    break;

                case S_CCOM2:           /* --- C style comment 2 */
                    if (c < 0)      /* if end of file, abort */
                        throw new IOException("unterminated comment" + this.lno());
                    if (c == '/') {  /* if end of comment found */
                        if (--level <= 0) state = S_SPACE;
                        else state = S_CCOM1;
                    } else if (c != '*')    /* if end of comment impossible */
                        state = S_CCOM1;    /* return to comment skipping */
                    break;                /* (possible start of comment) */

                case S_CCOM3:           /* --- C style comment 3 */
                    if (c < 0)      /* if end of file, abort */
                        throw new IOException("unterminated comment" + this.lno());
                    if (c == '*') {  /* if start of comment found */
                        level++;
                        state = S_CCOM1;
                    } else if (c != '/')    /* if start of comment impossible */
                        state = S_CCOM1;    /* return to comment skipping */
                    break;                /* (possible end of comment) */

                default:                /* if state is illegal, abort */
                    throw new IOException("illegal scanner state" + this.lno());

            }  /* switch() */
        }  /* while(1) */
    }  /* nextToken() */

  /*------------------------------------------------------------------*/

    /**
     * Push back the last token into the input.
     * Only the last token can be pushed back into the input;
     * two consecutive calls have the same effect as a single call.
     *
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void ungetToken() {
        this.pbtok = true;
    }

  /*------------------------------------------------------------------*/

    /**
     * Push back the last token into the input.
     * Only the last token can be pushed back into the input;
     * two consecutive calls have the same effect as a single call.
     *
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void pushBack() {
        this.pbtok = true;
    }

  /*------------------------------------------------------------------*/

    /**
     * Get the next token and compare it to an expected character.
     *
     * @param c the expected character
     * @throws IOException if the expected character is not found
     * @since 2005.02.21 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void getChar(char c) throws IOException {                             /* --- check for a specific character */
        if (this.nextToken() != c)  /* check the next token */
            throw new IOException(((c != '\n') ? "'" + c + "'" : "'\\n'")
                    + " expected instead of '"
                    + this.value + "'" + this.lno());
    }  /* getChar() */

  /*------------------------------------------------------------------*/

    /**
     * Get the next token and compare it to an expected identifier.
     * <p>If <code>null</code> is passed for the expected identifier,
     * it is only checked whether an identifier follows, while the
     * value of this identifier is ignored.</p>
     *
     * @param id the expected identifier
     * @throws IOException if the expected identifier is not found
     * @since 2005.02.21 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void getID(String id) throws IOException {                             /* --- check for an identifier */
        if ((this.nextToken() != T_ID)  /* check the next token */
                && (this.ttype != T_NUM))
            throw new IOException("identifier expected instead of '"
                    + this.value + "'" + this.lno());
        if (id == null) return;     /* if a specific id is given, */
        if (!id.equals(this.value)) /* compare the token value to it */
            throw new IOException("'" + id + "' expected instead of '"
                    + this.value + "'" + this.lno());
    }  /* getID() */

  /*------------------------------------------------------------------*/

    /**
     * Get the next token and check whether it is an identifier.
     *
     * @throws IOException if the next token is not an identifier
     * @since 2005.02.21 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void getID() throws IOException {
        getID(null);
    }

  /*------------------------------------------------------------------*/

    /**
     * Get the next token and check whether it is an number.
     *
     * @throws IOException if the next token is not a number
     * @since 2005.02.21 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void getNumber() throws IOException {                             /* --- check for a number */
        if (this.nextToken() != T_NUM) /* check the next token */
            throw new IOException("number expected instead of \""
                    + this.value + "\"" + this.lno());
    }  /* getNumber() */

  /*------------------------------------------------------------------*/

    /**
     * Get the next token and check whether it is an number.
     *
     * @throws IOException if an I/O error occurs
     * @since 2007.02.08 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public void close() throws IOException {
        this.reader.close();
    }

  /*------------------------------------------------------------------*/

    /**
     * Format a string so that it can be read as a token.
     *
     * @param s the string to format
     * @return the formatted string
     * @since 2007.02.05 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public static final String format(String s, boolean quotes) {                             /* --- format a string */
        int i, n;          /* loop variables, buffer */
        int c, t;          /* a character and its class */
        StringBuffer b;             /* formatted string */

        n = s.length();             /* get the number of characters */
        if (n <= 0) quotes = true;  /* an empty string needs quotes */
        if (!quotes) {              /* if quotes are not mandatory, */
            for (i = 0; i < n; i++)   /* traverse the given string */
                if (scftab[s.charAt(i)] != 0) {
                    quotes = true;
                    break;
                }/* if a character needs quotes, */
        }                           /* set the quotes flag and abort */
        if (!quotes) return s;      /* if no quotes needed, abort */
        b = new StringBuffer("\""); /* start with an opening quote */
        for (i = 0; i < n; i++) {   /* traverse the given string */
            c = s.charAt(i) & 0xff;   /* get the next character */
            t = scftab[c];            /* and its character class */
            if (t < 2)           /* if it is a normal character, */
                b.append((char) c);      /* just store it */
            else if (t > 2) {         /* if it is an ANSI escape character, */
                b.append('\\');
                b.append((char) t);
            }      /* store it as '\c' */ else {                    /* if it is any other character */
                b.append('\\');
                b.append('x');
                t = c >> 4;
                b.append((char) ((t > 9) ? (t - 10 + 'a') : (t + '0')));
                t = c & 0xf;
                b.append((char) ((t > 9) ? (t - 10 + 'a') : (t + '0')));
            }                         /* store the character code */
        }                           /* as a hexadecimal number */
        b.append('"');              /* store the closing quote */
        return b.toString();        /* return the formatted string */
    }  /* format() */

  /*------------------------------------------------------------------*/

    /**
     * Main function for testing basic functionality.
     *
     * @since 2004.05.10 (Christian Borgelt)
     */
  /*------------------------------------------------------------------*/
    public static void main(String args[]) {                             /* --- main function for testing */
        Scanner scanner;            /* scanner for testing */
        int token;              /* next token */

        try {                       /* test the scanner */
            switch (args.length) {    /* evaluate the argument list */
                case 0:
                    scanner = new Scanner("This is a test");
                    break;
                case 1:
                    scanner = new Scanner(args[0]);
                    break;
                default:
                    scanner = new Scanner(new FileReader(args[0]));
                    break;
            }
            while (true) {            /* scan string until the end */
                token = scanner.nextToken();
                if ((token < 0) || (token == T_EOF)) break;
                System.out.println(token + " : " + scanner.value);
            }
        }                       /* print token and value */ catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }  /* main() */

}  /* class Scanner */
