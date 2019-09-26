/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java;

import net.sourceforge.pmd.lang.LanguageModuleImpl;


public class JavaLanguage extends LanguageModuleImpl {

    public static final String NAME = "Java";
    public static final String TERSE_NAME = "java";
    private static final JavaLanguage INSTANCE = new JavaLanguage();


    private JavaLanguage() {
        super(NAME, TERSE_NAME);
    }

    public static JavaLanguage getInstance() {
        return INSTANCE;
    }

}
