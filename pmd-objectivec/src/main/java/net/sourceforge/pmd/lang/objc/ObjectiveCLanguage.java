/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.objc;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;
import net.sourceforge.pmd.lang.objc.cpd.ObjectiveCTokenizer;

/**
 * Defines the Language module for Objective-C
 */
public class ObjectiveCLanguage extends BaseLanguageModule {

    public static final String ID = "objc";

    /**
     * Creates a new instance of {@link ObjectiveCLanguage} with the default
     * extensions for Objective-C files.
     */
    public ObjectiveCLanguage() {
        super("Objective-C", "objectivec", ID, "h", "m");
        addSingleVersion(new CpdOnlyHandler(ObjectiveCTokenizer::new));
    }
}
