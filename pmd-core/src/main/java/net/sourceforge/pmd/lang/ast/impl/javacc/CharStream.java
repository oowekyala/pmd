/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc;


import java.io.EOFException;
import java.io.IOException;

import net.sourceforge.pmd.util.document.FileLocation;
import net.sourceforge.pmd.util.document.TextDocument;

/**
 * PMD flavour of character streams used by JavaCC parsers.
 *
 * TODO for when all JavaCC languages are aligned:
 * * rename methods to match decent naming conventions
 * * move to impl.javacc package
 */
public final class CharStream {

    private final JavaccTokenDocument tokenDoc;
    private final EscapeTracker.Cursor cursor;

    private CharStream(JavaccTokenDocument tokenDoc, EscapeTracker.Cursor cursor) {
        this.tokenDoc = tokenDoc;
        this.cursor = cursor;
    }

    /**
     * Create a new char stream for the given document.
     */
    public static CharStream create(JavaccTokenDocument doc) throws IOException {
        try (EscapeAwareReader reader = doc.newReader(doc.getTextDocument().getText())) {
            reader.translate();
            return new CharStream(doc, reader.escapes.new Cursor(reader.input));
        }
    }

    /**
     * Returns the next character from the input. After a {@link #backup(int)},
     * some of the already read chars must be spit out again.
     *
     * @return The next character
     *
     * @throws EOFException Upon EOF
     */
    public char readChar() throws EOFException {
        return cursor.next();
    }


    /**
     * Calls {@link #readChar()} and returns its value, marking its position
     * as the beginning of the next token. All characters must remain in
     * the buffer between two successive calls to this method to implement
     * backup correctly.
     */
    public char BeginToken() throws EOFException {
        cursor.mark();
        return cursor.next();
    }


    /**
     * Returns a string made up of characters from the token mark up to
     * to the current buffer position.
     */
    public String GetImage() {
        StringBuilder sb = new StringBuilder();
        cursor.markToString(sb);
        return sb.toString();
    }


    /**
     * Returns an array of characters that make up the suffix of length 'len' for
     * the current token. This is used to build up the matched string
     * for use in actions in the case of MORE. A simple and inefficient
     * implementation of this is as follows :
     *
     * <pre>{@code
     * String t = tokenImage();
     * return t.substring(t.length() - len).toCharArray();
     * }</pre>
     *
     * @param len Length of the returned array
     *
     * @return The suffix
     *
     * @throws IndexOutOfBoundsException If len is greater than the length of the
     *                                   current token
     */
    public char[] GetSuffix(int len) {
        String t = GetImage();
        return t.substring(t.length() - len).toCharArray();
    }


    public void appendSuffix(StringBuilder sb, int len) {
        String t = GetImage();
        sb.append(t, t.length() - len, t.length());
    }


    /**
     * Pushes a given number of already read chars into the buffer.
     * Subsequent calls to {@link #readChar()} will read those characters
     * before proceeding to read the underlying char stream.
     *
     * <p>A lexer calls this method if it has already read some characters,
     * but cannot use them to match a (longer) token. So, they will
     * be used again as the prefix of the next token.
     *
     * @throws AssertionError If the requested amount is greater than the
     *                        number of read chars
     */
    public void backup(int amount) {
        cursor.backup(amount);
    }

    /** Returns the column number of the last character for the current token. */
    public int getEndColumn() {
        return endLocation().getEndColumn();
    }


    /** Returns the line number of the last character for current token. */
    public int getEndLine() {
        return endLocation().getEndLine();
    }


    private FileLocation endLocation() {
        TextDocument textDoc = tokenDoc.getTextDocument();
        return textDoc.toLocation(textDoc.createRegion(getEndOffset(), 0));
    }


    /** Returns the start offset of the current token (in the original source), inclusive. */
    public int getStartOffset() {
        return cursor.markOutOffset();
    }


    /** Returns the end offset of the current token (in the original source), exclusive. */
    public int getEndOffset() {
        return cursor.curOutOffset();
    }


    /**
     * Returns the token document for the tokens being built. Having it
     * here is the most convenient place for the time being.
     */
    public JavaccTokenDocument getTokenDocument() {
        return tokenDoc;
    }

}