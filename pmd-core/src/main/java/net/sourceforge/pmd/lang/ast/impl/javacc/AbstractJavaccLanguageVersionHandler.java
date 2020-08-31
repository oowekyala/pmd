/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.javacc;

import net.sourceforge.pmd.cpd.Tokenizer.CpdProperties;
import net.sourceforge.pmd.cpd.internal.JavaCCTokenizer;
import net.sourceforge.pmd.lang.AbstractPmdLanguageVersionHandler;
import net.sourceforge.pmd.lang.ParserOptions;

/**
 *
 */
public abstract class AbstractJavaccLanguageVersionHandler extends AbstractPmdLanguageVersionHandler {


    @Override
    public JavaCCTokenizer getCpdTokenizer(CpdProperties cpdProperties) {
        return null;
    }

    @Override
    public abstract JjtreeParserAdapter<?> getParser(ParserOptions parserOptions);
}
