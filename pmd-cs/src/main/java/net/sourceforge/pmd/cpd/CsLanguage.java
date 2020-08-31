/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for C#
 */
public class CsLanguage extends BaseLanguageModule {
    public CsLanguage() {
        super("C#", "cs", "cs", ".cs");
        addDefaultVersion("", new CpdOnlyHandler(CsTokenizer::new));
    }
}
