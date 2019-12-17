/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc;

import java.io.EOFException;
import java.io.IOException;

import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.ast.CharStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.OffsetAwareReader;

/**
 * An implementation of {@link CharStream} which can wrap any {@link OffsetAwareReader}.
 * This balances responsibilities clearly:
 * <ul>
 * <li>The {@link OffsetAwareReader} is responsible for escaping, IO
 * buffering, and position retrieval.</li>
 * <li>This instance is responsible only for conforming to JavaCC's
 * {@link CharStream} specification, ie buffering the current token
 * and backtracking within its boundaries.</li>
 * </ul>
 */
public class CharStreamImpl implements CharStream {

    private static final int DEFAULT_INITIAL_BUFSIZE = 64;
    private static final int BUFFER_SIZE_INCREMENT = 64;


    /** Token document for the produced tokens. */
    private final JavaccTokenDocument document;

    /** Reader outputting translated chars. */
    private final OffsetAwareReader reader;


    /** Position in {@link #buffer} right before the next char to read. */
    private int bufpos = -1;
    /**
     * Buffer for translated chars. This needs to fit the whole current
     * token ({@link #BeginToken()}), so is grown when the current token
     * overflows. When {@link #BeginToken()} is called, previous contents
     * of the buffer may be overwritten.
     */
    private char[] buffer;

    /**
     * Last valid index in the {@link #buffer}. Crossing it means
     * refilling the buffer.
     */
    private int maxNextCharInd = 0;

    /**
     * Number of chars in the {@link #buffer} that the {@link #reader}
     * is ahead of the {@link #bufpos}. The bufpos is set back by {@link #backup(int)},
     * and then, calls to {@link #readChar()} read from the buffer and not
     * the stream while we catch up.
     */
    private int inBuf = 0;

    /** Current size of the {@link #buffer}. */
    private int bufsize;

    /**
     * Number of slots in the {@link #buffer} that may be overwritten by reads.
     * When {@link #BeginToken()} is called, the chars of the previous tokens
     * are freed to be overwritten.
     */
    private int available;

    /** Index in the {@link #buffer} of the start of the current token. */
    private int tokenBegin;


    /** Current output offset. */
    private int curOffset = -1;

    /** Output offset of the start of the current token. */
    private int tokenStartOffset;

    private boolean inputConsumed = false;

    public CharStreamImpl(OffsetAwareReader dstream, int buffersize, JavaccTokenDocument document) {
        AssertionUtil.requireOver1("buffer size", buffersize);
        AssertionUtil.requireParamNotNull("document", document);

        this.reader = dstream;
        this.bufsize = buffersize;
        this.available = buffersize;
        this.buffer = new char[buffersize];
        this.document = document;
    }

    public CharStreamImpl(OffsetAwareReader dstream, JavaccTokenDocument document) {
        this(dstream, DEFAULT_INITIAL_BUFSIZE, document);
    }


    @Override
    public JavaccTokenDocument getTokenDocument() {
        return document;
    }


    @Override
    public char readChar() throws IOException {
        if (inBuf > 0) {
            --inBuf;

            ++bufpos;
            if (bufpos == bufsize) {
                bufpos = 0;
            }

            return buffer[bufpos];
        }

        ++bufpos;
        ++curOffset;
        if (bufpos >= maxNextCharInd) {
            fillBuff();
        }


        return buffer[bufpos];
    }

    private void fillBuff() throws IOException {
        if (inputConsumed) {
            --bufpos;
            if (tokenBegin == -1) {
                tokenBegin = bufpos;
            }
            throw new EOFException();
        }

        ensureCapacity();

        assert available >= maxNextCharInd : "Capacity";

        try {

            int read = reader.read(buffer, maxNextCharInd, available - maxNextCharInd);
            if (read <= 0) {
                throw new EOFException();
            } else {
                maxNextCharInd += read;
            }
        } catch (EOFException e) {
            this.inputConsumed = true;
            --bufpos;
            if (tokenBegin == -1) {
                tokenBegin = bufpos;
            }
            try {
                reader.close();
            } catch (IOException ioe) {
                e.addSuppressed(ioe);
            }
            throw e;
        }
    }

    private void ensureCapacity() {
        if (maxNextCharInd == available) {
            if (available == bufsize) {
                if (tokenBegin > BUFFER_SIZE_INCREMENT) {
                    maxNextCharInd = 0;
                    bufpos = 0;
                    available = tokenBegin;
                } else if (tokenBegin < 0) {
                    maxNextCharInd = 0;
                    bufpos = 0;
                } else {
                    expandBuff(false);
                }
            } else if (available > tokenBegin) {
                available = bufsize;
            } else if ((tokenBegin - available) < BUFFER_SIZE_INCREMENT) {
                expandBuff(true);
            } else {
                available = tokenBegin;
            }
        }
    }


    protected void expandBuff(boolean wrapAround) {
        char[] newbuffer = new char[bufsize + BUFFER_SIZE_INCREMENT];

        if (wrapAround) {
            System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
            System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
            buffer = newbuffer;

            bufpos += bufsize - tokenBegin;
            maxNextCharInd = bufpos;
        } else {
            System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
            buffer = newbuffer;

            bufpos -= tokenBegin;
            maxNextCharInd = bufpos;
        }


        bufsize += BUFFER_SIZE_INCREMENT;
        available = bufsize;
        tokenBegin = 0;
    }

    @Override
    public char BeginToken() throws IOException {
        tokenBegin = -1;
        if (curOffset >= 0) {
            reader.releaseBefore(curOffset - inBuf);
        }
        tokenStartOffset = outputEndOffset();
        char c = readChar();
        tokenBegin = bufpos;
        return c;
    }

    @Override
    public int getEndColumn() {
        return reader.getColumn(outputEndOffset());
    }

    @Override
    public int getEndLine() {
        return reader.getLine(outputEndOffset());
    }

    @Override
    public int getStartOffset() {
        return reader.getInputOffset(tokenStartOffset);
    }

    @Override
    public int getEndOffset() {
        return reader.getInputOffset(outputEndOffset());
    }

    /** Exclusive */
    public int outputEndOffset() {
        return curOffset - inBuf + 1;
    }

    @Override
    public void backup(int amount) {
        assert
            amount <= availableBacktrack()
            : "Cannot backtrack by " + amount + " characters, only " + availableBacktrack() + " are buffered";

        inBuf += amount;
        bufpos -= amount;
        if (bufpos < 0) {
            bufpos += bufsize;
        }
    }

    private int availableBacktrack() {
        if (bufpos >= tokenBegin) {
            return bufpos - tokenBegin + 1;
        } else {
            return (bufsize - tokenBegin) + bufpos + 1;
        }
    }

    @Override
    public String GetImage() {
        assert tokenBegin >= 0 : "No marked token";

        if (bufpos >= tokenBegin) {
            return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
        } else {
            return new String(buffer, tokenBegin, bufsize - tokenBegin)
                + new String(buffer, 0, bufpos + 1);
        }
    }

    @Override
    public char[] GetSuffix(int len) {
        char[] ret = new char[len];

        if ((bufpos + 1) >= len) {
            System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
        } else {
            System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0, len - bufpos - 1);
            System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
        }

        return ret;
    }


}
