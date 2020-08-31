/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.kotlin;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;
import net.sourceforge.pmd.lang.kotlin.cpd.KotlinTokenizer;

/**
 * Language implementation for Kotlin
 */
public class KotlinLanguage extends BaseLanguageModule {

    public static final String ID = "kotlin";

    /**
     * Creates a new Kotlin Language instance.
     */
    public KotlinLanguage() {
        super("Kotlin", "kotlin", ID, ".kt");
        addSingleVersion(new CpdOnlyHandler(KotlinTokenizer::new));
    }
}
