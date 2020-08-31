/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cpp;

import net.sourceforge.pmd.lang.cpp.cpd.CPPTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Defines the Language module for C/C++
 */
public class CPPLanguage extends BaseLanguageModule {

    /**
     * Creates a new instance of {@link CPPLanguage} with the default extensions
     * for c/c++ files.
     */
    public CPPLanguage() {
        super("C++", "cpp", "cpp", ".h", ".hpp", ".hxx", ".c", ".cpp", ".cxx", ".cc", ".C");
        addDefaultVersion("", new CpdOnlyHandler(CPPTokenizer::new));
    }
}
