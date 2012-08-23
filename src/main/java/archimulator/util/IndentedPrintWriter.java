package archimulator.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndentedPrintWriter extends PrintWriter {
    private static char[] ls = System.getProperty("line.separator").toCharArray();
    private int truncatedNL;
    private char[] spc;
    private boolean start = true;
    private char indentationChar;
    private boolean autoFlush;

    /**
     * Create a new writer over the given one, with the given indentation value.
     *
     * @param writer      the writer wrapped by this IndentationPrintWriter
     * @param indent the indentation level
     */
    public IndentedPrintWriter(Writer writer, int indent) {
        this(writer, indent, false);
    }

    /**
     * Create a new writer over the given stream, with the given indentation value.
     *
     * @param out       the stream wrapped by this IndentationPrintWriter
     * @param indent    the indentation level
     * @param autoFlush if true, the println() methods will flush the output buffer
     */
    public IndentedPrintWriter(OutputStream out, int indent, boolean autoFlush) {
        this(new BufferedWriter(new OutputStreamWriter(out)), indent, autoFlush);
    }

    /**
     * Create a new writer over the given stream, with the given indentation value,
     * and no autoflush.
     *
     * @param out    the stream wrapped by this IndentationPrintWriter
     * @param indent the indentation level
     */
    public IndentedPrintWriter(OutputStream out, int indent) {
        this(out, indent, false);
    }

    /**
     * Create a new writer over the given one, with the given indentation value.
     *
     * @param writer         the writer wrapped by this IndentationPrintWriter
     * @param indent    the indentation level
     * @param autoFlush if true, the println() methods will flush the output buffer
     */
    public IndentedPrintWriter(Writer writer, int indent, boolean autoFlush) {
        super(writer, autoFlush);
        this.truncatedNL = 0;
        this.indentationChar = ' ';
        this.autoFlush = autoFlush;
        setIndentation(indent);
    }

    /**
     * Create a new writer over the given one, with the default indentation value,
     * and no autoflush.
     *
     * @param writer the writer wrapped by this IndentationPrintWriter
     */
    public IndentedPrintWriter(Writer writer) {
        this(writer, 0);
    }

    /**
     * Create a new writer over the given stream, with the default indentation value.
     *
     * @param out       the stream wrapped by this IndentationPrintWriter
     * @param autoFlush if true, the println() methods will flush the output buffer
     */
    public IndentedPrintWriter(OutputStream out, boolean autoFlush) {
        this(out, 0, autoFlush);
    }


    /**
     * Create a new writer over the given stream, with the default indentation value
     * and no autoflush.
     *
     * @param out the stream wrapped by this IndentationPrintWriter
     */
    public IndentedPrintWriter(OutputStream out) {
        this(out, 0);
    }

    /**
     * Create a new writer over the given one, with the default indentation value.
     *
     * @param writer         the writer wrapped by this IndentationPrintWriter
     * @param autoFlush if true, the println() methods will flush the output buffer
     */
    public IndentedPrintWriter(Writer writer, boolean autoFlush) {
        this(writer, 0, autoFlush);
    }

    /**
     * Return the current indentation level
     *
     * @return the current indentation level
     */
    public int getIndentation() {
        return spc.length;
    }

    /**
     * Sets the current indentation level
     *
     * @param indent the desired indentation level
     */
    public synchronized void setIndentation(int indent) {
        if (indent < 0) throw new RuntimeException("Attmpting to set negative indentation");
        spc = new char[indent];
        Arrays.fill(spc, indentationChar);
    }

    /**
     * Increment the current indentation level of the given level.
     *
     * @param level the indentation level to be incremented
     */
    public void incrementIndentation(int level) {
        setIndentation(getIndentation() + level);
    }

    /**
     * Decrement the current indentation level of the given level.
     * 0 is the minimum level.
     *
     * @param level the indentation level to be reduced
     */
    public void decrementIndentation(int level) {
        setIndentation(getIndentation() - level);
    }

    /**
     * Increment the current indentation level
     */
    public void incrementIndentation() {
        incrementIndentation(1);
    }

    /**
     * Dencrement the current indentation level. 0 is the minimum level.
     */
    public void decrementIndentation() {
        decrementIndentation(1);
    }

    /**
     * Set the character used to indent to the given character
     *
     * @param c the character to use for indentation
     */
    public synchronized void setIndentationChar(char c) {
        this.indentationChar = c;
        setIndentation(getIndentation());
    }

    /**
     * Return the character used to indent to the given character
     *
     * @return the character to use for indentation
     */
    public char getIndentationChar() {
        return indentationChar;
    }

    /**
     * Write a substring of a string, of given length from a given offset
     */
    public void write(String s, int off, int len) {
        write(s.toCharArray(), off, len);
    }

    /**
     * Write a character
     */
    public void write(int c) {
        write(new char[]{(char) c}, 0, 1);
    }

    /**
     * Write a portion of character array, of given length from a given offset
     */
    public void write(char buf[], int off, int len) {
        synchronized (this) {
            if (start) {
                super.write(spc, 0, spc.length);
                start = false;
            }

            List<Integer> pos = new ArrayList<Integer>();
            int truncated = truncatedNL; // Remember if we start in a truncated-newline situation
            for (int i = off; i < off + len; i++) {
                if (isNL(buf, i, off + len)) pos.add(i);
            }
            int p1 = 0;
            String s;
            for (Object po : pos) {
                int p2 = (Integer) po;
                super.write(buf, p1, p2 - p1);
                super.write(ls, 0, ls.length);
                super.write(spc, 0, spc.length);
                p1 = p2 + ls.length - truncated;
                if (truncated != 0) truncated = 0; // Just the first time
            }
            super.write(buf, p1, off + len - p1);
            if (autoFlush) super.flush();
        }
    }

    /**
     * Checks if buf matches the line separator this.ls,
     * setting this.truncatedNL if a partial match exists
     * but the buffer portion is too short for a complete
     * match
     */
    private boolean isNL(char[] buf, int start, int end) {
        for (int i = truncatedNL; i < ls.length && i < end; i++) {
            if (buf[start + i - truncatedNL] != ls[i]) {
                if (truncatedNL != 0) truncatedNL = 0;
                return false;
            }
        }
        if (end - start + truncatedNL < ls.length) {
            truncatedNL = end - start;
            return false;
        }
        if (truncatedNL != 0) truncatedNL = 0;
        return true;
    }


    /**
     * Terminate the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public void println() {
        super.println();
        start = true;
    }
}