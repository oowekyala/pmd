/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;

/**
 *
 */
public class JavadocLanguage extends BaseLanguageModule {

    public JavadocLanguage() {
        super("Javadoc", "Javadoc", "javadoc", "javadoc");
        addDefaultVersion("", new LanguageVersionHandler() {
            @Override
            public Parser getParser(ParserOptions parserOptions) {
                return task -> JavadocParserFacade.parseJavadoc(task.getTextDocument());
            }
        });
    }
}
