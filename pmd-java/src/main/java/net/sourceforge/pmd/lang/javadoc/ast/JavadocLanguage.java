/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc.ast;

import net.sourceforge.pmd.lang.impl.SimpleLanguageModuleBase;

/**
 *
 */
public class JavadocLanguage extends SimpleLanguageModuleBase {

    public static final JavadocLanguage INSTANCE = new JavadocLanguage();


    public JavadocLanguage() {
        super(LanguageMetadata.withId("javadoc").name("Javadoc").extensions("_not_an_extension").dependsOnLanguage("java"),
              () -> JavadocParserFacade::parseJavadoc);
    }

}
