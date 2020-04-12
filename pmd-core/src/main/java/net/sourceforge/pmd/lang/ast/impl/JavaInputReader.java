/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl;

import static java.lang.Integer.min;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import net.sourceforge.pmd.util.StringUtil;

/**
 * An implementation of java.io.Reader that translates Java unicode escapes.
 * This implementation has efficient block IO but poor char-by-char performance.
 */
@SuppressWarnings("PMD.AssignmentInOperand")
public final class JavaInputReader extends Reader {

    /** Untranslated characters. */
    private char[] input;
    /** Position of the next char to read in the input. */
    private int bufpos;
    /** Keep track of adjustments to make to the offsets, caused by unicode escapes. */
    private final EscapeTracker escapes = new EscapeTracker();

    private int savedNotEscapeSpecialEnd = Integer.MAX_VALUE;

    public JavaInputReader(CharSequence input, int startIdxInclusive, int endIdxExclusive) {
        assert input != null;
        assert startIdxInclusive >= 0;
        assert endIdxExclusive >= 0;
        assert endIdxExclusive >= startIdxInclusive;

        int len = endIdxExclusive - startIdxInclusive;

        this.input = new char[len];
        input.toString().getChars(startIdxInclusive, endIdxExclusive, this.input, 0);
        bufpos = 0;
    }

    public JavaInputReader(CharSequence input) {
        this(input, 0, input.length());
    }


    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        ensureOpen();
        if (this.bufpos == input.length) {
            return -1;
        }

        int readChars = 0;
        while (readChars < len && this.bufpos < input.length) {
            int bpos = this.bufpos;
            int nextJump = gobbleMaxWithoutEscape(bpos, len - readChars);
            int newlyReadChars = nextJump - bpos;

            assert newlyReadChars >= 0 && (readChars + newlyReadChars) <= len;

            if (newlyReadChars != 0) {
                System.arraycopy(input, bpos, cbuf, off + readChars, newlyReadChars);
            } else if (nextJump == input.length) {
                // eof
                break;
            }
            readChars += newlyReadChars;
        }
        return readChars;
    }


    /**
     * Returns the max offset, EXclusive, with which we can cut the input
     * array from the bufpos to dump it into the output array. This sets
     * the bufpos to where we should start the next jump.
     */
    private int gobbleMaxWithoutEscape(final int bufpos, final int maxReadahead) throws IOException {
        int off = bufpos;
        int max = min(bufpos + maxReadahead, input.length);
        boolean noBackSlash = false;
        int notEscapeEnd = this.savedNotEscapeSpecialEnd;
        while (off < max && (noBackSlash = input[off] != '\\' || notEscapeEnd < off)) {
            off++;
        }

        if (noBackSlash) {
            this.bufpos = off;
            return off;
        }

        final int firstBslashOff = off;
        while (off < input.length && input[off] == '\\') {
            off++;
        }

        int bslashCount = off - firstBslashOff;
        // this condition is "is there an escape at offset firstBslashOff"
        if ((bslashCount & 1) == 1    // odd number of backslashes
            && off < input.length - 4 // at least 5 chars to form the escape ('u' + 4 hex digits)
            && input[off] == 'u') {   // the char after the last backslash is a 'u'

            replaceFirstBackslashWithEscape(firstBslashOff, off);
            this.savedNotEscapeSpecialEnd = Integer.MAX_VALUE;
            this.bufpos = off + 5;
            this.escapes.recordEscape(firstBslashOff, off + 5 - firstBslashOff);
            return firstBslashOff + 1;
        } else {
            // not an escape sequence
            int min = min(bufpos + maxReadahead, off);
            // save the number of backslashes that are part of the escape,
            // might have been cut in half by the maxReadahead
            this.savedNotEscapeSpecialEnd = min < off ? off : Integer.MAX_VALUE;
            this.bufpos = min;
            return min;
        }
    }

    private void replaceFirstBackslashWithEscape(int posOfFirstBackSlash, int offOfTheU) throws IOException {
        try {
            char c = (char)
                    ( hexVal(input[++offOfTheU]) << 12
                    | hexVal(input[++offOfTheU]) << 8
                    | hexVal(input[++offOfTheU]) << 4
                    | hexVal(input[++offOfTheU])
                    );
            input[posOfFirstBackSlash] = c; // replace the start char of the backslash
        } catch (NumberFormatException e) {

            String message = "Invalid escape sequence at line "
                + getLine(posOfFirstBackSlash) + ", column "
                + getColumn(posOfFirstBackSlash);

            throw new IOException(message, e);
        }
    }

    @Override
    public void close() throws IOException {
        this.bufpos = -1;
        this.input = null;
    }


    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (input == null) {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return true;
    }

    /**
     * Returns the offset in the input text of the given translated offset.
     * This includes the length of any unicode escapes.
     *
     * <pre>
     * input:      "a\u00a0b"
     * translated: "a b"
     *
     * inputOffset(0) = 0
     * inputOffset(1) = 1
     * inputOffset(2) = 7 // includes the length of the escape
     * </pre>
     */
    public int inputOffset(int outputOffset) {
        return escapes.inputOffsetAt(outputOffset);
    }

    private int getLine(int idxInInput) {
        return StringUtil.lineNumberAt(CharBuffer.wrap(input), idxInInput);
    }

    private int getColumn(int idxInInput) {
        return StringUtil.columnNumberAt(CharBuffer.wrap(input), idxInInput);
    }

    private static int hexVal(char c) {
        switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return c - '0';
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
            return c - ('A' - 10);
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
            return c - ('a' - 10);
        default:
            throw new NumberFormatException("Character '" + c + "' is not a valid hexadecimal digit");
        }
    }

    /**
     * Records where escapes occurred in the input document. This is quite
     * an inefficient way to deal with it, yet in the common case where there
     * are few/no escapes, it's enough I think.
     */
    private static class EscapeTracker {

        private static final int[] EMPTY = new int[0];

        /**
         * Offsets in the {@link #input} where a unicode escape occurred.
         * Represented as pairs [off, len] where
         * - off is the offset in the source file where the escape occurred
         * - len is the length in characters of the escape (which is translated to a single char).
         */
        private int[] inputOffsets = EMPTY;
        /** Index of the next write in the {@link #inputOffsets}. */
        private int nextFreeIdx = 0;

        /**
         * Calls to this method must occur in source order (ie param
         * offsetInInput increases monotonically).
         */
        void recordEscape(int offsetInInput, int len) {
            if (nextFreeIdx + 1 >= inputOffsets.length) {
                // double capacity, add 1 to not stay stuck at zero
                int[] newOffsets = new int[(inputOffsets.length + 1) * 2];
                System.arraycopy(inputOffsets, 0, newOffsets, 0, inputOffsets.length);
                this.inputOffsets = newOffsets;
            }

            inputOffsets[nextFreeIdx++] = offsetInInput;
            inputOffsets[nextFreeIdx++] = len - 1; // -1 because the translated escape has length 1
        }

        /**
         * Convert an offset in the translated file into an offset in
         * the untranslated input.
         */
        public int inputOffsetAt(int translatedOffset) {
            // basically accumulate the lengths of all escapes occurring before the given translatedOffset
            int sum = translatedOffset;
            for (int i = 0; i < nextFreeIdx; i += 2) {
                if (inputOffsets[i] < sum) {
                    sum += inputOffsets[i + 1];
                } else {
                    break;
                }
            }
            return sum;
        }


        @Override
        public String toString() {
            StringBuilder res = new StringBuilder("Escape set {");
            for (int i = 0; i < nextFreeIdx; i += 2) {
                res.append("(at=").append(inputOffsets[i]).append(", len=").append(inputOffsets[i + 1]).append("), ");
            }

            return res.append('}').toString();
        }
    }

}
