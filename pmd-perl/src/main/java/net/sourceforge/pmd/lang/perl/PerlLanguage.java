/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.perl;

import net.sourceforge.pmd.lang.perl.cpd.PerlTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

public class PerlLanguage extends BaseLanguageModule {
    public static final String ID = "perl";

    public PerlLanguage() {
        super("Perl", "perl", ID, "pm", "pl", "t");
        addSingleVersion(new CpdOnlyHandler(PerlTokenizer::new));
    }
}
