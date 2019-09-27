/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java;

import static net.sourceforge.pmd.lang.services.LanguageServices.DEFAULT_LANGUAGE_VERSION;
import static net.sourceforge.pmd.lang.services.LanguageServices.FILE_LANGUAGE_PICKER;
import static net.sourceforge.pmd.lang.services.LanguageServices.LANGUAGE_VERSIONS;
import static net.sourceforge.pmd.lang.services.common.FileLanguagePicker.ofExtensions;
import static net.sourceforge.pmd.lang.services.common.LanguageVersionDefaulter.thisOne;

import java.util.Set;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.services.PmdLanguagePlugin;
import net.sourceforge.pmd.lang.services.ServiceBundle.MutableServiceBundle;


public class JavaLanguagePlugin implements PmdLanguagePlugin {

    @Override
    public Language getLanguage(Set<Language> alreadyKnown) {
        return JavaLanguage.getInstance();
    }

    @Override
    public void initialize(Language lang, MutableServiceBundle contextBuilder) {
        contextBuilder.registerEnum(LANGUAGE_VERSIONS, JavaVersion.class);
        contextBuilder.register(FILE_LANGUAGE_PICKER, ofExtensions("java"));
        contextBuilder.register(DEFAULT_LANGUAGE_VERSION, thisOne(lang, JavaVersion.J13));
    }
}
