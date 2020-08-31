/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for Dart
 */
public class DartLanguage extends BaseLanguageModule {

    /**
     * Creates a new Dart Language instance.
     */
    public DartLanguage() {
        super("Dart", "dart", "dart", ".dart");
        addDefaultVersion("", new CpdOnlyHandler(DartTokenizer::new));
    }
}
