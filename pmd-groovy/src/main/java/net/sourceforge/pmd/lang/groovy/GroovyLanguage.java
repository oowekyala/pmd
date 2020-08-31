/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.groovy;

import net.sourceforge.pmd.lang.groovy.cpd.GroovyTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for Groovy
 */
public class GroovyLanguage extends BaseLanguageModule {

    public static final String ID = "groovy";

    /**
     * Creates a new Groovy Language instance.
     */
    public GroovyLanguage() {
        super("Groovy", "groovy", ID, ".groovy");
        addSingleVersion(new CpdOnlyHandler(GroovyTokenizer::new));
    }
}
