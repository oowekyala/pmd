/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.dart;

import net.sourceforge.pmd.lang.dart.cpd.DartTokenizer;
import net.sourceforge.pmd.lang.BaseLanguageModule;
import net.sourceforge.pmd.lang.CpdOnlyHandler;

/**
 * Language implementation for Dart
 */
public class DartLanguage extends BaseLanguageModule {

    public static final String ID = "dart";

    /**
     * Creates a new Dart Language instance.
     */
    public DartLanguage() {
        super("Dart", "dart", ID, "dart");
        addDefaultVersion("", new CpdOnlyHandler(DartTokenizer::new));
    }
}
