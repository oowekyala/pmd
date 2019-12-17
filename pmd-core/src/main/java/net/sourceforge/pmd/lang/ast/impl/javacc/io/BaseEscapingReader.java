/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

/**
 * Base class for {@link OffsetAwareReader}s that translate some escape sequences.
 */
@SuppressWarnings("PMD.AssignmentInOperand")
public abstract class BaseEscapingReader extends Reader implements OffsetAwareReader {

    private static final String INVALID_ESCAPE = "Invalid escape sequence at line %d, column %d: %s";

    /**
     * Default can be small, buffer should be sized according to the
     * maximum expected escape length.
     */
    private static final int DEFAULT_BUFSIZE = 60;

    /** Source of untranslated characters. */
    private final OffsetAwareReader input;

    /** Buffer for translated characters. */
    private char[] buffer;

    /**
     * Position the next char should be written to in the buffer. If
     * ahead > 0, then we read chars at a distance {@link #ahead} from
     * the bufpos.
     */
    private int bufpos = 0;

    /**
     * Amount of chars we should reinterpret in the buffer. When {@link #ahead}
     * crosses this, chars get passed to {@link #readImpl(int)} and
     * {@link #bufRead()} fetches chars from the buffer transparently.
     */
    private int retryFrom = 0;

    /** Offset from the {@link #bufpos} at which we should read the next char. */
    private int ahead = 0;

    /** Keep track of adjustments to make to the offsets, caused by unicode escapes. */
    private EscapeTracker escapes = new EscapeTracker();

    /** Index of the *next* char to read in the {@link #input}. */
    private int idxInInput;

    private boolean isEof;


    protected BaseEscapingReader(OffsetAwareReader input, int bufsize) {
        assert bufsize > 0;
        assert input != null;

        this.input = input;
        this.buffer = new char[bufsize];
    }

    protected BaseEscapingReader(OffsetAwareReader input) {
        this(input, DEFAULT_BUFSIZE);
    }

    @Override
    public final long skip(long n) throws IOException {
        ensureOpen();
        return super.skip(n);
    }

    @Override
    public final int read(char[] cbuf, int off, int len) throws IOException {
        ensureOpen();
        if (isEof) {
            return -1;
        }

        int read = 0;
        int i = off;
        final int end = off + len;
        while (i < end) {
            try {
                int c = read();
                if (c < 0) {
                    return read;
                }
                cbuf[i] = (char) c;
            } catch (EOFException e) {
                isEof = true;
                return read;
            }


            read++;
            i++;
        }
        return read;
    }

    @Override
    public final int read() throws IOException {
        ensureOpen();

        if (ahead > 0 && !isInRetry()) {
            // means the previous call to readImpl backtracked
            final char c = buffer[bufpos - ahead];
            ahead--;
            return c;
        } else if (isEof) {
            return -1;
        }

        if (ahead == 0) {
            clearBuf();
        }
        return readImpl(idxInInput);
    }

    /**
     * Read a char, possibly translating an escape. While we're not sure
     * if what we read is an escape or not, characters should be buffered
     * with {@link #bufRead()}.
     *
     * <p>If at some point we decide the read prefix is not an escape,
     * this method should call {@link #backtrack(int)}. Subsequent calls
     * to {@link #read()} will use the characters already in the buffer.
     *
     * <p>If we find an escape, {@link #recordEscape(int, int)} should
     * be called with the idxInInput parameter, and the translated char
     * should be returned.
     *
     * @param idxInInput Index in the input where we are at the start of this method
     *
     * @throws IOException if {@link #readByte()} throws
     * @throws IOException if an invalid escape is recorded
     */
    protected abstract int readImpl(int idxInInput) throws IOException;

    /**
     * Records an escape. This will update the translation of output to
     * input offsets.
     *
     * @param inputOffset Offset where the escape starts in the input
     * @param lenDiff     Difference between the length of the escape in the
     *                    source and the length of the character sequence the
     *                    escape translates to in the output document (typically 1).
     */
    protected final void recordEscape(int inputOffset, int lenDiff) {
        escapes.recordEscape(inputOffset, lenDiff);
    }


    /**
     * Backtrack the given amount in the translated plane. This must be
     * called if the reader has seen some chars that may start an escape,
     * but actually do not.
     *
     * <p>Part of the backtracked characters may be reinterpreted by
     * {@link #readImpl(int)}. For example, you may read '\', then '\'
     * and decide that the first '\' is not an escape, but the second
     * one may still start an escape. In that case call {@code backtrack(1, 1)}
     * and return '\' from {@link #readImpl(int)}.
     *
     * <p>OTOH, eg in Java, "\\u0" is not an escape (even number of backslashes).
     * If the second '\' was fed to {@link #readImpl(int)}, it would see
     * the suffix '\' 'u' '0', find an odd number of backslashes, and fail because
     * '\' 'u' '0' is not a valid unicode escape. Here, upon seeing the second
     * backslash, you should call {@link #backtrack(int) backtrack(1, 0)}.
     *
     * @param amount    Amount of characters that will be read from the buffer.
     *                  This is the offset of the first char to read, relative
     *                  to the end of the buffer. Eg if the buffer is "abc", an
     *                  amount of 2 will read back "bc".
     * @param retryFrom Size of the buffer suffix that may still be an escape.
     *                  Those characters will be reinterpreted by {@link #readImpl(int)}.
     *
     * @throws AssertionError If we were asked to backtrack more than we actually can
     */
    protected final void backtrack(int amount, int retryFrom) {
        assert 0 <= amount && amount < bufpos
            : "Wrong backtrack amount " + amount + ", only read " + bufpos + " chars";

        assert 0 <= retryFrom && retryFrom <= amount
            : "Wrong retry amount " + retryFrom + ", available " + amount;

        if (amount == 0) {
            return;
        }

        ahead += amount;

        if (retryFrom > 0) {
            this.retryFrom = retryFrom;
        }
    }

    /**
     * Backtrack the given amount in the translated plane.
     *
     * @see #backtrack(int, int)
     */
    protected final void backtrack(int amount) {
        backtrack(amount, 0);
    }

    /** Turns the buffer into a string. */
    protected final String bufferToString() {
        return isInRetry()
               ? new String(buffer, bufpos - retryFrom, bufpos)
               : new String(buffer, 0, bufpos);
    }


    protected IOException invalidEscape(Throwable cause, int idxInInput, String text) {
        int l = input.getLine(idxInInput);
        int c = input.getColumn(idxInInput);

        String message = String.format(INVALID_ESCAPE, l, c, text);

        return new IOException(message, cause);
    }

    /**
     * Read a new character from the input stream.
     *
     * @throws EOFException If eof is reached
     */
    private char readByte() throws IOException {
        int c = input.read();
        if (c < 0) {
            isEof = true;
            throw new EOFException();
        }
        idxInInput++;
        return (char) c;
    }

    /** Throw away the contents of the buffer. */
    private void clearBuf() {
        bufpos = 0;
    }

    /**
     * Append the given character into the buffer.
     */
    private char bufAppend(char c) {
        if (bufpos >= buffer.length) {
            growBuffer();
        }
        buffer[bufpos] = c;
        bufpos++;
        if (ahead > 0) {
            ahead++;
        }
        return c;
    }

    /**
     * Read the next character and appends it to the buffer.
     */
    protected final char bufRead() throws IOException {
        if (isInRetry()) {
            char c = buffer[bufpos - ahead];
            ahead--;
            if (ahead == 0) {
                retryFrom = 0;
            }
            return c;
        }
        return bufAppend(readByte());
    }

    /**
     * Returns true if we're {@link #bufRead()} should read from backtracked characters.
     * This situation is described on {@link #backtrack(int, int)}.
     */
    private boolean isInRetry() {
        return ahead > 0 && ahead <= retryFrom;
    }

    private void growBuffer() {
        char[] newBuf = new char[2 * (buffer.length + 1)];
        System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
        buffer = newBuf;
    }


    @Override
    public void close() throws IOException {
        this.bufpos = -1;
        this.buffer = null;
        this.input.close();
    }


    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (buffer == null) {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public final int getInputOffset(int outputOffset) {
        return input.getInputOffset(escapes.getInputOffset(outputOffset));
    }

    @Override
    public final void releaseBefore(int outputOffset) {
        escapes.forget(outputOffset);
        input.releaseBefore(escapes.getInputOffset(outputOffset));
    }

    @Override
    public final int getLine(int outputOffset) {
        return input.getLine(escapes.getInputOffset(outputOffset));
    }

    @Override
    public final int getColumn(int outputOffset) {
        return input.getColumn(escapes.getInputOffset(outputOffset));
    }

    /**
     * Records where escapes occurred in the input document. This is quite
     * an inefficient way to deal with it, yet in the common case where there
     * are few/no escapes, it's enough I think.
     *
     * <p>Only keeps track of a window of offsets (based on {@link OffsetAwareReader#releaseBefore(int)}).
     */
    private static class EscapeTracker {

        private static final int[] EMPTY = new int[0];
        /*
         * Threshold for the size of the {@link #inputOffsets} above
         * which they're pruned when {@link #forget(int)} is called.
         */
        //        private static final int FORGET_THRESHOLD = 60;

        /**
         * Offsets in the {@link #input} where a unicode escape occurred.
         * Represented as pairs (off, len) where
         * - off is the offset in the source file where the escape occurred
         * - len is the length difference between the source and output char
         */
        private int[] inputOffsets = EMPTY;
        /** Index of the next write in the {@link #inputOffsets}. */
        private int nextFreeIdx = 0;
        /** Offset that precedes the array. */
        private int arrayOffset = 0;
        //        private int checkpoint = 0;

        /**
         * Calls to this method must occur in source order (ie param
         * offsetInInput increases monotonically).
         */
        void recordEscape(int offsetInInput, int lenDiff) {
            if (nextFreeIdx + 1 >= inputOffsets.length) {
                // double capacity, add 1 to not stay stuck at zero
                int[] newOffsets = new int[2 * (inputOffsets.length + 1)];
                System.arraycopy(inputOffsets, 0, newOffsets, 0, inputOffsets.length);
                this.inputOffsets = newOffsets;
            }

            inputOffsets[nextFreeIdx++] = offsetInInput;
            inputOffsets[nextFreeIdx++] = lenDiff;
        }

        /**
         * Convert an offset in the translated file into an offset in
         * the untranslated input.
         */
        public int getInputOffset(int outputOffset) {
            // basically accumulate the lengths of all escapes occurring before the given translatedOffset
            int inOff = outputOffset;
            for (int i = 0; i < nextFreeIdx && inputOffsets[i] < inOff; i += 2) {
                inOff += inputOffsets[i + 1];
            }
            return inOff + arrayOffset;
        }


        @Override
        public String toString() {
            StringBuilder res = new StringBuilder("Escape set {");
            for (int i = 0; i < nextFreeIdx; i += 2) {
                res.append("(at=").append(inputOffsets[i]).append(", len=").append(inputOffsets[i + 1]).append("), ");
            }

            return res.append('}').toString();
        }

        public void forget(int outputOffset) {
            // TODO
            /*
            if (inputOffsets.length < FORGET_THRESHOLD
                || outputOffset == checkpoint) {
                return;
            }
            assert outputOffset > checkpoint;

            int i;
            int inOff = outputOffset;
            for (i = 0; i < nextFreeIdx && inputOffsets[i] < inOff; i += 2) {
                inOff += inputOffsets[i + 1];
            }
            // i is the first index to keep
            // inOff is the input offset for the outputOffset

            arrayOffset = inOff + arrayOffset;
            // shift the components to save to the beginning of the array
            int len = nextFreeIdx - i;
            System.arraycopy(inputOffsets, i, inputOffsets, 0, len);
            nextFreeIdx = len;
            checkpoint = outputOffset;
            */
        }
    }

}
