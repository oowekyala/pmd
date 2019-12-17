/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc.io;

import java.io.EOFException;
import java.io.IOException;

/**
 * An {@link OffsetAwareReader} that translates java unicode sequences.
 */
@SuppressWarnings("PMD.AssignmentInOperand")
public final class JavaEscapeReader extends BaseEscapingReader {

    public JavaEscapeReader(OffsetAwareReader input, int bufsize) {
        super(input, bufsize);
    }

    public JavaEscapeReader(OffsetAwareReader input) {
        super(input);
    }

    @Override
    protected int readImpl(final int idxInInput) throws IOException {
        char c = bufRead();

        if (c == '\\') {
            // Seen a backslash, now see if this is a unicode escape

            int backSlashCnt = 1;

            // Read all the backslashes
            while ((c = bufRead()) == '\\') {  // suppress checkstyle now
                backSlashCnt++;
            }


            if ((backSlashCnt & 1) == 0 || c != 'u') {
                // Even number of backslashes || no 'u' : not an escape
                // Go back to after first backslash
                backtrack(backSlashCnt);
                return '\\';
            }
            // Odd number of backslashes + 'u' -> we're looking at a unicode escape

            backSlashCnt++; // the first 'u'

            while ((c = bufRead()) == 'u') {
                // skip the 'u's
                backSlashCnt++;
            }

            char c2;
            char c1;
            char c0;

            try {
                // Put them in the buffer.
                // If we fail we can use the buffer to make an exception message
                c2 = bufRead();
                c1 = bufRead();
                c0 = bufRead();

                c = (char) (hexVal(c) << 12 | hexVal(c2) << 8 | hexVal(c1) << 4 | hexVal(c0));
            } catch (EOFException | NumberFormatException e) {
                throw invalidEscape(e, idxInInput, bufferToString());
            }

            // 4 hex digits, -1 because the escape is translated to 1 char
            final int escapeLenDiff = backSlashCnt + (4 - 1);
            recordEscape(idxInInput, escapeLenDiff);
        }
        return c;
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
}
