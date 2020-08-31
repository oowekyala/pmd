/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.php;

import net.sourceforge.pmd.cpd.AnyTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for PHP
 */
public class PHPLanguage extends BaseLanguageModule {

    public static final String ID = "php";

    /**
     * Creates a new PHP Language instance.
     */
    public PHPLanguage() {
        super("PHP", "php", ID, "php", "class");
        addSingleVersion(new CpdOnlyHandler(AnyTokenizer::new));
    }
}
