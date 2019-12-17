/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cpp.internal;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.CharStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.CharStreamFactory;
import net.sourceforge.pmd.lang.ast.impl.javacc.CharStreamImpl;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccTokenDocument;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.BaseEscapingReader;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.LineTrackingReader;
import net.sourceforge.pmd.lang.ast.impl.javacc.io.OffsetAwareReader;
import net.sourceforge.pmd.lang.cpp.ast.CppParserConstants;

/**
 * An {@link OffsetAwareReader} that translates C++ newline escapes.
 */
public final class CppEscapeReader extends BaseEscapingReader {


    private static final char BACK_SLASH = '\\';
    private static final char NL = '\n';
    private static final char CR = '\r';

    public CppEscapeReader(OffsetAwareReader input, int bufsize) {
        super(input, bufsize);
    }

    public CppEscapeReader(OffsetAwareReader input) {
        super(input);
    }

    @Override
    protected int readImpl(final int idxInInput) throws IOException {
        char c = bufRead();

        if (c == BACK_SLASH) {
            // seen a backslash, now see if a newline follows

            c = bufRead();
            if (c == NL) {
                recordEscape(idxInInput, 2); // those two chars count as zero
                return read(); // may be followed by other escapes
            } else if (c == CR) {
                c = bufRead();
                if (c == NL) {
                    recordEscape(idxInInput, 3); // those three chars count as zero
                    return read();
                } else {
                    backtrack(2, 1); // reread \n + current c
                    return BACK_SLASH;
                }
            } else {
                backtrack(1, 1); // reread current c
                return BACK_SLASH;
            }
        }

        return c;
    }


    /**
     * A char stream that translates C++ newline escapes.
     */
    public static CharStream cppCharStream(Reader dstream) {
        String source = CharStreamFactory.toString(dstream);
        JavaccTokenDocument document = new JavaccTokenDocument(source) {

            @Override
            protected @Nullable String describeKindImpl(int kind) {
                return kind >= 0 && kind < CppParserConstants.tokenImage.length
                       ? CppParserConstants.tokenImage[kind]
                       : null;
            }
        };
        OffsetAwareReader reader = new LineTrackingReader(new StringReader(source));

        return new CharStreamImpl(new CppEscapeReader(reader), document);
    }
}
