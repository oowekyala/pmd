/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.BaseLanguageModule;

/**
 *
 */
public class JavadocLanguage extends BaseLanguageModule {

    static final JavadocLanguage INSTANCE = new JavadocLanguage();

    public static final String NAME = "Javadoc";
    public static final String TERSE_NAME = "javadoc";

    //todo unregistered

    public JavadocLanguage() {
        super(NAME, "Javadoc", TERSE_NAME, "_not_an_extension_");
        addDefaultVersion("", () -> task -> JavadocParserFacade.parseJavadoc(task.getTextDocument()));
    }
}
