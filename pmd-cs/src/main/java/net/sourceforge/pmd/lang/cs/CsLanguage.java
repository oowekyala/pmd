/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cs;

import net.sourceforge.pmd.lang.cs.cpd.CsTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for C#
 */
public class CsLanguage extends BaseLanguageModule {

    public static final String ID = "cs";

    public CsLanguage() {
        super("C#", "cs", ID, ".cs");
        addSingleVersion(new CpdOnlyHandler(CsTokenizer::new));
    }
}
