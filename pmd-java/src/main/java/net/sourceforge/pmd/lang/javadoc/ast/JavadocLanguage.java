/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.ParserOptions;

/**
 *
 */
public class JavadocLanguage extends BaseLanguageModule {

    public static final String NAME = "Javadoc";
    public static final String TERSE_NAME = "javadoc";

    //todo unregistered

    public JavadocLanguage() {
        super(NAME, "Javadoc", TERSE_NAME, "_not_an_extension_");
        addDefaultVersion("", new LanguageVersionHandler() {
            @Override
            public Parser getParser(ParserOptions parserOptions) {
                return task -> JavadocParserFacade.parseJavadoc(task.getTextDocument());
            }
        });
    }
}
