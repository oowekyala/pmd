/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc.io;

import java.io.IOException;
import java.io.Reader;

/**
 * A reader that tracks the line and columns of its input within the
 * limits provided by {@link OffsetAwareReader#releaseBefore(int)}. This
 * can turn any Reader into an {@link OffsetAwareReader}. It also
 * implements {@link #getLine(int)} and {@link #getColumn(int)} much
 * more efficiently than {@link CharSeqReader}, which may be relevant
 * for some use cases (eg CPD).
 */
public class LineTrackingReader extends Reader implements OffsetAwareReader {

    private static final int FIRST_LINE = 1;
    private static final int FIRST_COL = 1;

    private static final int BASE_BUF_SIZE = 64;

    private final Reader input;
    protected int[] bufline;
    protected int[] bufcolumn;
    private boolean prevCharIsLF;
    private boolean prevCharIsCR;

    private int line = FIRST_LINE;
    private int column = FIRST_COL;
    /**
     * Offset from which we have to keep track of line & columns, before
     * that everything can be overwritten.
     */
    private int savedOffset;
    /**
     * Position in buffer of the {@link #savedOffset}.
     */
    private int savedBufpos;

    private int curOffset;

    public LineTrackingReader(Reader input) {
        this(input, BASE_BUF_SIZE);
    }

    public LineTrackingReader(Reader input, int bufsize) {
        this.input = input;
        this.bufline = new int[bufsize];
        this.bufcolumn = new int[bufsize];
    }


    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (input == null) {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        int read = input.read();
        if (read < 0) {
            return read; // EOF
        }

        resizeBuf(1);

        curOffset++;
        updateLineColumn((char) read, offsetToBufPos(curOffset));
        return read;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        ensureOpen();
        int read = input.read(cbuf, off, len);
        if (read < 0) {
            return read; // EOF
        }

        resizeBuf(read);

        for (int i = off; i < read; i++) {
            curOffset++;
            updateLineColumn(cbuf[i], offsetToBufPos(curOffset));
        }

        return read;
    }

    private int offsetToBufPos(int offset) {
        return (offset - savedOffset + savedBufpos) % bufline.length;
    }

    /**
     * Resize the buffer to handle at least the current live offsets,
     * plus the newly read entries, preserve the current offsets by copying
     * them to the beginning of the new array.
     */
    private void resizeBuf(int read) {
        // a minima we need to store offsets from savedOffset to curOffset + read
        // to accommodate the new chars
        int min = curOffset + read - savedOffset;

        int curLen = bufline.length;
        if (curLen < min) {
            int[] newlines = new int[newSize(min)];
            int[] newcols = new int[newSize(min)];

            int endPos = curOffset - savedOffset + savedBufpos;
            if (endPos < curLen) {
                System.arraycopy(bufline, savedBufpos, newlines, 0, curLen);
                System.arraycopy(bufcolumn, savedBufpos, newcols, 0, curLen);
            } else {
                // wraps around

                int restLen = curLen - savedBufpos;
                int wrappedLen = endPos - curLen;

                System.arraycopy(bufline, savedBufpos, newlines, 0, restLen);
                System.arraycopy(bufcolumn, savedBufpos, newcols, 0, restLen);

                System.arraycopy(bufline, 0, newlines, restLen, wrappedLen);
                System.arraycopy(bufcolumn, 0, newcols, restLen, wrappedLen);
            }

            this.savedBufpos = 0;
            this.bufline = newlines;
            this.bufcolumn = newcols;
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public void releaseBefore(int outputOffset) {
        assert savedOffset <= outputOffset
            : "releaseBefore calls are supposed to be called with increasing offsets";

        assert outputOffset <= curOffset
            : "cannot release an offset we've not yet seen";

        savedBufpos = offsetToBufPos(outputOffset);
        savedOffset = outputOffset;
    }

    @Override
    public int getInputOffset(int outputOffset) {
        return outputOffset;
    }

    @Override
    public int getLine(int outputOffset) {
        return bufline[offsetToBufPos(outputOffset)];
    }

    @Override
    public int getColumn(int outputOffset) {
        return bufcolumn[offsetToBufPos(outputOffset)];
    }

    private void updateLineColumn(char c, int bufpos) {
        column++;

        if (prevCharIsLF) {
            prevCharIsLF = false;
            column = FIRST_COL;
            line++;
        } else if (prevCharIsCR) {
            prevCharIsCR = false;
            if (c == '\n') {
                column = FIRST_COL;
                line++;
            }
        }

        switch (c) {
        case '\r':
            prevCharIsCR = true;
            break;
        case '\n':
            prevCharIsLF = true;
            break;
        default:
            break;
        }

        bufline[bufpos] = line;
        bufcolumn[bufpos] = column;
    }

    private static int newSize(int min) {
        return nearestPowOf2(min);
    }

    private static int nearestPowOf2(int n) {
        if (n == 0) {
            return 0; // or throw exception
        }
        int log2 = (int) Math.ceil(Math.log(n) / Math.log(2) + 1e-10);
        return 1 << log2;
    }
}
