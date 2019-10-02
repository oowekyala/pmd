/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import net.sourceforge.pmd.util.StringUtil;

public class JavaInputReader extends Reader {

    /** Untranslated characters. */
    private final String input;
    /** Buffer for translated characters. */
    private final char[] buffer;

    /** Position in buffer. */
    private int bufpos = -1;
    /** Number of characters the {@link #bufpos} is behind the current valid offset in the {@link #buffer}. */
    private int ahead = 0;

    /** Keep track of adjustments to make to the offsets, caused by unicode escapes. */
    private EscapeTracker escapes = new EscapeTracker();
    /** Index of the next char to read in the {@link #input}. */
    private int idxInInput = 0;
    /** Mark recorded by {@link #mark(int)}, if negative, no mark is recorded. */
    private int mark = -1;

    public JavaInputReader(String input, int bufsize) {
        assert bufsize > 0;
        assert input != null;

        this.input = input;
        this.buffer = new char[bufsize];
    }

    private char readByte() throws IOException {
        if (idxInInput++ >= input.length()) {
            throw new EOFException();
        }

        return input.charAt(idxInInput);
    }

    /** Move the {@link #bufpos} around the {@link #buffer} by the given amount. */
    private void shift(int amount) {
        int diff = (bufpos += amount) - buffer.length;
        if (diff > 0) {
            bufpos = diff;
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = -1;
        int i = off;
        final int end = off + len;
        while (i < end) {
            try {
                cbuf[i] = (char) read();
            } catch (EOFException eof) {
                return read;
            }

            read++;
            i++;
        }
        return read;
    }

    @Override
    public int read() throws IOException {
        shift(1);

        if (ahead-- > 0) {
            return buffer[bufpos];
        }

        char c;

        final int posOfFirstBackSlash = idxInInput;
        if ((buffer[bufpos] = c = readByte()) != '\\') {
            return c;
        } else { // seen a backslash

            int backSlashCnt = 1;

            // Read all the backslashes
            while ((buffer[bufpos] = c = readByte()) == '\\') {
                backSlashCnt++;
                shift(1);
            }

            if ((c != 'u') || ((backSlashCnt & 1) != 1)) {
                backup(backSlashCnt);  // go back to the first backslash, keep chars in buffer
                return '\\';
            }

            int escapeLen = backSlashCnt;
            // odd number of backslashes + 'u' -> we're looking at a unicode escape
            try {
                while ((c = readByte()) == 'u') {
                    // read
                    backSlashCnt++;
                }

                shift(1);

                buffer[bufpos] = c = (char)
                    (hexVal(c) << 12 | hexVal(readByte()) << 8 | hexVal(readByte()) << 4 | hexVal(readByte()));

            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid escape sequence at " + getLine() + ":" + getColumn(), e);
            }

            escapes.recordEscape(posOfFirstBackSlash, escapeLen);

            if (backSlashCnt == 1) {
                return c;
            } else {
                backup(backSlashCnt);
                return '\\';
            }
        }
    }

    /** Retreat. */
    public void backup(int amount) {
        ahead += amount;
        shift(-amount);
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (bufpos < 0) {
            throw new IOException("Cannot mark a non-opened stream"); // TODO this is to avoid inconsistencies for now
        }
        this.mark = bufpos;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void close() throws IOException {
        mark = -1;
        reset();
    }

    @Override
    public void reset() throws IOException {
        if (mark > 0) {
            backup(markLength());
            mark = -1;
        } else {
            bufpos = -1;
            mark = -1;
            ahead = 0;
            idxInInput = 0;
            escapes = new EscapeTracker();
        }
    }

    public int markLength() {
        if (mark < 0) {
            throw new IllegalStateException("No recorded mark");
        } else if (mark <= bufpos) {
            return bufpos - mark;
        } else {
            return (buffer.length - bufpos) + mark;
        }
    }

    public String markImage() {
        if (mark < 0) {
            throw new IllegalStateException("No recorded mark");
        } else if (mark <= bufpos) {
            return new String(buffer, mark, bufpos - mark + 1);
        } else {
            return new String(buffer, mark, buffer.length - mark)
                + new String(buffer, 0, bufpos + 1);
        }
    }

    /** Returns the offset of the mark in the input text (inclusive). */
    public int markOffset() {
        if (mark < 0) {
            throw new IllegalStateException("No recorded mark");
        } else {
            return escapes.inputOffsetAt(mark);
        }
    }

    /** Returns the current offset in the input text (exclusive). */
    public int curOffset() {
        return escapes.inputOffsetAt(bufpos) + 1;
    }

    private int getLine() {
        int inOffset = this.escapes.inputOffsetAt(bufpos);
        return StringUtil.lineNumberAt(input, inOffset);
    }

    private int getColumn() {
        int inOffset = this.escapes.inputOffsetAt(bufpos);
        return StringUtil.columnNumberAt(input, inOffset);
    }

    private static int hexVal(char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        case 'a':
        case 'A':
            return 10;
        case 'b':
        case 'B':
            return 11;
        case 'c':
        case 'C':
            return 12;
        case 'd':
        case 'D':
            return 13;
        case 'e':
        case 'E':
            return 14;
        case 'f':
        case 'F':
            return 15;
        }

        throw new IllegalArgumentException("Character " + c + " is not a valid hexadecimal digit");
    }

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
        int inputOffsetAt(int translatedOffset) {
            // basically accumulate the lengths of all escapes occurring
            // before the translated offset

            //
            // a\u00a0b
            // a b
            //
            //
            // inputOffsetAt(0) = 0
            // inputOffsetAt(1) = 1
            // inputOffsetAt(2) = 7
            //

            int sum = translatedOffset;
            for (int i = 0; i < nextFreeIdx; i += 2) {
                if (inputOffsets[i] < translatedOffset) {
                    sum += inputOffsets[i + 1];
                } else {
                    break;
                }
            }
            return sum;
        }
    }

}
