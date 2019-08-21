/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;


import java.io.IOException;
import java.io.Reader;

import net.sourceforge.pmd.lang.java.ast.ParseException;

public class JavadocParser {

    private final CharSequence fileText;
    private final Reader reader;

    private int curOffset;

    public JavadocParser(Reader reader, int startOffset, CharSequence fileText) {
        this.reader = reader;
        this.curOffset = startOffset;
        this.fileText = fileText;
    }

}
