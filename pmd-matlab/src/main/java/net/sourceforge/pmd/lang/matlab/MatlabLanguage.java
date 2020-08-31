/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.matlab;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;
import net.sourceforge.pmd.lang.matlab.cpd.MatlabTokenizer;

/**
 * Defines the Language module for Matlab
 */
public class MatlabLanguage extends BaseLanguageModule {

    public static final String ID = "matlab";

    /**
     * Creates a new instance of {@link MatlabLanguage} with the default
     * extensions for matlab files.
     */
    public MatlabLanguage() {
        super("Matlab", "matlab", ID, "m");
        addSingleVersion(new CpdOnlyHandler(MatlabTokenizer::new));
    }
}
