/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.python;

import net.sourceforge.pmd.lang.python.cpd.PythonTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Defines the Language module for Python
 */
public class PythonLanguage extends BaseLanguageModule {

    public static final String ID = "python";

    /**
     * Creates a new instance of {@link PythonLanguage} with the default
     * extensions for python files.
     */
    public PythonLanguage() {
        super("Python", "python", ID, "py");
        addSingleVersion(new CpdOnlyHandler(PythonTokenizer::new));
    }
}
