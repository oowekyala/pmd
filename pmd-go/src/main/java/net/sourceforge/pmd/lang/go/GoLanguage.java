/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.go;

import net.sourceforge.pmd.lang.go.cpd.GoTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * @author oinume@gmail.com
 */
public class GoLanguage extends BaseLanguageModule {

    public GoLanguage() {
        super("Go", "go", "go", ".go");
        addSingleVersion(new CpdOnlyHandler(GoTokenizer::new));
    }
}
